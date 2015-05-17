package org.ethereum.blockstore;

import org.ethereum.core.Block;
import org.ethereum.db.InMemoryBlockStore;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author: Roman Mandeleil
 * Created on: 30/01/2015 11:04
 */

public class InMemoryBlockStoreTest {

    private static final Logger logger = LoggerFactory.getLogger("test");
    private InMemoryBlockStore blockStore;

    @Before
    public void setup() throws URISyntaxException, IOException {

        blockStore = new InMemoryBlockStore();
        URL scenario1 = ClassLoader
                .getSystemResource("blockstore/load.dmp");

        File file = new File(scenario1.toURI());
        List<String> strData = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        for (String blockRLP : strData) {
            Block block = new Block(
                    Hex.decode(blockRLP));
            logger.trace("adding block.hash: [{}] block.number: [{}]",
                    block.getShortHash(),
                    block.getNumber());
            blockStore.saveBlock(block, null);
        }
    }

    @Test
    public void testSaving8003Blocks() {

        Block bestBlock = blockStore.getBestBlock();
        Long bestIndex = blockStore.getBestBlock().getNumber();
        Long firstIndex = bestIndex - InMemoryBlockStore.MAX_BLOCKS;

        assertTrue(bestIndex  == 8003);
        assertTrue(firstIndex == 7003);

        assertTrue(blockStore.getBlockByNumber(7000) == null);
        assertTrue(blockStore.getBlockByNumber(8004) == null);

        Block byHashBlock = blockStore.getBlockByHash(bestBlock.getHash());
        assertTrue(bestBlock.getNumber() == byHashBlock.getNumber());

        byte[] hashFor7500 = blockStore.getBlockByNumber(7500).getHash();
        Block block7500 = blockStore.getBlockByHash(hashFor7500);
        assertTrue(block7500.getNumber() == 7500);
    }

    @Test
    public void testListOfHashes(){

        Block block = blockStore.getBlockByNumber(7500);
        byte[] hash = block.getHash();

        List<byte[]> hashes = blockStore.getListOfHashesStartFrom(hash, 700);

        byte[] lastHash = hashes.get(hashes.size() - 1);

        assertEquals(Hex.toHexString(blockStore.getBestBlock().getHash()),
                     Hex.toHexString(lastHash));

        assertTrue(hashes.size() == 504);
    }

}
