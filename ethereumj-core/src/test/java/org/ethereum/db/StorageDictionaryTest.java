package org.ethereum.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ethereum.datasource.mapdb.MapDBFactoryImpl;
import org.ethereum.vm.DataWord;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
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

        StorageDictionaryDb db = new StorageDictionaryDb();
        db.mapDBFactory = new MapDBFactoryImpl();
        db.init();

        byte[] contractAddr = Hex.decode("abcdef");
        Assert.assertFalse(kp.isValid());

        db.put(contractAddr, kp);

        Assert.assertTrue(kp.isValid());
        kp.addPath(new DataWord("1114"), createPath("a/b2/c1"));

        Assert.assertTrue(kp.isValid());
        db.put(contractAddr, kp);

        db.close();
        db.init();
        StorageDictionary kp1 = db.get(contractAddr);

        System.out.println(kp1.dump());

        Assert.assertEquals(kp.root, kp1.root);
        db.close();
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
