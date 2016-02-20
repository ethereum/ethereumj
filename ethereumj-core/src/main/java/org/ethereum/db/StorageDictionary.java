package org.ethereum.db;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.ethereum.util.Utils;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.StorageDictionaryHandler;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static java.lang.Math.min;

/**
 * Keep the information for mapping of the contract storage hashKeys into a meaningful structures
 * representing storageIndexes, arrays indexes, offsets and mapping keys.
 * All the hashkeys used in the contract are organized into a tree structure according to their corresponding
 * structures in the source language.
 * Taking the following Solidity members for example:
 * <code>
 * int a0;
 * int a1[];
 * mapping(int => int[]) a2;
 * </code>
 * The execution of the code
 * <code>
 * a1[333] = 1;
 * a2['MyKey'][220] = 1;
 * a2['MyKey'][221] = 1;
 * a2['AnotherKey'][1] = 1;
 * </code>
 * would result in the following tree:
 * <pre>
 *     .1
 *       [333]
 *     .2
 *       ('MyKey')
 *         [220]
 *         [221]
 *       ('AnotherKey')
 *         [1]
 * </pre>
 * Each store entry contains storage hashkey which might be used to obtain the actual value from the contract storage
 *
 * The tree is stored compacted.
 * Compacted means that the elements which have children with only a single
 * child with type Offset and key '0' can be compacted: the meaningless
 * children are removed from the hierarchy.
 * E.g. the following subtree:
 * <pre>
 *     .1
 *       ('aaa')
 *          +0
 *            [0]
 *            [1]
 *       ('bbb)
 *          +0
 *            [0]
 * </pre>
 * would be compacted to
 * <pre>
 *     .1
 *       ('aaa')
 *         [0]
 *         [1]
 *       ('bbb)
 *         [0]
 * </pre>
 *
 * If it appears that the subtree shouldn't be compacted i.e. a new child appears
 * <pre>
 *     .1
 *       ('aaa')
 *          +1
 * </pre>
 * the subtree is 'decompacted' i.e. all '+0' children are returned back
 *
 * Created by Anton Nashatyrev on 09.09.2015.
 */
public class StorageDictionary {

    private static final int MAX_CHILDREN_TO_SORT = 100;
    private static final boolean SORT_MAP_KEYS = false;

    public enum Type {
        Root,
        StorageIndex,  // top-level Contract field index
        Offset,   // Either Offset in struct, or index in static array or both combined
        ArrayIndex,  // dynamic array index
        MapKey   // the key of the 'mapping'
    }

//    class ByteArraySerializer implements JsonSerializer<byte[]>

