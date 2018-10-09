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
package org.ethereum.manager;


import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Blockchain;
import org.ethereum.core.ImportResult;
import org.ethereum.core.Transaction;
import org.ethereum.db.DbFlushManager;
import org.ethereum.util.ExecutorPipeline;
import org.ethereum.validator.BlockHeaderValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

@Component
public class BlockLoader {

    public interface DumpWalker extends Iterable<byte[]>, Closeable {
        @Override
        default void close() throws IOException {
        }
    }

    private final static Logger logger = LoggerFactory.getLogger("blockqueue");
    private final static DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm:ss.SSSS");

    private final BlockHeaderValidator headerValidator;
    private final Blockchain blockchain;
    private final DbFlushManager dbFlushManager;

    private ExecutorPipeline<Block, Block> exec1;
    private ExecutorPipeline<Block, ?> exec2;

    @Autowired
    public BlockLoader(BlockHeaderValidator headerValidator, Blockchain blockchain, DbFlushManager dbFlushManager) {
        this.headerValidator = headerValidator;
        this.blockchain = blockchain;
        this.dbFlushManager = dbFlushManager;
    }

    private void initPipelines() {
        exec1 = new ExecutorPipeline(8, 1000, true, (Function<Block, Block>) b -> {
            if (b.getNumber() >= blockchain.getBestBlock().getNumber()) {
                for (Transaction tx : b.getTransactionsList()) {
                    tx.getSender();
                }
            }
            return b;
        }, throwable -> logger.error("Unhandled exception: ", throwable));

        exec2 = exec1.add(1, 1000, block -> {
            try {
                blockWork(block);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void blockWork(Block block) {
        if (block.getNumber() >= blockchain.getBestBlock().getNumber() || blockchain.getBlockByHash(block.getHash()) == null) {

            if (block.getNumber() > 0 && !isValid(block.getHeader())) {
                throw new RuntimeException();
            }

            long start = System.currentTimeMillis();
            ImportResult result = blockchain.tryToConnect(block);

            if (block.getNumber() % 10 == 0) {
                LocalDateTime finish = LocalDateTime.now();

                System.out.printf("%s Imported block %s: %s (prework: %d, work: %d, blocks: %d) in %d ms.\n",
                        finish.format(df),
                        block.getShortDescr(),
                        result,
                        exec1.getQueue().size(),
                        exec2.getQueue().size(),
                        exec1.getOrderMap().size(),
                        System.currentTimeMillis() - start);
            }

        } else if (block.getNumber() % 10000 == 0) {
            System.out.println("Skipping block #" + block.getNumber());
        }
    }

    /**
     * Tries import blocks from specified dumps.
     *
     * @param walkerFactory {@link DumpWalker} factory, which should instantiate new walker per each dump;
     * @param paths         list of dumps to import;
     * @return <code>true</code> if all blocks within all dumps have been successfully imported, <code>false</code> otherwise.
     */
    public boolean loadBlocks(Function<Path, DumpWalker> walkerFactory, Path... paths) {
        if (ArrayUtils.isEmpty(paths)) {
            logger.warn("There is nothing to import.");
            return false;
        }

        initPipelines();

        AtomicLong maxBlockNumber = new AtomicLong();
        boolean allBlocksImported;
        try {

            for (Path dump : paths) {
                try (DumpWalker walker = walkerFactory.apply(dump)) {
                    walker.forEach(rlp -> {
                        Block block = new Block(rlp);
                        if (maxBlockNumber.get() < block.getNumber()) {
                            maxBlockNumber.set(block.getNumber());
                        }
                        exec1.push(block);
                    });
                }
            }

            exec1.join();
            dbFlushManager.flushSync();

            allBlocksImported = maxBlockNumber.get() == blockchain.getBestBlock().getNumber();
        } catch (Exception e) {
            e.printStackTrace();
            allBlocksImported = false;
        }

        if (allBlocksImported) {
            System.out.printf("All of %s blocks was successfully loaded.\n", maxBlockNumber);
        } else {
            System.out.printf("Some blocks have been lost during the loading.");
        }

        return allBlocksImported;
    }

    /**
     * Tries import blocks from specified dumps with default {@link DumpWalker}.
     *
     * @param paths list of dumps to import;
     * @return <code>true</code> if all blocks within all dumps have been successfully imported, <code>false</code> otherwise.
     */
    public boolean loadBlocks(Path... paths) {
        return loadBlocks(HexLineDumpWalker::new, paths);
    }

    private boolean isValid(BlockHeader header) {
        return headerValidator.validateAndLog(header, logger);
    }

    private class HexLineDumpWalker implements DumpWalker {

        private final Scanner scanner;

        public HexLineDumpWalker(Path path) {
            try {
                System.out.println("Loading hex encoded blocks dump from: " + path);
                this.scanner = new Scanner(Files.newInputStream(path), "UTF-8");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() throws IOException {
            this.scanner.close();
        }

        @Override
        public Iterator<byte[]> iterator() {
            return new Iterator<byte[]>() {
                @Override
                public boolean hasNext() {
                    return scanner.hasNextLine();
                }

                @Override
                public byte[] next() {
                    return Hex.decode(scanner.nextLine());
                }
            };
        }
    }
}
