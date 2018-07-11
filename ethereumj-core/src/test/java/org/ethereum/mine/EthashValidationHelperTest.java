package org.ethereum.mine;

import org.junit.Test;

import static org.ethereum.mine.EthashValidationHelper.CacheOrder.direct;
import static org.ethereum.mine.EthashValidationHelper.CacheOrder.reverse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Mikhail Kalinin
 * @since 11.07.2018
 */
public class EthashValidationHelperTest {

    static class EthashAlgoMock extends org.ethereum.mine.EthashAlgo {
        @Override
        public int[] makeCache(long cacheSize, byte[] seed) {
            return new int[0];
        }
    }

    @Test // sequential direct caching
    public void testRegularFlow() {
        EthashValidationHelper ethash = new EthashValidationHelper(direct);
        ethash.ethashAlgo = new EthashAlgoMock();

        // init on block 193, 0 and 1st epochs are cached
        ethash.preCache(193);

        assertNotNull(ethash.getCachedFor(193));
        assertNotNull(ethash.getCachedFor(30_000));
        assertEquals(ethash.caches.size(), 2);

        ethash = new EthashValidationHelper(direct);
        ethash.ethashAlgo = new EthashAlgoMock();

        // genesis
        ethash.preCache(0);

        assertNotNull(ethash.getCachedFor(0));
        assertEquals(ethash.caches.size(), 1);
        assertNull(ethash.getCachedFor(30_000));

        // block 100, nothing has changed
        ethash.preCache(100);
        assertNotNull(ethash.getCachedFor(100));
        assertEquals(ethash.caches.size(), 1);

        // block 193, next epoch must be added
        ethash.preCache(193);
        assertNotNull(ethash.getCachedFor(193));
        assertNotNull(ethash.getCachedFor(30_000));
        assertEquals(ethash.caches.size(), 2);

        // block  30_192, nothing has changed
        ethash.preCache(30_192);
        assertNotNull(ethash.getCachedFor(30_192));
        assertNotNull(ethash.getCachedFor(192));
        assertEquals(ethash.caches.size(), 2);

        // block  30_193, two epochs are kept: 1st and 2nd
        ethash.preCache(30_193);
        assertNotNull(ethash.getCachedFor(30_193));
        assertNotNull(ethash.getCachedFor(60_000));
        assertNull(ethash.getCachedFor(193));
        assertEquals(ethash.caches.size(), 2);
    }

    @Test // sequential direct caching with gap between 0 and K block, K and N block
    public void testRegularFlowWithGap() {
        EthashValidationHelper ethash = new EthashValidationHelper(direct);
        ethash.ethashAlgo = new EthashAlgoMock();

        // genesis
        ethash.preCache(0);

        assertNotNull(ethash.getCachedFor(0));
        assertEquals(ethash.caches.size(), 1);
        assertNull(ethash.getCachedFor(30_000));

        // block 100_000, cache must have been reset
        ethash.preCache(100_000);
        assertNotNull(ethash.getCachedFor(100_000));
        assertNotNull(ethash.getCachedFor(120_000));
        assertNull(ethash.getCachedFor(0));
        assertEquals(ethash.caches.size(), 2);

        // block 120_193, caches shifted by one epoch
        ethash.preCache(120_193);
        assertNotNull(ethash.getCachedFor(120_000));
        assertNotNull(ethash.getCachedFor(150_000));
        assertNull(ethash.getCachedFor(100_000));
        assertEquals(ethash.caches.size(), 2);

        // block 300_000, caches have been reset once again
        ethash.preCache(300_000);
        assertNotNull(ethash.getCachedFor(300_000));
        assertNotNull(ethash.getCachedFor(299_000));
        assertNull(ethash.getCachedFor(120_000));
        assertNull(ethash.getCachedFor(150_000));
        assertEquals(ethash.caches.size(), 2);
    }

