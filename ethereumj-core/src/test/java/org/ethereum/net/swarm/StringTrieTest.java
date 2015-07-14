package org.ethereum.net.swarm;

import org.junit.*;
import org.junit.Test;

/**
 * Created by Admin on 11.06.2015.
 */
public class StringTrieTest {

    class A extends StringTrie.TrieNode<A> {
        String id;

        public A() {
        }

        public A(A parent, String relPath) {
            super(parent, relPath);
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        protected A createNode(A parent, String path) {
            return new A(parent, path);
        }

        @Override
        public String toString() {
            return "A[" + (id != null ? id : "") + "]";
        }
    }

    class T extends StringTrie<A> {

        public T() {
            super(new A());
        }

        @Override
        public A add(String path) {
            A ret = super.add(path);
            ret.setId(path);
            return ret;
        }
    };

    @Test
    public void testAdd() {
        T trie = new T();

        trie.add("aaa");
        trie.add("bbb");
        trie.add("aad");
        trie.add("aade");
        trie.add("aadd");

        System.out.println(Util.dumpTree(trie.rootNode));

        Assert.assertEquals("aaa", trie.get("aaa").getAbsolutePath());
        Assert.assertEquals("bbb", trie.get("bbb").getAbsolutePath());
        Assert.assertEquals("aad", trie.get("aad").getAbsolutePath());
        Assert.assertEquals("aa", trie.get("aaqqq").getAbsolutePath());
        Assert.assertEquals("", trie.get("bbe").getAbsolutePath());
    }

    @Test
    public void testAddRootLeaf() {
        T trie = new T();

        trie.add("ax");
        trie.add("ay");
        trie.add("a");

        System.out.println(Util.dumpTree(trie.rootNode));
    }

    @Test
    public void testAddDuplicate() {
        T trie = new T();

        A a = trie.add("a");
        A ay = trie.add("ay");
        A a1 = trie.add("a");
        Assert.assertTrue(a == a1);
        A ay1 = trie.add("ay");
        Assert.assertTrue(ay == ay1);
    }

    @Test
    public void testAddLeafRoot() {
        T trie = new T();

        trie.add("a");
        trie.add("ax");

        System.out.println(Util.dumpTree(trie.rootNode));
    }

    @Test
    public void testAddDelete() {
        T trie = new T();

        trie.add("aaaa");
        trie.add("aaaaxxxx");
        trie.add("aaaaxxxxeeee");
        System.out.println(Util.dumpTree(trie.rootNode));
        trie.delete("aaaa");
        System.out.println(Util.dumpTree(trie.rootNode));
        trie.delete("aaaaxxxx");
        System.out.println(Util.dumpTree(trie.rootNode));
    }
}
