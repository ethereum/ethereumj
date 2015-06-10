package org.ethereum.db;

import org.ethereum.core.Block;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.math.BigInteger.ZERO;
import static org.junit.Assert.*;

/**
 * @author: Roman Mandeleil
 * Created on: 30/01/2015 11:04
 */

public class InMemoryBlockStoreTest extends AbstractInMemoryBlockStoreTest {

    private static final Logger logger = LoggerFactory.getLogger("test");

    private List<Block> blocks = new ArrayList<>();

    @Before
    public void setup() throws URISyntaxException, IOException {

        URL scenario1 = ClassLoader
                .getSystemResource("blockstore/load.dmp");

        File file = new File(scenario1.toURI());
        List<String> strData = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        BigInteger cumDifficulty = ZERO;

        for (String blockRLP : strData) {

            Block block = new Block(
                    Hex.decode(blockRLP));

            if (block.getNumber() % 1000 == 0)
                logger.info("adding block.hash: [{}] block.number: [{}]",
                        block.getShortHash(),
                        block.getNumber());

            blocks.add(block);
            cumDifficulty = cumDifficulty.add(block.getCumulativeDifficulty());
        }

        logger.info("total difficulty: {}", cumDifficulty);
    }

    @Test
    public void testEmpty(){
        BlockStore blockStore = new InMemoryBlockStore();
        blockStore.setSessionFactory(sessionFactory());
        assertNull(blockStore.getBestBlock());
    }

    @Test
    public void testFlush(){
        BlockStore blockStore = new InMemoryBlockStore();
        blockStore.setSessionFactory(sessionFactory());

        for( Block block : blocks ){
            blockStore.saveBlock(block, null);
        }

        blockStore.flush();
    }

    @Test
    public void testSimpleLoad(){

        BlockStore blockStore = new InMemoryBlockStore();
        SessionFactory sessionFactory = sessionFactory();

        for( Block block : blocks ){
            blockStore.saveBlock(block, null);
        }

        blockStore.setSessionFactory(sessionFactory);
        blockStore.flush();

        blockStore = new InMemoryBlockStore();
        blockStore.setSessionFactory(sessionFactory);

        blockStore.load();

        assertTrue(blockStore.getBestBlock().getNumber() == 8003);
    }

    @Test
    public void testFlushEach1000(){

        InMemoryBlockStore blockStore = new InMemoryBlockStore();
        SessionFactory sessionFactory = sessionFactory();
        blockStore.setSessionFactory(sessionFactory);

        for( int i = 0; i < blocks.size(); ++i ){

            blockStore.saveBlock(blocks.get(i), null);
            if ( i % 1000 == 0){
                blockStore.flush();
                assertTrue(blockStore.blocks.size() == 1);
            }
        }
    }


    @Test
    public void testBlockHashByNumber(){

        BlockStore blockStore = new InMemoryBlockStore();
        SessionFactory sessionFactory = sessionFactory();

        for( Block block : blocks ){
            blockStore.saveBlock(block, null);
        }

        String hash = Hex.toHexString(blockStore.getBlockHashByNumber(7000));
        assertTrue(hash.startsWith("459a8f"));

        hash = Hex.toHexString(blockStore.getBlockHashByNumber(6000));
        assertTrue(hash.startsWith("7a577a"));

        hash = Hex.toHexString(blockStore.getBlockHashByNumber(5000));
        assertTrue(hash.startsWith("820aa7"));

        blockStore.setSessionFactory(sessionFactory);
        blockStore.flush();

        hash = Hex.toHexString(blockStore.getBlockHashByNumber(7000));
        assertTrue(hash.startsWith("459a8f"));

        hash = Hex.toHexString(blockStore.getBlockHashByNumber(6000));
        assertTrue(hash.startsWith("7a577a"));

        hash = Hex.toHexString(blockStore.getBlockHashByNumber(5000));
        assertTrue(hash.startsWith("820aa7"));
    }

    @Test
    public void testBlockByNumber(){

        BlockStore blockStore = new InMemoryBlockStore();
        SessionFactory sessionFactory = sessionFactory();

        for( Block block : blocks ){
            blockStore.saveBlock(block, null);
        }

        String hash = Hex.toHexString(blockStore.getBlockByNumber(7000).getHash());
        assertTrue(hash.startsWith("459a8f"));

        hash = Hex.toHexString(blockStore.getBlockByNumber(6000).getHash());
        assertTrue(hash.startsWith("7a577a"));

        hash = Hex.toHexString(blockStore.getBlockByNumber(5000).getHash());
        assertTrue(hash.startsWith("820aa7"));

        blockStore.setSessionFactory(sessionFactory);
        blockStore.flush();

        hash = Hex.toHexString(blockStore.getBlockByNumber(7000).getHash());
        assertTrue(hash.startsWith("459a8f"));

        hash = Hex.toHexString(blockStore.getBlockByNumber(6000).getHash());
        assertTrue(hash.startsWith("7a577a"));

        hash = Hex.toHexString(blockStore.getBlockByNumber(5000).getHash());
        assertTrue(hash.startsWith("820aa7"));
    }


    @Test
    public void testGetBlockByNumber() {

        BlockStore blockStore = new InMemoryBlockStore();
        SessionFactory sessionFactory = sessionFactory();
        blockStore.setSessionFactory(sessionFactory);

        for( Block block : blocks ){
            blockStore.saveBlock(block, null);
        }

        assertEquals("4312750101",  blockStore.getTotalDifficulty().toString());

        blockStore.flush();
        assertEquals("4312750101",  blockStore.getTotalDifficulty().toString());
    }


    @Test
    public void testDbGetBlockByHash(){

        BlockStore blockStore = new InMemoryBlockStore();
        SessionFactory sessionFactory = sessionFactory();
        blockStore.setSessionFactory(sessionFactory);

        for( Block block : blocks ){
            blockStore.saveBlock(block, null);
        }

        byte[] hash7000 = Hex.decode("459a8f0ee5d4b0c9ea047797606c94f0c1158ed0f30120490b96f7df9893e1fa");
        byte[] hash6000 = Hex.decode("7a577a6b0b7e72e51a646c4cec82cf684c977bca6307e2a49a4116af49316159");
        byte[] hash5000 = Hex.decode("820aa786619e1a2ae139877ba342078c83e5bd65c559069336c13321441e03dc");

        Long number = blockStore.getBlockByHash(hash7000).getNumber();
        assertTrue(number == 7000);

        number = blockStore.getBlockByHash(hash6000).getNumber();
        assertTrue(number == 6000);

        number = blockStore.getBlockByHash(hash5000).getNumber();
        assertTrue(number == 5000);

    }

    @Ignore // TO much time to run it on general basis
    @Test
    public void save100KBlocks() throws FileNotFoundException {

        String blocksFile = "E:\\temp\\_poc-9-blocks\\poc-9-492k.dmp";

        FileInputStream inputStream = new FileInputStream(blocksFile);
        Scanner scanner = new Scanner(inputStream, "UTF-8");

        BlockStore blockStore = new InMemoryBlockStore();
        blockStore.setSessionFactory(sessionFactory());


        while (scanner.hasNextLine()) {

            byte[] blockRLPBytes = Hex.decode( scanner.nextLine());
            Block block = new Block(blockRLPBytes);

            System.out.println(block.getNumber());

            blockStore.saveBlock(block, null);

            if (block.getNumber() > 100_000) break;
        }

        blockStore.flush();
    }

}
