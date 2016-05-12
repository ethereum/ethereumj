package org.ethereum.core;

import org.apache.commons.collections4.map.LRUMap;
import org.ethereum.config.SystemProperties;
import org.ethereum.db.BlockStore;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.listener.EthereumListener;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.*;

import static java.math.BigInteger.ZERO;
import static org.ethereum.util.BIUtil.toBI;

/**
 * Keeps logic providing pending state management
 *
 * @author Mikhail Kalinin
 * @since 28.09.2015
 */
@Component
public class PendingStateImpl implements PendingState {

    public static class TransactionSortedSet extends TreeSet<Transaction> {
        public TransactionSortedSet() {
            super(new Comparator<Transaction>() {

                @Override
                public int compare(Transaction tx1, Transaction tx2) {
                    long nonceDiff = ByteUtil.byteArrayToLong(tx1.getNonce()) -
                            ByteUtil.byteArrayToLong(tx2.getNonce());
                    if (nonceDiff != 0) {
                        return nonceDiff > 0 ? 1 : -1;
                    }
                    return FastByteComparisons.compareTo(tx1.getHash(), 0, 32, tx2.getHash(), 0, 32);
                }
            });
        }
    }

    private static final Logger logger = LoggerFactory.getLogger("state");

    @Autowired
    private SystemProperties config = SystemProperties.getDefault();

    @Autowired
    private EthereumListener listener;

    @Autowired @Qualifier("repository")
    private Repository repository;

    @Autowired
    private Blockchain blockchain;

    @Autowired
    private BlockStore blockStore;

    @Autowired
    private ProgramInvokeFactory programInvokeFactory;

//    @Resource
//    @Qualifier("wireTransactions")
    private final List<PendingTransaction> wireTransactions = new ArrayList<>();

    // to filter out the transactions we have already processed
    // transactions could be sent by peers even if they were already included into blocks
    private final Map<ByteArrayWrapper, Object> redceivedTxs = new LRUMap<>(500000);

    @Resource
    @Qualifier("pendingStateTransactions")
    private final List<Transaction> pendingStateTransactions = new ArrayList<>();

    private Repository pendingState;

    private Block best = null;

    public PendingStateImpl() {
    }

    public PendingStateImpl(EthereumListener listener, BlockchainImpl blockchain) {
        this.listener = listener;
        this.blockchain = blockchain;
        this.repository = blockchain.getRepository();
        this.blockStore = blockchain.getBlockStore();
        this.programInvokeFactory = blockchain.getProgramInvokeFactory();
    }

    @PostConstruct
    public void init() {
        this.pendingState = repository.startTracking();
    }

    @Override
    public Repository getRepository() {
        return pendingState;
    }

    @Override
    public synchronized List<Transaction> getWireTransactions() {

        List<Transaction> txs = new ArrayList<>();

        for (PendingTransaction tx : wireTransactions) {
            txs.add(tx.getTransaction());
        }

        return txs;
    }

    public Block getBestBlock() {
        if (best == null) {
            best = blockchain.getBestBlock();
        }
        return best;
    }

    private boolean addNewTxIfNotExist(Transaction tx) {
        ByteArrayWrapper hash = new ByteArrayWrapper(tx.getHash());
        synchronized (redceivedTxs) {
            if (!redceivedTxs.containsKey(hash)) {
                redceivedTxs.put(hash, null);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void addWireTransactions(List<Transaction> transactions) {

        final List<Transaction> newTxs = new ArrayList<>();
        final List<PendingTransaction> newPTxs = new ArrayList<>();
        int unknownTx = 0;

        if (transactions.isEmpty()) return;

        long number = getBestBlock().getNumber();
        for (Transaction tx : transactions) {

            if (addNewTxIfNotExist(tx)) {
                unknownTx++;
                PendingTransaction ptx = new PendingTransaction(tx, number);
                if (isValid(ptx)) {
                    newPTxs.add(ptx);
                    newTxs.add(tx);
                } else {
                    logger.info("Non valid TX: " + tx);
                }
            }
        }

        // tight synchronization here since a lot of duplicate transactions can arrive from many peers
        // and isValid(tx) call is very expensive
        synchronized (this) {
            wireTransactions.addAll(newPTxs);
        }

        if (!newTxs.isEmpty()) {
            EventDispatchThread.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.onPendingTransactionsReceived(newTxs);
                    listener.onPendingStateChanged(PendingStateImpl.this);
                }
            });
        }
        logger.info("Wire transaction list added: {} new, {} valid of received {}, #of known txs: {}", unknownTx, newTxs.size(), transactions.size(), redceivedTxs.size());
    }

    private boolean isValid(PendingTransaction tx) {
        BigInteger txNonce = toBI(tx.getTransaction().getNonce());

        byte[] txSender = tx.getSender();
        AccountState accountState = repository.getAccountState(txSender);
        if (accountState != null) {
            BigInteger currNonce = accountState.getNonce();
            if (currNonce.equals(txNonce)) return true;
        }
        for (int i = wireTransactions.size() - 1; i >=0; i--) {
            if (Arrays.equals(wireTransactions.get(i).getSender(), txSender)) {
                long pendingNonce = ByteUtil.byteArrayToLong(wireTransactions.get(i).getTransaction().getNonce());
                if (txNonce.longValue() == pendingNonce + 1) {
                    return true;
                } else {
                    break;
                }
            }
        }
        return txNonce.equals(ZERO);
    }

