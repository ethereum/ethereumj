package org.ethereum.mine;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.collections4.CollectionUtils;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.db.BlockStore;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumImpl;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.Math.max;
import static org.ethereum.config.Constants.UNCLE_GENERATION_LIMIT;
import static org.ethereum.config.Constants.UNCLE_LIST_LIMIT;

/**
 * Created by Anton Nashatyrev on 10.12.2015.
 */
@Component
public class BlockMiner {
    private static final Logger logger = LoggerFactory.getLogger("mine");

    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    @Autowired
    private Blockchain blockchain;

    @Autowired
    private IndexedBlockStore blockStore;

    @Autowired
    private Ethereum ethereum;

    @Autowired
    private CompositeEthereumListener listener;

    @Autowired
    private SystemProperties config;

    @Autowired
    protected PendingState pendingState;

    private List<MinerListener> listeners = new CopyOnWriteArrayList<>();

    private BigInteger minGasPrice;
    private long minBlockTimeout;
    private int cpuThreads;
    private boolean fullMining = true;

    private boolean isMining;

    private Block miningBlock;
    private ListenableFuture<Long> ethashTask;
    private long lastBlockMinedTime;

    @PostConstruct
    private void init() {
        minGasPrice = config.getMineMinGasPrice();
        minBlockTimeout = config.getMineMinBlockTimeoutMsec();
        cpuThreads = config.getMineCpuThreads();
        fullMining = config.isMineFullDataset();
        listener.addListener(new EthereumListenerAdapter() {
            @Override
            public void onPendingStateChanged(PendingState pendingState) {
                BlockMiner.this.onPendingStateChanged();
            }

            @Override
            public void onSyncDone() {
                if (config.minerStart() && config.isSyncEnabled()) {
                    logger.info("Sync complete, start mining...");
                    startMining();
                }
            }
        });

        if (config.minerStart() && !config.isSyncEnabled()) {
            logger.info("Sync disabled, start mining now...");
            startMining();
        }
    }

    public void setFullMining(boolean fullMining) {
        this.fullMining = fullMining;
    }

    public void setCpuThreads(int cpuThreads) {
        this.cpuThreads = cpuThreads;
    }

    public void startMining() {
        isMining = true;
        fireMinerStarted();
        logger.info("Miner started");
        restartMining();
    }

    public void stopMining() {
        isMining = false;
        cancelCurrentBlock();
        fireMinerStopped();
        logger.info("Miner stopped");
    }

    protected List<Transaction> getAllPendingTransactions() {
        PendingStateImpl.TransactionSortedSet ret = new PendingStateImpl.TransactionSortedSet();
        ret.addAll(pendingState.getPendingTransactions());
        ret.addAll(pendingState.getWireTransactions());
        Iterator<Transaction> it = ret.iterator();
        while(it.hasNext()) {
            Transaction tx = it.next();
            if (!isAcceptableTx(tx)) {
                logger.debug("Miner excluded the transaction: {}", tx);
                it.remove();
            }
        }
        return new ArrayList<>(ret);
    }

    private void onPendingStateChanged() {
        if (!isMining) return;

        logger.debug("onPendingStateChanged()");
        if (miningBlock == null) {
            restartMining();
        } else if (miningBlock.getNumber() <= ((PendingStateImpl) pendingState).getBestBlock().getNumber()) {
            logger.debug("Restart mining: new best block: " + blockchain.getBestBlock().getShortDescr());
            restartMining();
        } else if (!CollectionUtils.isEqualCollection(miningBlock.getTransactionsList(), getAllPendingTransactions())) {
            logger.debug("Restart mining: pending transactions changed");
            restartMining();
        } else {
            if (logger.isDebugEnabled()) {
                String s = "onPendingStateChanged() event, but pending Txs the same as in currently mining block: ";
                for (Transaction tx : getAllPendingTransactions()) {
                    s += "\n    " + tx;
                }
                logger.debug(s);
            }
        }
    }

    protected boolean isAcceptableTx(Transaction tx) {
        return minGasPrice.compareTo(new BigInteger(1, tx.getGasPrice())) <= 0;
    }

    protected synchronized void cancelCurrentBlock() {
        if (ethashTask != null && !ethashTask.isCancelled()) {
            ethashTask.cancel(true);
            fireBlockCancelled(miningBlock);
            logger.debug("Tainted block mining cancelled: {}", miningBlock.getShortDescr());
            ethashTask = null;
            miningBlock = null;
        }
    }

