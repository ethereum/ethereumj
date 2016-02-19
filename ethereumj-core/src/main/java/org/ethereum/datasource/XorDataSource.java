package org.ethereum.datasource;

import org.ethereum.util.ByteUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Anton Nashatyrev on 18.02.2016.
 */
public class XorDataSource implements KeyValueDataSource {
    KeyValueDataSource source;
    byte[] subKey;

    public XorDataSource(KeyValueDataSource source, byte[] subKey) {
        this.source = source;
        this.subKey = subKey;
    }

    private byte[] convertKey(byte[] key) {
        return ByteUtil.xor(key, subKey);
    }

    @Override
    public byte[] get(byte[] key) {
        return source.get(convertKey(key));
    }

    @Override
    public byte[] put(byte[] key, byte[] value) {
        return source.put(convertKey(key), value);
    }

    @Override
    public void delete(byte[] key) {
        source.delete(convertKey(key));
    }

    @Override
    public Set<byte[]> keys() {
        Set<byte[]> keys = source.keys();
        HashSet<byte[]> ret = new HashSet<>(keys.size());
        for (byte[] key : keys) {
            ret.add(convertKey(key));
        }
        return ret;
    }

    @Override
    public void updateBatch(Map<byte[], byte[]> rows) {
        Map<byte[], byte[]> converted = new HashMap<>(rows.size());
        for (Map.Entry<byte[], byte[]> entry : rows.entrySet()) {
            converted.put(convertKey(entry.getKey()), entry.getValue());
        }
        source.updateBatch(converted);
    }

    @Override
    public void setName(String name) {
        source.setName(name);
    }

    @Override
    public String getName() {
        return source.getName();
    }

    @Override
    public void init() {
        source.init();
    }

    @Override
    public boolean isAlive() {
        return source.isAlive();
    }

    @Override
    public void close() {
        source.close();
    }
}
