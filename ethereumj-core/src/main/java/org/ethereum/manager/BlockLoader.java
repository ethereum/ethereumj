package org.ethereum.manager;


import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.util.ExecutorPipeline;
import org.ethereum.util.Functional;
import org.ethereum.validator.BlockHeaderValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
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

    Scanner scanner = null;

    DateFormat df = new SimpleDateFormat("HH:mm:ss.SSSS");

    private void blockWork(Block block) {
        if (block.getNumber() >= blockchain.getBestBlock().getNumber() || blockchain.getBlockByHash(block.getHash()) == null) {

            if (block.getNumber() > 0 && !isValid(block.getHeader())) {
                throw new RuntimeException();
            }

            ImportResult result = blockchain.tryToConnect(block);
            System.out.println(df.format(new Date()) + " Imported block " + block.getShortDescr() + ": " + result + " (prework: "
                    + exec1.getQueue().size() + ", work: " + exec2.getQueue().size() + ", blocks: " + exec1.getOrderMap().size() + ")");

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
                for (Transaction tx : b.getTransactionsList()) {
                    tx.getSender();
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
            FileInputStream inputStream = null;
            inputStream = new FileInputStream(fileSrc);
            scanner = new Scanner(inputStream, "UTF-8");

            System.out.println("Loading blocks: " + fileSrc);

            while (scanner.hasNextLine()) {

                byte[] blockRLPBytes = Hex.decode(scanner.nextLine());
                Block block = new Block(blockRLPBytes);

                exec1.push(block);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            exec1.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        blockchain.flush();

        System.out.println(" * Done * ");
    }

    private boolean isValid(BlockHeader header) {

        if (!headerValidator.validate(header)) {

            if (logger.isErrorEnabled())
                headerValidator.logErrors(logger);

            return false;
        }

        return true;
    }
}
