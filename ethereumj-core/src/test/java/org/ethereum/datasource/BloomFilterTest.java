package org.ethereum.datasource;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * The class mostly borrowed from http://github.com/magnuss/java-bloomfilter
 *
 * @author Magnus Skjegstad <magnus@skjegstad.com>
 */
public class BloomFilterTest {
    static Random r = new Random();

    public static byte[] getBytesFromUUID(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return bb.array();
    }

    @Test
    public void testConstructorCNK() throws Exception {
        System.out.println("BloomFilter(c,n,k)");

        for (int i = 0; i < 10000; i++) {
            double c = r.nextInt(20) + 1;
            int n = r.nextInt(10000) + 1;
            int k = r.nextInt(20) + 1;
            BloomFilter bf = new BloomFilter(c, n, k);
            assertEquals(bf.getK(), k);
            assertEquals(bf.getExpectedBitsPerElement(), c, 0);
            assertEquals(bf.getExpectedNumberOfElements(), n);
            assertEquals(bf.size(), c*n, 0);
        }
    }

    /**
     * Test of equals method, of class BloomFilter.
     * @throws UnsupportedEncodingException
     */
    @Test
    public void testEquals() throws UnsupportedEncodingException {
        System.out.println("equals");
        BloomFilter instance1 = new BloomFilter(1000, 100);
        BloomFilter instance2 = new BloomFilter(1000, 100);

        for (int i = 0; i < 100; i++) {
            byte[] val = UUID.randomUUID().toString().getBytes("UTF-8");
            instance1.add(val);
            instance2.add(val);
        }

        assert(instance1.equals(instance2));
        assert(instance2.equals(instance1));

        byte[] anotherEntryByteArray = "Another Entry".getBytes("UTF-8");
        instance1.add(anotherEntryByteArray); // make instance1 and instance2 different before clearing

        instance1.clear();
        instance2.clear();

        assert(instance1.equals(instance2));
        assert(instance2.equals(instance1));

        for (int i = 0; i < 100; i++) {
            byte[] val = UUID.randomUUID().toString().getBytes("UTF-8");
            instance1.add(val);
            instance2.add(val);
        }

        assertTrue(instance1.equals(instance2));
        assertTrue(instance2.equals(instance1));
    }

    /**
     * Test of hashCode method, of class BloomFilter.
     * @throws UnsupportedEncodingException
     */
    @Test
    public void testHashCode() throws UnsupportedEncodingException {
        System.out.println("hashCode");

        BloomFilter instance1 = new BloomFilter(1000, 100);
        BloomFilter instance2 = new BloomFilter(1000, 100);

        assertTrue(instance1.hashCode() == instance2.hashCode());

        for (int i = 0; i < 100; i++) {
            byte[] val = UUID.randomUUID().toString().getBytes("UTF-8");
            instance1.add(val);
            instance2.add(val);
        }

        assertTrue(instance1.hashCode() == instance2.hashCode());

        instance1.clear();
        instance2.clear();

        assertTrue(instance1.hashCode() == instance2.hashCode());

        instance1 = new BloomFilter(100, 10);
        instance2 = new BloomFilter(100, 9);
        assertFalse(instance1.hashCode() == instance2.hashCode());

        instance1 = new BloomFilter(100, 10);
        instance2 = new BloomFilter(99, 9);
        assertFalse(instance1.hashCode() == instance2.hashCode());

        instance1 = new BloomFilter(100, 10);
        instance2 = new BloomFilter(50, 10);
        assertFalse(instance1.hashCode() == instance2.hashCode());
    }

    /**
     * Test of expectedFalsePositiveProbability method, of class BloomFilter.
     */
    @Test
    public void testExpectedFalsePositiveProbability() {
        // These probabilities are taken from the bloom filter probability table at
        // http://pages.cs.wisc.edu/~cao/papers/summary-cache/node8.html
        System.out.println("expectedFalsePositiveProbability");
        BloomFilter instance = new BloomFilter(1000, 100);
        double expResult = 0.00819; // m/n=10, k=7
        double result = instance.expectedFalsePositiveProbability();
        assertEquals(instance.getK(), 7);
        assertEquals(expResult, result, 0.000009);

        instance = new BloomFilter(100, 10);
        expResult = 0.00819; // m/n=10, k=7
        result = instance.expectedFalsePositiveProbability();
        assertEquals(instance.getK(), 7);
        assertEquals(expResult, result, 0.000009);

        instance = new BloomFilter(20, 10);
        expResult = 0.393; // m/n=2, k=1
        result = instance.expectedFalsePositiveProbability();
        assertEquals(1, instance.getK());
        assertEquals(expResult, result, 0.0005);

        instance = new BloomFilter(110, 10);
        expResult = 0.00509; // m/n=11, k=8
        result = instance.expectedFalsePositiveProbability();
        assertEquals(8, instance.getK());
        assertEquals(expResult, result, 0.00001);
    }

    /**
     * Test of clear method, of class BloomFilter.
     */
    @Test
    public void testClear() {
        System.out.println("clear");
        BloomFilter instance = new BloomFilter(1000, 100);
        for (int i = 0; i < instance.size(); i++)
            instance.setBit(i, true);
        instance.clear();
        for (int i = 0; i < instance.size(); i++)
            assertSame(instance.getBit(i), false);
    }

    /**
     * Test of add method, of class BloomFilter.
     * @throws Exception
     */
    @Test
    public void testAdd() throws Exception {
        System.out.println("add");
        BloomFilter instance = new BloomFilter(1000, 100);

        for (int i = 0; i < 100; i++) {
            byte[] bytes = getBytesFromUUID(UUID.randomUUID());
            instance.add(bytes);
            assert(instance.contains(bytes));
        }
    }

