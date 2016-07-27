package org.ethereum.trie;

import org.ethereum.core.BlockHeader;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.datasource.KeyValueDataSource;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Anton Nashatyrev on 27.07.2016.
 */
public class JournalPruneTest {

    class StringJDS extends JournalPruneDataSource {
        public StringJDS(KeyValueDataSource src) {
            super(src);
        }

        public synchronized byte[] put(String key) {
            return super.put(key.getBytes(), key.getBytes());
        }

        public synchronized void delete(String key) {
            super.delete(key.getBytes());
        }

        public String get(String key) {
            return new String(super.get(key.getBytes()));
        }
    }

    private void checkDb(HashMapDB db, String ... keys) {
        assertEquals(db.getSize(), keys.length);
        for (String key : keys) {
            assertTrue(db.get(key.getBytes()) != null);
        }
    }

    private void putKeys(HashMapDB db, String ... keys) {
        for (String key : keys) {
            db.put(key.getBytes(), key.getBytes());
        }
    }

    @Test
    public void simpleTest() {
        HashMapDB db = new HashMapDB();
        StringJDS jds = new StringJDS(db);

        putKeys(db, "a1", "a2");

        jds.put("a3");
        jds.delete("a2");
        jds.storeBlockChanges(createHeader(1, 0));
        jds.put("a2");
        jds.delete("a3");
        jds.storeBlockChanges(createHeader(2, 0));
        jds.delete("a2");
        jds.storeBlockChanges(createHeader(3, 0));

        jds.prune(createHeader(1, 0));
        checkDb(db, "a1", "a2", "a3");

        jds.prune(createHeader(2, 0));
        checkDb(db, "a1", "a2");

        jds.prune(createHeader(3, 0));
        checkDb(db, "a1");

        assertEquals(0, jds.getBlockUpdates().size());
        assertEquals(0, jds.getRefCount().size());
    }

    @Test
    public void forkTest1() {
        HashMapDB db = new HashMapDB();
        StringJDS jds = new StringJDS(db);

        putKeys(db, "a1", "a2", "a3");

        jds.put("a4");
        jds.put("a1");
        jds.delete("a2");
        jds.storeBlockChanges(createHeader(1, 0));
        jds.put("a5");
        jds.delete("a3");
        jds.put("a2");
        jds.put("a1");
        jds.storeBlockChanges(createHeader(1, 1));

        checkDb(db, "a1", "a2", "a3", "a4", "a5");

        jds.prune(createHeader(1, 0));
        checkDb(db, "a1", "a3", "a4");

        assertEquals(0, jds.getBlockUpdates().size());
        assertEquals(0, jds.getRefCount().size());
    }

    @Test
    public void forkTest2() {
        HashMapDB db = new HashMapDB();
        StringJDS jds = new StringJDS(db);

        putKeys(db, "a1", "a2", "a3");

        jds.delete("a1");
        jds.delete("a3");
        jds.storeBlockChanges(createHeader(1, 0));
        jds.put("a4");
        jds.storeBlockChanges(createHeader(1, 1));
        jds.storeBlockChanges(createHeader(2, 0));
        jds.put("a1");
        jds.delete("a2");
        jds.storeBlockChanges(createHeader(2, 1));
        jds.put("a4");
        jds.storeBlockChanges(createHeader(2, 2));
        jds.put("a3");
        jds.storeBlockChanges(createHeader(3, 0));

        checkDb(db, "a1", "a2", "a3", "a4");

        jds.prune(createHeader(1, 0));
        checkDb(db, "a1", "a2", "a3", "a4");

        jds.prune(createHeader(2, 0));
        checkDb(db, "a2", "a3");

        jds.prune(createHeader(3, 0));
        checkDb(db, "a2", "a3");

        assertEquals(0, jds.getBlockUpdates().size());
        assertEquals(0, jds.getRefCount().size());
    }

    @Test
    public void forkTest3() {
        HashMapDB db = new HashMapDB();
        StringJDS jds = new StringJDS(db);

        putKeys(db, "a1");

        jds.put("a2");
        jds.storeBlockChanges(createHeader(1, 0));
        jds.put("a1");
        jds.put("a2");
        jds.put("a3");
        jds.storeBlockChanges(createHeader(1, 1));
        jds.put("a1");
        jds.put("a2");
        jds.put("a3");
        jds.storeBlockChanges(createHeader(1, 2));

        checkDb(db, "a1", "a2", "a3");

        jds.prune(createHeader(1, 0));
        checkDb(db, "a1", "a2");

        assertEquals(0, jds.getBlockUpdates().size());
        assertEquals(0, jds.getRefCount().size());
    }



    public BlockHeader createHeader(int num, int fork) {
        byte[] coinbase = new byte[32];
        coinbase[0] = (byte) fork;
        return new BlockHeader(new byte[32], new byte[32], coinbase, new byte[32], new byte[32],
                num, new byte[] {0}, 0, 0, new byte[0], new byte[0], new byte[0]);
    }

}
