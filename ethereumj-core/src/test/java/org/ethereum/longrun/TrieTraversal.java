package org.ethereum.longrun;

import org.ethereum.core.AccountState;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.NodeKeyCompositor;
import org.ethereum.datasource.Source;
import org.ethereum.datasource.SourceCodec;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.TrieImpl;
import org.ethereum.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Runtime.getRuntime;

/**
 * @author Mikhail Kalinin
 * @since 16.01.2018
 */
public abstract class TrieTraversal  {

    protected static final Logger logger = LoggerFactory.getLogger("TestLogger");

    final byte[] root;
    final Source<byte[], byte[]> src;
    final TraversalStats stats;
    final AtomicInteger nodesCount = new AtomicInteger(0);

    protected TrieTraversal(final Source<byte[], byte[]> src, final byte[] root, final TraversalStats stats) {
        this.src = src;
        this.root = root;
        this.stats = stats;
    }

    private static class TraversalStats {
        long startedAt = System.currentTimeMillis();
        long updatedAt = System.currentTimeMillis();
        int checkpoint = 0;
        int passed = 0;
    }

    public static TrieTraversal ofState(final Source<byte[], byte[]> src, final byte[] root, boolean includeAccounts) {
        return new StateTraversal(src, root, includeAccounts);
    }

    public static TrieTraversal ofStorage(final Source<byte[], byte[]> src, final byte[] stateRoot, final byte[] address) {

        TrieImpl stateTrie = new SecureTrie(src, stateRoot);
        byte[] encoded = stateTrie.get(address);
        if (encoded == null) {
            logger.error("Account {} does not exist", Hex.toHexString(address));
            throw new RuntimeException("Account does not exist");
        }

        AccountState state = new AccountState(encoded);
        if (FastByteComparisons.equal(state.getStateRoot(), HashUtil.EMPTY_TRIE_HASH)) {
            logger.error("Storage of account {} is empty", Hex.toHexString(address));
            throw new RuntimeException("Account storage is empty");
        }

        final NodeKeyCompositor nodeKeyCompositor = new NodeKeyCompositor(address);
        return new StorageTraversal(
                new SourceCodec.KeyOnly<>(src, nodeKeyCompositor),
                state.getStateRoot(),
                new TraversalStats(),
                address,
                true
        );
    }

    public int go() {

        onStartImpl();
        stats.startedAt = System.currentTimeMillis();

        SecureTrie trie = new SecureTrie(src, root);
        trie.scanTree(new TrieImpl.ScanAction() {
            @Override
            public void doOnNode(byte[] hash, TrieImpl.Node node) {
                nodesCount.incrementAndGet();
                ++stats.passed;
                onNodeImpl(hash, node);
            }

            @Override
            public void doOnValue(byte[] nodeHash, TrieImpl.Node node, byte[] key, byte[] value) {
                if (nodeHash == null) { // other value nodes are counted in doOnNode call
                    nodesCount.incrementAndGet();
                    ++stats.passed;
                }
                onValueImpl(nodeHash, node, key, value);
            }
        });

        onEndImpl();

        return nodesCount.get();
    }

    protected abstract void onNodeImpl(byte[] hash, TrieImpl.Node node);
    protected abstract void onValueImpl(byte[] nodeHash, TrieImpl.Node node, byte[] key, byte[] value);
    protected abstract void onStartImpl();
    protected abstract void onEndImpl();

    static class StateTraversal extends TrieTraversal {

        final boolean includeAccounts;

        private Thread statsLogger;

        private static final int MAX_THREADS = 20;
        private BlockingQueue<Runnable> traversalQueue;
        private ThreadPoolExecutor traversalExecutor;
        private final Object mutex = new Object();

        private void setupAsyncGroup() {
            traversalQueue = new LinkedBlockingQueue<>();
            traversalExecutor = new ThreadPoolExecutor(MAX_THREADS, MAX_THREADS,
                    0L, TimeUnit.MILLISECONDS,
                    traversalQueue);
        }

