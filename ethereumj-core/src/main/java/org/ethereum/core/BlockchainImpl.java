package org.ethereum.core;

import org.codehaus.plexus.util.FileUtils;
import org.ethereum.db.BlockStore;
import org.ethereum.facade.Blockchain;
import org.ethereum.facade.Repository;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.AdminInfo;
import org.ethereum.net.BlockQueue;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.util.AdvancedDeviceUtils;
import org.ethereum.vm.ProgramInvokeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.core.Denomination.SZABO;

/**
 * The Ethereum blockchain is in many ways similar to the Bitcoin blockchain,
 * although it does have some differences.
 *
 * The main difference between Ethereum and Bitcoin with regard to the blockchain architecture
 * is that, unlike Bitcoin, Ethereum blocks contain a copy of both the transaction list
 * and the most recent state. Aside from that, two other values, the block number and
 * the difficulty, are also stored in the block.
 *
 * The block validation algorithm in Ethereum is as follows:
 * <ol>
 * <li>Check if the previous block referenced exists and is valid.</li>
 * <li>Check that the timestamp of the block is greater than that of the referenced previous block and less than 15 minutes into the future</li>
 * <li>Check that the block number, difficulty, transaction root, uncle root and gas limit (various low-level Ethereum-specific concepts) are valid.</li>
 * <li>Check that the proof of work on the block is valid.</li>
 * <li>Let S[0] be the STATE_ROOT of the previous block.</li>
 * <li>Let TX be the block's transaction list, with n transactions.
 * For all in in 0...n-1, set S[i+1] = APPLY(S[i],TX[i]).
 * If any applications returns an error, or if the total gas consumed in the block
 * up until this point exceeds the GASLIMIT, return an error.</li>
 * <li>Let S_FINAL be S[n], but adding the block reward paid to the miner.</li>
 * <li>Check if S_FINAL is the same as the STATE_ROOT. If it is, the block is valid; otherwise, it is not valid.</li>
 * </ol>
 * See <a href="https://github.com/ethereum/wiki/wiki/White-Paper#blockchain-and-mining">Ethereum Whitepaper</a>
 *
 * www.etherJ.com
 *
 * @author Roman Mandeleil
 * @author Nick Savers
 * @since 20.05.2014
 */
@Component
public class BlockchainImpl implements Blockchain {

    /* A scalar value equal to the mininum limit of gas expenditure per block */
    private static long MIN_GAS_LIMIT = 125000L;

    private static final Logger logger = LoggerFactory.getLogger("blockchain");
    private static final Logger stateLogger = LoggerFactory.getLogger("state");

    // to avoid using minGasPrice=0 from Genesis for the wallet
    private static final long INITIAL_MIN_GAS_PRICE = 10 * SZABO.longValue();

    @Autowired
    private Repository repository;
    private Repository track;

    @Autowired
    private BlockStore blockStore;

    private Block bestBlock;
    private BigInteger totalDifficulty = BigInteger.ZERO;

    @Autowired
    Wallet wallet;

    @Autowired
    private EthereumListener listener;

    @Autowired
    private BlockQueue blockQueue;

    @Autowired
    private ChannelManager channelManager;

    private boolean syncDoneCalled = false;

    @Autowired
    ProgramInvokeFactory programInvokeFactory;

    @Autowired
    private AdminInfo adminInfo;

    private List<Chain> altChains = new ArrayList<>();
    private List<Block> garbage = new ArrayList<>();

    @Override
    public byte[] getBestBlockHash() {
        return getBestBlock().getHash();
    }

    @Override
    public long getSize() {
        return bestBlock.getNumber() + 1;
    }

    @Override
    public Block getBlockByNumber(long blockNr) {
        return blockStore.getBlockByNumber(blockNr);
    }

    @Override
    public TransactionReceipt getTransactionReceiptByHash(byte[] hash) {

        return blockStore.getTransactionReceiptByHash(hash);
    }

