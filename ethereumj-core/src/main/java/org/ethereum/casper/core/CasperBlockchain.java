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
package org.ethereum.casper.core;

import org.ethereum.casper.config.CasperProperties;
import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.BlockSummary;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.ImportResult;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutionSummary;
import org.ethereum.core.TransactionExecutor;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.datasource.Source;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.math.BigInteger.ONE;
import static org.ethereum.casper.config.net.CasperTestNetConfig.NULL_SIGN_SENDER;
import static org.ethereum.core.ImportResult.EXIST;
import static org.ethereum.core.ImportResult.IMPORTED_BEST;
import static org.ethereum.core.ImportResult.IMPORTED_NOT_BEST;
import static org.ethereum.core.ImportResult.INVALID_BLOCK;
import static org.ethereum.core.ImportResult.NO_PARENT;

public class CasperBlockchain extends BlockchainImpl {

    private static final Logger logger = LoggerFactory.getLogger("blockchain");
    private static final Logger stateLogger = LoggerFactory.getLogger("state");

    private static final BigInteger PRETTY_BIG = BigInteger.valueOf(10).pow(40);
    private static final BigInteger NON_REVERT_MIN_DEPOSIT = BigInteger.valueOf(10).pow(18);

    private static final long EPOCH_SWITCH_GASLIMIT = 3_141_592;  // Sames as block gas limit

    @Autowired
    private CasperFacade casper;

    @Autowired
    @Qualifier("finalizedBlocks")
    private Source<byte[], byte[]> finalizedBlocks;

    public CasperBlockchain()  {
        throw new RuntimeException("Empty constructor not available");
    }

    @Autowired
    public CasperBlockchain(SystemProperties config) {
        super(config);
    }

    private boolean switchRevertsFinalizedBlock(final Block block) {
        if (block == null) return false;
        Block oldHead = getBestBlock();
        Block newHead = block;

        // Assuming old head > new head and there are no finalized blocks between
        while (oldHead.getNumber() > newHead.getNumber()) {
            byte[] finalized = finalizedBlocks.get(oldHead.getHash());
            if (finalized != null) {
                logger.warn("Attempt to revert failed: checkpoint {} is finalized", oldHead.getShortDescr());
                return true;
            }
            oldHead = getBlockByHash(oldHead.getParentHash());
        }

        // Assuming new head > old head and there could be connected in one chain
        while (newHead.getNumber() > oldHead.getNumber()) {
            newHead = getBlockByHash(newHead.getParentHash());
            if (newHead == null) {
                logger.warn("Proposed head block {} is not connected with canonical chain", block.getShortDescr());
                return false;
            }
        }

        // As we are currently on one height we could be on one chain before any finalized block
        while(!Arrays.areEqual(oldHead.getHash(), newHead.getHash())) {
            byte[] finalized = finalizedBlocks.get(oldHead.getHash());
            if (finalized != null) {
                logger.warn("Attempt to revert failed: checkpoint {} is finalized", oldHead.getShortDescr());
                return true;
            }
            oldHead = getBlockByHash(oldHead.getParentHash());
            newHead = getBlockByHash(newHead.getParentHash());
            if (newHead == null) {
                logger.warn("Proposed head block {} is not connected with canonical chain", block.getShortDescr());
                return false;
            }
        }
        return false;
    }

    private BigInteger getScore(final Block block) {
        Object[] res = casper.constCall(block, "get_last_justified_epoch");
        return ((BigInteger) res[0]).multiply(PRETTY_BIG).add(getPoWDifficulty(block));
    }

    @Override
    public synchronized ImportResult tryToConnect(final Block block) {

        if (logger.isDebugEnabled())
            logger.debug("Try connect block hash: {}, number: {}",
                    Hex.toHexString(block.getHash()).substring(0, 6),
                    block.getNumber());

        if (blockExists(block)) return EXIST; // retry of well known block

        // TODO: Remove try/catch, debug only
        try {
            return casperConnect(block);
        } catch (Exception ex) {
            throw ex;
        }
    }