    /**
     * Class represents a tree element
     * All leaf elements represent the actual store slots and thus have a hashKey
     * Non-leaf elements (such as dynamic arrays) can also have a hashKey since e.g. the
     * array length (in Solidity) is stored in the slot which represents an array
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonAutoDetect(getterVisibility= JsonAutoDetect.Visibility.NONE,
            fieldVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE)
    public static class PathElement implements Comparable<PathElement> {

        StorageDictionary sd;

        @JsonProperty
        public Type type;
        @JsonProperty
        public String key;
        @JsonProperty
        public byte[] storageKey;

        // null means undefined yet
        // true means children were compacted
        // false means children were decompacted
        @JsonProperty
        public Boolean childrenCompacted = null;
        @JsonProperty
        public int childrenCount = 0;
        @JsonProperty
        public byte[] parentHash;
        @JsonProperty
        public byte[] nextSiblingHash;
        @JsonProperty
        public byte[] firstChildHash;
        @JsonProperty
        public byte[] lastChildHash;

        public PathElement() {
        }

        // using some 'random' hash for root since storageKey '0' is used
        private static final byte[] rootHash = Hex.decode("cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc");
        static PathElement createRoot() {
            PathElement root = new PathElement(Type.Root, 0, rootHash);
            return root;
        }

        public PathElement(Type type, int indexOffset, byte[] storageKey) {
            this.type = type;
            key = "" + indexOffset;
            this.storageKey = storageKey;
        }

        public PathElement(String key, byte[] storageKey) {
            type = Type.MapKey;
            this.key = key;
            this.storageKey = storageKey;
        }

        public PathElement getParent() {
            return sd.get(parentHash);
        }

        public PathElement getFirstChild() {
            return sd.get(firstChildHash);
        }

        private PathElement getLastChild() {
            return sd.get(lastChildHash);
        }

        public PathElement getNextSibling() {
            return sd.get(nextSiblingHash);
        }

        public PathElement addChild(PathElement newChild) {
            PathElement existingChild = sd.get(newChild.storageKey);
            if (existingChild != null && Arrays.equals(storageKey, existingChild.parentHash)) {
                return existingChild;
            }

            if (childrenCount > MAX_CHILDREN_TO_SORT || (!SORT_MAP_KEYS && newChild.type == Type.MapKey)) {
                // no more sorting just add to the end
                insertChild(getLastChild(), newChild);
            } else {
                Iterator<PathElement> chIt = getChildrenIterator();
                PathElement insertAfter = null;
                while(chIt.hasNext()) {
                    PathElement next = chIt.next();
                    if (newChild.compareTo(next) < 0) {
                        break;
                    }
                    insertAfter = next;
                }
                insertChild(insertAfter, newChild);
            }

            return newChild;
        }

        private boolean isMapping() {
            return getFirstChild().type == Type.MapKey;
        }

        public PathElement insertChild(PathElement insertAfter, PathElement newChild) {
            if (insertAfter == null) {
                // first element
                newChild.nextSiblingHash = firstChildHash;
                firstChildHash = newChild.storageKey;
                if (childrenCount == 0) {
                    lastChildHash = firstChildHash;
                }
            } else if (insertAfter.nextSiblingHash == null) {
                // last element
                insertAfter.nextSiblingHash = newChild.storageKey;
                insertAfter.invalidate();
                lastChildHash = newChild.storageKey;
            } else {
                insertAfter.nextSiblingHash = newChild.storageKey;
                insertAfter.invalidate();
            }

            newChild.parentHash = this.storageKey;
            sd.put(newChild);
            newChild.invalidate();
            childrenCount++;
            this.invalidate();

            return newChild;
        }

        public void addChildPath(PathElement[] pathElements) {
            if (pathElements.length == 0) return;

            boolean addCompacted;
            if (pathElements.length > 1 && pathElements[1].canBeCompactedWithParent()) {
                // this one particular path we are adding can be compacted
                if (childrenCompacted == Boolean.FALSE) {
                    addCompacted = false;
                } else {
                    childrenCompacted = Boolean.TRUE;
                    addCompacted = true;
                }
            } else {
                addCompacted = false;
                if (childrenCompacted == Boolean.TRUE) {
                    childrenCompacted = Boolean.FALSE;
                    // we already added compacted children - need to decompact them now
                    decompactAllChildren();
                } else {
                    childrenCompacted = Boolean.FALSE;
                }
            }

            if (addCompacted) {
                PathElement compacted = compactPath(pathElements[0], pathElements[1]);
                PathElement child = addChild(compacted);
                child.addChildPath(Arrays.copyOfRange(pathElements, 2, pathElements.length));
                sd.put(compacted);
            } else {
                PathElement child = addChild(pathElements[0]);
                child.addChildPath(Arrays.copyOfRange(pathElements, 1, pathElements.length));
            }
        }

        private PathElement compactPath(PathElement parent, PathElement child) {
            PathElement compacted = new PathElement();
            compacted.type = parent.type;
            compacted.key = parent.key;
            compacted.storageKey = child.storageKey;
            return compacted;
        }

        private void decompactAllChildren() {
            PathElement child = getFirstChild();
            removeAllChildren();
            while(child != null) {
                PathElement[] decoPath = decompactElement(child);
                addChildPath(decoPath);
                child = child.getNextSibling();
            }
        }

        private void removeAllChildren() {
            childrenCount = 0;
            firstChildHash = null;
            lastChildHash = null;
        }

        private PathElement[] decompactElement(PathElement pe) {
            PathElement parent = new PathElement();
            parent.type = pe.type;
            parent.key = pe.key;
            parent.storageKey = getVirtualStorageKey(pe.storageKey);
            sd.put(parent);
            PathElement child = new PathElement(Type.Offset, 0, pe.storageKey);
            child.childrenCount = pe.childrenCount;
            child.firstChildHash = pe.firstChildHash;
            child.lastChildHash = pe.lastChildHash;
            sd.put(child);
            return new PathElement[]{parent, child};
        }

        public static byte[] getVirtualStorageKey(byte[] childStorageKey) {
            BigInteger i = ByteUtil.bytesToBigInteger(childStorageKey).subtract(BigInteger.ONE);
            return ByteUtil.bigIntegerToBytes(i, 32);
        }

        private boolean canBeCompactedWithParent() {
            return type == Type.Offset && "0".equals(key);
        }

        public Iterator<PathElement> getChildrenIterator() {
            return new Iterator<PathElement>() {
                PathElement cur = getFirstChild();
                @Override
                public boolean hasNext() {
                    return cur != null;
                }

                @Override
                public PathElement next() {
                    PathElement ret = cur;
                    cur = cur.getNextSibling();
                    return ret;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        public byte[] getHash() {
            return storageKey;
        }

        private void invalidate() {
            sd.dirtyNodes.add(this);
        }

        public int getChildrenCount() {
            return childrenCount;
        }

        public String[] getFullPath() {
            if (type == Type.Root) return new String[0];
            return Utils.mergeArrays(getParent().getFullPath(), new String[]{key});
        }

        public String getContentType() {
            if (getChildrenCount() == 0) return "";
            if (getFirstChild().type == Type.MapKey) return "mapping";
            if (getFirstChild().type == Type.ArrayIndex) return "array";
            if (getFirstChild().type == Type.Offset) return "struct";
            return "";
        }

        @Override
        public int compareTo(PathElement o) {
            if (type != o.type) return type.compareTo(o.type);
            if (type == Type.Offset || type == Type.StorageIndex || type == Type.ArrayIndex) {
                try {
                    return new BigInteger(key, 16).compareTo(new BigInteger(o.key, 16));
                } catch (NumberFormatException e) {
                    // fallback to string compare
                }
            }
            return key.compareTo(o.key);
        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + (key != null ? key.hashCode() : 0);
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PathElement that = (PathElement) o;
            if (type != that.type) return false;
            return !(key != null ? !key.equals(that.key) : that.key != null);

        }

        private String shortHash(byte[] hash) {
            if (hash == null || hash.length == 0) return "";
            String s = Hex.toHexString(hash);
            return s.substring(0, min(8, s.length()));
        }

        public String dump() {
            return toString() +
                    "(storageKey=" + shortHash(storageKey) + ", " +
                    "childCount=" + childrenCount + ", " +
                    "childrenCompacted=" + childrenCompacted + ", " +
                    "parentHash=" + shortHash(parentHash) + ", " +
                    "firstChildHash=" + shortHash(firstChildHash) + ", " +
                    "lastChildHash=" + shortHash(lastChildHash) + ", " +
                    "nextSiblingHash=" + shortHash(nextSiblingHash) + ")";
        }

        @Override
        public String toString() {
            if (type == Type.Root) return "ROOT";
            if (type == Type.StorageIndex) return "." + key;
            if (type == Type.Offset) return "+" + key;
            if (type == Type.MapKey) return "('" + key + "')";
            return "[" + key + "]";
        }

        public String toString(ContractDetails storage, int indent) {
            String s =  (storageKey == null ? Utils.repeat(" ", 64) : Hex.toHexString(storageKey)) + " : " +
                    Utils.repeat("  ", indent) + this;
            if (storageKey != null && storage != null) {
                DataWord data = storage.get(new DataWord(storageKey));
                s += " = " + (data == null ? "<null>" : StorageDictionaryHandler.guessValue(data.getData()));
            }
            s += "\n";
            int limit = 50;
            if (getChildrenCount() > 0) {
                Iterator<PathElement> it = getChildrenIterator();
                while (it.hasNext()) {
                    PathElement child = it.next();
                    s += child.toString(storage, indent + 1);
                    if (limit-- <= 0) {
                        s += "\n             [Total: " + getChildrenCount() + " Rest skipped]\n";
                        break;
                    }
                }
            }
            return s;
        }

        byte[] encodeHash(byte[] hash) {
            return hash == null ? new byte[0] : hash;
        }

        public byte[] serialize() {
            return RLP.encodeList(
                    RLP.encodeInt(type.ordinal()),
                    RLP.encodeString(key),
                    RLP.encodeElement(encodeHash(storageKey)),
                    RLP.encodeElement(childrenCompacted == null ? new byte[0] : (childrenCompacted ? new byte[] {1} : new byte[] {0})),
                    RLP.encodeInt(childrenCount),
                    RLP.encodeElement(encodeHash(parentHash)),
                    RLP.encodeElement(encodeHash(nextSiblingHash)),
                    RLP.encodeElement(encodeHash(firstChildHash)),
                    RLP.encodeElement(encodeHash(lastChildHash))
                );
        }

        PathElement copyLight() {
            PathElement ret = new PathElement();
            ret.type = type;
            ret.key = key;
            ret.storageKey = storageKey;
            return ret;
        }
//        public byte[] serialize() {
//            try {
//                ObjectMapper om = new ObjectMapper();
//                return om.writeValueAsString(this).getBytes();
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
//        }
    }

    private PathElement get(byte[] hash) {
        if (hash == null) return null;
        PathElement ret = cache.get(new ByteArrayWrapper(hash));
        if (ret == null) {
            ret = load(hash);
            if (ret != null) {
                put(ret);
            }
        }
        return ret;
    }

    private void put(PathElement pe) {
        cache.put(new ByteArrayWrapper(pe.storageKey), pe);
        pe.sd = this;
    }

    public PathElement load(byte[] hash) {
        byte[] bytes = storageDb.get(hash);
        if (bytes == null) return null;
        PathElement ret = deserializePathElement(bytes);
        ret.sd = this;
        return ret;
    }

    public void store() {
        for (PathElement node : dirtyNodes) {
            storageDb.put(node.getHash(), node.serialize());
        }
        dirtyNodes.clear();
    }

    public StorageDictionary getFiltered(Set<DataWord> hashFilter) {
        HashMapDB filterSource = new HashMapDB();
        StorageDictionary ret = new StorageDictionary(filterSource);
        for (DataWord hash : hashFilter) {
            PathElement pathElement = get(hash.getData());
            ArrayList<PathElement> path = new ArrayList<>();
            while(pathElement.type != Type.Root) {
                path.add(0, pathElement.copyLight());
                pathElement = pathElement.getParent();
            }
            ret.addPath(path.toArray(new PathElement[0]));
        }
        return ret;
    }

    private KeyValueDataSource storageDb;
    private Map<ByteArrayWrapper, PathElement> cache = new HashMap<>();

    private List<PathElement> dirtyNodes = new ArrayList<>();

    private PathElement root;
    private boolean exist;

    public StorageDictionary(KeyValueDataSource storageDb) {
        this.storageDb = storageDb;
        root = load(PathElement.rootHash);
        if (root == null) {
            root = PathElement.createRoot();
            put(root);
            exist = false;
        } else {
            exist = true;
        }
    }

    public boolean isExist() {
        return exist;
    }

    public boolean hasChanges() {
        return !dirtyNodes.isEmpty();
    }

    public synchronized void addPath(PathElement[] path) {
        int startIdx = path.length - 1;
        PathElement existingPE = null;
        while(startIdx >= 0) {
            if ((existingPE = get(path[startIdx].getHash())) != null) {
                break;
            }
            startIdx--;
        }
        existingPE = startIdx >= 0 ? existingPE : root;
        startIdx++;
        existingPE.addChildPath(Arrays.copyOfRange(path, startIdx, path.length));
    }

    public String dump(ContractDetails storage) {
        return root.toString(storage, 0);
    }

    public String dump() {
        return dump(null);
    }

    byte[] decodeHash(byte[] bb) {
        return bb;
    }

    public PathElement deserializePathElement(byte[] bb) {
        PathElement ret = new PathElement();
        RLPList list = (RLPList) RLP.decode2(bb).get(0);
        ret.type = Type.values()[ByteUtil.byteArrayToInt(list.get(0).getRLPData())];
        ret.key = new String(list.get(1).getRLPData());
        ret.storageKey = decodeHash(list.get(2).getRLPData());
        byte[] compB = list.get(3).getRLPData();
        ret.childrenCompacted = compB == null ? null : (compB[0] == 0 ? Boolean.FALSE : Boolean.TRUE);
        ret.childrenCount = ByteUtil.byteArrayToInt(list.get(4).getRLPData());
        ret.parentHash = decodeHash(list.get(5).getRLPData());
        ret.nextSiblingHash = decodeHash(list.get(6).getRLPData());
        ret.firstChildHash= decodeHash(list.get(7).getRLPData());
        ret.lastChildHash= decodeHash(list.get(8).getRLPData());
        return ret;
    }

    public static byte[] toStorageKey(String hex) {
        return Hex.decode(Utils.align(hex, '0', 64, false));
    }

    public static void main(String[] args) throws Exception {
        HashMapDB db = new HashMapDB();
        StorageDictionary sd = new StorageDictionary(db);
        PathElement pe = new PathElement(Type.Offset, 1, Hex.decode("01020304"));
        pe.parentHash = Hex.decode("01020304");
        byte[] bytes = pe.serialize();
//        System.out.println(new String(bytes));
        PathElement pe1 = sd.deserializePathElement(bytes);
        System.out.println(pe1.dump());

        sd.addPath(new PathElement[]{
                new PathElement(Type.StorageIndex, 0, toStorageKey("00")),
                new PathElement("key1", PathElement.getVirtualStorageKey(toStorageKey("ababab"))),
                new PathElement(Type.Offset, 0, toStorageKey("ababab")),
                new PathElement(Type.ArrayIndex, 0, toStorageKey("eeee")),
        });
        sd.addPath(new PathElement[]{
                new PathElement(Type.StorageIndex, 0, toStorageKey("00")),
                new PathElement("key1", PathElement.getVirtualStorageKey(toStorageKey("ababab"))),
                new PathElement(Type.Offset, 0, toStorageKey("ababab")),
                new PathElement(Type.ArrayIndex, 1, toStorageKey("eee1")),
        });
        sd.store();
        System.out.println("======");
        System.out.println(sd.root.toString(null, 0));
        for (byte[] k : db.keys()) {
            PathElement p = sd.deserializePathElement(db.get(k));
            System.out.println(Hex.toHexString(k) + " => " + p.dump());
        }


        sd.addPath(new PathElement[]{
                new PathElement(Type.StorageIndex, 0, toStorageKey("00")),
                new PathElement("key2", PathElement.getVirtualStorageKey(toStorageKey("ababab2"))),
                new PathElement(Type.Offset, 0, toStorageKey("ababab2")),
                new PathElement(Type.ArrayIndex, 0, toStorageKey("eeed")),
        });
        sd.store();
        System.out.println("======");
        System.out.println(sd.root.toString(null, 0));
        for (byte[] k : db.keys()) {
            PathElement p = sd.deserializePathElement(db.get(k));
            System.out.println(Hex.toHexString(k) + " => " + p.dump());
        }


        sd.addPath(new PathElement[]{
                new PathElement(Type.StorageIndex, 0, toStorageKey("00")),
                new PathElement("key1", PathElement.getVirtualStorageKey(toStorageKey("ababab"))),
                new PathElement(Type.Offset, 1, toStorageKey("ababac")),
        });
        sd.store();
        System.out.println("======");
        System.out.println(sd.root.toString(null, 0));
        for (byte[] k : db.keys()) {
            PathElement p = sd.deserializePathElement(db.get(k));
            System.out.println(Hex.toHexString(k) + " => " + p.dump());
        }

        System.out.println(new StorageDictionary(db).root.toString(null, 0));
    }
}
