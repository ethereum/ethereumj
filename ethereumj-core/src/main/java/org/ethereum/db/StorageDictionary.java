package org.ethereum.db;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.util.Utils;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.StorageDictionaryHandler;
import org.spongycastle.util.encoders.Hex;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

import static java.lang.Math.max;
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
 * Created by Anton Nashatyrev on 09.09.2015.
 */
public class StorageDictionary {

    public enum Type {
        Root,
        StorageIndex,  // top-level Contract field index
        Offset,   // Either Offset in struct, or index in static array or both combined
        ArrayIndex,  // dynamic array index
        MapKey   // the key of the 'mapping'
    }

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

        @JsonProperty
        public Type type;
        @JsonProperty
        public String key;
        private DataWord hashKey;
        private SortedMap<PathElement, PathElement> children = new TreeMap<>();

        private transient boolean isValid = true;   // 'transient' just a marker here
        private transient PathElement parent = null;  // 'transient' just a marker here
        private transient Boolean canCompact = null;
        private transient List<PathElement> compactedChildren = null;

        public PathElement() {
        }

        public PathElement(Type type, int indexOffset) {
            this.type = type;
            key = "" + indexOffset;
        }

        public PathElement(String key) {
            type = Type.MapKey;
            this.key = key;
        }

        public void serialize(ObjectOutputStream oos) throws IOException {
            oos.writeObject(type);
            oos.writeObject(key);
            oos.writeObject(hashKey == null ? null : hashKey.getData());
            oos.writeInt(children.size());
            for (PathElement pathElement : children.values()) {
                pathElement.serialize(oos);
            }
        }

        public static PathElement deserialize(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            PathElement pe = new PathElement();
            pe.type = (Type) ois.readObject();
            pe.key = (String) ois.readObject();
            byte[] bb = (byte[]) ois.readObject();
            pe.hashKey = bb == null ? new DataWord(new byte[0]) : new DataWord(bb);
            int size = ois.readInt();
            for (int i = 0; i < size; i++) {
                PathElement child = deserialize(ois);
                pe.children.put(child, child);
            }
            return pe;
        }

        public void add(PathElement[] path, DataWord key) {
            if (path.length == 0) {
                if (this.hashKey != null) {
                    if (!this.hashKey.equals(key)) {
                        // throw new RuntimeException("Shouldn't happen: different keys");
                        // actually may happen to non-Solidity contracts, so just ignore
                    }
                } else {
                    this.hashKey = key;
                    invalidate();
                }
                return;
            }

            PathElement child = children.get(path[0]);
            if (child == null) {
//                if (children.size() >= 10000) {
//                    // TODO: for a while don't exceed storage threshold
//                    return;
//                }
                child = path[0];
                child.parent = this;
                children.put(child, child);

                invalidate();
            }
            child.add(Arrays.copyOfRange(path, 1, path.length), key);
        }

        private void invalidate() {
            isValid = false;
            canCompact = null;
            compactedChildren = null;
            if (type != Type.Root) {
                parent.invalidate();
            }
        }

        public void validate() {
            isValid = true;
            for (PathElement element : children.values()) {
                element.validate();
            }
        }

        public boolean isValid() {
            return isValid;
        }

        @JsonProperty
        public String getHashKey() {
            return hashKey == null ? null : Hex.toHexString(hashKey.getData());
        }

        @JsonProperty
        public void setHashKey(String hashKey) {
            this.hashKey = hashKey == null ? null : new DataWord(Hex.decode(hashKey));
        }

        @JsonProperty
        public List<PathElement> getChildren() {
            return children.isEmpty() ? null : new ArrayList<>(children.values());
        }

        /**
         * Create a filtered and compacted copy of the element and its successors.
         * Filtered means only the elements (and their predecessors) with hashKeys
         * specified by the 'filter' remain
         * Compacted means that the elements which have children with only a single
         * child with type Offset and key '0' can be compacted: the meaningful
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
         * The tree is not stored compacted since there is no guarantee that the
         * '+0' elements are not actually the structures (static arrays) which
         * other elements are just never assigned and will not be assigned in future.
         * I.e. in the previous sample the Solidity declaration can be both
         * 'mapping(int => int[])'  or 'mapping(int => int[2][])'
         * In the first case the compaction is appropriate while in the second
         * we shouldn't normally compact the tree (while it's ok for representation purpose)
         *
         * @param filter 'null' if not filter applied or a set of hashKeys to be left in
         *               the tree
         * @return  Compacted and filtered copy of the subtree
         */
        public PathElement copyCompactedFiltered(Set<DataWord> filter) {
            PathElement copy = new PathElement();
            copy.type = type;
            copy.key = key;
            copy.hashKey = hashKey == null ? null : hashKey.clone();
            copy.parent = parent;
            List<PathElement> children = getChildrenCompacted();
            List<PathElement> filteredChildren = new ArrayList<>();
            for (PathElement child : children) {
                PathElement pe = child.copyCompactedFiltered(filter);
                if (pe != null) filteredChildren.add(pe);
            }
            if (filteredChildren.isEmpty() && (hashKey == null ||
                    (filter != null && !filter.contains(hashKey)))) return null;

            copy.setChildren(filteredChildren);
            return copy;
        }