    private synchronized ImportResult casperConnect(final Block block) {
        final ImportResult ret;
        final BlockSummary summary;

        if (blockStore.isBlockExist(block.getParentHash())) {
            recordBlock(block);
            Repository repo;

            Block parentBlock = getBlockByHash(block.getParentHash());
            Block bestBlock = getBestBlock();
            repo = getRepository().getSnapshotTo(parentBlock.getStateRoot());

            // We already handle not matched block stateRoot and
            // real root due to Casper background txs
            State savedState = pushState(block.getParentHash());
            this.fork = true;
            try {
                summary = add(repo, block);
                if (summary == null) {
                    return INVALID_BLOCK;
                }
            } catch (Throwable th) {
                logger.error("Unexpected error: ", th);
                return INVALID_BLOCK;
            } finally {
                this.fork = false;
            }

            if (getScore(bestBlock).compareTo(getScore(block)) >= 0 ||
                    switchRevertsFinalizedBlock(block)) {
                logger.info("Skipping block {} which is not a descendant of current head checkpoint", block.getNumber());
                // Stay on previous branch
                popState();
                ret = IMPORTED_NOT_BEST;
            } else {

                // Main branch become this branch
                // cause we proved that total difficulty
                // is greater
                blockStore.reBranch(block);

                // The main repository rebranch
                setRepository(repo);

                dropState();

                finalizeCheckpoint(block);

                ret = IMPORTED_BEST;
            }

            listener.onBlock(summary);
            listener.trace(String.format("Block chain size: [ %d ]", this.getSize()));

            if (ret == IMPORTED_BEST) {
                eventDispatchThread.invokeLater(() -> getPendingState().processBest(block, summary.getReceipts()));
            }
            return ret;
        } else {
            return NO_PARENT;
        }
    }

    /**
     * Finalizes Casper epoch checkpoint if needed
     */
    private void finalizeCheckpoint(final Block block) {
        Object[] res = casper.constCall(block, "get_last_finalized_epoch");
        long finalizedEpoch = ((BigInteger) res[0]).longValue();
        Object[] res2 = casper.constCall(block, "get_current_epoch");
        long currentEpoch = ((BigInteger) res2[0]).longValue();
        if (finalizedEpoch == currentEpoch - 1) {
            // Actually one hash per epoch, just the getter for array
            Object[] res3 = casper.constCall(block, "get_checkpoint_hashes", finalizedEpoch);
            byte[] checkpointHash = (byte[]) res3[0];
            if (!Arrays.areEqual(checkpointHash, new byte[32])) {  // new byte[32] == 00-filled
                Block histBlock = getBlockByHash(checkpointHash);
                Object[] res4 = casper.constCall(histBlock, "get_total_curdyn_deposits");
                BigInteger curDeposits = (BigInteger) res4[0];
                Object[] res5 = casper.constCall(histBlock, "get_total_prevdyn_deposits");
                BigInteger prevDeposits = (BigInteger) res5[0];
                if (curDeposits.compareTo(NON_REVERT_MIN_DEPOSIT) > 0 &&
                        prevDeposits.compareTo(NON_REVERT_MIN_DEPOSIT) > 0) {
                    finalizedBlocks.put(checkpointHash, new byte[] {0x01}); // True
                    logger.info("Finalized checkpoint {} {}", finalizedEpoch, Hex.toHexString(checkpointHash));
                } else {
                    logger.info("Trivially finalized checkpoint {}", finalizedEpoch);
                }
            }
        }
    }

    @Override
    protected boolean checkBlockSummary(BlockSummary summary, Repository track) {
        boolean res = super.checkBlockSummary(summary, track);
        if (!res) {  // Already bad block
            return false;
        }

        // Casper-specific checks

        // Check for failed casper txs
        TransactionReceipt failedCasperVote = null;
        for (int i = 0; i < summary.getReceipts().size(); ++i) {
            TransactionReceipt receipt = summary.getReceipts().get(i);
            if(!receipt.isSuccessful() && casper.isVote(receipt.getTransaction())) {
                failedCasperVote = receipt;
                break;
            }
        }
        if (failedCasperVote != null) {
            logger.warn("Block contains failed casper vote (receipt: {}, tx: {})",
                    failedCasperVote, failedCasperVote.getTransaction());
            return false;
        }

        return true;
    }

