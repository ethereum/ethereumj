package org.ethereum.manager;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockSummary;
import org.ethereum.core.SnapshotManifest;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionInfo;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.DbSource;
import org.ethereum.datasource.Serializers;
import org.ethereum.datasource.Source;
import org.ethereum.datasource.SourceCodec;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.DbFlushManager;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.db.RepositoryHashedKeysTrie;
import org.ethereum.db.StateSource;
import org.ethereum.db.TransactionStore;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.sync.SyncManager;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.TrieImpl;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.RLP;
import org.ethereum.util.Value;
import org.ethereum.vm.DataWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Creation and resolving of Snapshots
 * https://github.com/ethcore/parity/wiki/Warp-Sync-Snapshot-Format
 */
@Component
@Lazy
public class SnapshotManager {

    private static final Logger logger = LoggerFactory.getLogger("snapshot");

    private CompositeEthereumListener listener;

    @Autowired @Qualifier("snapshotDS")
    private DbSource<byte[]> snapshotDS;

    @Autowired @Qualifier("stateDS")
    private DbSource<byte[]> stateDS;

    @Autowired
    private StateSource stateSource;

    @Autowired
    private RepositoryHashedKeysTrie repository;

    @Autowired
    private IndexedBlockStore blockStore;

    @Autowired
    private TransactionStore txStore;

    @Autowired
    private DbFlushManager dbFlushManager;

    @Autowired
    private SyncManager syncManager;

    private boolean syncDone = false;

    private static final long BLOCK_FREQUENCY = 10_000;

    private static final long CHUNK_SIZE = 4 * 1024 * 1024;

    private static final int EXTRA_BLOCKS_WAIT = 32;

    public static final byte[] MANIFEST_KEY = HashUtil.sha3("SNAPSHOT_MANIFEST".getBytes());

    @Autowired
    public SnapshotManager(CompositeEthereumListener listener, SystemProperties config) {
        this.listener = listener;
        if (config.getWarpSnapshotCreation()) initCreation();
    }

    private void initCreation() {
        listener.addListener(new EthereumListenerAdapter() {
            @Override
            public void onSyncDone(SyncState state) {
                if (state == SyncState.COMPLETE) {
                    syncDone = true;
                }
            }

            @Override
            public void onBlock(final BlockSummary blockSummary) {
                if (syncDone && blockSummary.getBlock().getNumber() % BLOCK_FREQUENCY == EXTRA_BLOCKS_WAIT) {
                    syncManager.setPause(true);
                    dbFlushManager.commit();
                    dbFlushManager.flush();
                    try {
                        long manifestBlockNumber = blockSummary.getBlock().getNumber() - EXTRA_BLOCKS_WAIT;
                        createSnapshot(blockStore.getChainBlockByNumber(manifestBlockNumber));
                    } catch (Exception e) {
                        logger.error("Failed to create snapshot for block #{}", blockSummary.getBlock().getNumber());
                    } finally {
                        syncManager.setPause(false);
                    }
                }
            }
        });
    }


    private static void getAccountKeys(final Source<byte[], byte[]> stateDS,
                                       final Set<byte[]> accountKeys, byte[] ... roots) {
        SecureTrie trie = new SecureTrie(new SourceCodec.BytesKey<>(stateDS, Serializers.TrieNodeSerializer));
        for (byte[] root : roots) {
            trie.scanTree(root, new TrieImpl.ScanAction() {
                @Override
                public void doOnNode(byte[] hash, Value node) {}

                @Override
                public void doOnValue(byte[] nodeHash, Value node, byte[] key, byte[] value) {
                    accountKeys.add(key);
                }
            });
        }
    }

    private void createSnapshot(Block block) {
        logger.info("Creating state snapshot for block #{}", block.getNumber());
        List<byte[]> stateHashes = createStateSnapshot(block);
        logger.info("Creating blocks snapshot for block #{}", block.getNumber());
        List<byte[]> blockHashes = createBlocksSnapshot(block);

        logger.info("Saving manifest");
        SnapshotManifest manifest = new SnapshotManifest(
                stateHashes, blockHashes,
                block.getStateRoot(),
                block.getNumber(),
                block.getHash());

        byte[] oldManifestBytes = snapshotDS.get(MANIFEST_KEY);
        if (oldManifestBytes != null) {
            SnapshotManifest oldManifest = new SnapshotManifest(oldManifestBytes);
            logger.info("Removing old manifest data: {}", oldManifest);
            for (byte[] stateChunkHash : oldManifest.getStateHashes()) {
                snapshotDS.put(stateChunkHash, null);
            }
            for (byte[] blocksChunkHash : oldManifest.getBlockHashes()) {
                snapshotDS.put(blocksChunkHash, null);
            }
        }

        snapshotDS.put(MANIFEST_KEY, manifest.getEncoded());
        logger.info("Manifest saved: {}", manifest);
    }

