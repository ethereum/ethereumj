package org.ethereum.net.shh;

import org.ethereum.crypto.ECKey;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by kest on 6/26/15.
 */
public class FilterTest {

    byte[] to = new ECKey().decompress().getPubKey();
    byte[] from = new ECKey().decompress().getPubKey();
    String[] topics = {"topic1", "topic2", "topic3", "topic4"};

    @Test
    public void test1() {
        Filter matcher = new Filter(null, null, new TopicMatcher(new String[]{}));
        Filter message = new Filter(to, from, new TopicMatcher(topics));

        assertTrue(matcher.match(message));
    }

    @Test
    public void test2() {
        Filter matcher = new Filter(to, null, new TopicMatcher(new String[]{}));
        Filter message = new Filter(to, from, new TopicMatcher(topics));

        assertTrue(matcher.match(message));
    }

    @Test
    public void test3() {
        Filter matcher = new Filter(to, null, new TopicMatcher(new String[]{}));
        Filter message = new Filter(null, from, new TopicMatcher(topics));

        assertTrue(!matcher.match(message));
    }

    @Test
    public void test4() {
        Filter matcher = new Filter(null, from, new TopicMatcher(new String[]{}));
        Filter message = new Filter(to, from, new TopicMatcher(topics));

        assertTrue(matcher.match(message));
    }

    @Test
    public void test5() {
        Filter matcher = new Filter(null, from, new TopicMatcher(new String[]{}));
        Filter message = new Filter(to, null, new TopicMatcher(topics));

        assertTrue(!matcher.match(message));
    }

    @Test
    public void test6() {
        Filter matcher = new Filter(null, from, new TopicMatcher(topics));
        Filter message = new Filter(to, from, new TopicMatcher(topics));

        assertTrue(matcher.match(message));
    }

    @Test
    public void test7() {
        Filter matcher = new Filter(null, null, new TopicMatcher(topics));
        Filter message = new Filter(to, from, new TopicMatcher(new String[]{}));

        assertTrue(!matcher.match(message));
    }
}
