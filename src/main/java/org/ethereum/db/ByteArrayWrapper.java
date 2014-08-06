package org.ethereum.db;

import java.util.Arrays;

import org.ethereum.util.FastByteComparisons;
import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 * 
 * @author: Roman Mandeleil Created on: 11/06/2014 15:02
 */
public class ByteArrayWrapper implements Comparable<ByteArrayWrapper> {

	private final byte[] data;

	public ByteArrayWrapper(byte[] data) {
		if (data == null) {
			throw new NullPointerException("Can't create a wrapper around null");
		}
		this.data = data;
	}

	public boolean equals(Object other) {
		if (!(other instanceof ByteArrayWrapper)) {
			return false;
		}
		byte[] otherData = ((ByteArrayWrapper) other).getData();
		return FastByteComparisons.compareTo(
				data, 0, data.length, 
				otherData, 0, otherData.length) == 0;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(data);
	}

	@Override
	public int compareTo(ByteArrayWrapper o) {
		return FastByteComparisons.compareTo(
				data, 0, data.length, 
				o.getData(), 0, o.getData().length);
	}
	
	public byte[] getData() {
		return data;
	}

    @Override
    public String toString() {
        return Hex.toHexString(data);
    }
}