    /**
     * Creates and saves state snapshots in snapshotDS
     * @param block     Block, for which state snapshot is taken
     * @return  list of state chunk hashes
     */
    private List<byte[]> createStateSnapshot(Block block) {

        byte[] stateRoot = block.getStateRoot();

        Set<byte[]> keys = new LinkedHashSet<>();
        repository.syncToRoot(stateRoot);
        Set<ByteArrayWrapper> codeHashes = new HashSet<>();  // hashes of code already added to chunks
        getAccountKeys(stateSource, keys, stateRoot);

        RLPChunk rlpChunk = new RLPChunk(CHUNK_SIZE);
        List<byte[]> stateChunkHashes = new ArrayList<>();
        for (byte[] currentKey : keys) {
            AccountState accountState = repository.getAccountState(currentKey);
            byte[] codeHash = null;
            byte[] storageRoot = null;
            byte[] code = null;
            byte codeFlag = 0x00;
            List<Pair<byte[], byte[]>> storage = new ArrayList<>();
            if (!FastByteComparisons.equal(accountState.getCodeHash(), HashUtil.EMPTY_DATA_HASH)) {
                codeHash = accountState.getCodeHash();
            }
            if (!FastByteComparisons.equal(accountState.getStateRoot(), HashUtil.EMPTY_TRIE_HASH)) {
                storageRoot = accountState.getStateRoot();
            }
            if (codeHash != null) {
                ByteArrayWrapper codeHashWrapped = new ByteArrayWrapper(codeHash);
                if (codeHashes.contains(codeHashWrapped)) {
                    codeFlag = 0x02;
                } else {
                    codeFlag = 0x01;
                    code = repository.getCode(currentKey);
                    codeHashes.add(codeHashWrapped);
                    codeHash = null;
                }
            }

            if (storageRoot != null) {
                Set<byte[]> storageKeys = new LinkedHashSet<>();
                getAccountKeys(stateSource, storageKeys, storageRoot);
                for (byte[] storageKey : storageKeys) {
                    storage.add(Pair.of(storageKey,
                            repository.getStorageValue(currentKey, new DataWord(storageKey)).getNoLeadZeroesData()));
                }
            }

            byte[] storageEncoded = null;
            List<byte[]> storageEntries = new ArrayList<>();
            for (Pair<byte[], byte[]> storageEntry : storage) {
                byte[] storageRowRlp = RLP.encodeList(RLP.encodeElement(storageEntry.getLeft()),
                        RLP.encodeElement(RLP.encodeElement(storageEntry.getRight())));
                storageEntries.add(storageRowRlp);
            }
            storageEncoded = RLP.encodeRLPList(storageEntries);

            byte[] nonceRlp = accountState.getNonce().compareTo(BigInteger.ZERO) == 0 ?
                    RLP.encodeElement(new byte[0]) : RLP.encodeBigInteger(accountState.getNonce());
            byte[] balanceRlp = accountState.getBalance().compareTo(BigInteger.ZERO) == 0 ?
                    RLP.encodeElement(new byte[0]) : RLP.encodeBigInteger(accountState.getBalance());
            byte[] codeFlagRlp = RLP.encodeElement(codeFlag == 0x00 ? new byte[0] : new byte[] {codeFlag});
            byte[] codeRlp = code == null ? RLP.encodeElement(codeHash) : RLP.encodeElement(code);

            byte[] accountDataRlp = RLP.encodeList(nonceRlp, balanceRlp, codeFlagRlp, codeRlp, storageEncoded);
            byte[] accountRlp = RLP.encodeList(RLP.encodeElement(currentKey), accountDataRlp);

            if (!rlpChunk.add(accountRlp)) {
                writeStateChunk(rlpChunk.getData(), stateChunkHashes);
                rlpChunk = new RLPChunk(CHUNK_SIZE);
                rlpChunk.add(accountRlp);
            }
        }

        if (!rlpChunk.getData().isEmpty()) writeStateChunk(rlpChunk.getData(), stateChunkHashes);

        return stateChunkHashes;
    }


    private class RLPChunk {

