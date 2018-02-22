package org.ethereum.db.prune;

import org.junit.Test;

import static org.ethereum.util.ByteUtil.intToBytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mikhail Kalinin
 * @since 31.01.2018
 */
public class SegmentTest {

    @Test
    public void simpleTest() {
        Segment s = new Segment(1, intToBytes(1), intToBytes(0));

        assertEquals(Chain.NULL, s.main);
        assertFalse(s.isComplete());
        assertEquals(0, s.getMaxNumber());
        assertEquals(0, s.size());

        Segment.Tracker t = s.startTracking();
        t.addMain(2, intToBytes(2), intToBytes(1));
        t.commit();

        assertTrue(s.isComplete());
        assertEquals(2, s.getMaxNumber());
        assertEquals(1, s.size());

        t = s.startTracking();
        t.addItem(2, intToBytes(21), intToBytes(1));
        t.addItem(2, intToBytes(22), intToBytes(1));
        t.addItem(3, intToBytes(3), intToBytes(2));
        t.addMain(3, intToBytes(3), intToBytes(2));  // should process double adding
        t.commit();

        assertTrue(s.isComplete());
        assertEquals(3, s.getMaxNumber());
        assertEquals(2, s.size());
        assertEquals(2, s.forks.size());

        t = s.startTracking();
        t.addItem(3, intToBytes(31), intToBytes(21));
        t.commit();

        assertFalse(s.isComplete());
        assertEquals(3, s.getMaxNumber());
        assertEquals(2, s.size());
        assertEquals(2, s.forks.size());
    }

    @Test // short forks
    public void testFork1() {
        Segment s = new Segment(1, intToBytes(1), intToBytes(0));

        s.startTracking()
                .addItem(2, intToBytes(2), intToBytes(1))
                .addMain(2, intToBytes(2), intToBytes(1))
                .commit();

        assertTrue(s.isComplete());

        s.startTracking()
                .addItem(3, intToBytes(31), intToBytes(2))
                .addItem(3, intToBytes(32), intToBytes(2))
                .addItem(3, intToBytes(3), intToBytes(2))
                .addMain(3, intToBytes(3), intToBytes(2))
                .commit();

        assertFalse(s.isComplete());
        assertEquals(2, s.size());
        assertEquals(2, s.forks.size());

        s.startTracking()
                .addItem(4, intToBytes(41), intToBytes(31))
                .addItem(4, intToBytes(4), intToBytes(3))
                .addMain(4, intToBytes(4), intToBytes(3))
                .commit();

        assertFalse(s.isComplete());
        assertEquals(3, s.size());
        assertEquals(2, s.forks.size());
        assertEquals(4, s.getMaxNumber());

        s.startTracking()
                .addItem(5, intToBytes(53), intToBytes(4))
                .addItem(5, intToBytes(5), intToBytes(4))
                .addMain(5, intToBytes(5), intToBytes(4))
                .commit();

        s.startTracking()
                .addItem(6, intToBytes(6), intToBytes(5))
                .addMain(6, intToBytes(6), intToBytes(5))
                .commit();

        assertTrue(s.isComplete());
        assertEquals(5, s.size());
        assertEquals(3, s.forks.size());
        assertEquals(6, s.getMaxNumber());
    }

