package org.ethereum.manager;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.BlockSummary;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.SnapshotManifest;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.DbSource;
import org.ethereum.datasource.Serializers;
import org.ethereum.datasource.Source;
import org.ethereum.datasource.SourceCodec;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.RepositoryHashedKeysTrie;
import org.ethereum.db.StateSource;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creation and resolving of Snapshots
 * https://github.com/ethcore/parity/wiki/Warp-Sync-Snapshot-Format
 */
@Component
@Lazy
public class SnapshotManager {

    private static final Logger logger = LoggerFactory.getLogger("snapshot");


    private SystemProperties config;

    private CompositeEthereumListener listener;

    @Autowired @Qualifier("snapshotDS")
    DbSource<byte[]> snapshotDS;

    @Autowired @Qualifier("stateDS")
    DbSource<byte[]> stateDS;

    @Autowired
    StateSource stateSource;

    @Autowired
    private RepositoryHashedKeysTrie repository;

    private boolean enabled = false;

    private boolean syncDone = false;

    private static final long BLOCK_FREQUENCY = 30_000;

    private static final long CHUNK_SIZE = 4 * 1024 * 1024;

    public static final byte[] MANIFEST_KEY = HashUtil.sha3("SNAPSHOT_MANIFEST".getBytes());

    @Autowired
    public SnapshotManager(SystemProperties config, CompositeEthereumListener listener, final BlockchainImpl blockchain) {
        this.config = config;
        this.listener = listener;
        if (config.getWarpSnapshotCreation()) enabled = true;
        if (enabled) init();
        // TODO: remove, test only
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        createSnapshot(blockchain.getBestBlock());
                    }
                },
                5000
        );
    }

    private void init() {
        listener.addListener(new EthereumListenerAdapter() {
            @Override
            public void onSyncDone(SyncState state) {
                if (state == SyncState.COMPLETE) {
                    syncDone = true;
                }
            }

            @Override
            public void onBlock(BlockSummary blockSummary) {
                if (syncDone && blockSummary.getBlock().getNumber() % BLOCK_FREQUENCY == 0) {
                    // TODO: Stop sync
                    // TODO: Flush everything
                    // Run snapshot service
                    createSnapshot(blockSummary.getBlock());
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

    Set<ByteArrayWrapper> codeHashes = new HashSet<>();

    private void createSnapshot(Block block) {

        byte[] stateRoot = block.getStateRoot();

        Set<byte[]> keys = new LinkedHashSet<>();
        repository.syncToRoot(stateRoot);
        getAccountKeys(stateSource, keys, stateRoot);
        List<byte[]> chunkAccountStates = new ArrayList<>();
        long currentSize = 0;
        int currentHeaderSize = 1;
        int initMaxSize = 55;
        long currentMaxSize = initMaxSize;
        int byteIncrement = 256;
        List<byte[]> stateHashes = new ArrayList<>();
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
                    storage.add(Pair.of(storageKey, repository.getStorageValue(currentKey, new DataWord(storageKey)).getNoLeadZeroesData()));
                }
            }

            byte[] storageEncoded = null;
            List<byte[]> storageEntries = new ArrayList<>();
            for (Pair<byte[], byte[]> storageEntry : storage) {
                byte[] storageRowRlp = RLP.encodeList(RLP.encodeElement(storageEntry.getLeft()), RLP.encodeElement(RLP.encodeElement(storageEntry.getRight())));
                storageEntries.add(storageRowRlp);
            }
            storageEncoded = RLP.encodeRLPList(storageEntries);

            byte[] nonceRlp = accountState.getNonce().compareTo(BigInteger.ZERO) == 0 ? RLP.encodeElement(new byte[0]) : RLP.encodeBigInteger(accountState.getNonce());
            byte[] balanceRlp = accountState.getBalance().compareTo(BigInteger.ZERO) == 0 ? RLP.encodeElement(new byte[0]) : RLP.encodeBigInteger(accountState.getBalance());
            byte[] codeFlagRlp = RLP.encodeElement(codeFlag == 0x00 ? new byte[0] : new byte[] {codeFlag});
            byte[] codeRlp = code == null ? RLP.encodeElement(codeHash) : RLP.encodeElement(code);

            byte[] accountDataRlp = RLP.encodeList(nonceRlp, balanceRlp, codeFlagRlp, codeRlp, storageEncoded);
            byte[] accountRlp = RLP.encodeList(RLP.encodeElement(currentKey), accountDataRlp);

            long estimatedSize = currentSize + accountRlp.length;
            while ((estimatedSize + currentHeaderSize) > currentMaxSize ) {
                currentHeaderSize++;
                currentMaxSize = currentMaxSize == initMaxSize ?  byteIncrement : currentMaxSize * byteIncrement;
            }

            // Second if to handle account states larger than CHUNK_SIZE
            if ((estimatedSize + currentHeaderSize) <= CHUNK_SIZE || chunkAccountStates.isEmpty()) {
                chunkAccountStates.add(accountRlp);
                currentSize = estimatedSize;
            } else {
                writeChunk(chunkAccountStates, stateHashes);
                currentMaxSize = initMaxSize;
                currentHeaderSize = 1;

                chunkAccountStates.add(accountRlp);
                currentSize = accountRlp.length;
                // TODO: refactor me to use once
                while ((estimatedSize + currentHeaderSize) > currentMaxSize ) {
                    currentHeaderSize++;
                    currentMaxSize = currentMaxSize == initMaxSize ?  byteIncrement : currentMaxSize * byteIncrement;
                }
            }
        }

        if (! chunkAccountStates.isEmpty()) writeChunk(chunkAccountStates, stateHashes);

        SnapshotManifest manifest = new SnapshotManifest(block.getNumber(), block.getHash());
        manifest.setStateRoot(stateRoot);
        manifest.setStateHashes(stateHashes);
        snapshotDS.put(MANIFEST_KEY, manifest.getEncoded());
        logger.info("Manifest written: {}", manifest);
    }

    /**
     * Writes chunk to snapshotDS
     * @param chunkAccountStates    RLP account states in prepared list
     * @param stateHashes           State hashes array, will be updated with compressed chunk hash
     */
    private void writeChunk(List<byte[]> chunkAccountStates, List<byte[]> stateHashes) {
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
        chunkAccountStates.clear();
        stateHashes.add(stateChunkHash);
        snapshotDS.put(stateChunkHash, compressedStateChunk);
    }
}
