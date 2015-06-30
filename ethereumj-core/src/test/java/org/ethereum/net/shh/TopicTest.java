package org.ethereum.net.shh;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertEquals;

public class TopicTest {

    @Test
    public void test1(){
        Topic topic = new Topic("cow");
        assertEquals("c85ef7d7", Hex.toHexString(topic.getBytes()));
    }

    @Test
    public void test2(){
        Topic topic = new Topic("cowcowcow");
        assertEquals("25068349", Hex.toHexString(topic.getBytes()));
    }

}
