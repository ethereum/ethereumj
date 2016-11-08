package org.ethereum.datasource;

import org.ethereum.util.ByteUtil;

import java.util.Arrays;

/**
 * Created by Anton Nashatyrev on 08.11.2016.
 */
public class CountingBytesSource extends SourceDelegateAdapter<byte[], byte[]>
        implements HashedKeySource<byte[], byte[]> {

    public CountingBytesSource(Source<byte[], byte[]> src) {
        super(src);
    }

    @Override
    public void put(byte[] key, byte[] val) {
        if (val == null) delete(key);

        byte[] srcVal = super.get(key);
        int srcCount = decodeCount(srcVal);
        super.put(key, encodeCount(val, srcCount + 1));
    }

    @Override
    public byte[] get(byte[] key) {
        return decodeValue(super.get(key));
    }

    @Override
    public void delete(byte[] key) {
        byte[] srcVal = super.get(key);
        int srcCount = decodeCount(srcVal);
        if (srcCount > 1) {
            super.put(key, encodeCount(decodeValue(srcVal), srcCount - 1));
        } else {
            super.delete(key);
        }
    }

    protected byte[] decodeValue(byte[] srcVal) {
        return Arrays.copyOfRange(srcVal, 4, srcVal.length);
    }

    protected int decodeCount(byte[] srcVal) {
        return srcVal == null ? 0 : ByteUtil.byteArrayToInt(Arrays.copyOfRange(srcVal, 0, 4));
    }

    protected byte[] encodeCount(byte[] val, int count) {
        return ByteUtil.merge(ByteUtil.intToBytes(count), val);
    }
}