        public List<PathElement> getChildrenCompacted() {
            if (!canCompact()) return getChildren();
            if (compactedChildren == null) {
                compactedChildren = new ArrayList<>();
                for (PathElement child : children.values()) {
                    PathElement compChild = new PathElement();
                    PathElement grandChild = child.children.values().iterator().next();
                    compChild.type = child.type;
                    compChild.key = child.key;
                    compChild.hashKey = grandChild.hashKey;
                    compChild.children = new TreeMap<>();
                    for (PathElement ggCh : grandChild.children.keySet()) {
                        PathElement ggChCopy = ggCh.copyWithNewParent(compChild);
                        compChild.children.put(ggChCopy, ggChCopy);
                    }
                    compChild.parent = this;
                    compactedChildren.add(compChild);
                }
            }
            return compactedChildren;
        }

        private boolean canCompact() {
            if (canCompact == null) {
                canCompact = true;
                for (PathElement child : children.values()) {
                    if (child.children.size() != 1) {
                        canCompact = false;
                        break;
                    }

                    PathElement grandchild = child.children.values().iterator().next();
                    if (grandchild.type != Type.Offset || !"0".equals(grandchild.key)) {
                        canCompact = false;
                        break;
                    }
                }
            }
            return canCompact;
        }

        private PathElement copyWithNewParent(PathElement newParent) {
            PathElement ret = new PathElement();
            ret.type = type;
            ret.key = key;
            ret.hashKey = hashKey;
            ret.children = children;
            ret.isValid = isValid;
            ret.parent = newParent;
            return ret;
        }

        public List<PathElement> getChildren(int from, int maxLen) {
            List<PathElement> children = getChildren();
            return children.subList(min(from, children.size()), min(from + maxLen, children.size()));
        }

        @JsonProperty
        public void setChildren(List<PathElement> children) {
            for (PathElement child : children) {
                this.children.put(child, child);
            }
        }

        public int count() {
            int ret = 1;
            for (PathElement element : children.keySet()) {
                ret += element.count();
            }
            return ret;
        }

        public String[] getFullPath() {
            if (type == Type.Root) return new String[0];
            return Utils.mergeArrays(parent.getFullPath(), new String[] {key});
        }

        public String getContentType() {
            List<PathElement> children = getChildren();
            if (children == null || children.size() == 0) return "";
            if (children.get(0).type == Type.MapKey) return "mapping";
            if (children.get(0).type == Type.ArrayIndex) return "array";
            if (children.get(0).type == Type.Offset) return "struct";
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

        @Override
        public String toString() {
            if (type == Type.Root) return "ROOT";
            if (type == Type.StorageIndex) return "." + key;
            if (type == Type.Offset) return "+" + key;
            if (type == Type.MapKey) return "('" + key + "')";
            return "[" + key + "]";
        }

        public String toString(ContractDetails storage, int indent) {
            String s =  (hashKey == null ? Utils.repeat(" ", 64) : hashKey) + " : " +
                    Utils.repeat("  ", indent) + this;
            if (hashKey != null && storage != null) {
                DataWord data = storage.get(hashKey);
                s += " = " + (data == null ? "<null>" : StorageDictionaryHandler.guessValue(data.getData()));
            }
            s += "\n";
            int limit = 50;
            List<PathElement> list = getChildren();
            if (list != null) {
                for (PathElement child : list) {
                    s += child.toString(storage, indent + 1);
                    if (limit-- <= 0) {
                        s += "\n             [Total: " + children.size() + " Rest skipped]\n";
                        break;
                    }
                }
            }
            return s;
        }
    }

    KeyValueDataSource storageDb;

    PathElement root = new PathElement(Type.Root, 0);

    public StorageDictionary() {}

    public StorageDictionary(PathElement root) {
        this.root = root;
    }

    public synchronized void addPath(DataWord hashKey, PathElement[] path) {
        root.add(path, hashKey);
    }

    /**
     * Returns the tree element by its keys path
     */
    public synchronized PathElement getByPath(String ... path) {
        PathElement ret = root;
        for (String pe : path) {
            PathElement prev = ret;
            for (PathElement c : ret.getChildren()) {
                if (pe.equals(c.key)) {
                    ret = c;
                    break;
                }
            }
            if (prev == ret) return null;
        }
        return ret;
    }

    /**
     * Creates compacted and filtered copy of the dictionary
     * (see {@link org.ethereum.db.StorageDictionary.PathElement#compactAndFilter(Set)} for details)
     */
    public synchronized StorageDictionary compactAndFilter(Set<DataWord> hashFilter) {
        return new StorageDictionary(root.copyCompactedFiltered(hashFilter));
    }

    public String dump(ContractDetails storage) {
        return root.toString(storage, 0);
    }

    public String dump() {
        return dump(null);
    }

    public void validate() {
        root.validate();
    }

    public boolean isValid() {
        return root.isValid();
    }

    public synchronized String serializeToJson() throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        return om.writeValueAsString(root);
    }

    public static StorageDictionary deserializeFromJson(InputStream json) throws IOException {
        ObjectMapper om = new ObjectMapper();
        StorageDictionary.PathElement root = om.readValue(json, StorageDictionary.PathElement.class);
        installRoots(root);
        return new StorageDictionary(root);
    }

    public static StorageDictionary deserializeFromJson(String json) throws IOException {
        ObjectMapper om = new ObjectMapper();
        StorageDictionary.PathElement root = om.readValue(json, StorageDictionary.PathElement.class);
        installRoots(root);
        return new StorageDictionary(root);
    }

    public synchronized byte[] serialize() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            root.serialize(oos);
            oos.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static StorageDictionary deserialize(byte[] bb) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bb);
            ObjectInputStream ois = new ObjectInputStream(bais);
            PathElement ret = PathElement.deserialize(ois);
            ois.close();
            installRoots(ret);
            return new StorageDictionary(ret);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void installRoots(PathElement el) {
        for (PathElement element : el.children.values()) {
            element.parent = el;
            installRoots(element);
        }
    }
}
