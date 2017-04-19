/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.core;

import static org.ethereum.listener.EthereumListener.PendingTransactionState.DROPPED;
import static org.ethereum.listener.EthereumListener.PendingTransactionState.INCLUDED;
import static org.ethereum.listener.EthereumListener.PendingTransactionState.NEW_PENDING;
import static org.ethereum.listener.EthereumListener.PendingTransactionState.PENDING;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.collections4.map.LRUMap;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.db.BlockStore;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.TransactionStore;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListener.PendingTransactionState;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    private static final Logger logger = LoggerFactory.getLogger("pending");

    @Autowired
    private SystemProperties config = SystemProperties.getDefault();

    @Autowired
    CommonConfig commonConfig = CommonConfig.getDefault();

    @Autowired
    private EthereumListener listener;

    @Autowired
    private BlockchainImpl blockchain;

    @Autowired
    private BlockStore blockStore;

    @Autowired
    private TransactionStore transactionStore;

    @Autowired
    private ProgramInvokeFactory programInvokeFactory;

//    private Repository repository;

    private final List<PendingTransaction> pendingTransactions = new ArrayList<>();

    // to filter out the transactions we have already processed
    // transactions could be sent by peers even if they were already included into blocks
    private final Map<ByteArrayWrapper, Object> receivedTxs = new LRUMap<>(100000);
    private final Object dummyObject = new Object();

    private Repository pendingState;

    private Block best = null;

    @Autowired
    public PendingStateImpl(final EthereumListener listener, final BlockchainImpl blockchain) {
        this.listener = listener;
        this.blockchain = blockchain;
//        this.repository = blockchain.getRepository();
        this.blockStore = blockchain.getBlockStore();
        this.programInvokeFactory = blockchain.getProgramInvokeFactory();
        this.transactionStore = blockchain.getTransactionStore();
    }

    public void init() {
        this.pendingState = getOrigRepository().startTracking();
    }

    private Repository getOrigRepository() {
        return blockchain.getRepositorySnapshot();
    }

    @Override
    public synchronized Repository getRepository() {
        if (pendingState == null) {
            init();
        }
        return pendingState;
    }

    @Override
    public synchronized List<Transaction> getPendingTransactions() {

        List<Transaction> txs = new ArrayList<>();

        for (PendingTransaction tx : pendingTransactions) {
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
        return receivedTxs.put(new ByteArrayWrapper(tx.getHash()), dummyObject) == null;
    }

    @Override
    public void addPendingTransaction(Transaction tx) {
        addPendingTransactions(Collections.singletonList(tx));
    }

    @Override
    public synchronized List<Transaction> addPendingTransactions(List<Transaction> transactions) {
        int unknownTx = 0;
        List<Transaction> newPending = new ArrayList<>();
        for (Transaction tx : transactions) {
            if (addNewTxIfNotExist(tx)) {
                unknownTx++;
                if (addPendingTransactionImpl(tx)) {
                    newPending.add(tx);
                }
            }
        }

        logger.debug("Wire transaction list added: total: {}, new: {}, valid (added to pending): {} (current #of known txs: {})",
                transactions.size(), unknownTx, newPending, receivedTxs.size());

        if (!newPending.isEmpty()) {
            listener.onPendingTransactionsReceived(newPending);
            listener.onPendingStateChanged(PendingStateImpl.this);
        }

        return newPending;
    }

    public synchronized void trackTransaction(Transaction tx) {
        List<TransactionInfo> infos = transactionStore.get(tx.getHash());
        if (!infos.isEmpty()) {
            for (TransactionInfo info : infos) {
                Block txBlock = blockStore.getBlockByHash(info.getBlockHash());
                if (txBlock.isEqual(blockStore.getChainBlockByNumber(txBlock.getNumber()))) {
                    // transaction included to the block on main chain
                    info.getReceipt().setTransaction(tx);
                    fireTxUpdate(info.getReceipt(), INCLUDED, txBlock);
                    return;
                }
            }
        }
        addPendingTransaction(tx);
    }

    private void fireTxUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("PendingTransactionUpdate: (Tot: %3s) %12s : %s %8s %s [%s]",
                    getPendingTransactions().size(),
                    state, Hex.toHexString(txReceipt.getTransaction().getSender()).substring(0, 8),
                    ByteUtil.byteArrayToLong(txReceipt.getTransaction().getNonce()),
                    block.getShortDescr(), txReceipt.getError()));
        }
        listener.onPendingTransactionUpdate(txReceipt, state, block);
    }

    /**
     * Executes pending tx on the latest best block
     * Fires pending state update
     * @param tx    Transaction
     * @return True if transaction gets NEW_PENDING state, False if DROPPED
     */
    private boolean addPendingTransactionImpl(final Transaction tx) {
        TransactionReceipt newReceipt = new TransactionReceipt();
        newReceipt.setTransaction(tx);

        String err = validate(tx);

        TransactionReceipt txReceipt;
        if (err != null) {
            txReceipt = createDroppedReceipt(tx, err);
        } else {
            txReceipt = executeTx(tx);
        }

        if (!txReceipt.isValid()) {
            fireTxUpdate(txReceipt, DROPPED, getBestBlock());
        } else {
            pendingTransactions.add(new PendingTransaction(tx, getBestBlock().getNumber()));
            fireTxUpdate(txReceipt, NEW_PENDING, getBestBlock());
        }
        return txReceipt.isValid();
    }

    private TransactionReceipt createDroppedReceipt(Transaction tx, String error) {
        TransactionReceipt txReceipt = new TransactionReceipt();
        txReceipt.setTransaction(tx);
        txReceipt.setError(error);
        return txReceipt;
    }

    // validations which are not performed within executeTx
    private String validate(Transaction tx) {
        try {
            tx.verify();
        } catch (Exception e) {
            return String.format("Invalid transaction: %s", e.getMessage());
        }

        if (config.getMineMinGasPrice().compareTo(ByteUtil.bytesToBigInteger(tx.getGasPrice())) > 0) {
            return "Too low gas price for transaction: " + ByteUtil.bytesToBigInteger(tx.getGasPrice());
        }

        return null;
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
    public synchronized void processBest(Block newBlock, List<TransactionReceipt> receipts) {

        if (getBestBlock() != null && !getBestBlock().isParentOf(newBlock)) {
            // need to switch the state to another fork

            Block commonAncestor = findCommonAncestor(getBestBlock(), newBlock);

            if (logger.isDebugEnabled()) logger.debug("New best block from another fork: "
                    + newBlock.getShortDescr() + ", old best: " + getBestBlock().getShortDescr()
                    + ", ancestor: " + commonAncestor.getShortDescr());

            // first return back the transactions from forked blocks
            Block rollback = getBestBlock();
            while(!rollback.isEqual(commonAncestor)) {
                List<PendingTransaction> blockTxs = new ArrayList<>();
                for (Transaction tx : rollback.getTransactionsList()) {
                    logger.trace("Returning transaction back to pending: " + tx);
                    blockTxs.add(new PendingTransaction(tx, commonAncestor.getNumber()));
                }
                pendingTransactions.addAll(0, blockTxs);
                rollback = blockchain.getBlockByHash(rollback.getParentHash());
            }

            // rollback the state snapshot to the ancestor
            pendingState = getOrigRepository().getSnapshotTo(commonAncestor.getStateRoot()).startTracking();

            // next process blocks from new fork
            Block main = newBlock;
            List<Block> mainFork = new ArrayList<>();
            while(!main.isEqual(commonAncestor)) {
                mainFork.add(main);
                main = blockchain.getBlockByHash(main.getParentHash());
            }

            // processing blocks from ancestor to new block
            for (int i = mainFork.size() - 1; i >= 0; i--) {
                processBestInternal(mainFork.get(i), null);
            }
        } else {
            logger.debug("PendingStateImpl.processBest: " + newBlock.getShortDescr());
            processBestInternal(newBlock, receipts);
        }

        best = newBlock;

        updateState(newBlock);

        listener.onPendingStateChanged(PendingStateImpl.this);
    }

    private void processBestInternal(Block block, List<TransactionReceipt> receipts) {

        clearPending(block, receipts);

        clearOutdated(block.getNumber());
    }

    private void clearOutdated(final long blockNumber) {
        List<PendingTransaction> outdated = new ArrayList<>();

        for (PendingTransaction tx : pendingTransactions) {
            if (blockNumber - tx.getBlockNumber() > config.txOutdatedThreshold()) {
                outdated.add(tx);

                fireTxUpdate(createDroppedReceipt(tx.getTransaction(),
                        "Tx was not included into last " + config.txOutdatedThreshold() + " blocks"),
                        DROPPED, getBestBlock());
            }
        }

        if (outdated.isEmpty()) return;

        if (logger.isDebugEnabled())
            for (PendingTransaction tx : outdated)
                logger.trace(
                        "Clear outdated pending transaction, block.number: [{}] hash: [{}]",
                        tx.getBlockNumber(),
                        Hex.toHexString(tx.getHash())
                );

        pendingTransactions.removeAll(outdated);
    }

    private void clearPending(Block block, List<TransactionReceipt> receipts) {
        for (int i = 0; i < block.getTransactionsList().size(); i++) {
            Transaction tx = block.getTransactionsList().get(i);
            PendingTransaction pend = new PendingTransaction(tx);

            if (pendingTransactions.remove(pend)) {
                try {
                    logger.trace("Clear pending transaction, hash: [{}]", Hex.toHexString(tx.getHash()));
                    TransactionReceipt receipt;
                    if (receipts != null) {
                        receipt = receipts.get(i);
                    } else {
                        TransactionInfo info = getTransactionInfo(tx.getHash(), block.getHash());
                        receipt = info.getReceipt();
                    }
                    fireTxUpdate(receipt, INCLUDED, block);
                } catch (Exception e) {
                    logger.error("Exception creating onPendingTransactionUpdate (block: " + block.getShortDescr() + ", tx: " + i, e);
                }
            }
        }
    }

    private TransactionInfo getTransactionInfo(byte[] txHash, byte[] blockHash) {
        TransactionInfo info = transactionStore.get(txHash, blockHash);
        Transaction tx = blockchain.getBlockByHash(info.getBlockHash()).getTransactionsList().get(info.getIndex());
        info.getReceipt().setTransaction(tx);
        return info;
    }

    private void updateState(Block block) {

        pendingState = getOrigRepository().startTracking();

        for (PendingTransaction tx : pendingTransactions) {
            TransactionReceipt receipt = executeTx(tx.getTransaction());
            fireTxUpdate(receipt, PENDING, block);
        }
    }

    private TransactionReceipt executeTx(Transaction tx) {

        logger.trace("Apply pending state tx: {}", Hex.toHexString(tx.getHash()));

        Block best = getBestBlock();

        TransactionExecutor executor = new TransactionExecutor(
                tx, best.getCoinbase(), getRepository(),
                blockStore, programInvokeFactory, createFakePendingBlock(), new EthereumListenerAdapter(), 0)
                .withCommonConfig(commonConfig);

        executor.init();
        executor.execute();
        executor.go();
        executor.finalization();

        return executor.getReceipt();
    }

    private Block createFakePendingBlock() {
        // creating fake lightweight calculated block with no hashes calculations
        Block block = new Block(best.getHash(),
                BlockchainImpl.EMPTY_LIST_HASH, // uncleHash
                new byte[32],
                new byte[32], // log bloom - from tx receipts
                new byte[0], // difficulty computed right after block creation
                best.getNumber() + 1,
                ByteUtil.longToBytesNoLeadZeroes(Long.MAX_VALUE), // max Gas Limit
                0,  // gas used
                best.getTimestamp() + 1,  // block time
                new byte[0],  // extra data
                new byte[0],  // mixHash (to mine)
                new byte[0],  // nonce   (to mine)
                new byte[32],  // receiptsRoot
                new byte[32],    // TransactionsRoot
                new byte[32], // stateRoot
                Collections.<Transaction>emptyList(), // tx list
                Collections.<BlockHeader>emptyList());  // uncle list
        return block;
    }

    public void setBlockchain(BlockchainImpl blockchain) {
        this.blockchain = blockchain;
    }
}
