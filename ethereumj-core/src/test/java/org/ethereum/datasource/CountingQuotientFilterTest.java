package org.ethereum.datasource;

import org.ethereum.datasource.CountingQuotientFilter;
import org.junit.Ignore;
import org.junit.Test;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.util.ByteUtil.intToBytes;

/**
 * @author Mikhail Kalinin
 * @since 15.02.2018
 */
public class CountingQuotientFilterTest {

    @Ignore
    @Test
    public void perfTest() {
        CountingQuotientFilter f = CountingQuotientFilter.create(50_000_000, 100_000);
        long s = System.currentTimeMillis();
        for (int i = 0; i < 5_000_000; i++) {
            f.insert(sha3(intToBytes(i)));

            if (i % 10 == 0) f.insert(sha3(intToBytes(0)));

            if (i > 100_000 && i % 2 == 0) {
                f.remove(sha3(intToBytes(i - 100_000)));
            }
            if (i % 10000 == 0) {
                System.out.println(i + ": " + (System.currentTimeMillis() - s));
                s = System.currentTimeMillis();
            }
        }
    }

    @Test
    public void simpleTest() {
        CountingQuotientFilter f = CountingQuotientFilter.create(1_000_000, 1_000_000);

        f.insert(sha3(intToBytes(0)));
        assert f.maybeContains(sha3(intToBytes(0)));

        f.insert(sha3(intToBytes(1)));
        f.remove(sha3(intToBytes(0)));
        assert f.maybeContains(sha3(intToBytes(1)));
        assert !f.maybeContains(sha3(intToBytes(0)));

        for (int i = 0; i < 10; i++) {
            f.insert(sha3(intToBytes(2)));
        }

        assert f.maybeContains(sha3(intToBytes(2)));

        for (int i = 0; i < 8; i++) {
            f.remove(sha3(intToBytes(2)));
        }
        assert f.maybeContains(sha3(intToBytes(2)));

        f.remove(sha3(intToBytes(2)));
        assert f.maybeContains(sha3(intToBytes(2)));

        f.remove(sha3(intToBytes(2)));
        assert !f.maybeContains(sha3(intToBytes(2)));

        f.remove(sha3(intToBytes(2))); // check that it breaks nothing
        assert !f.maybeContains(sha3(intToBytes(2)));
    }

    @Test // elements have same fingerprint, but different hash
    public void softCollisionTest() {
        CountingQuotientFilter f = CountingQuotientFilter.create(1_000_000, 1_000_000);

        f.insert(-1L);
        f.insert(Long.MAX_VALUE);
        f.insert(Long.MAX_VALUE - 1);

        assert f.maybeContains(-1L);
        assert f.maybeContains(Long.MAX_VALUE);
        assert f.maybeContains(Long.MAX_VALUE - 1);

        f.remove(-1L);
        assert f.maybeContains(Long.MAX_VALUE);
        assert f.maybeContains(Long.MAX_VALUE - 1);

        f.remove(Long.MAX_VALUE);
        assert f.maybeContains(Long.MAX_VALUE - 1);

        f.remove(Long.MAX_VALUE - 1);
        assert !f.maybeContains(-1L);
        assert !f.maybeContains(Long.MAX_VALUE);
        assert !f.maybeContains(Long.MAX_VALUE - 1);
    }

    @Test // elements have same fingerprint, but different hash
    public void softCollisionTest2() {
        CountingQuotientFilter f = CountingQuotientFilter.create(1_000_000, 1_000_000);

        f.insert(0xE0320F4F9B35343FL);
        f.insert(0xFF2D4CCA9B353435L);
        f.insert(0xFF2D4CCA9B353435L);

        f.remove(0xE0320F4F9B35343FL);
        f.remove(0xFF2D4CCA9B353435L);

        assert f.maybeContains(0xFF2D4CCA9B353435L);
    }

    @Test // elements have same hash
    public void hardCollisionTest() {
        CountingQuotientFilter f = CountingQuotientFilter.create(10_000_000, 10_000_000);

        f.insert(Long.MAX_VALUE);
        f.insert(Long.MAX_VALUE);
        f.insert(Long.MAX_VALUE);
        assert f.maybeContains(Long.MAX_VALUE);

        f.remove(Long.MAX_VALUE);
        f.remove(Long.MAX_VALUE);
        assert f.maybeContains(Long.MAX_VALUE);

        f.remove(-1L);
        assert !f.maybeContains(-1L);
    }

    @Test
    public void resizeTest() {
        CountingQuotientFilter f = CountingQuotientFilter.create(1_000, 1_000);

        f.insert(Long.MAX_VALUE);
        f.insert(Long.MAX_VALUE);
        f.insert(Long.MAX_VALUE);
        f.insert(Long.MAX_VALUE - 1);

        for (int i = 100_000; i < 200_000; i++) {
            f.insert(intToBytes(i));
        }

        assert f.maybeContains(Long.MAX_VALUE);
        for (int i = 100_000; i < 200_000; i++) {
            assert f.maybeContains(intToBytes(i));
        }

        assert f.maybeContains(Long.MAX_VALUE);
        assert f.maybeContains(Long.MAX_VALUE - 1);

        f.remove(Long.MAX_VALUE);
        f.remove(Long.MAX_VALUE);
        f.remove(Long.MAX_VALUE - 1);
        assert f.maybeContains(Long.MAX_VALUE);

        f.remove(Long.MAX_VALUE);
        assert !f.maybeContains(Long.MAX_VALUE);

        f.remove(Long.MAX_VALUE - 1);
        assert !f.maybeContains(Long.MAX_VALUE - 1);
    }
}
