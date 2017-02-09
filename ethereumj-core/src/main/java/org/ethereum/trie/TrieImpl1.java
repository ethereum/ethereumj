package org.ethereum.trie;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.Source;
import org.ethereum.util.RLP;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.apache.commons.lang3.concurrent.ConcurrentUtils.constantFuture;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.ethereum.util.RLP.EMPTY_ELEMENT_RLP;
import static org.ethereum.util.RLP.encodeElement;
import static org.ethereum.util.RLP.encodeList;

/**
 * Created by Anton Nashatyrev on 07.02.2017.
 */
public class TrieImpl1 implements Trie<byte[]> {

    public static final class Key {
        public static final int ODD_OFFSET_FLAG = 0x1;
        public static final int TERMINATOR_FLAG = 0x2;
        private final byte[] key;
        private final int off;
        private final boolean terminal;

        public static Key fromNormal(byte[] key) {
            return new Key(key);
        }

        public static Key fromPacked(byte[] key) {
            return new Key(key, ((key[0] >> 4) & ODD_OFFSET_FLAG) != 0 ? 1 : 2, ((key[0] >> 4) & TERMINATOR_FLAG) != 0);
        }

        public Key(byte[] key, int off, boolean terminal) {
            this.terminal = terminal;
            this.off = off;
            this.key = key;
        }

        private Key(byte[] key) {
            this(key, 0, true);
        }

        public byte[] toPacked() {
            int flags = ((off & 1) != 0 ? ODD_OFFSET_FLAG : 0) | (terminal ? TERMINATOR_FLAG : 0);
            byte[] ret = new byte[getLength() / 2 + 1];
            int toCopy = (flags & ODD_OFFSET_FLAG) != 0 ? ret.length : ret.length - 1;
            System.arraycopy(key, key.length - toCopy, ret, ret.length - toCopy, toCopy);
            ret[0] &= 0x0F;
            ret[0] |= flags << 4;
            return ret;
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

        public Key getCommonPrefix(Key k) {
            // TODO can be optimized
            int prefixLen = 0;
            int thisLenght = getLength();
            int kLength = k.getLength();
            while(prefixLen < thisLenght && prefixLen < kLength && getHex(prefixLen) == k.getHex(prefixLen)) prefixLen++;
            byte[] prefixKey = new byte[(prefixLen + 1) >> 1];
            Key ret = new Key(prefixKey, (prefixLen & 1) == 0 ? 0 : 1,
                    prefixLen == getLength() && prefixLen == k.getLength() && terminal && k.isTerminal());
            for (int i = 0; i < prefixLen; i++) {
                ret.setHex(i, k.getHex(i));
            }
            return ret;
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

        private void setHex(int idx, int hex) {
            int byteIdx = (off + idx) >> 1;
            if (((off + idx) & 1) == 0) {
                key[byteIdx] &= 0x0F;
                key[byteIdx] |= hex << 4;
            } else {
                key[byteIdx] &= 0xF0;
                key[byteIdx] |= hex;
            }
        }

        public int getHex(int idx) {
            byte b = key[(off + idx) >> 1];
            return (((off + idx) & 1) == 0 ? (b >> 4) : b) & 0xF;
        }

        @Override
        public boolean equals(Object obj) {
            Key k = (Key) obj;
            int len = getLength();

            if (len != k.getLength()) return false;
            // TODO can be optimized
            for (int i = 0; i < len; i++) {
                if (getHex(i) != k.getHex(i)) return false;
            }
            return isTerminal() == k.isTerminal();
        }

        @Override
        public String toString() {
            return Hex.toHexString(key).substring(off) + (isTerminal() ? "T" : "");
        }
    }

    public enum NodeType {
        BranchNode,
        KVNodeValue,
        KVNodeNode
    }

    private final static Object NULL_NODE = new Object();
    private static ExecutorService executor;

    public static ExecutorService getExecutor() {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(8,
                    new ThreadFactoryBuilder().setNameFormat("trie-calc-thread-%d").build());
        }
        return executor;
    }

    public final class Node {
        private byte[] hash = null;
        private byte[] rlp = null;
        private RLP.LList parsedRlp = null;
        private boolean dirty = false;

        private Object[] children = null;

        // new empty BranchNode
        public Node() {
            children = new Object[17];
            dirty = true;
        }

        // new KVNode with key and (value or node)
        public Node(Key key, Object valueOrNode) {
            this(new Object[]{key, valueOrNode});
            dirty = true;
        }

        // new Node with hash or RLP
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

        private Node(Object[] children) {
            this.children = children;
        }

        private void resolve() {
            if (rlp != null || parsedRlp != null) return;
            rlp = getHash(hash);
            if (rlp == null) {
                throw new RuntimeException("Invalid Trie state, can't resolve hash " + Hex.toHexString(hash));
            }
        }