    @Test // sequential reverse flow, like a flow that is used in reverse header downloading
    public void testReverseFlow() {
        EthashValidationHelper ethash = new EthashValidationHelper(reverse);
        ethash.ethashAlgo = new EthashAlgoMock();

        // init on 15_000 block, 0 and 1st epochs are cached
        ethash.preCache(15_000);
        assertNotNull(ethash.getCachedFor(15_000));
        assertNotNull(ethash.getCachedFor(30_000));
        assertEquals(ethash.caches.size(), 2);

        ethash = new EthashValidationHelper(reverse);
        ethash.ethashAlgo = new EthashAlgoMock();

        // init on 14_999 block, only 0 epoch is cached
        ethash.preCache(14_999);
        assertNotNull(ethash.getCachedFor(14_999));
        assertNull(ethash.getCachedFor(30_000));
        assertEquals(ethash.caches.size(), 1);

        ethash = new EthashValidationHelper(reverse);
        ethash.ethashAlgo = new EthashAlgoMock();

        // init on 100_000 block, 2nd and 3rd epochs are cached
        ethash.preCache(100_000);
        assertNotNull(ethash.getCachedFor(100_000));
        assertNotNull(ethash.getCachedFor(80_000));
        assertNull(ethash.getCachedFor(120_000));
        assertEquals(ethash.caches.size(), 2);

        // block 75_000, nothing has changed
        ethash.preCache(75_000);
        assertNotNull(ethash.getCachedFor(100_000));
        assertNotNull(ethash.getCachedFor(75_000));
        assertNull(ethash.getCachedFor(120_000));
        assertNull(ethash.getCachedFor(59_000));
        assertEquals(ethash.caches.size(), 2);

        // block 74_999, caches are shifted by 1 epoch toward 0 epoch
        ethash.preCache(74_999);
        assertNotNull(ethash.getCachedFor(74_999));
        assertNotNull(ethash.getCachedFor(59_000));
        assertNull(ethash.getCachedFor(100_000));
        assertEquals(ethash.caches.size(), 2);

        // block 44_999, caches are shifted by 1 epoch toward 0 epoch
        ethash.preCache(44_999);
        assertNotNull(ethash.getCachedFor(44_999));
        assertNotNull(ethash.getCachedFor(19_000));
        assertNull(ethash.getCachedFor(80_000));
        assertEquals(ethash.caches.size(), 2);

        // block 14_999, caches are shifted by 1 epoch toward 0 epoch
        ethash.preCache(14_999);
        assertNotNull(ethash.getCachedFor(14_999));
        assertNotNull(ethash.getCachedFor(0));
        assertNotNull(ethash.getCachedFor(30_000));
        assertEquals(ethash.caches.size(), 2);

        // block 1, nothing has changed
        ethash.preCache(1);
        assertNotNull(ethash.getCachedFor(1));
        assertNotNull(ethash.getCachedFor(0));
        assertNotNull(ethash.getCachedFor(30_000));
        assertEquals(ethash.caches.size(), 2);

        // block 0, nothing has changed
        ethash.preCache(0);
        assertNotNull(ethash.getCachedFor(0));
        assertNotNull(ethash.getCachedFor(30_000));
        assertEquals(ethash.caches.size(), 2);
    }

    @Test // sequential reverse flow with gap
    public void testReverseFlowWithGap() {
        EthashValidationHelper ethash = new EthashValidationHelper(reverse);
        ethash.ethashAlgo = new EthashAlgoMock();

        // init on 300_000 block
        ethash.preCache(300_000);
        assertNotNull(ethash.getCachedFor(300_000));
        assertNotNull(ethash.getCachedFor(275_000));
        assertNull(ethash.getCachedFor(330_000));
        assertEquals(ethash.caches.size(), 2);

        // jump to 100_000 block, 2nd and 3rd epochs are cached
        ethash.preCache(100_000);
        assertNotNull(ethash.getCachedFor(100_000));
        assertNotNull(ethash.getCachedFor(80_000));
        assertNull(ethash.getCachedFor(120_000));
        assertEquals(ethash.caches.size(), 2);

        // block 74_999, caches are shifted by 1 epoch toward 0 epoch
        ethash.preCache(74_999);
        assertNotNull(ethash.getCachedFor(74_999));
        assertNotNull(ethash.getCachedFor(59_000));
        assertNull(ethash.getCachedFor(100_000));
        assertEquals(ethash.caches.size(), 2);

        // jump to 14_999, caches are shifted by 1 epoch toward 0 epoch
        ethash.preCache(14_999);
        assertNotNull(ethash.getCachedFor(14_999));
        assertNotNull(ethash.getCachedFor(0));
        assertEquals(ethash.caches.size(), 1);
    }
}
