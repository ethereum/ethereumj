package org.ethereum.datasource;

import java.util.Map;

/**
 * Created by Anton Nashatyrev on 24.10.2016.
 */
public class LegacySourceAdapter implements BatchSource<byte[], byte[]> {
    KeyValueDataSource src;

    public LegacySourceAdapter(KeyValueDataSource src) {
        this.src = src;
    }

    @Override
    public byte[] get(byte[] key) {
        return src.get(key);
    }

    @Override
    public void put(byte[] key, byte[] value) {
        src.put(key, value);
    }

    @Override
    public void delete(byte[] key) {
        src.delete(key);
    }

    @Override
    public void updateBatch(Map<byte[], byte[]> rows) {
        src.updateBatch(rows);
    }

    @Override
    public boolean flush() {
        ((Flushable) src).flush();
        return true;
    }
}
