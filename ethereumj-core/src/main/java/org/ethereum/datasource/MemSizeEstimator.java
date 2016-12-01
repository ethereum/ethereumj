package org.ethereum.datasource;

/**
 * Created by Anton Nashatyrev on 01.12.2016.
 */
public interface MemSizeEstimator<E> {

    long estimateSize(E e);

    MemSizeEstimator<byte[]> ByteArrayEstimator = new MemSizeEstimator<byte[]>() {
        @Override
        public long estimateSize(byte[] bytes) {
            return bytes == null ? 0 : bytes.length + 4; // 4 - compressed ref size
        }
    };
}
