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
package org.ethereum.mine;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.util.ByteUtil.longToBytes;
import static org.ethereum.mine.MinerIfc.MiningResult;

/**
 * More high level validator/miner class which keeps a cache for the last requested block epoch
 *
 * Created by Anton Nashatyrev on 04.12.2015.
 */
public class Ethash {
    private static final Logger logger = LoggerFactory.getLogger("mine");
    private static EthashParams ethashParams = new EthashParams();

    private static Ethash cachedInstance = null;
    private static long cachedBlockEpoch = 0;
    //    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static ListeningExecutorService executor = MoreExecutors.listeningDecorator(
            new ThreadPoolExecutor(8, 8, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("ethash-pool-%d").build()));

    public static boolean fileCacheEnabled = true;

    /**
     * Returns instance for the specified block number either from cache or calculates a new one
     */
    public static Ethash getForBlock(SystemProperties config, long blockNumber) {
        long epoch = blockNumber / ethashParams.getEPOCH_LENGTH();
        if (cachedInstance == null || epoch != cachedBlockEpoch) {
            cachedInstance = new Ethash(config, epoch * ethashParams.getEPOCH_LENGTH());
            cachedBlockEpoch = epoch;
        }
        return cachedInstance;
    }

    private EthashAlgo ethashAlgo = new EthashAlgo(ethashParams);

    private long blockNumber;
    private int[] cacheLight = null;
    private int[] fullData = null;
    private SystemProperties config;
    private long startNonce = -1;

    public Ethash(SystemProperties config, long blockNumber) {
        this.config = config;
        this.blockNumber = blockNumber;
        if (config.getConfig().hasPath("mine.startNonce")) {
            startNonce = config.getConfig().getLong("mine.startNonce");
        }
    }

    public synchronized int[] getCacheLight() {
        if (cacheLight == null) {
            File file = new File(config.ethashDir(), "mine-dag-light.dat");
            if (fileCacheEnabled && file.canRead()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                    logger.info("Loading light dataset from " + file.getAbsolutePath());
                    long bNum = ois.readLong();
                    if (bNum == blockNumber) {
                        cacheLight = (int[]) ois.readObject();
                        logger.info("Dataset loaded.");
                    } else {
                        logger.info("Dataset block number miss: " + bNum + " != " + blockNumber);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            if (cacheLight == null) {
                logger.info("Calculating light dataset...");
                cacheLight = getEthashAlgo().makeCache(getEthashAlgo().getParams().getCacheSize(blockNumber),
                        getEthashAlgo().getSeedHash(blockNumber));
                logger.info("Light dataset calculated.");

                if (fileCacheEnabled) {
                    file.getParentFile().mkdirs();
                    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))){
                        logger.info("Writing light dataset to " + file.getAbsolutePath());
                        oos.writeLong(blockNumber);
                        oos.writeObject(cacheLight);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return cacheLight;
    }

    public synchronized int[] getFullDataset() {
        if (fullData == null) {
            File file = new File(config.ethashDir(), "mine-dag.dat");
            if (fileCacheEnabled && file.canRead()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                    logger.info("Loading dataset from " + file.getAbsolutePath());
                    long bNum = ois.readLong();
                    if (bNum == blockNumber) {
                        fullData = (int[]) ois.readObject();
                        logger.info("Dataset loaded.");
                    } else {
                        logger.info("Dataset block number miss: " + bNum + " != " + blockNumber);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            if (fullData == null){

                logger.info("Calculating full dataset...");
                fullData = getEthashAlgo().calcDataset(getFullSize(), getCacheLight());
                logger.info("Full dataset calculated.");

                if (fileCacheEnabled) {
                    file.getParentFile().mkdirs();
                    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                        logger.info("Writing dataset to " + file.getAbsolutePath());
                        oos.writeLong(blockNumber);
                        oos.writeObject(fullData);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return fullData;
    }

    private long getFullSize() {
        return getEthashAlgo().getParams().getFullSize(blockNumber);
    }

    private EthashAlgo getEthashAlgo() {
        return ethashAlgo;
    }

    /**
     *  See {@link EthashAlgo#hashimotoLight}
     */
    public Pair<byte[], byte[]> hashimotoLight(BlockHeader header, long nonce) {
        return hashimotoLight(header, longToBytes(nonce));
    }

    private  Pair<byte[], byte[]> hashimotoLight(BlockHeader header, byte[] nonce) {
        return getEthashAlgo().hashimotoLight(getFullSize(), getCacheLight(),
                sha3(header.getEncodedWithoutNonce()), nonce);
    }

    /**
     *  See {@link EthashAlgo#hashimotoFull}
     */
    public Pair<byte[], byte[]> hashimotoFull(BlockHeader header, long nonce) {
        return getEthashAlgo().hashimotoFull(getFullSize(), getFullDataset(), sha3(header.getEncodedWithoutNonce()),
                longToBytes(nonce));
    }

    public ListenableFuture<MiningResult> mine(final Block block) {
        return mine(block, 1);
    }

    /**
     *  Mines the nonce for the specified Block with difficulty BlockHeader.getDifficulty()
     *  When mined the Block 'nonce' and 'mixHash' fields are updated
     *  Uses the full dataset i.e. it faster but takes > 1Gb of memory and may
     *  take up to 10 mins for starting up (depending on whether the dataset was cached)
     *
     *  @param block The block to mine. The difficulty is taken from the block header
     *               This block is updated when mined
     *  @param nThreads CPU threads to mine on
     *  @return the task which may be cancelled. On success returns nonce
     */
    public ListenableFuture<MiningResult> mine(final Block block, int nThreads) {
        return new MineTask(block, nThreads,  new Callable<MiningResult>() {
            AtomicLong taskStartNonce = new AtomicLong(startNonce >= 0 ? startNonce : new Random().nextLong());
            @Override
            public MiningResult call() throws Exception {
                long threadStartNonce = taskStartNonce.getAndAdd(0x100000000L);
                long nonce = getEthashAlgo().mine(getFullSize(), getFullDataset(),
                        sha3(block.getHeader().getEncodedWithoutNonce()),
                        ByteUtil.byteArrayToLong(block.getHeader().getDifficulty()), threadStartNonce);
                final Pair<byte[], byte[]> pair = hashimotoLight(block.getHeader(), nonce);
                return new MiningResult(nonce, pair.getLeft(), block);
            }
        }).submit();
    }

    public ListenableFuture<MiningResult> mineLight(final Block block) {
        return mineLight(block, 1);
    }

    /**
     *  Mines the nonce for the specified Block with difficulty BlockHeader.getDifficulty()
     *  When mined the Block 'nonce' and 'mixHash' fields are updated
     *  Uses the light cache i.e. it slower but takes only ~16Mb of memory and takes less
     *  time to start up
     *
     *  @param block The block to mine. The difficulty is taken from the block header
     *               This block is updated when mined
     *  @param nThreads CPU threads to mine on
     *  @return the task which may be cancelled. On success returns nonce
     */
    public ListenableFuture<MiningResult> mineLight(final Block block, int nThreads) {
        return new MineTask(block, nThreads,  new Callable<MiningResult>() {
            AtomicLong taskStartNonce = new AtomicLong(startNonce >= 0 ? startNonce : new Random().nextLong());
            @Override
            public MiningResult call() throws Exception {
                long threadStartNonce = taskStartNonce.getAndAdd(0x100000000L);
                final long nonce = getEthashAlgo().mineLight(getFullSize(), getCacheLight(),
                        sha3(block.getHeader().getEncodedWithoutNonce()),
                        ByteUtil.byteArrayToLong(block.getHeader().getDifficulty()), threadStartNonce);
                final Pair<byte[], byte[]> pair = hashimotoLight(block.getHeader(), nonce);
                return new MiningResult(nonce, pair.getLeft(), block);
            }
        }).submit();
    }

    /**
     *  Validates the BlockHeader against its getDifficulty() and getNonce()
     */
    public boolean validate(BlockHeader header) {
        byte[] boundary = header.getPowBoundary();
        byte[] hash = hashimotoLight(header, header.getNonce()).getRight();

        return FastByteComparisons.compareTo(hash, 0, 32, boundary, 0, 32) < 0;
    }

    class MineTask extends AnyFuture<MiningResult> {
        Block block;
        int nThreads;
        Callable<MiningResult> miner;

        public MineTask(Block block, int nThreads, Callable<MiningResult> miner) {
            this.block = block;
            this.nThreads = nThreads;
            this.miner = miner;
        }

        public MineTask submit() {
            for (int i = 0; i < nThreads; i++) {
                ListenableFuture<MiningResult> f = executor.submit(miner);
                add(f);
            }
            return this;
        }

        @Override
        protected void postProcess(MiningResult result) {
            Pair<byte[], byte[]> pair = hashimotoLight(block.getHeader(), result.nonce);
            block.setNonce(longToBytes(result.nonce));
            block.setMixHash(pair.getLeft());
        }
    }
}
