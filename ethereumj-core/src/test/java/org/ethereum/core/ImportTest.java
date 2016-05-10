package org.ethereum.core;


import org.ethereum.TestContext;
import org.ethereum.config.NoAutoscan;
import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.db.BlockStore;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.manager.WorldManager;
import org.hibernate.SessionFactory;
import org.junit.AfterClass;
import org.junit.Ignore;
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
@NoAutoscan
public class ImportTest {

    private static final Logger logger = LoggerFactory.getLogger("test");
    private SystemProperties config = SystemProperties.getDefault();

    @Configuration
    @ComponentScan(basePackages = "org.ethereum")
    @NoAutoscan
    static class ContextConfiguration extends TestContext {

        @Bean
        public BlockStore blockStore(SessionFactory sessionFactory){

            IndexedBlockStore blockStore = new IndexedBlockStore();
            blockStore.init(new HashMapDB(), new HashMapDB());

            return blockStore;
        }
    }

    @Autowired
    WorldManager worldManager;

    @AfterClass
    public static void close(){
//        FileUtil.recursiveDelete(CONFIG.databaseDir());
    }


    @Ignore
    @Test
    public void testScenario1() throws URISyntaxException, IOException {

        BlockchainImpl blockchain = (BlockchainImpl) worldManager.getBlockchain();
        logger.info("Running as: {}", config.genesisInfo());

        URL scenario1 = ClassLoader
                .getSystemResource("blockload/scenario1.dmp");

        File file = new File(scenario1.toURI());
        List<String> strData = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        byte[] root = config.getGenesis().getStateRoot();
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
