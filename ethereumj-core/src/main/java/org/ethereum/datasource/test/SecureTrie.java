package org.ethereum.datasource.test;

import org.ethereum.util.Value;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;

public class SecureTrie extends TrieImpl {
    public SecureTrie(Source<byte[], Value> cache, byte[] root) {
        super(cache, root);
    }

    @Override
    public byte[] get(byte[] key) {
        return super.get(sha3(key));
    }

    @Override
    public void put(byte[] key, byte[] value) {
        super.put(sha3(key), value);
    }

    @Override
    public void delete(byte[] key) {
        put(key, EMPTY_BYTE_ARRAY);
    }
}
