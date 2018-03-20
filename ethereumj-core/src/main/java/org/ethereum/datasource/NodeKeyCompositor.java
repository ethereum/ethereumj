package org.ethereum.datasource;

import org.ethereum.config.CommonConfig;
import org.ethereum.db.RepositoryRoot;

import static java.lang.System.arraycopy;
import static org.ethereum.crypto.HashUtil.sha3;

/**
 * Composes keys for contract storage nodes. <br/><br/>
 *
 * <b>Input:</b> 32-bytes node key, 20-bytes contract address <br/>
 * <b>Output:</b> 32-bytes composed key <i>[first 16-bytes of node key : first 16-bytes of address hash]</i> <br/><br/>
 *
 * Example: <br/>
 * Contract address hash <i>a9539c810cc2e8fa20785bdd78ec36ccb25e1b5be78dbadf6c4e817c6d170bbb</i> <br/>
 * Key of one of the storage nodes <i>bbbbbb5be78dbadf6c4e817c6d170bbb47e9916f8f6cc4607c5f3819ce98497b</i> <br/>
 * Composed key will be <i>a9539c810cc2e8fa20785bdd78ec36ccbbbbbb5be78dbadf6c4e817c6d170bbb</i> <br/><br/>
 *
 * This mechanism is a part of flat storage source which is free from reference counting
 *
 * @see CommonConfig#trieNodeSource()
 * @see RepositoryRoot#RepositoryRoot(Source, byte[])
 *
 * @author Mikhail Kalinin
 * @since 05.12.2017
 */
public class NodeKeyCompositor implements Serializer<byte[], byte[]> {

    public static final int HASH_LEN = 32;
    public static final int PREFIX_BYTES = 16;
    private byte[] addrHash;

    public NodeKeyCompositor(byte[] addrOrHash) {
        this.addrHash = addrHash(addrOrHash);
    }

    @Override
    public byte[] serialize(byte[] key) {
        return composeInner(key, addrHash);
    }

    @Override
    public byte[] deserialize(byte[] stream) {
        return stream;
    }

    public static byte[] compose(byte[] key, byte[] addrOrHash) {
        return composeInner(key, addrHash(addrOrHash));
    }

    private static byte[] composeInner(byte[] key, byte[] addrHash) {

        validateKey(key);

        byte[] derivative = new byte[key.length];
        arraycopy(key, 0, derivative, 0, PREFIX_BYTES);
        arraycopy(addrHash, 0, derivative, PREFIX_BYTES, PREFIX_BYTES);

        return derivative;
    }

    private static void validateKey(byte[] key) {
        if (key.length != HASH_LEN)
            throw new IllegalArgumentException("Key is not a hash code");
    }

    private static byte[] addrHash(byte[] addrOrHash) {
        return addrOrHash.length == HASH_LEN ? addrOrHash : sha3(addrOrHash);
    }
}
