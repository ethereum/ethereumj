package org.ethereum.net.shh;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertEquals;

public class TopicTest {

    @Test
    public void test1(){
        Topic topic = new Topic("cow");
        assertEquals("91887637", Hex.toHexString(topic.getBytes()));
    }

    @Test
    public void test2(){
        Topic topic = new Topic("cowcowcow");
        assertEquals("3a6de614", Hex.toHexString(topic.getBytes()));
    }

}
