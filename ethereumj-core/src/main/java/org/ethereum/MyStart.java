package org.ethereum;

import org.ethereum.datasource.leveldb.LevelDbDataSource;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.TrieImpl;
import org.spongycastle.util.encoders.Hex;

import java.util.List;

import static org.ethereum.crypto.HashUtil.sha3;

public class MyStart {

    public static void main(String[] args) {
        byte[] key1 = "abcde".getBytes();
        byte[] key2 = "abcdz".getBytes();

        LevelDbDataSource mydb = new LevelDbDataSource("myvouchtest-" + (Math.random()));
        mydb.init();
        SecureTrie mytrie = new SecureTrie(mydb);
        mytrie.setAsync(false);
        mytrie.put(key1, "123".getBytes());
        mytrie.put(key2, "321".getBytes());
        mytrie.flush();

        List<TrieImpl.Node> proofNodes = mytrie.proveNodes(key2);
        System.out.println("root=" + Hex.toHexString(mytrie.getRootHash()));
        for (TrieImpl.Node next : proofNodes) {
            System.out.println("proofnode=" + (Hex.toHexString(mytrie.getEncoded(next))));
        }

        List<byte[]> proofs = mytrie.prove(key2);
        for (byte[] next : proofs) {
            System.out.println("proofs=" + (Hex.toHexString(next)));
        }
    }
}

