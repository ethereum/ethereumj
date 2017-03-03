package org.ethereum.datasource;

import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;

import java.util.Arrays;

/**
 * 'Reference counting' Source. Unlike regular Source if an entry was
 * e.g. 'put' twice it is actually deleted when 'delete' is called twice
 * I.e. each put increments counter and delete decrements counter, the
 * entry is deleted when the counter becomes zero.
 *
 * Please note that the counting mechanism makes sense only for
 * {@link HashedKeySource} like Sources when any taken key can correspond to
 * the only value
 *
 * This Source is constrained to byte[] values only as the counter
 * needs to be encoded to the backing Source value as byte[]
 *
 * Created by Anton Nashatyrev on 08.11.2016.
 */
public class CountingBytesSource extends AbstractChainedSource<byte[], byte[], byte[], byte[]>
        implements HashedKeySource<byte[], byte[]> {

    BloomFilter moreThan2Bloom;

    public CountingBytesSource(Source<byte[], byte[]> src) {
        this(src, false);

    }
    public CountingBytesSource(Source<byte[], byte[]> src, boolean bloom) {
        super(src);
        if (bloom) moreThan2Bloom = new BloomFilter(0.01, 1_000_000);
    }

    @Override
    public void put(byte[] key, byte[] val) {
        if (val == null) {
            delete(key);
            return;
        }

        synchronized (this) {
            byte[] srcVal = getSource().get(key);
            int srcCount = decodeCount(srcVal);
            if (moreThan2Bloom != null && srcCount >= 1) moreThan2Bloom.add(key);
            getSource().put(key, encodeCount(val, srcCount + 1));
        }
    }

    @Override
    public byte[] get(byte[] key) {
        return decodeValue(getSource().get(key));
    }

    @Override
    public void delete(byte[] key) {
        synchronized (this) {
            int srcCount;
            byte[] srcVal = null;
            if (moreThan2Bloom == null || moreThan2Bloom.contains(key)) {
                srcVal = getSource().get(key);
                srcCount = decodeCount(srcVal);
            } else {
                srcCount = 1;
            }
            if (srcCount > 1) {
                getSource().put(key, encodeCount(decodeValue(srcVal), srcCount - 1));
            } else {
                getSource().delete(key);
            }
        }
    }

    @Override
    protected boolean flushImpl() {
        return false;
    }

    /**
     * Extracts value from the backing Source counter + value byte array
     */
    protected byte[] decodeValue(byte[] srcVal) {
        return srcVal == null ? null : Arrays.copyOfRange(srcVal, RLP.decode(srcVal, 0).getPos(), srcVal.length);
    }

    /**
     * Extracts counter from the backing Source counter + value byte array
     */
    protected int decodeCount(byte[] srcVal) {
        return srcVal == null ? 0 : ByteUtil.byteArrayToInt((byte[]) RLP.decode(srcVal, 0).getDecoded());
    }

    /**
     * Composes value and counter into backing Source value
     */
    protected byte[] encodeCount(byte[] val, int count) {
        return ByteUtil.merge(RLP.encodeInt(count), val);
    }
}