    @Override
    public synchronized void addPendingTransaction(final Transaction tx) {
        pendingStateTransactions.add(tx);
        executeTx(tx);
        EventDispatchThread.invokeLater(new Runnable() {
            @Override
            public void run() {
                listener.onPendingTransactionsReceived(Collections.singletonList(tx));
                listener.onPendingStateChanged(PendingStateImpl.this);
            }
        });
    }

    @Override
    public List<Transaction> getPendingTransactions() {
        return pendingStateTransactions;
    }

    public List<Transaction> getAllPendingTransactions() {
        List<Transaction> ret = new ArrayList<>(pendingStateTransactions);
        ret.addAll(getWireTransactions());
        return ret;
    }


    private Block findCommonAncestor(Block b1, Block b2) {
        while(!b1.isEqual(b2)) {
            if (b1.getNumber() >= b2.getNumber()) {
                b1 = blockchain.getBlockByHash(b1.getParentHash());
            }

            if (b1.getNumber() < b2.getNumber()) {
                b2 = blockchain.getBlockByHash(b2.getParentHash());
            }
            if (b1 == null || b2 == null) {
                // shouldn't happen
                throw new RuntimeException("Pending state can't find common ancestor: one of blocks has a gap");
            }
        }
        return b1;
    }

    @Override
    public synchronized void processBest(Block newBlock) {

        if (getBestBlock() != null && !getBestBlock().isParentOf(newBlock)) {
            // need to switch the state to another fork

            Block commonAncestor = findCommonAncestor(getBestBlock(), newBlock);

            if (logger.isDebugEnabled()) logger.debug("New best block from another fork: "
                    + newBlock.getShortDescr() + ", old best: " + getBestBlock().getShortDescr()
                    + ", ancestor: " + commonAncestor.getShortDescr());

            // first return back the transactions from forked block
            Block rollback = getBestBlock();
            while(!rollback.isEqual(commonAncestor)) {
                List<PendingTransaction> l = new ArrayList<>();
                for (Transaction tx : rollback.getTransactionsList()) {
                    logger.debug("Returning transaction back to pending: " + tx);
                    l.add(new PendingTransaction(tx, commonAncestor.getNumber()));
                }
                wireTransactions.addAll(l);
                rollback = blockchain.getBlockByHash(rollback.getParentHash());
            }

            // rollback the state snapshot to the ancestor
            pendingState = repository.getSnapshotTo(commonAncestor.getStateRoot()).startTracking();

            // next process blocks from new fork
            Block main = newBlock;
            List<Block> mainFork = new ArrayList<>();
            while(!main.isEqual(commonAncestor)) {
                mainFork.add(main);
                main = blockchain.getBlockByHash(main.getParentHash());
            }

            // processing blocks from ancestor to new block
            for (int i = mainFork.size() - 1; i >= 0; i--) {
                processBestInternal(mainFork.get(i));
            }
        } else {
            logger.debug("PendingStateImpl.processBest: " + newBlock.getShortDescr());
            processBestInternal(newBlock);
        }

        best = newBlock;

        updateState();

        EventDispatchThread.invokeLater(new Runnable() {
            public void run() {
                listener.onPendingStateChanged(PendingStateImpl.this);
            }
        });
    }

    private void processBestInternal(Block block) {

        clearWire(block.getTransactionsList());

        clearOutdated(block.getNumber());

        clearPendingState(block.getTransactionsList());
    }

    private void clearOutdated(final long blockNumber) {
        List<PendingTransaction> outdated = new ArrayList<>();

        synchronized (wireTransactions) {
            for (PendingTransaction tx : wireTransactions)
                if (blockNumber - tx.getBlockNumber() > config.txOutdatedThreshold())
                    outdated.add(tx);
        }

        if (outdated.isEmpty()) return;

        if (logger.isInfoEnabled())
            for (PendingTransaction tx : outdated)
                logger.info(
                        "Clear outdated wire transaction, block.number: [{}] hash: [{}]",
                        tx.getBlockNumber(),
                        Hex.toHexString(tx.getHash())
                );

        wireTransactions.removeAll(outdated);
    }

    private void clearWire(List<Transaction> txs) {
        for (Transaction tx : txs) {
            PendingTransaction pend = new PendingTransaction(tx);

            if (logger.isInfoEnabled() && wireTransactions.contains(pend))
                logger.info("Clear wire transaction, hash: [{}]", Hex.toHexString(tx.getHash()));

            wireTransactions.remove(pend);
        }
    }

    private void clearPendingState(List<Transaction> txs) {
        if (logger.isInfoEnabled()) {
            for (Transaction tx : txs)
                if (pendingStateTransactions.contains(tx))
                    logger.info("Clear pending state transaction, hash: [{}]", Hex.toHexString(tx.getHash()));
        }

        pendingStateTransactions.removeAll(txs);
    }

    private void updateState() {

        pendingState = repository.startTracking();

        synchronized (pendingStateTransactions) {
            for (Transaction tx : pendingStateTransactions) executeTx(tx);
        }
    }

    private TransactionReceipt executeTx(Transaction tx) {

        logger.info("Apply pending state tx: {}", Hex.toHexString(tx.getHash()));

        Block best = blockchain.getBestBlock();

        TransactionExecutor executor = new TransactionExecutor(
                tx, best.getCoinbase(), pendingState,
                blockStore, programInvokeFactory, best
        );

        executor.init();
        executor.execute();
        executor.go();
        executor.finalization();

        return executor.getReceipt();
    }

    public void setBlockchain(Blockchain blockchain) {
        this.blockchain = blockchain;
    }
}
