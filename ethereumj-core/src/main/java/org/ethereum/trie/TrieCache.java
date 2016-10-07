package org.ethereum.trie;

import org.ethereum.db.ByteArrayWrapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Anton Nashatyrev on 05.10.2016.
 */
public class TrieCache implements Trie {

    private Trie src;

    private Map<ByteArrayWrapper, byte[]> cache = new HashMap<>();
    private Set<ByteArrayWrapper> dirtyEntries = new HashSet<>();

    public TrieCache(Trie src) {
        this.src = src;
    }

    @Override
    public byte[] get(byte[] key) {
        ByteArrayWrapper keyW = new ByteArrayWrapper(key);
        byte[] ret = cache.get(keyW);
        if (ret == null) {
            ret = src.get(key);
            cache.put(keyW, ret);
        }
        return ret;
    }

    @Override
    public void update(byte[] key, byte[] value) {
        ByteArrayWrapper keyW = new ByteArrayWrapper(key);
        cache.put(keyW, value);
        dirtyEntries.add(keyW);
    }

    @Override
    public void delete(byte[] key) {
        ByteArrayWrapper keyW = new ByteArrayWrapper(key);
        cache.remove(keyW);
        dirtyEntries.add(keyW);
    }

    @Override
    public byte[] getRootHash() {
        if (dirtyEntries.isEmpty()) {
            return src.getRootHash();
        } else {
            throw new RuntimeException("Not supported for cached Trie");
        }
    }

    @Override
    public void setRoot(byte[] root) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void sync() {
        for (ByteArrayWrapper key : dirtyEntries) {
            src.update(key.getData(), cache.get(key));
        }
        dirtyEntries.clear();
    }

    @Override
    public void undo() {
        cache.clear();
        dirtyEntries.clear();
    }

    @Override
    public String getTrieDump() {
        throw new RuntimeException("Not supported");
    }

    @Override
    public boolean validate() {
        throw new RuntimeException("Not supported");
    }
}