        private long maxSize;
        private long currentSize;
        private int currentHeaderSize;
        private long currentMaxSize;
        private boolean canAdd = true;

        private static final int INIT_MAX_SIZE = 55; // maximum size of payload for 1-byte header
        private static final int BYTE_INC = 256; // multiplier of maximum payload length with each header byte added

        List<byte[]> data = new ArrayList<>();

        RLPChunk(long maxSize) {
            this.maxSize = maxSize;
            this.currentSize = 0;
            this.currentHeaderSize = 1;
            this.currentMaxSize = INIT_MAX_SIZE;
        }

        public boolean add(byte[] element) {
            return add(element, false);
        }

        public boolean addForce(byte[] element) {
            return add(element, true);
        }

        private boolean add(byte[] element, boolean force) {
            if (!canAdd && !force) return false;

            long estimatedSize = currentSize + element.length;
            while ((estimatedSize + currentHeaderSize) > currentMaxSize ) {
                currentHeaderSize++;
                currentMaxSize = currentMaxSize == INIT_MAX_SIZE ?  BYTE_INC : currentMaxSize * BYTE_INC;
            }

            // Second if handles elements larger than CHUNK_SIZE
            if ((estimatedSize + currentHeaderSize) <= maxSize || data.isEmpty() || force) {
                data.add(element);
                currentSize = estimatedSize;
                return true;
            } else {
                canAdd = false;
                return false;
            }
        }

        public List<byte[]> getData() {
            return data;
        }
    }

    /**
     * Writes state chunk to snapshotDS
     * @param chunkAccountStates    RLP account states in prepared list
     * @param stateChunkHashes      State chunk hashes array, will be updated with compressed chunk hash
     */
    private void writeStateChunk(List<byte[]> chunkAccountStates, List<byte[]> stateChunkHashes) {
        byte[] uncompressedStateChunk = RLP.encodeRLPList(chunkAccountStates);
        byte[] compressedStateChunk = null;
        try {
            compressedStateChunk = Snappy.compress(uncompressedStateChunk);
        } catch (IOException e) {
            logger.error("Fatal error during compression of state chunk, size {} bytes", uncompressedStateChunk.length);
            throw new RuntimeException("Fatal error during compression of state chunk");
        }
        byte[] stateChunkHash = HashUtil.sha3(compressedStateChunk);
        logger.info("Writing state chunk with {} accounts, hash {} (uncompressed size: {}, compressed size: {})",
                chunkAccountStates.size(), Hex.toHexString(stateChunkHash), uncompressedStateChunk.length,
                compressedStateChunk.length);
        stateChunkHashes.add(stateChunkHash);
        snapshotDS.put(stateChunkHash, compressedStateChunk);
    }

