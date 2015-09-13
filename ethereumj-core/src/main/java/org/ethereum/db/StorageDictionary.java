package org.ethereum.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ethereum.net.swarm.Util;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.StorageDictionaryHandler;
import org.spongycastle.util.encoders.Hex;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

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
        StorageIndex,
        Offset,//  the same as ArrayIndex,
        MapKey
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PathElement implements Comparable<PathElement> {

        public Type type;
        public String key;

        private DataWord hashKey;
        private final SortedMap<PathElement, PathElement> children = new TreeMap<>();
        private transient boolean isValid = true;
        private transient PathElement parent = null;

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

        public void add(PathElement[] path, DataWord key) {
            if (path.length == 0) {
                if (this.hashKey != null) {
                    if (!this.hashKey.equals(key)) {
                        throw new RuntimeException("Shouldn't happen: different keys");
                    }
                } else {
                    this.hashKey = key;
                    invalidate();
                }
                return;
            }

            PathElement child = children.get(path[0]);
            if (child == null) {
                if (children.size() >= 2000) {
                    // TODO: for a while don't exceed storage threshold
                    return;
                }
                child = path[0];
                child.parent = this;
                children.put(child, child);

                invalidate();
            }
            child.add(Arrays.copyOfRange(path, 1, path.length), key);
        }

        private void invalidate() {
            isValid = false;
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

        @JsonIgnore
        public boolean isValid() {
            return isValid;
        }

        public String getHashKey() {
            return hashKey == null ? null : Hex.toHexString(hashKey.getData());
        }

        public void setHashKey(String hashKey) {
            this.hashKey = hashKey == null ? null : new DataWord(Hex.decode(hashKey));
        }

        public List<PathElement> getChildren() {
            return children.isEmpty() ? null : new ArrayList<>(children.values());
        }

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

        @Override
        public int compareTo(PathElement o) {
            if (type != o.type) return type.compareTo(o.type);
            if (type == Type.Offset || type == Type.StorageIndex) {
                return new BigInteger(key, 16).compareTo(new BigInteger(o.key, 16));
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
            if (type == Type.MapKey) return "('" + key + "')";
            return "[" + key + "]";
        }

        public String toString(ContractDetails storage, int indent) {
            String s =  (hashKey == null ? Util.repeat(" ", 64) : hashKey) + " : " +
                    Util.repeat(" ", indent) + this;
            if (hashKey != null && storage != null) {
                DataWord data = storage.get(hashKey);
                s += " = " + (data == null ? "<null>" : StorageDictionaryHandler.guessValue(data.getData()));
            }
            s += "\n";
            for (PathElement child : children.values()) {
                s += child.toString(storage, indent + 2);
            }
            return s;
        }
    }

    PathElement root = new PathElement(Type.Root, 0);

    public StorageDictionary() {}

    public StorageDictionary(PathElement root) {
        this.root = root;
    }

    public void addPath(DataWord hashKey, PathElement[] path) {
        root.add(path, hashKey);
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

    public String serializeToJson() throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        return om.writeValueAsString(root);
    }

    public static StorageDictionary deserializeFromJson(String json) throws IOException {
        ObjectMapper om = new ObjectMapper();
        StorageDictionary.PathElement root = om.readValue(json, StorageDictionary.PathElement.class);
        installRoots(root);
        return new StorageDictionary(root);
    }

    private static void installRoots(PathElement el) {
        for (PathElement element : el.children.values()) {
            element.parent = el;
            installRoots(element);
        }
    }
}