    /**
     * Test of contains method, of class BloomFilter.
     * @throws Exception
     */
    @Test
    public void testContains() throws Exception {
        System.out.println("contains");
        BloomFilter instance = new BloomFilter(10000, 10);

        for (int i = 0; i < 10; i++) {
            byte[] bytes = getBytesFromUUID(UUID.randomUUID());
            instance.add(bytes);
            assert(instance.contains(bytes));
        }

        assertFalse(instance.contains(UUID.randomUUID().toString().getBytes("UTF-8")));
    }

    /**
     * Test of getBit method, of class BloomFilter.
     */
    @Test
    public void testGetBit() {
        System.out.println("getBit");
        BloomFilter instance = new BloomFilter(1000, 100);
        Random r = new Random();

        for (int i = 0; i < 100; i++) {
            boolean b = r.nextBoolean();
            instance.setBit(i, b);
            assertSame(instance.getBit(i), b);
        }
    }

    /**
     * Test of setBit method, of class BloomFilter.
     */
    @Test
    public void testSetBit() {
        System.out.println("setBit");

        BloomFilter instance = new BloomFilter(1000, 100);
        Random r = new Random();

        for (int i = 0; i < 100; i++) {
            instance.setBit(i, true);
            assertSame(instance.getBit(i), true);
        }

        for (int i = 0; i < 100; i++) {
            instance.setBit(i, false);
            assertSame(instance.getBit(i), false);
        }
    }

    /**
     * Test of size method, of class BloomFilter.
     */
    @Test
    public void testSize() {
        System.out.println("size");
        for (int i = 100; i < 1000; i++) {
            BloomFilter instance = new BloomFilter(i, 10);
            assertEquals(instance.size(), i);
        }
    }

    /** Test error rate *
     * @throws UnsupportedEncodingException
     */
    @Test
    public void testFalsePositiveRate1() throws UnsupportedEncodingException {
        // Numbers are from // http://pages.cs.wisc.edu/~cao/papers/summary-cache/node8.html
        System.out.println("falsePositiveRate1");

        for (int j = 10; j < 21; j++) {
            System.out.print(j-9 + "/11");
            List<byte[]> v = new ArrayList<byte[]>();
            BloomFilter instance = new BloomFilter(100*j,100);

            for (int i = 0; i < 100; i++) {
                byte[] bytes = new byte[100];
                r.nextBytes(bytes);
                v.add(bytes);
            }
            for (byte[] bytes : v) {
                instance.add(bytes);
            }

            long f = 0;
            double tests = 300000;
            for (int i = 0; i < tests; i++) {
                byte[] bytes = new byte[100];
                r.nextBytes(bytes);
                if (instance.contains(bytes)) {
                    if (!v.contains(bytes)) {
                        f++;
                    }
                }
            }

            double ratio = f / tests;

            System.out.println(" - got " + ratio + ", math says " + instance.expectedFalsePositiveProbability());
            assertEquals(instance.expectedFalsePositiveProbability(), ratio, 0.01);
        }
    }

    /** Test for correct k **/
    @Test
    public void testGetK() {
        // Numbers are from http://pages.cs.wisc.edu/~cao/papers/summary-cache/node8.html
        System.out.println("testGetK");
        BloomFilter instance = null;

        instance = new BloomFilter(2, 1);
        assertEquals(1, instance.getK());

        instance = new BloomFilter(3, 1);
        assertEquals(2, instance.getK());

        instance = new BloomFilter(4, 1);
        assertEquals(3, instance.getK());

        instance = new BloomFilter(5, 1);
        assertEquals(3, instance.getK());

        instance = new BloomFilter(6, 1);
        assertEquals(4, instance.getK());

        instance = new BloomFilter(7, 1);
        assertEquals(5, instance.getK());

        instance = new BloomFilter(8, 1);
        assertEquals(6, instance.getK());

        instance = new BloomFilter(9, 1);
        assertEquals(6, instance.getK());

        instance = new BloomFilter(10, 1);
        assertEquals(7, instance.getK());

        instance = new BloomFilter(11, 1);
        assertEquals(8, instance.getK());

        instance = new BloomFilter(12, 1);
        assertEquals(8, instance.getK());
    }

    /**
     * Test of contains method, of class BloomFilter.
     */
    @Test
    public void testContains_GenericType() throws UnsupportedEncodingException {
        System.out.println("contains");
        int items = 100;
        BloomFilter instance = new BloomFilter(0.01, items);

        for (int i = 0; i < items; i++) {
            byte[] b = UUID.randomUUID().toString().getBytes("UTF-8");
            instance.add(b);
            assertTrue(instance.contains(b));
        }
    }

    /**
     * Test of contains method, of class BloomFilter.
     */
    @Test
    public void testContains_byteArr() {
        System.out.println("contains");

        int items = 100;
        BloomFilter instance = new BloomFilter(0.01, items);

        for (int i = 0; i < items; i++) {
            byte[] bytes = new byte[500];
            r.nextBytes(bytes);
            instance.add(bytes);
            assertTrue(instance.contains(bytes));
        }
    }

    /**
     * Test of count method, of class BloomFilter.
     */
    @Test
    public void testCount() throws UnsupportedEncodingException {
        System.out.println("count");
        int expResult = 100;
        BloomFilter instance = new BloomFilter(0.01, expResult);
        for (int i = 0; i < expResult; i++) {
            byte[] bytes = new byte[100];
            r.nextBytes(bytes);
            instance.add(bytes);
        }
        int result = instance.count();
        assertEquals(expResult, result);

        instance = new BloomFilter(0.01, expResult);
        for (int i = 0; i < expResult; i++) {
            instance.add(UUID.randomUUID().toString().getBytes("UTF-8"));
        }
        result = instance.count();
        assertEquals(expResult, result);
    }
}