    @Override
    public Block getBlockByHash(byte[] hash) {
        return blockStore.getBlockByHash(hash);
    }

    @Override
    public List<byte[]> getListOfHashesStartFrom(byte[] hash, int qty) {
        return blockStore.getListOfHashesStartFrom(hash, qty);
    }


    public void tryToConnect(Block block) {

        recordBlock(block);

        if (logger.isDebugEnabled())
            logger.debug("Try connect block hash: {}, number: {}",
                    Hex.toHexString(block.getHash()).substring(0, 6),
                    block.getNumber());

        if (blockStore.getBlockByHash(block.getHash()) != null) {

            if (logger.isDebugEnabled())
                logger.debug("Block already exist hash: {}, number: {}",
                        Hex.toHexString(block.getHash()).substring(0, 6),
                        block.getNumber());

            // retry of well known block
            return;
        }

        // The simple case got the block
        // to connect to the main chain
        if (bestBlock.isParentOf(block)) {
            add(block);
            return;
        }

        if (!hasParentOnTheChain(block) && block.getNumber() > bestBlock.getNumber()) {

            logger.info("*** Blockchain will rollback and resynchronise now ");

            long rollbackIdx = bestBlock.getNumber() - 30;
            if (rollbackIdx <= 0) rollbackIdx = bestBlock.getNumber() - bestBlock.getNumber() / 10;

            Block rollbackBlock = blockStore.getBlockByNumber(rollbackIdx);
            repository.syncToRoot(rollbackBlock.getStateRoot());

            BigInteger deltaTD = blockStore.getTotalDifficultySince(rollbackBlock.getNumber());
            totalDifficulty = totalDifficulty.subtract(deltaTD);
            bestBlock = rollbackBlock;

            blockStore.deleteBlocksSince(rollbackBlock.getNumber());

            channelManager.ethSync();
            return;
        }

        // provisional, by the garbage will be
        // defined how to deal with it in the
        // future.
        garbage.add(block);
    }


    @Override
    public void add(Block block) {

        track = repository.startTracking();
        if (block == null)
            return;

        // keep chain continuity
        if (!Arrays.equals(getBestBlock().getHash(),
                block.getParentHash())) return;

        if (block.getNumber() >= CONFIG.traceStartBlock() && CONFIG.traceStartBlock() != -1) {
            AdvancedDeviceUtils.adjustDetailedTracing(block.getNumber());
        }

        this.processBlock(block);
        stateLogger.info("applied reward for block: [{}]  \n  state: [{}]",
                block.getNumber(),
                Hex.toHexString(repository.getRoot()));

        track.commit();
        repository.flush(); // saving to the disc


        // Remove all wallet transactions as they already approved by the net
        wallet.removeTransactions(block.getTransactionsList());


        listener.trace(String.format("Block chain size: [ %d ]", this.getSize()));
        listener.onBlock(block);

        if (blockQueue.size() == 0 &&
                !syncDoneCalled &&
                channelManager.isAllSync()) {

            logger.info("Sync done");
            syncDoneCalled = true;
            listener.onSyncDone();
        }
    }


    public Block getParent(BlockHeader header) {

        return blockStore.getBlockByHash(header.getParentHash());
    }

    /**
     * Calculate GasLimit
     * See Yellow Paper: http://www.gavwood.com/Paper.pdf - page 5, 4.3.4 (25)
     *
     * @return long value of the gasLimit
     */
    public long calcGasLimit(BlockHeader header) {
        if (header.isGenesis())
            return Genesis.GAS_LIMIT;
        else {
            Block parent = getParent(header);
            return Math.max(MIN_GAS_LIMIT, (parent.getGasLimit() * (1024 - 1) + (parent.getGasUsed() * 6 / 5)) / 1024);
        }
    }


