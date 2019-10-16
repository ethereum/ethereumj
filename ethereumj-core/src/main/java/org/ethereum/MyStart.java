package org.ethereum;

import org.ethereum.datasource.leveldb.LevelDbDataSource;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.TrieImpl;
import org.ethereum.trie.TrieKey;
import org.spongycastle.util.encoders.Hex;

import java.util.Iterator;
import java.util.List;

import static org.ethereum.crypto.HashUtil.sha3;

public class MyStart {

    public static void main(String[] args) {
        byte[] key1 = Hex.decode("ea8b9ddd889ed201acddd507edf8adc100c2d9bb5a09ee6011c8f28ba4d69584");
        byte[] key2 = Hex.decode("ea8b9ddd889ed201acddd507edf8adc100c2d9bb5a09ee6011c8f28ba4d69583");

        LevelDbDataSource mydb = new LevelDbDataSource("myvouchtest-" + (Math.random()));
        mydb.init();
        TrieImpl mytrie = new TrieImpl(mydb);
        mytrie.setAsync(false);
        mytrie.put(key1, "123".getBytes());
        mytrie.put(key2, "321".getBytes());
        mytrie.flush();

        List<TrieImpl.Node> proofNodes = mytrie.prove(key2);
        System.out.println("root=" + Hex.toHexString(mytrie.getRootHash()));
        for (TrieImpl.Node next : proofNodes) {
            System.out.println("proofnode=" + Hex.toHexString(mytrie.getEncoded(next)));
        }
    }
}

