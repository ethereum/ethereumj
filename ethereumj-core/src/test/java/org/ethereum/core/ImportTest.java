package org.ethereum.core;


import org.ethereum.TestContext;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.db.BlockStore;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.manager.WorldManager;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ImportTest {

    private static final Logger logger = LoggerFactory.getLogger("test");

    @Configuration
    @ComponentScan(basePackages = "org.ethereum")
    static class ContextConfiguration extends TestContext {

        @Bean
        public BlockStore blockStore(SessionFactory sessionFactory){

            IndexedBlockStore blockStore = new IndexedBlockStore();
            blockStore.init(new HashMap<Long, List<IndexedBlockStore.BlockInfo>>(), new HashMapDB(), null, null);

            return blockStore;
        }
    }

    @Autowired
    WorldManager worldManager;

    @After
    public void close(){
        worldManager.close();
    }


    @Test
    public void testScenario1() throws URISyntaxException, IOException {

        BlockchainImpl blockchain = (BlockchainImpl) worldManager.getBlockchain();

        URL scenario1 = ClassLoader
                .getSystemResource("blockload/scenario1.dmp");

        File file = new File(scenario1.toURI());
        List<String> strData = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        byte[] root = Genesis.getInstance().getStateRoot();
        for (String blockRLP : strData) {
            Block block = new Block(
                    Hex.decode(blockRLP));
            logger.info("sending block.hash: {}", Hex.toHexString(block.getHash()));
            blockchain.tryToConnect(block);
            root = block.getStateRoot();
        }

        Repository repository = (Repository)worldManager.getRepository();
        logger.info("asserting root state is: {}", Hex.toHexString(root));
        assertEquals(Hex.toHexString(root),
                Hex.toHexString(repository.getRoot()));
    }

}