    @Test // long fork with short forks
    public void testFork2() {
        Segment s = new Segment(1, intToBytes(1), intToBytes(0));

        s.startTracking()
                .addItem(2, intToBytes(2), intToBytes(1))
                .addMain(2, intToBytes(2), intToBytes(1))
                .commit();

        assertTrue(s.isComplete());

        s.startTracking()
                .addItem(3, intToBytes(30), intToBytes(2))
                .addItem(3, intToBytes(31), intToBytes(2))
                .addItem(3, intToBytes(3), intToBytes(2))
                .addMain(3, intToBytes(3), intToBytes(2))
                .commit();

        assertFalse(s.isComplete());
        assertEquals(2, s.size());
        assertEquals(2, s.forks.size());

        s.startTracking()
                .addItem(4, intToBytes(40), intToBytes(30))
                .addItem(4, intToBytes(41), intToBytes(31))
                .addItem(4, intToBytes(42), intToBytes(3))
                .addItem(4, intToBytes(4), intToBytes(3))
                .addMain(4, intToBytes(4), intToBytes(3))
                .commit();

        assertFalse(s.isComplete());
        assertEquals(3, s.size());
        assertEquals(3, s.forks.size());
        assertEquals(4, s.getMaxNumber());

        s.startTracking()
                .addItem(5, intToBytes(50), intToBytes(40))
                .addItem(5, intToBytes(53), intToBytes(4))
                .addItem(5, intToBytes(5), intToBytes(4))
                .addMain(5, intToBytes(5), intToBytes(4))
                .commit();

        s.startTracking()
                .addItem(6, intToBytes(60), intToBytes(50))
                .addItem(6, intToBytes(6), intToBytes(5))
                .addMain(6, intToBytes(6), intToBytes(5))
                .commit();

        assertFalse(s.isComplete());
        assertEquals(5, s.size());
        assertEquals(4, s.forks.size());
        assertEquals(6, s.getMaxNumber());

        s.startTracking()
                .addItem(7, intToBytes(7), intToBytes(6))
                .addMain(7, intToBytes(7), intToBytes(6))
                .commit();

        assertTrue(s.isComplete());
        assertEquals(6, s.size());
        assertEquals(4, s.forks.size());
        assertEquals(7, s.getMaxNumber());
    }

    @Test // several branches started at fork block
    public void testFork3() {
        /*
        2: 2 -> 3: 3  -> 4: 4  -> 5: 5  -> 6: 6 <-- Main
            \
             -> 3: 31 -> 4: 41
                    \          -> 5: 53
                     \        /
                      -> 4: 42 -> 5: 52
                              \
                               -> 5: 54
         */

        Segment s = new Segment(1, intToBytes(1), intToBytes(0));

        s.startTracking()
                .addItem(2, intToBytes(2), intToBytes(1))
                .addMain(2, intToBytes(2), intToBytes(1))
                .commit();

        s.startTracking()
                .addItem(3, intToBytes(3), intToBytes(2))
                .addItem(3, intToBytes(31), intToBytes(2))
                .addMain(3, intToBytes(3), intToBytes(2))
                .commit();

        s.startTracking()
                .addItem(4, intToBytes(4), intToBytes(3))
                .addItem(4, intToBytes(41), intToBytes(31))
                .addItem(4, intToBytes(42), intToBytes(31))
                .addMain(4, intToBytes(4), intToBytes(3))
                .commit();

        s.startTracking()
                .addItem(5, intToBytes(5), intToBytes(4))
                .addItem(5, intToBytes(52), intToBytes(42))
                .addItem(5, intToBytes(53), intToBytes(42))
                .addItem(5, intToBytes(54), intToBytes(42))
                .addMain(5, intToBytes(5), intToBytes(4))
                .commit();

        s.startTracking()
                .addItem(6, intToBytes(6), intToBytes(5))
                .addMain(6, intToBytes(6), intToBytes(5))
                .commit();

        Chain main = Chain.fromItems(
                new ChainItem(2, intToBytes(2), intToBytes(1)),
                new ChainItem(3, intToBytes(3), intToBytes(2)),
                new ChainItem(4, intToBytes(4), intToBytes(3)),
                new ChainItem(5, intToBytes(5), intToBytes(4)),
                new ChainItem(6, intToBytes(6), intToBytes(5))
        );

        Chain fork1 = Chain.fromItems(
                new ChainItem(3, intToBytes(31), intToBytes(2)),
                new ChainItem(4, intToBytes(41), intToBytes(31))
        );

        Chain fork2 = Chain.fromItems(
                new ChainItem(4, intToBytes(42), intToBytes(31)),
                new ChainItem(5, intToBytes(52), intToBytes(42))
        );
        Chain fork3 = Chain.fromItems(
                new ChainItem(5, intToBytes(53), intToBytes(42))
        );
        Chain fork4 = Chain.fromItems(
                new ChainItem(5, intToBytes(54), intToBytes(42))
        );

        assertEquals(main, s.main);

        assertEquals(4, s.forks.size());
        assertEquals(fork1, s.forks.get(0));
        assertEquals(fork2, s.forks.get(1));
        assertEquals(fork3, s.forks.get(2));
        assertEquals(fork4, s.forks.get(3));
    }
}
