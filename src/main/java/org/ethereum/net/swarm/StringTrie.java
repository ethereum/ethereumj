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

import java.util.*;

/**
 * Trie (or prefix tree) structure, which stores Nodes with String keys in highly branched structure
 * for quick search by string prefixes.
 * E.g. if there is a single node 'aaa' and the node 'aab' is added then the tree will look like:
 *      aa
 *     /  \
 *    a    b
 * Where leaf nodes correspond to added elements
 *
 * @param <P> Subclass of TrieNode
 */
public abstract class StringTrie<P extends StringTrie.TrieNode<P>> {

    /**
     * Tree Node
     */
    public static abstract class TrieNode<N extends TrieNode<N>> {
        N parent;
        Map<String, N> children = new LinkedHashMap<>();
        String path; // path relative to parent

        /**
         * Create a root node (null parent and empty path)
         */
        protected TrieNode() {
            this(null, "");
        }

        public TrieNode(N parent, String relPath) {
            this.parent = parent;
            this.path = relPath;
        }

        public String getRelativePath() {
            return path;
        }

        /**
         * Calculates absolute path relative to the root node
         */
        public String getAbsolutePath() {
            return (parent != null ? parent.getAbsolutePath() : "") + path;
        }

        public N getParent() {
            return parent;
        }

        /**
         *  Finds the descendant which has longest common path prefix with the passed path
         */
        N getMostSuitableChild(String relPath) {
            N n = loadChildren().get(getKey(relPath));
            if (n == null) return (N) this;
            if (relPath.startsWith(n.getRelativePath())) {
                return n.getMostSuitableChild(relPath.substring(n.getRelativePath().length()));
            } else {
                return (N) this;
            }
        }

        /**
         * @return the direct child which has the key the same as the relPath key
         * null if no such child
         */
        N getChild(String relPath) {
            return loadChildren().get(getKey(relPath));
        }

        /**
         * Add a new descendant with specified path
         * @param relPath the path relative to this node
         * @return added node wich path is [relPath] relative to this node
         */
        N add(String relPath) {
            return addChild(relPath);
        }

        N addChild(String relPath) {
            N child = getChild(relPath);
            if (child == null) {
                N newNode = createNode((N) this, relPath);
                putChild(newNode);
                return  newNode;
            } else {
                if (!child.isLeaf() && relPath.startsWith(child.getRelativePath())) {
                    return child.addChild(relPath.substring(child.getRelativePath().length()));
                }
                if (child.isLeaf() && relPath.equals(child.getRelativePath())) {
                    return child;
                }
                String commonPrefix = Util.getCommonPrefix(relPath, child.getRelativePath());
                N newSubRoot = createNode((N) this, commonPrefix);
                child.path = child.path.substring(commonPrefix.length());
                child.parent = newSubRoot;
                N newNode = createNode(newSubRoot, relPath.substring(commonPrefix.length()));

                newSubRoot.putChild(child);
                newSubRoot.putChild(newNode);
                this.putChild(newSubRoot);

                newSubRoot.nodeChanged();
                this.nodeChanged();
                child.nodeChanged();

                return newNode;
            }
        }

        /**
         * Deletes current leaf node, rebalancing the tree if needed
         * @throws RuntimeException if this node is not leaf
         */
        void delete() {
            if (!isLeaf()) throw new RuntimeException("Can't delete non-leaf entry: " + this);
            N parent = getParent();
            parent.loadChildren().remove(getKey(getRelativePath()));
            if (parent.loadChildren().size() == 1 && parent.parent != null) {
                Map<String, N> c = parent.loadChildren();
                N singleChild = c.values().iterator().next();
                singleChild.path = parent.path + singleChild.path;
                singleChild.parent = parent.parent;
                parent.parent.loadChildren().remove(getKey(parent.path));
                parent.parent.putChild(singleChild);
                parent.parent.nodeChanged();
                singleChild.nodeChanged();
            }
        }

        void putChild(N n) {
            loadChildren().put(getKey(n.path), n);
        }

        /**
         * Returns the children if any. Doesn't cause any lazy loading
         */
        public Collection<N> getChildren() {
            return children.values();
        }

        /**
         * Returns the children after loading (if required)
         */
        protected Map<String, N> loadChildren() {
            return children;
        }

        public boolean isLeaf() {
            return loadChildren().isEmpty();
        }

        /**
         * Calculates the key for the string prefix.
         * The longer the key the more children remain on the same tree level
         * I.e. if the key is the first character of the path (e.g. with ASCII only chars),
         * the max number of children in a single node is 128.
         * @return Key corresponding to this path
         */
        protected String getKey(String path) {
            return path.length() > 0 ? path.substring(0,1) : "";
        }

        /**
         * Subclass should create the instance of its own class
         * normally the implementation should invoke TrieNode(parent, path) superconstructor.
         */
        protected abstract N createNode(N parent, String path);

        /**
         * The node is notified on changes (either its path or direct children changed)
         */
        protected void nodeChanged() {}
    }

    P rootNode;

    public StringTrie(P rootNode) {
        this.rootNode = rootNode;
    }

    public P get(String path) {
        return rootNode.getMostSuitableChild(path);
    }

    public P add(String path) {
        return add(rootNode, path);
    }

    public P add(P parent, String path) {
        return parent.addChild(path);
    }

    public P delete(String path) {
        P p = get(path);
        if (path.equals(p.getAbsolutePath()) && p.isLeaf()) {
            p.delete();
            return p;
        } else {
            return null;
        }
    }

    /**
     * @return Pre-order walk tree elements iterator
     */
//    public Iterator<P> iterator() {
//        return new Iterator<P>() {
//            P curNode = rootNode;
//            Stack<Iterator<P>> childIndices = new Stack<>();
//
//            @Override
//            public boolean hasNext() {
//                return curNode != null;
//            }
//
//            @Override
//            public P next() {
//                P ret = curNode;
//                if (!curNode.getChildren().isEmpty()) {
//                    Iterator<P> it = curNode.getChildren().iterator();
//                    childIndices.push(it);
//                    curNode = it.next();
//                } else {
//                    curNode = null;
//                    while(curNode != null && !childIndices.isEmpty()) {
//                        Iterator<P> peek = childIndices.peek();
//                        if (peek.hasNext()) {
//                            curNode = peek.next();
//                        } else {
//                            childIndices.pop();
//                        }
//                    }
//                }
//                return ret;
//            }
//        };
//    }
//
//    /**
//     * @return Pre-order walk tree non-leaf elements iterator
//     */
//    public Iterator<P> nonLeafIterator() {
//        return new Iterator<P>() {
//            Iterator<P> leafIt = iterator();
//            P cur = findNext();
//
//            @Override
//            public boolean hasNext() {
//                return cur != null;
//            }
//
//            @Override
//            public P next() {
//                P ret = cur;
//                cur = findNext();
//                return ret;
//            }
//
//            private P findNext() {
//                P ret = null;
//                while(leafIt.hasNext() && (ret = leafIt.next()).isLeaf());
//                return ret;
//            }
//        };
//    }

//    protected abstract P createNode(P parent, String path);
//
//    protected void nodeChanged(P node) {}
}
