package org.ethereum.db;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.BlockWrapper;
import org.ethereum.datasource.mapdb.MapDBFactory;
import org.ethereum.datasource.mapdb.MapDBFactoryImpl;
import org.ethereum.datasource.mapdb.Serializers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Mikhail Kalinin
 * @since 10.07.2015
 */
public class BlockStressTest {

    private static final Logger logger = LoggerFactory.getLogger("test");

    private static final String TEST_DB_DIR = "test_db/block_stress/";
    private static final String BLOCK_SOURCE = "block_src";
    private Map<byte[], Block> blockSource;
    private DB blockSourceDB;
    private MapDBFactory mapDBFactory;
    private byte[] nodeId = new byte[64];

    @Before
    public void setup() {
        SystemProperties.getDefault().setDataBaseDir(TEST_DB_DIR);

        mapDBFactory = new MapDBFactoryImpl();
        blockSourceDB = mapDBFactory.createDB(BLOCK_SOURCE);
        blockSource = blockSourceDB.hashMapCreate(BLOCK_SOURCE)
                .keySerializer(Serializer.BYTE_ARRAY)
                .valueSerializer(Serializers.BLOCK)
                .makeOrGet();

        Random rnd = new Random(System.currentTimeMillis());
        rnd.nextBytes(nodeId);
    }

    @After
    public void cleanup() {
        blockSourceDB.close();
    }

    @Ignore("long stress test")
    @Test // loads blocks from file and store them into disk DB
    public void prepareData() throws URISyntaxException, IOException {
        URL dataURL = ClassLoader.getSystemResource("blockstore/big_data.dmp");

        File file = new File(dataURL.toURI());

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String blockRLP;
        while(null != (blockRLP = reader.readLine())) {
            Block block = new Block(
                    Hex.decode(blockRLP)
            );
            blockSource.put(block.getHash(), block);

            if (block.getNumber() % 10000 == 0)
                logger.info(
                        "adding block.hash: [{}] block.number: [{}]",
                        block.getShortHash(),
                        block.getNumber()
                );
        }
        logger.info("total blocks loaded: {}", blockSource.size());
    }

    @Ignore("long stress test")
    @Test // interesting how much time will take reading block by its hash
    public void testBlockSource() {
        long start, end;

        start = System.currentTimeMillis();
        Set<byte[]> hashes = blockSource.keySet();
        end = System.currentTimeMillis();

        logger.info("getKeys: {} ms", end - start);

        start = System.currentTimeMillis();
        int counter = 0;
        for(byte[] hash : hashes) {
            blockSource.get(hash);
            if(++counter % 10000 == 0) {
                logger.info("reading: {} done from {}", counter, hashes.size());
            }
        }
        end = System.currentTimeMillis();

        logger.info("reading: total time {} ms", end - start);
        logger.info("reading: time per block {} ms", (end - start) / (float)hashes.size());
    }
}
