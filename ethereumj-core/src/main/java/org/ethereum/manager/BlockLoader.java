package org.ethereum.manager;


import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
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

    public static class LimitedQueue<E> extends LinkedBlockingQueue<E>
    {
        public LimitedQueue(int maxSize)
        {
            super(maxSize);
        }

        @Override
        public boolean offer(E e)
        {
            // turn offer() and add() into a blocking calls (unless interrupted)
            try {
                put(e);
                return true;
            } catch(InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            return false;
        }

    }

    @Autowired
    private BlockHeaderValidator headerValidator;

    @Autowired
    SystemProperties config;

    @Autowired
    private Blockchain blockchain;

    Scanner scanner = null;

    BlockingQueue<Runnable> preworkQueue = new LimitedQueue<>(1000);
    ThreadPoolExecutor blockPreworkExec = new ThreadPoolExecutor(8, 8, 0L, TimeUnit.MILLISECONDS, preworkQueue);
    BlockingQueue<Runnable> workQueue = new LimitedQueue<>(10000);
    ThreadPoolExecutor blockWorkExec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, workQueue);

    volatile long nextBlock = -1;
    Map<Long, Block> preworkedBlocks = Collections.synchronizedMap(new HashMap<Long, Block>());

    private void blockPrework(Block b) {
        for (Transaction tx : b.getTransactionsList()) {
            tx.getSender();
        }
        synchronized (this) {
            if (b.getNumber() == nextBlock) {
                submitBlockWork(b);
                long newNextBlock = nextBlock + 1;
                while (true) {
                    Block block = preworkedBlocks.remove(newNextBlock);
                    if (block == null) {
                        break;
                    } else {
                        submitBlockWork(block);
                        newNextBlock++;
                    }
                }
                nextBlock = newNextBlock;
            } else {
                preworkedBlocks.put(b.getNumber(), b);
            }
        }
    }

    private void submitBlockWork(final Block b) {
        blockWorkExec.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    blockWork(b);
                } catch (Exception e) {
                    logger.error("Work error: ", e);
                }
            }
        });
    }

    DateFormat df = new SimpleDateFormat("HH:mm:ss.SSSS");

    private void blockWork(Block block) {
        if (block.getNumber() >= blockchain.getBestBlock().getNumber()) {

            if (block.getNumber() > 0 && !isValid(block.getHeader())) {
                throw new RuntimeException();
            }

            ImportResult result = blockchain.tryToConnect(block);
            System.out.println(df.format(new Date()) + " Imported block " + block.getShortDescr() + ": " + result + " (prework: "
                    + preworkQueue.size() + ", work: " + workQueue.size() + ", blocks: " + preworkedBlocks.size() + ")");

        } else {

            if (block.getNumber() % 10000 == 0)
                System.out.println("Skipping block #" + block.getNumber());
        }
    }

    private void processBlock(final Block b) {
        if (nextBlock < 0) nextBlock = b.getNumber();
        blockPreworkExec.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    blockPrework(b);
                } catch (Exception e) {
                    logger.error("Prework error: ", e);
                }
            }
        });
    }

    public void loadBlocks() {

        String fileSrc = config.blocksLoader();
        try {
            FileInputStream inputStream = null;
            inputStream = new FileInputStream(fileSrc);
            scanner = new Scanner(inputStream, "UTF-8");

            System.out.println("Loading blocks: " + fileSrc);

            while (scanner.hasNextLine()) {

                byte[] blockRLPBytes = Hex.decode(scanner.nextLine());
                Block block = new Block(blockRLPBytes);

                processBlock(block);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

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
