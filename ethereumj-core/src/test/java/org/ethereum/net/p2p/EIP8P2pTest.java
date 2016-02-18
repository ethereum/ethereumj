package org.ethereum.net.p2p;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.spongycastle.util.encoders.Hex.decode;

/**
 * @author Mikhail Kalinin
 * @since 18.02.2016
 */
public class EIP8P2pTest {

    // devp2p hello packet advertising version 55 and containing a few additional list elements
    @Test
    public void test1() {

        HelloMessage msg = new HelloMessage(decode(
                "f87137916b6e6574682f76302e39312f706c616e39cdc5836574683dc6846d6f726b1682270fb840" +
                "fda1cff674c90c9a197539fe3dfb53086ace64f83ed7c6eabec741f7f381cc803e52ab2cd55d5569" +
                "bce4347107a310dfd5f88a010cd2ffd1005ca406f1842877c883666f6f836261720304"));

        assertEquals(55, msg.getP2PVersion());
    }
}