    private List<byte[]> createBlocksSnapshot(Block block) {

        Block endBlock = blockStore.getChainBlockByNumber(block.getNumber() - BLOCK_FREQUENCY);
        List<byte[]> blockChunkHashes = new ArrayList<>();
        long extraDataLength = RLP.encodeBigInteger(BigInteger.valueOf(endBlock.getNumber())).length +
                RLP.encodeElement(endBlock.getHash()).length +
                RLP.encodeBigInteger(blockStore.getTotalDifficultyForHash(endBlock.getHash())).length;
        RLPChunk rlpChunk = new RLPChunk(CHUNK_SIZE - extraDataLength);

        for (long i = block.getNumber(); i > endBlock.getNumber(); i--) {
            Block currentBlock = blockStore.getChainBlockByNumber(i);
            byte[] coinBaseRlp = RLP.encodeElement(currentBlock.getCoinbase());
            byte[] stateRootRlp = RLP.encodeElement(currentBlock.getStateRoot());
            byte[] logsBloomRlp = RLP.encodeElement(currentBlock.getLogBloom());
            byte[] difficultyRlp = RLP.encodeElement(currentBlock.getDifficulty());
            byte[] gasLimitRlp = RLP.encodeElement(currentBlock.getGasLimit());
            byte[] gasUsedRlp = RLP.encodeBigInteger(BigInteger.valueOf(currentBlock.getGasUsed()));
            byte[] timestampRlp = RLP.encodeBigInteger(BigInteger.valueOf(currentBlock.getTimestamp()));
            byte[] extraDataRlp = RLP.encodeElement(currentBlock.getExtraData());

            // transactions
            List<byte[]> transactionRlpList = new ArrayList<>();
            for (Transaction transaction : currentBlock.getTransactionsList()) {
                transactionRlpList.add(transaction.getEncoded());
            }
            byte[] transactionsRlp = RLP.encodeRLPList(transactionRlpList);

            // uncles
            List<byte[]> uncleRlpList = new ArrayList<>();
            for (BlockHeader uncleHeader : currentBlock.getUncleList()) {
                uncleRlpList.add(uncleHeader.getEncoded());
            }
            byte[] unclesRlp = RLP.encodeRLPList(uncleRlpList);

            byte[] mixHashRlp = RLP.encodeElement(currentBlock.getMixHash());
            byte[] nonceRlp = RLP.encodeElement(currentBlock.getNonce());

            byte[] abridgedBlockRlp = RLP.encodeList(
                    coinBaseRlp, stateRootRlp, logsBloomRlp, difficultyRlp, gasLimitRlp, gasUsedRlp, timestampRlp,
                    extraDataRlp, transactionsRlp, unclesRlp, mixHashRlp, nonceRlp
            );

            List<byte[]> receiptRlpList = new ArrayList<>();
            for (Transaction transaction :currentBlock.getTransactionsList()) {
                TransactionInfo transactionInfo = txStore.get(transaction.getHash(), currentBlock.getHash());
                receiptRlpList.add(transactionInfo.getReceipt().getEncoded(true));
            }
            byte[] receiptsRlp = RLP.encodeRLPList(receiptRlpList);

            byte[] currentBlockRlp = RLP.encodeList(abridgedBlockRlp, receiptsRlp);

            if (!rlpChunk.add(currentBlockRlp)) {
                rlpChunk.addForce(RLP.encodeBigInteger(blockStore.getTotalDifficultyForHash(currentBlock.getHash())));
                rlpChunk.addForce(RLP.encodeElement(currentBlock.getHash()));
                rlpChunk.addForce(RLP.encodeBigInteger(BigInteger.valueOf(currentBlock.getNumber())));
                List<byte[]> data = rlpChunk.getData();
                Collections.reverse(data);
                writeBlockChunk(data, blockChunkHashes);
                rlpChunk = new RLPChunk(CHUNK_SIZE - extraDataLength);
                rlpChunk.add(currentBlockRlp);
            }
        }

        if (!rlpChunk.getData().isEmpty()) {
            rlpChunk.addForce(RLP.encodeBigInteger(blockStore.getTotalDifficultyForHash(endBlock.getHash())));
            rlpChunk.addForce(RLP.encodeElement(endBlock.getHash()));
            rlpChunk.addForce(RLP.encodeBigInteger(BigInteger.valueOf(endBlock.getNumber())));
            List<byte[]> data = rlpChunk.getData();
            Collections.reverse(data);
            writeBlockChunk(data, blockChunkHashes);
        }

        return blockChunkHashes;
    }

    /**
     * Writes block chunk to snapshotDS
     * @param blocksData            Abridged blocks rlp with receipts and previous block data
     * @param blockChunkHashes      Block chunk hashes array, will be updated with compressed chunk hash
     */
    private void writeBlockChunk(List<byte[]> blocksData, List<byte[]> blockChunkHashes) {
        byte[] uncompressedBlocksChunk = RLP.encodeRLPList(blocksData);
        byte[] compressedBlocksChunk = null;
        try {
            compressedBlocksChunk = Snappy.compress(uncompressedBlocksChunk);
        } catch (IOException e) {
            logger.error("Fatal error during compression of blocks chunk, size {} bytes",
                    uncompressedBlocksChunk.length);
            throw new RuntimeException("Fatal error during compression of blocks chunk");
        }
        byte[] blocksChunkHash = HashUtil.sha3(compressedBlocksChunk);
        logger.info("Writing blocks chunk with {} blocks, hash {} (uncompressed size: {}, compressed size: {})",
                blocksData.size() - 3, Hex.toHexString(blocksChunkHash), uncompressedBlocksChunk.length,
                compressedBlocksChunk.length);
        blockChunkHashes.add(blocksChunkHash);
        snapshotDS.put(blocksChunkHash, compressedBlocksChunk);
    }

    public SnapshotManifest getManifest() {
        SnapshotManifest manifest = null;
        byte[] manifestBytes = snapshotDS.get(MANIFEST_KEY);
        if (manifestBytes != null) manifest = new SnapshotManifest(manifestBytes);

        return manifest;
    }

    public byte[] getChunk(byte[] chunkHash) {
        return snapshotDS.get(chunkHash);
    }
}