        public byte[] encode() {
            return encode(1, true);
        }
        private byte[] encode(final int depth, boolean forceHash) {
            if (!dirty) {
                return hash != null ? encodeElement(hash) : rlp; // TODO parsedRLP
            } else {
                NodeType type = getType();
                byte[] ret;
                if (type == NodeType.BranchNode) {
                    if (depth == 2 && async) {
                        // on depth 2 from one side there is a big chance to find a number of updated
                        // trie branches to parallelize
                        // from the other side there are not too many branches
                        final Future[] encoded = new Future[17];
                        for (int i = 0; i < 16; i++) {
                            final Node child = branchNodeGetChild(i);
                            encoded[i] = getExecutor().submit(new Callable<byte[]>() {
                                @Override
                                public byte[] call() throws Exception {
                                    return child == null ? EMPTY_ELEMENT_RLP : child.encode(depth + 1, false);
                                }
                            });
                        }
                        byte[] value = branchNodeGetValue();
                        encoded[16] = constantFuture(encodeElement(value));
                        try {
                            ret = encodeRlpListFutures(encoded);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        byte[][] encoded = new byte[17][];
                        for (int i = 0; i < 16; i++) {
                            Node child = branchNodeGetChild(i);
                            encoded[i] = child == null ? EMPTY_ELEMENT_RLP : child.encode(depth + 1, false);
                        }
                        byte[] value = branchNodeGetValue();
                        encoded[16] = encodeElement(value);
                        ret = encodeList(encoded);
                    }
                } else if (type == NodeType.KVNodeNode) {
                    ret = encodeList(encodeElement(kvNodeGetKey().toPacked()), kvNodeGetChildNode().encode(depth + 1, false));
                } else {
                    byte[] value = kvNodeGetValue();
                    ret = encodeList(encodeElement(kvNodeGetKey().toPacked()),
                                    encodeElement(value == null ? EMPTY_BYTE_ARRAY : value));
                }
                if (hash != null) {
                    deleteHash(hash);
                }
                dirty = false;
                if (ret.length < 32 && !forceHash) {
                    rlp = ret;
                    return ret;
                } else {
                    hash = HashUtil.sha3(ret);
                    addHash(hash, ret);
                    return encodeElement(hash);
                }
            }
        }

        private byte[] encodeRlpListFutures(Future<byte[]> ... list) throws ExecutionException, InterruptedException {
            byte[][] vals = new byte[list.length][];
            for (int i = 0; i < list.length; i++) {
                vals[i] = list[i].get();
            }
            return encodeList(vals);
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
            if (n == null && parsedRlp != null) {
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

        public Node branchNodeSetChild(int hex, Node node) {
            parse();
            assert getType() == NodeType.BranchNode;
            children[hex] = node;
            dirty = true;
            return this;
        }

        public byte[] branchNodeGetValue() {
            parse();
            assert getType() == NodeType.BranchNode;
            Object n = children[16];
            if (n == null && parsedRlp != null) {
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

        public Node branchNodeSetValue(byte[] val) {
            parse();
            assert getType() == NodeType.BranchNode;
            children[16] = val;
            dirty = true;
            return this;
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
        public Node kvNodeSetValue(byte[] value) {
            parse();
            assert getType() == NodeType.KVNodeValue;
            children[1] = value;
            dirty = true;
            return this;
        }

        public Object kvNodeGetValueOrNode() {
            parse();
            assert getType() != NodeType.BranchNode;
            return children[1];
        }

        public Node kvNodeSetValueOrNode(Object valueOrNode) {
            parse();
            assert getType() != NodeType.BranchNode;
            children[1] = valueOrNode;
            dirty = true;
            return this;
        }

        public NodeType getType() {
            parse();

            return children.length == 17 ? NodeType.BranchNode :
                    (children[1] instanceof Node ? NodeType.KVNodeNode : NodeType.KVNodeValue);
        }

        /***********  Dump methods  ************/

        public String dumpStruct(String indent, String prefix) {
            String ret = indent + prefix + getType() + (dirty ? " *" : "") +
                    (hash == null ? "" : "(hash: " + Hex.toHexString(hash).substring(0, 6) + ")");
            if (getType() == NodeType.BranchNode) {
                byte[] value = branchNodeGetValue();
                ret += (value == null ? "" : " [T] = " + Hex.toHexString(value)) + "\n";
                for (int i = 0; i < 16; i++) {
                    Node child = branchNodeGetChild(i);
                    if (child != null) {
                        ret += child.dumpStruct(indent + "  ", "[" + i + "] ");
                    }
                }

            } else if (getType() == NodeType.KVNodeNode) {
                ret += " [" + kvNodeGetKey() + "]\n";
                ret += kvNodeGetChildNode().dumpStruct(indent + "  ", "");
            } else {
                ret += " [" + kvNodeGetKey() + "] = " + Hex.toHexString(kvNodeGetValue()) + "\n";
            }
            return ret;
        }

        public List<String> dumpTrieNode(boolean compact) {
            List<String> ret = new ArrayList<>();
            if (hash != null) {
                ret.add(hash2str(hash, compact) + " ==> " + dumpContent(false, compact));
            }

            if (getType() == NodeType.BranchNode) {
                for (int i = 0; i < 16; i++) {
                    Node child = branchNodeGetChild(i);
                    if (child != null) ret.addAll(child.dumpTrieNode(compact));
                }
            } else if (getType() == NodeType.KVNodeNode) {
                ret.addAll(kvNodeGetChildNode().dumpTrieNode(compact));
            }
            return ret;
        }

        private String dumpContent(boolean recursion, boolean compact) {
            if (recursion && hash != null) return hash2str(hash, compact);
            String ret;
            if (getType() == NodeType.BranchNode) {
                ret = "[";
                for (int i = 0; i < 16; i++) {
                    Node child = branchNodeGetChild(i);
                    ret += i == 0 ? "" : ",";
                    ret += child == null ? "" : child.dumpContent(true, compact);
                }
                byte[] value = branchNodeGetValue();
                ret += value == null ? "" : ", " + val2str(value, compact);
                ret += "]";
            } else if (getType() == NodeType.KVNodeNode) {
                ret = "[<" + kvNodeGetKey() + ">, " + kvNodeGetChildNode().dumpContent(true, compact) + "]";
            } else {
                ret = "[<" + kvNodeGetKey() + ">, " + val2str(kvNodeGetValue(), compact) + "]";
            }
            return ret;
        }

        @Override
        public String toString() {
            return getType() + (dirty ? " *" : "") + (hash == null ? "" : "(hash: " + Hex.toHexString(hash) + " )");
        }
    }

    Source<byte[], byte[]> cache;
    Node root;
    boolean async;

    public TrieImpl1(Source<byte[], byte[]> cache, byte[] root) {
        this.cache = cache;
        setRoot(root);
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    private void encode() {
        if (root != null) {
            root.encode();
        }
    }

    public void setRoot(byte[] root) {
        this.root = root == null ? null : new Node(root);
    }

    private byte[] getHash(byte[] hash) {
        return cache.get(hash);
    }
    private void addHash(byte[] hash, byte[] ret) {
        cache.put(hash, ret);
    }
    private void deleteHash(byte[] hash) {
        cache.delete(hash);
    }


    public byte[] get(byte[] key) {
        Key k = Key.fromNormal(key);
        return get(root, k);
    }

    public void put(byte[] key, byte[] value) {
        Key k = Key.fromNormal(key);
        if (root == null) {
            root = new Node(k, value);
        } else {
            root = insert(root, k, value);
        }
    }

    private Node insert(Node n, Key k, Object nodeOrValue) {
        NodeType type = n.getType();
        if (type == NodeType.BranchNode) {
            if (k.isEmpty()) return n.branchNodeSetValue((byte[]) nodeOrValue);
            Node childNode = n.branchNodeGetChild(k.getHex(0));
            if (childNode != null) {
                return n.branchNodeSetChild(k.getHex(0), insert(childNode, k.shift(1), nodeOrValue));
            } else {
                Key childKey = k.shift(1);
                Node newChildNode;
                if (!childKey.isEmpty()) {
                    newChildNode = new Node(childKey, nodeOrValue);
                } else {
                    newChildNode = nodeOrValue instanceof Node ?
                            (Node) nodeOrValue : new Node(childKey, nodeOrValue);
                }
                return n.branchNodeSetChild(k.getHex(0), newChildNode);
            }
        } else {
            Key commonPrefix = k.getCommonPrefix(n.kvNodeGetKey());
            if (commonPrefix.isEmpty()) {
                Node newBranchNode = new Node();
                insert(newBranchNode, n.kvNodeGetKey(), n.kvNodeGetValueOrNode());
                insert(newBranchNode, k, nodeOrValue);
                return newBranchNode;
            } else if (commonPrefix.equals(k)) {
                return n.kvNodeSetValueOrNode(nodeOrValue);
            } else if (commonPrefix.equals(n.kvNodeGetKey())) {
                insert(n.kvNodeGetChildNode(), k.shift(commonPrefix.getLength()), nodeOrValue);
                return n;
            } else {
                Node newBranchNode = new Node();
                Node newKvNode = new Node(commonPrefix, newBranchNode);
                // TODO can be optimized
                insert(newKvNode, n.kvNodeGetKey(), n.kvNodeGetValueOrNode());
                insert(newKvNode, k, nodeOrValue);
                return newKvNode;
            }
        }
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
        encode();
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

    public String dumpStructure() {
        return root == null ? "<empty>" : root.dumpStruct("", "");
    }
    public String dumpTrie() {
        return dumpTrie(true);
    }
    public String dumpTrie(boolean compact) {
        if (root == null) return "<empty>";
        encode();
        StrBuilder ret = new StrBuilder();
        List<String> strings = root.dumpTrieNode(compact);
        ret.append("Root: " + hash2str(getRootHash(), compact) + "\n");
        for (String s : strings) {
            ret.append(s).append('\n');
        }
        return ret.toString();
    }

    private static String hash2str(byte[] hash, boolean shortHash) {
        String ret = Hex.toHexString(hash);
        return "0x" + (shortHash ? ret.substring(0,8) : ret);
    }

    private static String val2str(byte[] val, boolean shortHash) {
        String ret = Hex.toHexString(val);
        if (val.length > 16) {
            ret = ret.substring(0,10) + "... len " + val.length;
        }
        return "\"" + ret + "\"";
    }
}
