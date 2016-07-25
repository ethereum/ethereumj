package org.ethereum.net.shh;

import org.ethereum.crypto.ECKey;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FilterTest {

    String to = WhisperImpl.toIdentity(new ECKey());
    String from = WhisperImpl.toIdentity(new ECKey());
    String[] topics = {"topic1", "topic2", "topic3", "topic4"};

    class FilterStub extends MessageWatcher {
        public FilterStub() {
        }

        public FilterStub(String to, String from, Topic[] filterTopics) {
            super(to, from, filterTopics);
        }

        @Override
        protected void newMessage(WhisperMessage msg) {

        }
    }

    @Test
    public void test1() {
        MessageWatcher matcher = new FilterStub();
        assertTrue(matcher.match(to, from, Topic.createTopics(topics)));
    }

    @Test
    public void test2() {
        MessageWatcher matcher = new FilterStub().setTo(to);
        assertTrue(matcher.match(to, from, Topic.createTopics(topics)));
    }

    @Test
    public void test3() {
        MessageWatcher matcher = new FilterStub().setTo(to);
        assertFalse(matcher.match(null, from, Topic.createTopics(topics)));
    }

    @Test
    public void test4() {
        MessageWatcher matcher = new FilterStub().setFrom(from);
        assertTrue(matcher.match(null, from, Topic.createTopics(topics)));
    }

    @Test
    public void test5() {
        MessageWatcher matcher = new FilterStub().setFrom(from);
        assertTrue(!matcher.match(to, null,  Topic.createTopics(topics)));
    }

    @Test
    public void test6() {
        MessageWatcher matcher = new FilterStub(null, from,  Topic.createTopics(topics));
        assertTrue(matcher.match(to, from,  Topic.createTopics(topics)));
    }

    @Test
    public void test7() {
        MessageWatcher matcher = new FilterStub(null, null,  Topic.createTopics(topics));
        assertTrue(!matcher.match(to, from,  Topic.createTopics(new String[]{})));
    }
}