    public boolean isValid(BlockHeader header) {
        boolean isValid = false;
        // verify difficulty meets requirements
        isValid = header.getDifficulty() == header.calcDifficulty();
        // verify gasLimit meets requirements
        isValid = isValid && header.getGasLimit() == calcGasLimit(header);
        // verify timestamp meets requirements
        isValid = isValid && header.getTimestamp() > getParent(header).getTimestamp();
        // verify extraData doesn't exceed 1024 bytes
        isValid = isValid && header.getExtraData() == null || header.getExtraData().length <= 1024;
        return isValid;
    }


    /**
     * This mechanism enforces a homeostasis in terms of the time between blocks;
     * a smaller period between the last two blocks results in an increase in the
     * difficulty level and thus additional computation required, lengthening the
     * likely next period. Conversely, if the period is too large, the difficulty,
     * and expected time to the next block, is reduced.
     */
    private boolean isValid(Block block) {

        boolean isValid = true;
        if (isValid) return (isValid); // todo get back to the real header validation

        if (!block.isGenesis()) {
            isValid = isValid(block.getHeader());

            for (BlockHeader uncle : block.getUncleList()) {
                // - They are valid headers (not necessarily valid blocks)
                isValid = isValid(uncle);
                // - Their parent is a kth generation ancestor for k in {2, 3, 4, 5, 6, 7}
                long generationGap = block.getNumber() - getParent(uncle).getNumber();
                isValid = generationGap > 1 && generationGap < 8;
                // - They were not uncles of the kth generation ancestor for k in {1, 2, 3, 4, 5, 6}
                generationGap = block.getNumber() - uncle.getNumber();
                isValid = generationGap > 0 && generationGap < 7;
            }
        }
        if (!isValid)
            logger.warn("WARNING: Invalid - {}", this);
        return isValid;

    }

    private void processBlock(Block block) {

        List<TransactionReceipt> receipts = new ArrayList<>();
        if (isValid(block)) {
            if (!block.isGenesis()) {
                if (!CONFIG.blockChainOnly()) {
                    wallet.addTransactions(block.getTransactionsList());
                    receipts = this.applyBlock(block);
                    wallet.processBlock(block);
                }
            }
            this.storeBlock(block, receipts);
        } else {
            logger.warn("Invalid block with nr: {}", block.getNumber());
        }
    }

    private List<TransactionReceipt> applyBlock(Block block) {

        int i = 1;
        long totalGasUsed = 0;
        List<TransactionReceipt> reciepts = new ArrayList<>();

        for (Transaction tx : block.getTransactionsList()) {
            stateLogger.info("apply block: [{}] tx: [{}] ", block.getNumber(), i);

            TransactionExecutor executor = new TransactionExecutor(tx, block.getCoinbase(), track,
                    programInvokeFactory, block);
            executor.execute();

            TransactionReceipt receipt = executor.getReceipt();
            totalGasUsed += receipt.getCumulativeGasLong();

            track.commit();
            receipt.setCumulativeGas(totalGasUsed);
            receipt.setPostTxState(repository.getRoot());
            receipt.setTransaction(tx);

            stateLogger.info("block: [{}] executed tx: [{}] \n  state: [{}]", block.getNumber(), i,
                    Hex.toHexString(repository.getRoot()));

            stateLogger.info("[{}] ", receipt.toString());

            if (stateLogger.isInfoEnabled())
                stateLogger.info("tx[{}].receipt: [{}] ", i, Hex.toHexString(receipt.getEncoded()));

            if (block.getNumber() >= CONFIG.traceStartBlock())
                repository.dumpState(block, totalGasUsed, i++, tx.getHash());

            reciepts.add(receipt);
        }

        this.addReward(block);
        this.updateTotalDifficulty(block);

        track.commit();

        if (block.getNumber() >= CONFIG.traceStartBlock())
            repository.dumpState(block, totalGasUsed, 0, null);

        return reciepts;
    }

