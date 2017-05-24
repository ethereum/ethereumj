/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.net.swarm;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Hierarchical structure of path items
 * The item can be one of two kinds:
 * - the leaf item which contains reference to the actual data with its content type
 * - the non-leaf item which contains reference to the child manifest with the dedicated content type
 */
public class Manifest {
    public enum Status {
        OK(200),
        NOT_FOUND(404);

        private int code;
        Status(int code) {
            this.code = code;
        }
    }

    // used for Json [de]serialization only
    private static class ManifestRoot {
        public List<ManifestEntry> entries = new ArrayList<>();

        public ManifestRoot() {
        }

        public ManifestRoot(List<ManifestEntry> entries) {
            this.entries = entries;
        }

        public ManifestRoot(ManifestEntry parent) {
            entries.addAll(parent.getChildren());
        }
    }

    /**
     *  Manifest item
     */
    @JsonAutoDetect(getterVisibility= JsonAutoDetect.Visibility.NONE,
                    fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
                    isGetterVisibility = JsonAutoDetect.Visibility.NONE)
    public static class ManifestEntry extends StringTrie.TrieNode<ManifestEntry> {
        public String hash;
        public String contentType;
        public Status status;

        private Manifest thisMF;

        public ManifestEntry() {
            super(null, "");
        }

        public ManifestEntry(String path, String hash, String contentType, Status status) {
            super(null, "");
            this.path = path;
            this.hash = hash;
            this.contentType = contentType;
            this.status = status;
        }

        ManifestEntry(ManifestEntry parent, String path) {
            super(parent, path);
            this.path = path;
        }

        /**
         *  Indicates if this entry contains reference to a child manifest
         */
        public boolean isManifestType() { return MANIFEST_MIME_TYPE.equals(contentType);}
        boolean isValid() {return hash != null;}
        void invalidate() {hash = null;}

        @Override
        public boolean isLeaf() {
            return !(isManifestType() || !children.isEmpty());
        }

        /**
         *  loads the child manifest
         */
        @Override
        protected Map<String, ManifestEntry> loadChildren() {
            if (isManifestType() && children.isEmpty() && isValid()) {
                ManifestRoot manifestRoot = load(thisMF.dpa, hash);
                children = new HashMap<>();
                for (Manifest.ManifestEntry entry : manifestRoot.entries) {
                    children.put(getKey(entry.path), entry);
                }
            }
            return children;
        }

        @JsonProperty
        public String getPath() {
            return path;
        }

        @JsonProperty
        public void setPath(String path) {
            this.path = path;
        }

        @Override
        protected ManifestEntry createNode(ManifestEntry parent, String path) {
            return new ManifestEntry(parent, path).setThisMF(parent.thisMF);
        }

        @Override
        protected void nodeChanged() {
            if (!isLeaf()) {
                contentType = MANIFEST_MIME_TYPE;
                invalidate();
            }
        }

        ManifestEntry setThisMF(Manifest thisMF) {
            this.thisMF = thisMF;
            return this;
        }

        @Override
        public String toString() {
            return "ManifestEntry{" +
                    "path='" + path + '\'' +
                    ", hash='" + hash + '\'' +
                    ", contentType='" + contentType + '\'' +
                    ", status=" + status +
                    '}';
        }
    }

    public final static String MANIFEST_MIME_TYPE = "application/bzz-manifest+json";

    private DPA dpa;
    private final StringTrie<ManifestEntry> trie;

    /**
     * Constructs the Manifest instance with backing DPA storage
     * @param dpa DPA
     */
    public Manifest(DPA dpa) {
        this(dpa, new ManifestEntry(null, ""));
    }

    private Manifest(DPA dpa, ManifestEntry root) {
        this.dpa = dpa;
        trie = new StringTrie<ManifestEntry>(root.setThisMF(this)) {};
    }

    /**
     * Retrieves the entry with the specified path loading necessary nested manifests on demand
     */
    public ManifestEntry get(String path) {
        return trie.get(path);
    }

    /**
     * Adds a new entry to the manifest hierarchy with loading necessary nested manifests on demand.
     * The entry path should contain the absolute path relative to this manifest root
     */
    public void add(ManifestEntry entry) {
        add(null, entry);
    }

    void add(ManifestEntry parent, ManifestEntry entry) {
        ManifestEntry added = parent == null ? trie.add(entry.path) : trie.add(parent, entry.path);
        added.hash = entry.hash;
        added.contentType = entry.contentType;
        added.status = entry.status;
    }

    /**
     * Deletes the leaf manifest entry with the specified path
     */
    public void delete(String path) {
        trie.delete(path);
    }

    /**
     * Loads the manifest with the specified hashKey from the DPA storage
     */
    public static Manifest loadManifest(DPA dpa, String hashKey) {
        ManifestRoot manifestRoot = load(dpa, hashKey);

        Manifest ret = new Manifest(dpa);
        for (Manifest.ManifestEntry entry : manifestRoot.entries) {
            ret.add(entry);
        }
        return ret;
    }

    private static Manifest.ManifestRoot load(DPA dpa, String hashKey) {
        try {
            SectionReader sr = dpa.retrieve(new Key(hashKey));
            ObjectMapper om = new ObjectMapper();
            String s = Util.readerToString(sr);
            ManifestRoot manifestRoot = om.readValue(s, ManifestRoot.class);
            return manifestRoot;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves this manifest (all its modified nodes) to this manifest DPA storage
     * @return hashKey of the saved Manifest
     */
    public String save() {
        return save(trie.rootNode);
    }

    private String save(ManifestEntry e) {
        if (e.isValid()) return e.hash;
        for (ManifestEntry c : e.getChildren()) {
            save(c);
        }
        e.hash = serialize(dpa, e);
        return e.hash;
    }


    private String serialize(DPA dpa, ManifestEntry manifest) {
        try {
            ObjectMapper om = new ObjectMapper();

            ManifestRoot mr = new ManifestRoot(manifest);
            String s = om.writeValueAsString(mr);

            String hash = dpa.store(Util.stringToReader(s)).getHexString();
            return hash;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return  manifest dump for debug purposes
     */
    public String dump() {
        return Util.dumpTree(trie.rootNode);
    }
}
