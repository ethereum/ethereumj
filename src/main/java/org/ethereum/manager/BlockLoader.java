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


import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.db.DbFlushManager;
import org.ethereum.util.*;
import org.ethereum.validator.BlockHeaderValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

@Component
public class BlockLoader {
    private static final Logger logger = LoggerFactory.getLogger("blockqueue");

    @Autowired
    private BlockHeaderValidator headerValidator;

    @Autowired
    SystemProperties config;

    @Autowired
    private BlockchainImpl blockchain;

    @Autowired
    DbFlushManager dbFlushManager;

    Scanner scanner = null;

    DateFormat df = new SimpleDateFormat("HH:mm:ss.SSSS");

    private void blockWork(Block block) {
        if (block.getNumber() >= blockchain.getBlockStore().getBestBlock().getNumber() || blockchain.getBlockStore().getBlockByHash(block.getHash()) == null) {

            if (block.getNumber() > 0 && !isValid(block.getHeader())) {
                throw new RuntimeException();
            }

            long s = System.currentTimeMillis();
            ImportResult result = blockchain.tryToConnect(block);

            if (block.getNumber() % 10 == 0) {
                System.out.println(df.format(new Date()) + " Imported block " + block.getShortDescr() + ": " + result + " (prework: "
                        + exec1.getQueue().size() + ", work: " + exec2.getQueue().size() + ", blocks: " + exec1.getOrderMap().size() + ") in " +
                        (System.currentTimeMillis() - s) + " ms");
            }

        } else {

            if (block.getNumber() % 10000 == 0)
                System.out.println("Skipping block #" + block.getNumber());
        }
    }

    ExecutorPipeline<Block, Block> exec1;
    ExecutorPipeline<Block, ?> exec2;

    public void loadBlocks() {
        exec1 = new ExecutorPipeline(8, 1000, true, new Functional.Function<Block, Block>() {
            @Override
            public Block apply(Block b) {
                if (b.getNumber() >= blockchain.getBlockStore().getBestBlock().getNumber()) {
                    for (Transaction tx : b.getTransactionsList()) {
                        tx.getSender();
                    }
                }
                return b;
            }
        }, new Functional.Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                logger.error("Unhandled exception: ", throwable);
            }
        });

        exec2 = exec1.add(1, 1000, new Functional.Consumer<Block>() {
            @Override
            public void accept(Block block) {
                try {
                    blockWork(block);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        String fileSrc = config.blocksLoader();
        try {
            final String blocksFormat = config.getConfig().hasPath("blocks.format") ? config.getConfig().getString("blocks.format") : null;
            System.out.println("Loading blocks: " + fileSrc + ", format: " + blocksFormat);

            if ("rlp".equalsIgnoreCase(blocksFormat)) {     // rlp encoded bytes
                Path path = Paths.get(fileSrc);
                // NOT OPTIMAL, but fine for tests
                byte[] data = Files.readAllBytes(path);
                RLPList list = RLP.decode2(data);
                for (RLPElement item : list) {
                    Block block = new Block(item.getRLPData());
                    exec1.push(block);
                }
            } else {                                        // hex string
                FileInputStream inputStream = new FileInputStream(fileSrc);
                scanner = new Scanner(inputStream, "UTF-8");

                while (scanner.hasNextLine()) {

                    byte[] blockRLPBytes = Hex.decode(scanner.nextLine());
                    Block block = new Block(blockRLPBytes);

                    exec1.push(block);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }


        try {
            exec1.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        dbFlushManager.flushSync();

        System.out.println(" * Done * ");
        System.exit(0);
    }

    private boolean isValid(BlockHeader header) {
        return headerValidator.validateAndLog(header, logger);
    }
}