    @Override
    protected BlockSummary applyBlock(Repository track, Block block) {

        logger.debug("applyBlock: block: [{}] tx.list: [{}]", block.getNumber(), block.getTransactionsList().size());

        BlockchainConfig blockchainConfig = config.getBlockchainConfig().getConfigForBlock(block.getNumber());
        blockchainConfig.hardForkTransfers(block, track);
        initCasper(track, block);

        long saveTime = System.nanoTime();
        int i = 1;
        long totalGasUsed = 0;
        List<TransactionReceipt> receipts = new ArrayList<>();
        List<TransactionExecutionSummary> summaries = new ArrayList<>();

        List<Transaction> txs = new ArrayList<>(block.getTransactionsList());

        // Initialize the next epoch in the Casper contract
        int epochLength = ((CasperProperties) config).getCasperEpochLength();
        if(block.getNumber() % epochLength == 0 && block.getNumber() != 0) {
            long startingEpoch = block.getNumber() / epochLength;
            byte[] data = casper.getContract().getByName("initialize_epoch").encode(startingEpoch);
            Transaction tx = new Transaction(
                    ByteUtil.bigIntegerToBytes(track.getNonce(NULL_SIGN_SENDER.getAddress())),
                    new byte[0],
                    ByteUtil.longToBytesNoLeadZeroes(EPOCH_SWITCH_GASLIMIT),
                    casper.getAddress(),
                    new byte[0],
                    data
            );
            tx.sign(NULL_SIGN_SENDER);
            txs.add(0, tx);
        }

        for (Transaction tx : txs) {
            stateLogger.debug("apply block: [{}] tx: [{}] ", block.getNumber(), i);

            Repository txTrack = track.startTracking();
            TransactionExecutor executor = createTransactionExecutor(tx, block.getCoinbase(), txTrack,
                    block, totalGasUsed);

            executor.init();
            executor.execute();
            executor.go();
            TransactionExecutionSummary summary = executor.finalization();

            totalGasUsed += executor.getGasUsed();

            txTrack.commit();
            final TransactionReceipt receipt = executor.getReceipt();

            if (blockchainConfig.eip658()) {
                receipt.setTxStatus(receipt.isSuccessful());
            } else {
                receipt.setPostTxState(track.getRoot());
            }

            stateLogger.info("block: [{}] executed tx: [{}] \n  state: [{}]", block.getNumber(), i,
                    Hex.toHexString(track.getRoot()));

            stateLogger.info("[{}] ", receipt.toString());

            if (stateLogger.isInfoEnabled())
                stateLogger.info("tx[{}].receipt: [{}] ", i, Hex.toHexString(receipt.getEncoded()));

            // TODO
//            if (block.getNumber() >= config.traceStartBlock())
//                repository.dumpState(block, totalGasUsed, i++, tx.getHash());

            receipts.add(receipt);
            if (summary != null) {
                summaries.add(summary);
            }
        }

        Map<byte[], BigInteger> rewards = addReward(track, block, summaries);

        stateLogger.info("applied reward for block: [{}]  \n  state: [{}]",
                block.getNumber(),
                Hex.toHexString(track.getRoot()));


        // TODO
//        if (block.getNumber() >= config.traceStartBlock())
//            repository.dumpState(block, totalGasUsed, 0, null);

        long totalTime = System.nanoTime() - saveTime;
        adminInfo.addBlockExecTime(totalTime);
        logger.debug("block: num: [{}] hash: [{}], executed after: [{}]nano", block.getNumber(), block.getShortHash(), totalTime);

        return new BlockSummary(block, rewards, receipts, summaries);
    }

    @Override
    public TransactionExecutor createTransactionExecutor(Transaction transaction, byte[] minerCoinbase, Repository track,
                                                            Block currentBlock, long gasUsedInTheBlock) {
        return new CasperTransactionExecutor(transaction, minerCoinbase,
                track, blockStore, getProgramInvokeFactory(), currentBlock, listener, gasUsedInTheBlock)
                .withCommonConfig(commonConfig);
    }

