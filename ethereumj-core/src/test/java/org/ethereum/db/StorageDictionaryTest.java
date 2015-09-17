package org.ethereum.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ethereum.datasource.mapdb.MapDBFactoryImpl;
import org.ethereum.vm.DataWord;
import org.junit.Assert;
import org.junit.Test;
import org.mapdb.HTreeMap;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by Anton Nashatyrev on 10.09.2015.
 */
public class StorageDictionaryTest {

    enum E {A, B}
    public static class T {
        public E e;
    }
    @Test
    public void jsonTest() throws IOException {
        T t = new T();
        t.e = E.A;

        ObjectMapper om = new ObjectMapper();
        String s = om.writerWithDefaultPrettyPrinter().writeValueAsString(t);

        System.out.println(s);

        T t1 = om.readValue(s, T.class);
        System.out.println(t1);
    }

    @Test
    public void simpleTest() throws Exception {
        StorageDictionary kp = new StorageDictionary();
        kp.addPath(new DataWord("1111"), createPath("a/b1/c1"));
        kp.addPath(new DataWord("1112"), createPath("a/b1"));
        kp.addPath(new DataWord("1113"), createPath("a/b1/c2"));
        kp.addPath(new DataWord("1114"), createPath("a/b2/c1"));
        System.out.println(kp.dump());

        ObjectMapper om = new ObjectMapper();
        String s = om.writerWithDefaultPrettyPrinter().writeValueAsString(kp.root);

        System.out.println(s);

        StorageDictionary.PathElement root = om.readValue(s, StorageDictionary.PathElement.class);
        System.out.println(root.toString(null, 0));
    }

    @Test
    public void dbTest() throws Exception {
        StorageDictionary kp = new StorageDictionary();
        kp.addPath(new DataWord("1111"), createPath("a/b1/c1"));
        kp.addPath(new DataWord("1112"), createPath("a/b1"));
        kp.addPath(new DataWord("1113"), createPath("a/b1/c2"));
        kp.addPath(new DataWord("1114"), createPath("a/b2/c1"));
        System.out.println(kp.dump());

        StorageDictionaryDb db = StorageDictionaryDb.INST;
//        StorageDictionaryDb db = new StorageDictionaryDb();
//        db.mapDBFactory = new MapDBFactoryImpl();
//        db.init();

        byte[] contractAddr = Hex.decode("abcdef");
        Assert.assertFalse(kp.isValid());

        db.put(StorageDictionaryDb.Layout.Solidity, contractAddr, kp);

        Assert.assertTrue(kp.isValid());
        kp.addPath(new DataWord("1114"), createPath("a/b2/c1"));

        Assert.assertTrue(kp.isValid());
        db.put(StorageDictionaryDb.Layout.Solidity, contractAddr, kp);

        kp.addPath(new DataWord("1115"), createPath("a/b2/c2"));
        kp.addPath(new DataWord("1115"), createPath("a/b2/c3"));

        db.put(StorageDictionaryDb.Layout.Solidity, contractAddr, kp);

        db.close();
        db.init();

        StorageDictionary kp1 = db.get(StorageDictionaryDb.Layout.Solidity, contractAddr);

        System.out.println(kp1.dump());

        Assert.assertEquals(kp.root, kp1.root);
        db.close();
        db.init();
    }

//    @Test
    public void dumpDB() throws Exception {
        StorageDictionaryDb db = StorageDictionaryDb.INST;
        HTreeMap<ByteArrayWrapper, StorageDictionary> table = db.getLayoutTable(StorageDictionaryDb.Layout.Solidity);

//        StorageDictionary priceFeedContract = table.get(new ByteArrayWrapper(Hex.decode("672e330b81a6d6c4fb2a7cad28b3f2295efaab77")));
//        System.out.println("priceFeedContract:\n" + priceFeedContract.dump());


        System.out.println("# of contracts: " + table.size());
        for (Map.Entry<ByteArrayWrapper, StorageDictionary> e : table.entrySet()) {
            System.out.printf("======= " + e.getKey() + ":\n" + e.getValue().dump() + "\n");
        }

//        StorageDictionary kp = new StorageDictionary();
//        kp.addPath(new DataWord("1111"), createPath("a/b1/c1"));
//        db.put(StorageDictionaryDb.Layout.Solidity, Hex.decode("abcdef"), kp);
    }

//    @Test
    public void cantDeleteCheck() {
        File f = new File("D:\\ws\\ethereumj\\database\\metadata\\storagedict.wal.0");
        System.out.println(f.canRead());
        System.out.println(f.canWrite());
        System.out.println(f.delete());
    }

//    @Test
    public void stressTest() throws Exception {
        StorageDictionary kp = new StorageDictionary();
        kp.addPath(new DataWord("1111"), createPath("a/b1/c1"));
        kp.addPath(new DataWord("1112"), createPath("a/b1"));
        kp.addPath(new DataWord("1113"), createPath("a/b1/c2"));
        kp.addPath(new DataWord("1114"), createPath("a/b2/c1"));
        System.out.println(kp.dump());

        StorageDictionaryDb db = StorageDictionaryDb.INST;
//        StorageDictionaryDb db = new StorageDictionaryDb();
//        db.mapDBFactory = new MapDBFactoryImpl();
//        db.init();

        byte[] contractAddr = Hex.decode("abcdef");
        Assert.assertFalse(kp.isValid());

        int cnt = 0;
        while(true) {
            kp.addPath(new DataWord("1114"), createPath("a/b2/c" + (cnt++)));
            contractAddr[0]++;
            db.put(StorageDictionaryDb.Layout.Solidity, contractAddr, kp);
            db.put(StorageDictionaryDb.Layout.Serpent, contractAddr, kp);
        }
    }

    static StorageDictionary.PathElement[] createPath(String s) {
        StringTokenizer st = new StringTokenizer(s, "/");
        StorageDictionary.PathElement[] ret = new StorageDictionary.PathElement[st.countTokens()];
        for (int i = 0; i < ret.length; i++) {
            String s1 = st.nextToken();
            ret[i] = new StorageDictionary.PathElement(s1);
        }
        return ret;
    }
}
