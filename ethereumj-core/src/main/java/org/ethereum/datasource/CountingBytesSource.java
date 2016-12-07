package org.ethereum.datasource;

import org.ethereum.util.ByteUtil;

import java.util.Arrays;

/**
 *     'Reference counting' Source. Unlike regular Source if an entry was
 * e.g. 'put' twice it is actually deleted when 'delete' is called twice
 * I.e. each put increments counter and delete decrements counter, the
 * entry is deleted when the counter becomes zero.
 *     Please note that the counting mechanism makes sense only for
 * {@link HashedKeySource} like Sources when any taken key can correspond to
 * the only value
 *     This Source is constrained to byte[] values only as the counter
 * needs to be encoded to the backing Source value as byte[]
 *
 * Created by Anton Nashatyrev on 08.11.2016.
 */
public class CountingBytesSource extends SourceDelegateAdapter<byte[], byte[]>
        implements HashedKeySource<byte[], byte[]> {

    public CountingBytesSource(Source<byte[], byte[]> src) {
        super(src);
    }

    @Override
    public void put(byte[] key, byte[] val) {
        if (val == null) {
            delete(key);
            return;
        }

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

    /**
     * Extracts value from the backing Source counter + value byte array
     */
    protected byte[] decodeValue(byte[] srcVal) {
        return srcVal == null  ? null : Arrays.copyOfRange(srcVal, 4, srcVal.length);
    }

    /**
     * Extracts counter from the backing Source counter + value byte array
     */
    protected int decodeCount(byte[] srcVal) {
        return srcVal == null ? 0 : ByteUtil.byteArrayToInt(Arrays.copyOfRange(srcVal, 0, 4));
    }

    /**
     * Composes value and counter into backing Source value
     */
    protected byte[] encodeCount(byte[] val, int count) {
        return ByteUtil.merge(ByteUtil.intToBytes(count), val);
    }
}
