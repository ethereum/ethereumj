package test.ethereum.core;

import test.ethereum.TestContext;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.Chain;
import org.ethereum.manager.WorldManager;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

/**
 * www.etherj.com
 *
 * @author: Roman Mandeleil
 * Created on: 09/11/2014 23:35
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader=AnnotationConfigContextLoader.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ForkTest {

    private static final Logger logger = LoggerFactory.getLogger("test");

    @Configuration
    @ComponentScan(basePackages = "org.ethereum")
    static class ContextConfiguration extends TestContext {
        static {
            SystemProperties.CONFIG.setDataBaseDir("test_db/"+ ForkTest.class);
        }
    }

    @Autowired
    WorldManager worldManager;

    @After
    public void doReset(){
        worldManager.reset();
    }


    @Test
    public void fork1() throws URISyntaxException, IOException {

        BlockchainImpl blockchain = (BlockchainImpl)worldManager.getBlockchain();

        URL scenario1 = ClassLoader
                .getSystemResource("fork/scenario1.dmp");

        File file = new File(scenario1.toURI());
        List<String> strData = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        for(String blockRLP : strData){
            Block block = new Block(
                    Hex.decode(blockRLP));
            logger.info("sending block.hash: {}", Hex.toHexString( block.getHash() ));
            blockchain.tryToConnect(block);
        }

        List<Chain> altChains = blockchain.getAltChains();
        List<Block> garbage   = blockchain.getGarbage();

        assertEquals(1,  altChains.size());
        assertEquals(13, altChains.get(0).getSize());
        assertEquals(20, blockchain.getSize());
        assertEquals(0,  garbage.size());
    }

    @Test
    public void fork2() throws URISyntaxException, IOException {

        BlockchainImpl blockchain = (BlockchainImpl) worldManager.getBlockchain();


        URL scenario2 = ClassLoader
                .getSystemResource("fork/scenario2.dmp");

        File file = new File(scenario2.toURI());
        List<String> strData = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        for(String blockRLP : strData){
            Block block = new Block(
                    Hex.decode(blockRLP));
            logger.info("sending block.hash: {}", Hex.toHexString( block.getHash() ));
            blockchain.tryToConnect(block);
        }

        List<Chain> altChains = blockchain.getAltChains();
        List<Block> garbage   = blockchain.getGarbage();

        assertEquals(2,  altChains.size());
//        assertEquals(13, altChains.get(0).getSize());
        assertEquals(new BigInteger("13238272"), altChains.get(0).getTotalDifficulty());
        assertEquals(new BigInteger("13369344"), altChains.get(1).getTotalDifficulty());

        assertEquals(new BigInteger("13238272"), blockchain.getTotalDifficulty() );
        assertEquals(100, blockchain.getSize());
        assertEquals(0,  garbage.size());

        System.out.println();
    }



}