    private void initCasper(Repository track, Block block) {
        // All changes should be applied only just after genesis, before 1st block state changes
        if (block.getNumber() != 1)
            return;

        List<Transaction> txs = casper.getInitTxs();
        byte[] casperAddress = ((CasperProperties) config).getCasperAddress();
        byte[] coinbase = blockStore.getChainBlockByNumber(0).getCoinbase();

        txs.forEach((tx) -> {
            // We need money!
            track.addBalance(NULL_SIGN_SENDER.getAddress(), BigInteger.valueOf(15).pow(18));

            Repository txTrack = track.startTracking();
            TransactionExecutor executor = createTransactionExecutor(tx, coinbase, txTrack, block, 0);

            executor.init();
            executor.execute();
            executor.go();
            executor.finalization();

            byte[] contractAddress = executor.getReceipt().getTransaction().getContractAddress();
            if (contractAddress != null) {
                logger.info("Casper init: contract deployed at {}, tx: [{}]", Hex.toHexString(contractAddress), tx);
            }
            if (!executor.getReceipt().isSuccessful()) {
                logger.error("Casper init failed on tx [{}], receipt [{}], breaking", tx, executor.getReceipt());
                throw new RuntimeException("Casper initialization transactions on 1st block failed");
            }

            txTrack.commit();
            BigInteger restBalance = track.getBalance(NULL_SIGN_SENDER.getAddress());
            track.addBalance(NULL_SIGN_SENDER.getAddress(), restBalance.negate());
            track.addBalance(casperAddress, track.getBalance(casperAddress).negate());
            track.addBalance(casperAddress, BigInteger.valueOf(10).pow(25));
        });
    }

    /**
     * This mechanism enforces a homeostasis in terms of the time between blocks;
     * a smaller period between the last two blocks results in an increase in the
     * difficulty level and thus additional computation required, lengthening the
     * likely next period. Conversely, if the period is too large, the difficulty,
     * and expected time to the next block, is reduced.
     */
    @Override
    protected boolean isValid(Repository repo, Block block) {

        boolean isValid = true;

        if (!block.isGenesis()) {
            isValid = isValid(block.getHeader());

            // Sanity checks
            String trieHash = Hex.toHexString(block.getTxTrieRoot());
            String trieListHash = Hex.toHexString(calcTxTrie(block.getTransactionsList()));


            if (!trieHash.equals(trieListHash)) {
                logger.warn("Block's given Trie Hash doesn't match: {} != {}", trieHash, trieListHash);
                return false;
            }

//            if (!validateUncles(block)) return false;

            List<Transaction> txs = block.getTransactionsList();
            if (!txs.isEmpty()) {
//                Repository parentRepo = repository;
//                if (!Arrays.equals(bestBlock.getHash(), block.getParentHash())) {
//                    parentRepo = repository.getSnapshotTo(getBlockByHash(block.getParentHash()).getStateRoot());
//                }

                Map<ByteArrayWrapper, BigInteger> curNonce = new HashMap<>();

                boolean votesStarted = false;
                for (Transaction tx : txs) {
                    byte[] txSender = tx.getSender();
                    ByteArrayWrapper key = new ByteArrayWrapper(txSender);
                    BigInteger expectedNonce = curNonce.get(key);
                    if (expectedNonce == null) {
                        expectedNonce = repo.getNonce(txSender);
                    }
                    // We shouldn't track nonce for NULL_SENDER
                    if (!key.equals(new ByteArrayWrapper(Transaction.NULL_SENDER))) {
                        curNonce.put(key, expectedNonce.add(ONE));
                    } else {
                        curNonce.put(key, expectedNonce);
                    }
                    BigInteger txNonce = new BigInteger(1, tx.getNonce());
                    if (!expectedNonce.equals(txNonce)) {
                        logger.warn("Invalid transaction: Tx nonce {} != expected nonce {} (parent nonce: {}): {}",
                                txNonce, expectedNonce, repo.getNonce(txSender), tx);
                        return false;
                    }

                    // Casper votes txs should come after regular txs
                    if (votesStarted && !casper.isVote(tx)) {
                        logger.warn("Invalid transaction: all transactions should be before casper votes: {}", tx);
                        return false;
                    }

                    if (!votesStarted && casper.isVote(tx)) {
                        votesStarted = true;
                    }
                }
            }
        }

        return isValid;
    }

    private BigInteger getPoWDifficulty(final Block block) {

        return blockStore.getTotalDifficultyForHash(block.getHash());
    }

    public void setCasper(CasperFacade casper) {
        this.casper = casper;
    }

    public void setFinalizedBlocks(Source<byte[], byte[]> finalizedBlocks) {
        this.finalizedBlocks = finalizedBlocks;
    }
}
