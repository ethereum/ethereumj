package org.ethereum.manager;


import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import static org.ethereum.config.SystemProperties.CONFIG;

@Component
public class BlockLoader {

    @Autowired
    private Blockchain blockchain;

    Scanner scanner = null;


    public void loadBlocks(){

        String fileSrc = CONFIG.blocksLoader();
        try {
            FileInputStream inputStream = null;
            inputStream = new FileInputStream(fileSrc);
            scanner = new Scanner(inputStream, "UTF-8");

            System.out.println("Loading blocks: " + fileSrc);

            while (scanner.hasNextLine()) {

                byte[] blockRLPBytes = Hex.decode( scanner.nextLine());
                Block block = new Block(blockRLPBytes);

                long t1 = System.nanoTime();
                if (block.getNumber() >= blockchain.getBestBlock().getNumber()){
                    blockchain.tryToConnect(block);
                    long t1_ = System.nanoTime();

                    float elapsed = ((float)(t1_ - t1) / 1_000_000);

                    if (block.getNumber() % 1000 == 0 || elapsed > 10_000) {
                        String result = String.format("Imported block #%d took: [%02.2f msec]",
                                block.getNumber(), elapsed);

                        System.out.println(result);
                    }
                } else{

                    if (block.getNumber() % 10000 == 0)
                        System.out.println("Skipping block #" + block.getNumber());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(" * Done * ");
    }
}
