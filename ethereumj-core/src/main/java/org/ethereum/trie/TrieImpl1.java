package org.ethereum.trie;

import org.ethereum.datasource.Source;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

/**
 * Created by Anton Nashatyrev on 07.02.2017.
 */
public class TrieImpl1  implements Trie<byte[]> {

    public static final class Key {
        private final byte[] key;
        private final int off;
        private final boolean terminal;

        public static Key fromNormal(byte[] key) {
            return new Key(key);
        }

        public static Key fromPacked(byte[] key) {
            return new Key(key, ((key[0] >> 4) & 1) != 0 ? 1 : 2, ((key[0] >> 4) & 2) != 0);
        }

        public Key(byte[] key, int off, boolean terminal) {
            this.terminal = terminal;
            this.off = off;
            this.key = key;
        }

        private Key(byte[] key) {
            this(key, 0, true);
        }

        public boolean isTerminal() {
            return terminal;
        }

        public boolean isEmpty() {
            return getLength() == 0;
        }

        public Key shift(int hexCnt) {
            return new Key(this.key, off + hexCnt, terminal);
        }

        public Key matchAndShift(Key k) {
            int len = getLength();
            int kLen = k.getLength();
            if (len < kLen) return null;

            if ((off & 1) == (k.off & 1)) {
                // optimization to compare whole keys bytes
                if ((off & 1) == 1) {
                    if (getHex(0) != k.getHex(0)) return null;
                }
                int idx1 = (off   + 1) >> 1;
                int idx2 = (k.off + 1) >> 1;
                int l = kLen >> 1;
                for (int i = 0; i < l; i++, idx1++, idx2++) {
                    if (key[idx1] != k.key[idx2]) return null;
                }
            } else {
            for (int i = 0; i < kLen; i++) {
                if (getHex(i) != k.getHex(i)) return null;
            }
            }
            return shift(kLen);
        }

        public int getLength() {
            return (key.length << 1) - off;
        }

        public int getHex(int idx) {
            byte b = key[(off + idx) >> 1];
            return (((off + idx) & 1) == 0 ? (b >> 4) : b) & 0xF;
        }
    }

    public enum NodeType {
        BranchNode,
        KVNodeValue,
        KVNodeNode
    }

    private final static Object NULL_NODE = new Object();
    public final class Node {
        private byte[] hash = null;
        private byte[] rlp = null;
        private RLP.LList parsedRlp = null;

        private Object[] children = null;

        public Node(byte[] hashOrRlp) {
            if (hashOrRlp.length == 32) {
                this.hash = hashOrRlp;
            } else {
                this.rlp = hashOrRlp;
            }
        }

        private Node(RLP.LList parsedRlp) {
            this.parsedRlp = parsedRlp;
        }

        private void resolve() {
            if (rlp != null || parsedRlp != null) return;
            rlp = resolveHash(hash);
            if (rlp == null) {
                throw new RuntimeException("Invalid Trie state, can't resolve hash " + Hex.toHexString(hash));
            }
        }

        private void parse() {
            if (children != null) return;
            resolve();

            RLP.LList list = parsedRlp == null ? RLP.decodeLazyList(rlp) : parsedRlp;

            if (list.size() == 2) {
                children = new Object[2];
                Key key = Key.fromPacked(list.getBytes(0));
                children[0] = key;
                if (key.isTerminal()) {
                    children[1] = list.getBytes(1);
                } else {
                    children[1] = list.isList(1) ? new Node(list.getList(1)) : new Node(list.getBytes(1));
                }
            } else {
                children = new Object[17];
                parsedRlp = list;
            }
        }

        public Node branchNodeGetChild(int hex) {
            parse();
            assert getType() == NodeType.BranchNode;
            Object n = children[hex];
            if (n == null) {
                if (parsedRlp.isList(hex)) {
                    n = new Node(parsedRlp.getList(hex));
                } else {
                    byte[] bytes = parsedRlp.getBytes(hex);
                    if (bytes.length == 0) {
                        n = NULL_NODE;
                    } else {
                        n = new Node(bytes);
                    }
                }
                children[hex] = n;
            }
            return n == NULL_NODE ? null : (Node) n;
        }

        public byte[] branchNodeGetValue() {
            parse();
            assert getType() == NodeType.BranchNode;
            Object n = children[16];
            if (n == null) {
                byte[] bytes = parsedRlp.getBytes(16);
                if (bytes.length == 0) {
                    n = NULL_NODE;
                } else {
                    n = bytes;
                }
                children[16] = n;
            }
            return n == NULL_NODE ? null : (byte[]) n;
        }

        public Key kvNodeGetKey() {
            parse();
            assert getType() == NodeType.KVNodeNode || getType() == NodeType.KVNodeValue;
            return (Key) children[0];
        }

        public Node kvNodeGetChildNode() {
            parse();
            assert getType() == NodeType.KVNodeNode;
            return (Node) children[1];
        }
        public byte[] kvNodeGetValue() {
            parse();
            assert getType() == NodeType.KVNodeValue;
            return (byte[]) children[1];
        }

        public NodeType getType() {
            parse();

            return children.length == 17 ? NodeType.BranchNode :
                    (children[1] instanceof Node ? NodeType.KVNodeNode : NodeType.KVNodeValue);
        }
    }

    Source<byte[], byte[]> cache;
    Node root;

    public TrieImpl1(Source<byte[], byte[]> cache, byte[] root) {
        this.cache = cache;
        setRoot(root);
    }

    public void setRoot(byte[] root) {
        this.root = root == null ? null : new Node(root);
    }

    private byte[] resolveHash(byte[] hash) {
        return cache.get(hash);
    }

    public byte[] get(byte[] key) {
        Key k = Key.fromNormal(key);
        return get(root, k);
    }

    public void put(byte[] key, byte[] value) {
        Key k = Key.fromNormal(key);
        root = insert(root, k, value);
    }

    private Node insert(Node n, Key k, byte[] value) {
        throw new RuntimeException("Not implemented yet");
//        NodeType type = n.getType();
//        if (type == NodeType.BranchNode) {
//            if (k.isEmpty()) return n.branchNodeGetValue();
//            Node childNode = n.branchNodeGetChild(k.getHex(0));
//            return get(childNode, k.shift(1));
//        } else {
//            Key k1 = k.matchAndShift(n.kvNodeGetKey());
//            if (k1 == null) return null;
//            if (type == NodeType.KVNodeValue) {
//                return k1.isEmpty() ? n.kvNodeGetValue() : null;
//            } else {
//                return get(n.kvNodeGetChildNode(), k1);
//            }
//        }
    }

    private byte[] get(Node n, Key k) {
        if (n == null) return null;

        NodeType type = n.getType();
        if (type == NodeType.BranchNode) {
            if (k.isEmpty()) return n.branchNodeGetValue();
            Node childNode = n.branchNodeGetChild(k.getHex(0));
            return get(childNode, k.shift(1));
        } else {
            Key k1 = k.matchAndShift(n.kvNodeGetKey());
            if (k1 == null) return null;
            if (type == NodeType.KVNodeValue) {
                return k1.isEmpty() ? n.kvNodeGetValue() : null;
            } else {
                return get(n.kvNodeGetChildNode(), k1);
            }
        }
    }

    @Override
    public byte[] getRootHash() {
        return root.hash;
    }

    @Override
    public void clear() {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public void delete(byte[] key) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public boolean flush() {
        return false;
    }
}
