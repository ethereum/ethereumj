package org.ethereum.net.shh;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;

/**
 * Created by Anton Nashatyrev on 24.09.2015.
 */
public class BloomFilterTest {
    private static final Logger logger = LoggerFactory.getLogger("test");

    @Test
    public void test1() {
        BloomFilter filter = BloomFilter.createNone();

        for (int i = 0; i < 100; i++) {
            filter.addTopic(new Topic("Topic" + i));
        }

        for (int i = 0; i < 100; i++) {
            assertTrue("Topic #" + i, filter.hasTopic(new Topic("Topic" + i)));
        }

        int falsePositiveCnt = 0;
        for (int i = 0; i < 10000; i++) {
            falsePositiveCnt += filter.hasTopic(new Topic("Topic_" + i)) ? 1 : 0;
        }
        logger.info("falsePositiveCnt: " + falsePositiveCnt);
        assertTrue(falsePositiveCnt < 1000); // false positive probability ~8.7%
    }
}