        private void shutdownAsyncGroup() {
            traversalQueue.clear();
            traversalExecutor.shutdownNow();
            try {
                traversalExecutor.awaitTermination(60, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                logger.error("Validating nodes: traversal has been interrupted", e);
                throw new RuntimeException("Traversal has been interrupted", e);
            }
        }

        private void waitForAsyncJobs() {
            try {
                synchronized (mutex) {
                    while (traversalExecutor.getActiveCount() > 0 || traversalQueue.size() > 0) {
                        logger.info("Validating nodes: waiting for incomplete jobs: running {}, pending {}",
                                traversalExecutor.getActiveCount(), traversalQueue.size());
                        mutex.wait();
                    }
                }
            } catch (InterruptedException e) {
                logger.error("Validating nodes: traversal has been interrupted", e);
                throw new RuntimeException("Traversal has been interrupted", e);
            }
        }

        private StateTraversal(final Source<byte[], byte[]> src, final byte[] root, boolean includeAccounts) {
            super(src, root, new TraversalStats());
            this.includeAccounts = includeAccounts;
        }

        @Override
        protected void onNodeImpl(byte[] hash, TrieImpl.Node node) {
        }

        @Override
        protected void onValueImpl(byte[] nodeHash, TrieImpl.Node node, byte[] key, byte[] value) {
            if (includeAccounts) {
                final AccountState accountState = new AccountState(value);
                if (!FastByteComparisons.equal(accountState.getCodeHash(), HashUtil.EMPTY_DATA_HASH)) {
                    nodesCount.incrementAndGet();
                    ++stats.passed;
                    assert (null != src.get(NodeKeyCompositor.compose(accountState.getCodeHash(), key)));
                }
                if (!FastByteComparisons.equal(accountState.getStateRoot(), HashUtil.EMPTY_TRIE_HASH)) {
                    logger.trace("Validating nodes: new storage discovered {}", HashUtil.shortHash(key));
                    final NodeKeyCompositor nodeKeyCompositor = new NodeKeyCompositor(key);
                    final StorageTraversal storage = new StorageTraversal(new SourceCodec.KeyOnly<>(src, nodeKeyCompositor),
                            accountState.getStateRoot(), stats, key);

                    synchronized (mutex) {
                        try {
                            while (traversalExecutor.getActiveCount() > maxThreads()) mutex.wait();
                        } catch (InterruptedException e) {
                            logger.error("Validating nodes: traversal has been interrupted", e);
                            throw new RuntimeException("Traversal has been interrupted", e);
                        }
                        traversalExecutor.submit(() -> {
                            nodesCount.addAndGet(storage.go());
                            synchronized (mutex) {
                                mutex.notifyAll();
                            }
                        });
                    }
                }
            }
        }

        private int maxThreads() {
            double freeMemRatio = freeMemRatio();
            if (freeMemRatio < 0.1) {
                return MAX_THREADS / 8;
            } else if (freeMemRatio < 0.2) {
                return MAX_THREADS / 4;
            } else {
                return MAX_THREADS;
            }
        }

        private double freeMemRatio() {

            long free = getRuntime().freeMemory();
            long total = getRuntime().totalMemory();
            long max = getRuntime().maxMemory();

            if ((double) total / max < 0.9) {
                return 1;
            } else {
                return (double) free / total;
            }
        }

        @Override
        protected void onStartImpl() {
            runStatsLogger();
            setupAsyncGroup();
        }

        private void runStatsLogger() {
            statsLogger = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(30000);
                        long cur = System.currentTimeMillis();
                        logger.info("Validating nodes: running for " + ((cur - stats.startedAt) / 1000) + " sec, " + stats.passed + " passed, " +
                                String.format("%.2f nodes/sec", (double) (stats.passed - stats.checkpoint) / (cur - stats.updatedAt) * 1000) +
                                ", storage threads " + (traversalExecutor != null ? traversalExecutor.getActiveCount() : 0) + "/" + maxThreads());
                        stats.checkpoint = stats.passed;
                        stats.updatedAt = cur;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            statsLogger.start();
        }

        @Override
        protected void onEndImpl() {
            waitForAsyncJobs();
            shutdownAsyncGroup();
            statsLogger.interrupt();

            logger.info("Validating nodes: traversal completed in " + ((System.currentTimeMillis() - stats.startedAt) / 1000) + " sec, "
                    + stats.passed + " nodes passed");
        }
    }

    static class StorageTraversal extends TrieTraversal {

        final byte[] stateAddressOrHash;
        boolean logStats = false;

        private StorageTraversal(final Source<byte[], byte[]> src, final byte[] root,
                                 TraversalStats stats, final byte[] stateAddressOrHash) {
            super(src, root, stats);
            this.stateAddressOrHash = stateAddressOrHash;
        }

        private StorageTraversal(final Source<byte[], byte[]> src, final byte[] root,
                                 TraversalStats stats, final byte[] stateAddressOrHash, boolean logStats) {
            super(src, root, stats);
            this.stateAddressOrHash = stateAddressOrHash;
            this.logStats = logStats;
        }

        @Override
        protected void onNodeImpl(byte[] hash, TrieImpl.Node node) {
            logStatsImpl();
        }

        @Override
        protected void onValueImpl(byte[] nodeHash, TrieImpl.Node node, byte[] key, byte[] value) {
            logStatsImpl();
        }

        private void logStatsImpl() {
            if (!logStats) return;

            if (stats.passed % 10000 == 0 && stats.passed > stats.checkpoint) {
                long cur = System.currentTimeMillis();
                logger.info("Validating storage " + HashUtil.shortHash(stateAddressOrHash) +
                        ": running for " + ((cur - stats.startedAt) / 1000) + " sec, " + stats.passed + " passed, " +
                        String.format("%.2f nodes/sec", (double) (stats.passed - stats.checkpoint) / (cur - stats.updatedAt) * 1000));
                stats.checkpoint = stats.passed;
                stats.updatedAt = cur;
            }
        }

        @Override
        protected void onStartImpl() {
            logger.trace("Validating nodes: start traversing {} storage",
                    stateAddressOrHash != null ? HashUtil.shortHash(stateAddressOrHash) : "unknown");
        }

        @Override
        protected void onEndImpl() {
            logger.trace("Validating nodes: end traversing {} storage",
                    stateAddressOrHash != null ? HashUtil.shortHash(stateAddressOrHash) : "unknown");
            if (logStats)
                logger.info("Validating storage " + HashUtil.shortHash(stateAddressOrHash) +
                        ": completed in " + ((System.currentTimeMillis() - stats.startedAt) / 1000) + " sec, " + stats.passed + " nodes passed");
        }
    }
}