    protected List<BlockHeader> getUncles(Block mineBest) {
        List<BlockHeader> ret = new ArrayList<>();
        long miningNum = mineBest.getNumber() + 1;
        Block mineChain = mineBest;

        long limitNum = max(0, miningNum - UNCLE_GENERATION_LIMIT);
        Set<ByteArrayWrapper> ancestors = BlockchainImpl.getAncestors(blockStore, mineBest, UNCLE_GENERATION_LIMIT + 1, true);
        Set<ByteArrayWrapper> knownUncles = BlockchainImpl.getUsedUncles(blockStore, mineBest, true);
        knownUncles.addAll(ancestors);
        knownUncles.add(new ByteArrayWrapper(mineBest.getHash()));

        outer:
        while(mineChain.getNumber() > limitNum) {
            List<Block> genBlocks = blockStore.getBlocksByNumber(mineChain.getNumber());
            if (genBlocks.size() > 1) {
                for (Block uncleCandidate : genBlocks) {
                    if (!knownUncles.contains(new ByteArrayWrapper(uncleCandidate.getHash())) &&
                        ancestors.contains(new ByteArrayWrapper(blockStore.getBlockByHash(uncleCandidate.getParentHash()).getHash()))) {

                        ret.add(uncleCandidate.getHeader());
                        if (ret.size() >= UNCLE_LIST_LIMIT) {
                            break outer;
                        }
                    }
                }
            }
            mineChain = blockStore.getBlockByHash(mineChain.getParentHash());
        }
        return ret;
    }

    protected void restartMining() {
        Block bestBlockchain = blockchain.getBestBlock();
        Block bestPendingState = ((PendingStateImpl) pendingState).getBestBlock();

        logger.debug("Best blocks: PendingState: " + bestPendingState.getShortDescr() +
                ", Blockchain: " + bestBlockchain.getShortDescr());

        Block newMiningBlock = blockchain.createNewBlock(bestPendingState, getAllPendingTransactions(),
                getUncles(bestPendingState));

        synchronized(this) {
            cancelCurrentBlock();
            miningBlock = newMiningBlock;
            ethashTask = fullMining ?
                    Ethash.getForBlock(miningBlock.getNumber()).mine(miningBlock, cpuThreads) :
                    Ethash.getForBlock(miningBlock.getNumber()).mineLight(miningBlock, cpuThreads);
            ethashTask.addListener(new Runnable() {
                //            private final Future<Long> task = ethashTask;
                @Override
                public void run() {
                    try {
                        ethashTask.get();
                        // wow, block mined!
                        blockMined(miningBlock);
                    } catch (InterruptedException | CancellationException e) {
                        // OK, we've been cancelled, just exit
                    } catch (Exception e) {
                        logger.warn("Exception during mining: ", e);
                    }
                }
            }, MoreExecutors.sameThreadExecutor());
        }
        fireBlockStarted(miningBlock);
        logger.debug("New block mining started: {}", miningBlock.getShortHash());
    }

    protected void blockMined(Block newBlock) throws InterruptedException {

        long t = System.currentTimeMillis();
        if (t - lastBlockMinedTime < minBlockTimeout) {
            long sleepTime = minBlockTimeout - (t - lastBlockMinedTime);
            logger.debug("Last block was mined " + (t - lastBlockMinedTime) + " ms ago. Sleeping " +
                    sleepTime + " ms before importing...");
            Thread.sleep(sleepTime);
        }

        fireBlockMined(newBlock);
        logger.info("Wow, block mined !!!: {}", newBlock.toString());

        lastBlockMinedTime = t;
        ethashTask = null;
        miningBlock = null;

        // broadcast the block
        logger.debug("Importing newly mined block " + newBlock.getShortHash() + " ...");
        ImportResult importResult = ((EthereumImpl) ethereum).addNewMinedBlock(newBlock);
        logger.debug("Mined block import result is " + importResult + " : " + newBlock.getShortHash());
    }

    /*****  Listener boilerplate  ******/

    public void addListener(MinerListener l) {
        listeners.add(l);
    }

    public void removeListener(MinerListener l) {
        listeners.remove(l);
    }

    protected void fireMinerStarted() {
        for (MinerListener l : listeners) {
            l.miningStarted();
        }
    }
    protected void fireMinerStopped() {
        for (MinerListener l : listeners) {
            l.miningStopped();
        }
    }
    protected void fireBlockStarted(Block b) {
        for (MinerListener l : listeners) {
            l.blockMiningStarted(b);
        }
    }
    protected void fireBlockCancelled(Block b) {
        for (MinerListener l : listeners) {
            l.blockMiningCanceled(b);
        }
    }
    protected void fireBlockMined(Block b) {
        for (MinerListener l : listeners) {
            l.blockMined(b);
        }
    }
}
