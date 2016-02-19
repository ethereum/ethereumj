package org.ethereum.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ethereum.crypto.SHA3Helper;
import org.ethereum.datasource.mapdb.MapDBFactoryImpl;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.DataWord;
import org.junit.Assert;
import org.junit.Test;
import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Anton Nashatyrev on 10.09.2015.
 */
public class StorageDictionaryTest {
    private static final Logger logger = LoggerFactory.getLogger("test");

//    @Test
//    public void simpleTest() throws Exception {
//        StorageDictionary kp = new StorageDictionary();
//        kp.addPath(new DataWord("1111"), createPath("a/b1/c1"));
//        kp.addPath(new DataWord("1112"), createPath("a/b1"));
//        kp.addPath(new DataWord("1113"), createPath("a/b1/c2"));
//        kp.addPath(new DataWord("1114"), createPath("a/b2/c1"));
//        logger.info(kp.dump());
//
//        ObjectMapper om = new ObjectMapper();
//        String s = om.writerWithDefaultPrettyPrinter().writeValueAsString(kp.root);
//
//        logger.info(s);
//
//        StorageDictionary.PathElement root = om.readValue(s, StorageDictionary.PathElement.class);
//        logger.info(root.toString(null, 0));
//    }
//
//    @Test
//    public void dbTest() throws Exception {
//        StorageDictionary kp = new StorageDictionary();
//        kp.addPath(new DataWord("1111"), createPath("a/b1/c1"));
//        kp.addPath(new DataWord("1112"), createPath("a/b1"));
//        kp.addPath(new DataWord("1113"), createPath("a/b1/c2"));
//        kp.addPath(new DataWord("1114"), createPath("a/b2/c1"));
//        logger.info(kp.dump());
//
//        StorageDictionaryDb db = StorageDictionaryDb.INST;
////        StorageDictionaryDb db = new StorageDictionaryDb();
////        db.mapDBFactory = new MapDBFactoryImpl();
////        db.init();
//
//        byte[] contractAddr = Hex.decode("abcdef");
//        Assert.assertFalse(kp.isValid());
//
//        db.put(StorageDictionaryDb.Layout.Solidity, contractAddr, kp);
//
//        Assert.assertTrue(kp.isValid());
//        kp.addPath(new DataWord("1114"), createPath("a/b2/c1"));
//
//        Assert.assertTrue(kp.isValid());
//        db.put(StorageDictionaryDb.Layout.Solidity, contractAddr, kp);
//
//        kp.addPath(new DataWord("1115"), createPath("a/b2/c2"));
//        kp.addPath(new DataWord("1115"), createPath("a/b2/c3"));
//
//        db.put(StorageDictionaryDb.Layout.Solidity, contractAddr, kp);
//
//        db.close();
//        db.init();
//
//        StorageDictionary kp1 = db.get(StorageDictionaryDb.Layout.Solidity, contractAddr);
//
//        logger.info(kp1.dump());
//
//        Assert.assertEquals(kp.root, kp1.root);
//        db.close();
//        db.init();
//    }
//
//    @Test
//    public void compactTest() {
//        {
//            StorageDictionary.PathElement field1 = new StorageDictionary.PathElement(StorageDictionary.Type.StorageIndex, 0);
//
//            StorageDictionary.PathElement key1 = new StorageDictionary.PathElement(StorageDictionary.Type.MapKey, 111);
//            StorageDictionary.PathElement key2 = new StorageDictionary.PathElement(StorageDictionary.Type.MapKey, 222);
//
//            StorageDictionary.PathElement val11 = new StorageDictionary.PathElement(StorageDictionary.Type.Offset, 0);
//            StorageDictionary.PathElement val12 = new StorageDictionary.PathElement(StorageDictionary.Type.Offset, 0);
//
//            StorageDictionary d = new StorageDictionary();
//            d.addPath(new DataWord(0x7000), new StorageDictionary.PathElement[]{
//                    field1, key1, val11});
//            d.addPath(new DataWord(0x7001), new StorageDictionary.PathElement[]{field1, key2, val12});
//
//            logger.info("\n" + d.root.toString(null, 0));
//            logger.info("\n" + d.root.toString(null, 0));
//
//            List<StorageDictionary.PathElement> childrenCompacted = d.compactAndFilter(null).getByPath("0").getChildren();
//            Assert.assertEquals(childrenCompacted.get(0).getHashKey(), val11.getHashKey());
//            Assert.assertEquals(childrenCompacted.get(1).getHashKey(), val12.getHashKey());
//            Assert.assertNull(childrenCompacted.get(0).getChildren());
//        }
//
//        {
//            StorageDictionary.PathElement field1 = new StorageDictionary.PathElement(StorageDictionary.Type.StorageIndex, 0);
//
//            StorageDictionary.PathElement key1 = new StorageDictionary.PathElement(StorageDictionary.Type.MapKey, 111);
//            StorageDictionary.PathElement key2 = new StorageDictionary.PathElement(StorageDictionary.Type.MapKey, 222);
//
//            StorageDictionary.PathElement val11 = new StorageDictionary.PathElement(StorageDictionary.Type.Offset, 0);
//            StorageDictionary.PathElement val12 = new StorageDictionary.PathElement(StorageDictionary.Type.Offset, 0);
//            StorageDictionary.PathElement val2 = new StorageDictionary.PathElement(StorageDictionary.Type.Offset, 1);
//
//            StorageDictionary d = new StorageDictionary();
//            d.addPath(new DataWord(0x7000), new StorageDictionary.PathElement[]{field1, key1, val11});
//            d.addPath(new DataWord(0x7001), new StorageDictionary.PathElement[]{field1, key2, val2});
//
//            logger.info("\n" + d.root.toString(null, 0));
//            logger.info("\n" + d.root.toString(null, 0));
//
//            Assert.assertNotNull(d.getByPath("0", "111", "0"));
//            Assert.assertNotNull(d.getByPath("0", "222", "1"));
//        }
//
//    }
//
//    @Test
//    public void compactFilterTest() throws Exception {
//        StorageDictionary dict = StorageDictionary.deserializeFromJson(getClass().getResourceAsStream("/db/StorageDictionaryTest_1.json"));
//        logger.info("Raw dictionary:\n" + dict.dump());
//        StorageDictionary dictNoFilter = dict.compactAndFilter(null);
//        logger.info("Compacted dictionary:\n" + dictNoFilter.dump());
//        StorageDictionary.PathElement root = dictNoFilter.getByPath();
//
//        List<StorageDictionary.PathElement> children = root.getChildren(2, 2);
//        Assert.assertEquals(2, children.size());
//        Assert.assertEquals("258", children.get(0).key);
//        Assert.assertEquals("259", children.get(1).key);
//
//        children = root.getChildren(2, 100);
//        Assert.assertEquals(6, children.size());
//
//        Set<DataWord> filter = createHashKeys("00", "01", "000111", "000113", "000114", "000302", "000303", "000304",
//                "000305", "0105", "0107");
//        StorageDictionary dictFilter = dict.compactAndFilter(filter);
//        logger.info("Compacted and filtered dictionary:\n" + dictFilter.dump());
//
//        children = dictFilter.getByPath().getChildren(0, 100);
//        Assert.assertEquals(6, children.size());
//
//        children = dictFilter.getByPath("258").getChildren(1, 100);
//        Assert.assertEquals(2, children.size());
//        Assert.assertEquals("key3", children.get(0).key);
//        Assert.assertNull(children.get(0).getChildren());
//
//        for (StorageDictionary.PathElement child : children) {
//            logger.info("" + child);
//        }
//    }
//
//    static Set<DataWord> createHashKeys(String ... ss) {
//        Set<DataWord> ret = new HashSet<>();
//        for (String s : ss) {
//            ret.add(new DataWord(s));
//        }
//        return ret;
//    }
//
////    @Test
//    public void dumpDB() throws Exception {
//        StorageDictionaryDb db = StorageDictionaryDb.INST;
//        HTreeMap<ByteArrayWrapper, StorageDictionary> table = db.getLayoutTable(StorageDictionaryDb.Layout.Solidity);
//
////        StorageDictionary priceFeedContract = table.get(new ByteArrayWrapper(Hex.decode("672e330b81a6d6c4fb2a7cad28b3f2295efaab77")));
////        logger.info("priceFeedContract:\n" + priceFeedContract.dump());
//
//
//        logger.info("# of contracts: " + table.size());
//        for (Map.Entry<ByteArrayWrapper, StorageDictionary> e : table.entrySet()) {
//            System.out.printf("======= " + e.getKey() + ":\n" + e.getValue().dump() + "\n");
//        }
//
//        StorageDictionary d = db.getOrCreate(StorageDictionaryDb.Layout.Solidity, Hex.decode("de0b295669a9fd93d5f28d9ec85e40f4cb697bae"));
//        System.out.println(d.dump());
//        System.out.println(d.serializeToJson());
//
////        StorageDictionary kp = new StorageDictionary();
////        kp.addPath(new DataWord("1111"), createPath("a/b1/c1"));
////        db.put(StorageDictionaryDb.Layout.Solidity, Hex.decode("abcdef"), kp);
//    }
//
////    @Test
//    public void stressTest() throws Exception {
//        StorageDictionary kp = new StorageDictionary();
//        kp.addPath(new DataWord("1111"), createPath("a/b1/c1"));
//        kp.addPath(new DataWord("1112"), createPath("a/b1"));
//        kp.addPath(new DataWord("1113"), createPath("a/b1/c2"));
//        kp.addPath(new DataWord("1114"), createPath("a/b2/c1"));
//        logger.info(kp.dump());
//
//        StorageDictionaryDb db = StorageDictionaryDb.INST;
////        StorageDictionaryDb db = new StorageDictionaryDb();
////        db.mapDBFactory = new MapDBFactoryImpl();
////        db.init();
//
//        byte[] contractAddr = Hex.decode("abcdef");
//        Assert.assertFalse(kp.isValid());
//
//        int cnt = 0;
//        while(true) {
//            kp.addPath(new DataWord("1114"), createPath("a/b2/c" + (cnt++)));
//            contractAddr[0]++;
//            db.put(StorageDictionaryDb.Layout.Solidity, contractAddr, kp);
//            db.put(StorageDictionaryDb.Layout.Serpent, contractAddr, kp);
//        }
//    }
//
    static StorageDictionary.PathElement[] createPath(String s) {
        StringTokenizer st = new StringTokenizer(s, "/");
        StorageDictionary.PathElement[] ret = new StorageDictionary.PathElement[st.countTokens()];
        String fullPath = "";
        for (int i = 0; i < ret.length; i++) {
            String s1 = st.nextToken();
            fullPath += s1;
            ret[i] = new StorageDictionary.PathElement(s1, SHA3Helper.sha3(fullPath.getBytes()));
        }
        return ret;
    }

