package org.ethereum.trie;

import org.ethereum.crypto.SHA3Helper;
import org.ethereum.datasource.KeyValueDataSource;
import org.spongycastle.util.encoders.Hex;

import static org.ethereum.crypto.SHA3Helper.sha3;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;

public class SecureTrie extends TrieImpl implements Trie{


    public SecureTrie(KeyValueDataSource db) {
        super(db, "");
    }

    public SecureTrie(KeyValueDataSource db, Object root) {
        super(db, root);
    }


    @Override
    public byte[] get(byte[] key) {
        return super.get(sha3(key));
    }

    @Override
    public void update(byte[] key, byte[] value) {
        super.update(sha3(key), value);
    }

    @Override
    public void delete(byte[] key) {
        this.update(key, EMPTY_BYTE_ARRAY);
    }

    @Override
    public byte[] getRootHash() {
        return super.getRootHash();
    }

    @Override
    public void setRoot(byte[] root) {
        super.setRoot(root);
    }

    @Override
    public void sync() {
        super.sync();
    }

    @Override
    public void undo() {
        super.undo();
    }

    @Override
    public String getTrieDump() {
        return super.getTrieDump();
    }

    @Override
    public boolean validate() {
    return super.validate();
    }
}
