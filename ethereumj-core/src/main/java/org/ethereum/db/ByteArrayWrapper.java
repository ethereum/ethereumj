package org.ethereum.db;

import com.google.common.primitives.UnsignedBytes;

import java.util.Arrays;
import java.util.Comparator;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 11/06/2014 15:02
 */

public class ByteArrayWrapper implements Comparable{

    private final byte[] data;

    public ByteArrayWrapper(byte[] data)
    {
        if (data == null)
        {
            throw new NullPointerException();
        }
        this.data = data;
    }

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof ByteArrayWrapper))
        {
            return false;
        }
        return Arrays.equals(data, ((ByteArrayWrapper)other).data);
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(data);
    }


    public byte[] getData() {
        return data;
    }

    @Override
    public int compareTo(Object second) {

        Comparator<byte[]> comparator = UnsignedBytes.lexicographicalComparator();

        return comparator.compare(this.data, ((ByteArrayWrapper)second).getData());
    }
}