    @Test
    public void largeStorageTest() throws Exception {

        for (int k = 0; k < 50; k++) {
            StorageDictionaryDb db = StorageDictionaryDb.INST;
            byte[] contractAddr = ByteUtil.intToBytes(k);

            StorageDictionary kp = db.getOrCreate(StorageDictionaryDb.Layout.Solidity, contractAddr);
            for (int j = 0; j < 2; j++) {
                long s = System.currentTimeMillis();
                for (int i = 0; i < 10000; i++) {
                    kp.addPath(createPath("0/" + i));

                }
                System.out.println("Fill took " + (System.currentTimeMillis() - s) + " ms");
                s = System.currentTimeMillis();
                kp.store();
                System.out.println("Store took " + (System.currentTimeMillis() - s) + " ms");

//            Assert.assertFalse(kp.isValid());

            }
            long s = System.currentTimeMillis();
            db.flush();
            System.out.println("Put took " + (System.currentTimeMillis() - s) + " ms");
        }

        for (int j = 0; j < 100; j++) {
            StorageDictionaryDb db = StorageDictionaryDb.INST;
            for (int k = 0; k < 5; k++) {
                byte[] contractAddr = ByteUtil.intToBytes(k);

                StorageDictionary kp = db.getOrCreate(StorageDictionaryDb.Layout.Solidity, contractAddr);
                for (int i = 0; i < 10; i++) {
                    kp.addPath(createPath("0/" + (1000000 + i)));

                    //                if (i % 1000 == 0) {
                    //                    long s = System.currentTimeMillis();
                    //                    db.put(StorageDictionaryDb.Layout.Solidity, contractAddr, kp);
                    //                    System.out.println("Put " + i + " took " + (System.currentTimeMillis() - s) + " ms");
                    //                }
                }

                //            Assert.assertFalse(kp.isValid());

            }
            long s = System.currentTimeMillis();
            db.flush();
            System.out.println("Incremental put took " + (System.currentTimeMillis() - s) + " ms");
        }

//        for (int k = 0; k < 5; k++) {
//
//            long s = System.currentTimeMillis();
//            StorageDictionary kp = StorageDictionaryDb.INST.get(StorageDictionaryDb.Layout.Solidity, ByteUtil.intToBytes(k));
//            for (int i = 0; i < 100; i++) {
//                kp.get(createPath("0/" + i));
//            }
//            System.out.println("Get took " + (System.currentTimeMillis() - s) + " ms");
//            logger.info(kp.root.toString(null, 0));
//        }

//        ObjectMapper om = new ObjectMapper();
//        String s = om.writerWithDefaultPrettyPrinter().writeValueAsString(kp.root);
//
//        logger.info(s);
//
//        StorageDictionary.PathElement root = om.readValue(s, StorageDictionary.PathElement.class);
//        logger.info(root.toString(null, 0));
    }
//
////    @Test
////    public void aa() {
////        StorageDictionaryDb db = new StorageDictionaryDb();
////        db.init(new File("D:\\ws\\work\\q8\\b\\storagedict"));
////        StorageDictionary sd = db.getOrCreate(StorageDictionaryDb.Layout.Solidity, Hex.decode("99b28a25e94d4fc009d4fcbc2f6b91440afb901d"));
////        System.out.println(sd.dump());
////        StorageDictionary sdc = sd.compactAndFilter(null);
////        System.out.println(sdc.dump());
////        StorageDictionary.PathElement pathElement = sdc.getByPath("1", "000000000000000000000000640eb8074d09975f11d756985e4fe0863e52c393");
////        System.out.println(Arrays.toString(pathElement.getFullPath()));
////        StorageDictionary.PathElement pathElement1 = sdc.getByPath("1", "000000000000000000000000640eb8074d09975f11d756985e4fe0863e52c393", "0");
////        System.out.println(Arrays.toString(pathElement1.getFullPath()));
////    }
}
