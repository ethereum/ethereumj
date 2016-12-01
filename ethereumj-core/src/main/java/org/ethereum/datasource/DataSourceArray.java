package org.ethereum.datasource;

import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.util.AbstractList;

/**
 * Created by Anton Nashatyrev on 17.03.2016.
 */
public class DataSourceArray<V> extends AbstractList<V> implements Flushable {
    private ObjectDataSource<V> src;
    private static final byte[] sizeKey = Hex.decode("FFFFFFFFFFFFFFFF");
    private int size = -1;

    public DataSourceArray(ObjectDataSource<V> src) {
        this.src = src;
    }

    @Override
    public boolean flush() {
        return src.flush();
    }

    @Override
    public V set(int idx, V value) {
        if (idx >= size()) {
            setSize(idx + 1);
        }
        src.put(ByteUtil.intToBytes(idx), value);
        return value;
    }

    @Override
    public void add(int index, V element) {
        set(index, element);
    }

    @Override
    public V remove(int index) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public V get(int idx) {
        if (idx < 0 || idx >= size()) throw new IndexOutOfBoundsException(idx + " > " + size);
        return src.get(ByteUtil.intToBytes(idx));
    }

    @Override
    public int size() {
        if (size < 0) {
            byte[] sizeBB = src.getSrc().get(sizeKey);
            size = sizeBB == null ? 0 : ByteUtil.byteArrayToInt(sizeBB);
        }
        return size;
    }

    private synchronized void setSize(int newSize) {
        size = newSize;
        src.getSrc().put(sizeKey, ByteUtil.intToBytes(newSize));
    }
}