    /**
     * Add reward to block- and every uncle coinbase
     * assuming the entire block is valid.
     *
     * @param block object containing the header and uncles
     */
    private void addReward(Block block) {

        // Add standard block reward
        BigInteger totalBlockReward = Block.BLOCK_REWARD;

        // Add extra rewards based on number of uncles
        if (block.getUncleList().size() > 0) {
            for (BlockHeader uncle : block.getUncleList()) {
                track.addBalance(uncle.getCoinbase(), Block.UNCLE_REWARD);
            }
            totalBlockReward = totalBlockReward.add(Block.INCLUSION_REWARD
                    .multiply(BigInteger.valueOf(block.getUncleList().size())));
        }
        track.addBalance(block.getCoinbase(), totalBlockReward);
    }

    @Override
    public void storeBlock(Block block, List<TransactionReceipt> receipts) {

        /* Debug check to see if the state is still as expected */
        if (logger.isWarnEnabled()) {
            String blockStateRootHash = Hex.toHexString(block.getStateRoot());
            String worldStateRootHash = Hex.toHexString(repository.getRoot());
            if (!blockStateRootHash.equals(worldStateRootHash)) {

                stateLogger.info("BLOCK: STATE CONFLICT! block: {} worldstate {} mismatch", block.getNumber(), worldStateRootHash);
                adminInfo.lostConsensus();

                // in case of rollback hard move the root
//                Block parentBlock = blockStore.getBlockByHash(block.getParentHash());
//                repository.syncToRoot(parentBlock.getStateRoot());
                // todo: after the rollback happens other block should be requested
            }
        }

        blockStore.saveBlock(block, receipts);
        this.setBestBlock(block);

        if (logger.isDebugEnabled())
            logger.debug("block added to the blockChain: index: [{}]", block.getNumber());
        if (block.getNumber() % 100 == 0)
            logger.info("*** Last block added [ #{} ]", block.getNumber());
    }


    public boolean hasParentOnTheChain(Block block) {
        return getParent(block.getHeader()) != null;
    }

    @Override
    public List<Chain> getAltChains() {
        return altChains;
    }

    @Override
    public List<Block> getGarbage() {
        return garbage;
    }


    @Override
    public BlockQueue getQueue() {
        return blockQueue;
    }

    @Override
    public void setBestBlock(Block block) {
        bestBlock = block;
    }

    @Override
    public Block getBestBlock() {
        return bestBlock;
    }

    @Override
    public void reset() {
        blockStore.reset();
        altChains = new ArrayList<>();
        garbage = new ArrayList<>();
    }

    @Override
    public void close() {
        blockQueue.close();
    }

    @Override
    public BigInteger getTotalDifficulty() {
        return totalDifficulty;
    }

    @Override
    public void updateTotalDifficulty(Block block) {
        this.totalDifficulty = totalDifficulty.add(block.getCumulativeDifficulty());
    }

    @Override
    public void setTotalDifficulty(BigInteger totalDifficulty) {
        this.totalDifficulty = totalDifficulty;
    }

    private void recordBlock(Block block) {

        if (!CONFIG.recordBlocks()) return;

        if (bestBlock.isGenesis()) {
            try {
                FileUtils.deleteDirectory(CONFIG.dumpDir());
            } catch (IOException e) {
            }
        }

        String dir = CONFIG.dumpDir() + "/";

        File dumpFile = new File(System.getProperty("user.dir") + "/" + dir + "_blocks_rec.txt");
        FileWriter fw = null;
        BufferedWriter bw = null;

        try {

            dumpFile.getParentFile().mkdirs();
            if (!dumpFile.exists()) dumpFile.createNewFile();

            fw = new FileWriter(dumpFile.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);

            if (bestBlock.isGenesis()) {
                bw.write(Hex.toHexString(bestBlock.getEncoded()));
                bw.write("\n");
            }

            bw.write(Hex.toHexString(block.getEncoded()));
            bw.write("\n");

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public void setProgramInvokeFactory(ProgramInvokeFactory factory) {
        this.programInvokeFactory = factory;
    }

    public void startTracking() {
        track = repository.startTracking();
    }

    public void commitTracking() {
        track.commit();
    }

}
