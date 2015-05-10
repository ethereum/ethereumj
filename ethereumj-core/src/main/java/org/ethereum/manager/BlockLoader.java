package org.ethereum.manager;


import org.ethereum.core.Block;
import org.ethereum.facade.Blockchain;
import org.ethereum.net.BlockQueue;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.ethereum.config.SystemProperties.CONFIG;

@Component
public class BlockLoader {

    @Autowired
    private Blockchain blockchain;


    public void loadBlocks(){

        String fileSrc = CONFIG.blocksLoader();
        try {
            File blocksFile = new File(fileSrc);
            System.out.println("Loading blocks: " + fileSrc);
            List<String> blocksList = Files.readAllLines(blocksFile.toPath(), StandardCharsets.UTF_8);

            for (String blockRLP : blocksList){

                byte[] blockRLPBytes = Hex.decode( blockRLP );
                Block block = new Block(blockRLPBytes);

                if (block.getNumber() > blockchain.getBestBlock().getNumber()){
                    System.out.println("Importing block #" + block.getNumber());
                    blockchain.tryToConnect(block);
                } else
                    System.out.println("Skipping block #" + block.getNumber());


            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
