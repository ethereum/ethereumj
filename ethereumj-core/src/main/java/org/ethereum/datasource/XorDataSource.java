package org.ethereum.datasource;

import org.ethereum.util.ByteUtil;

/**
 * Created by Anton Nashatyrev on 18.02.2016.
 */
public class XorDataSource<V> implements Source<byte[], V> {
    Source<byte[], V> source;
    byte[] subKey;

    public XorDataSource(Source<byte[], V> source, byte[] subKey) {
        this.source = source;
        this.subKey = subKey;
    }

    private byte[] convertKey(byte[] key) {
        return ByteUtil.xorAlignRight(key, subKey);
    }

    @Override
    public V get(byte[] key) {
        return source.get(convertKey(key));
    }

    @Override
    public void put(byte[] key, V value) {
        source.put(convertKey(key), value);
    }

    @Override
    public void delete(byte[] key) {
        source.delete(convertKey(key));
    }

    @Override
    public boolean flush() {
        return source.flush();
    }
}
