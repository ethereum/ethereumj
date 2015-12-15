package org.ethereum.mine;

import org.apache.commons.collections4.CollectionUtils;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
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

/**
 * Created by Anton Nashatyrev on 10.12.2015.
 */
@Lazy
@Component
public class BlockMiner {
    private static final Logger logger = LoggerFactory.getLogger("mine");

    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    @Autowired
    private Blockchain blockchain;

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
    private boolean isMining;

    private Block miningBlock;
    private Future<Long> ethashTask;
    private long lastBlockMinedTime;

    @PostConstruct
    private void init() {
        minGasPrice = config.getMineMinGasPrice();
        minBlockTimeout = config.getMinBlockTimeoutMsec();
        listener.addListener(new EthereumListenerAdapter() {
            @Override
            public void onPendingTransactionsReceived(List<Transaction> transactions) {
                BlockMiner.this.onPendingTransactionsReceived(transactions);
            }
        });
    }

    public void startMining() {
        isMining = true;
        fireMinerStarted();
        logger.info("Miner started");
        restartMining(Collections.<Transaction>emptyList());
    }

    public void stopMining() {
        isMining = false;
        cancelCurrentBlock();
        fireMinerStopped();
        logger.info("Miner stopped");
    }

    protected List<Transaction> getAllPendingTransactions() {
        List<Transaction> ret = new ArrayList<>();
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
        return ret;
    }

//    @Override
//    public void onBlock(Block block, List<TransactionReceipt> receipts) {
//        List<Transaction> curPendingTxs = getAllPendingTransactions();
//        if (!CollectionUtils.isEqualCollection(miningPendingTxs, curPendingTxs)) {
//            restartMining(curPendingTxs);
//        }
//    }

    public void onPendingTransactionsReceived(List<Transaction> transactions) {
        if (!isMining) return;

        logger.debug("Miner received new pending txs");
        List<Transaction> curPendingTxs = getAllPendingTransactions();
        if (miningBlock == null || transactions.isEmpty() ||
                !CollectionUtils.isEqualCollection(miningBlock.getTransactionsList(), curPendingTxs)) {
            restartMining(curPendingTxs);
        }
    }

    protected boolean isAcceptableTx(Transaction tx) {
        return minGasPrice.compareTo(new BigInteger(1, tx.getGasPrice())) <= 0;
    }

    protected void cancelCurrentBlock() {
        if (ethashTask != null && !ethashTask.isCancelled()) {
            ethashTask.cancel(true);
            fireBlockCancelled(miningBlock);
            logger.debug("Tainted block mining cancelled: {}", miningBlock.getShortHash());
        }
    }

    protected void restartMining(List<Transaction> txs) {
        cancelCurrentBlock();
        miningBlock = blockchain.createNewBlock(blockchain.getBestBlock(), txs);
        ethashTask = Ethash.getForBlock(miningBlock.getNumber()).mineLight(miningBlock);
        fireBlockStarted(miningBlock);
        logger.debug("New block mining started: {}", miningBlock.getShortHash());
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    ethashTask.get();
                    // wow, block mined!
                    blockMined(miningBlock);
                } catch (InterruptedException|ExecutionException e) {
                    // OK, we've been cancelled, just exit
                }
            }
        });
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

//        restartMining(Collections.<Transaction>emptyList());
    }

    /*****  Listener boilerplate  ******/

    public void addListener(MinerListener l) {
        listeners.add(l);
    }

    public void removeListener(MinerListener l) {
        listeners.remove(l);
    }

    public void fireMinerStarted() {
        for (MinerListener l : listeners) {
            l.miningStarted();
        }
    }
    public void fireMinerStopped() {
        for (MinerListener l : listeners) {
            l.miningStopped();
        }
    }
    public void fireBlockStarted(Block b) {
        for (MinerListener l : listeners) {
            l.blockMiningStarted(b);
        }
    }
    public void fireBlockCancelled(Block b) {
        for (MinerListener l : listeners) {
            l.blockMiningCanceled(b);
        }
    }
    public void fireBlockMined(Block b) {
        for (MinerListener l : listeners) {
            l.blockMined(b);
        }
    }
}
