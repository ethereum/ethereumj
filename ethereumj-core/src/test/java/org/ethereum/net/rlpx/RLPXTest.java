/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.net.rlpx;

import org.ethereum.crypto.ECKey;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.util.ByteUtil.merge;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RLPXTest {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("test");

    @Test // ping test
    public void test1() {

        Node node = Node.instanceOf("85.65.19.231:30303");
        ECKey key = ECKey.fromPrivate(BigInteger.TEN);

        Message ping = PingMessage.create(node, node, key);
        logger.info("{}", ping);

        byte[] wire = ping.getPacket();
        PingMessage ping2 = (PingMessage) Message.decode(wire);
        logger.info("{}", ping2);

        assertEquals(ping.toString(), ping2.toString());

        String key2 = ping2.getKey().toString();
        assertEquals(key.toString(), key2.toString());
    }

    @Test // pong test
    public void test2() {

        byte[] token = sha3("+++".getBytes(Charset.forName("UTF-8")));
        ECKey key = ECKey.fromPrivate(BigInteger.TEN);

        Message pong = PongMessage.create(token, key);
        logger.info("{}", pong);

        byte[] wire = pong.getPacket();
        PongMessage pong2 = (PongMessage) Message.decode(wire);
        logger.info("{}", pong);

        assertEquals(pong.toString(), pong2.toString());

        String key2 = pong2.getKey().toString();
        assertEquals(key.toString(), key2.toString());
    }

    @Test // neighbors message
    public void test3() {

        String ip = "85.65.19.231";
        int port = 30303;

        byte[] part1 = sha3("007".getBytes(Charset.forName("UTF-8")));
        byte[] part2 = sha3("007".getBytes(Charset.forName("UTF-8")));
        byte[] id = merge(part1, part2);

        Node node = new Node(id, ip, port);

        List<Node> nodes = Collections.singletonList(node);
        ECKey key = ECKey.fromPrivate(BigInteger.TEN);

        Message neighbors = NeighborsMessage.create(nodes, key);
        logger.info("{}", neighbors);

        byte[] wire = neighbors.getPacket();
        NeighborsMessage neighbors2 = (NeighborsMessage) Message.decode(wire);
        logger.info("{}", neighbors2);

        assertEquals(neighbors.toString(), neighbors2.toString());

        String key2 = neighbors2.getKey().toString();
        assertEquals(key.toString(), key2.toString());
    }

    @Test // find node message
    public void test4() {

        byte[] id = sha3("+++".getBytes(Charset.forName("UTF-8")));
        ECKey key = ECKey.fromPrivate(BigInteger.TEN);

        Message findNode = FindNodeMessage.create(id, key);
        logger.info("{}", findNode);

        byte[] wire = findNode.getPacket();
        FindNodeMessage findNode2 = (FindNodeMessage) Message.decode(wire);
        logger.info("{}", findNode2);

        assertEquals(findNode.toString(), findNode2.toString());

        String key2 = findNode2.getKey().toString();
        assertEquals(key.toString(), key2.toString());
    }


    @Test(expected = Exception.class)// failure on MDC
    public void test5() {

        byte[] id = sha3("+++".getBytes(Charset.forName("UTF-8")));
        ECKey key = ECKey.fromPrivate(BigInteger.TEN);

        Message findNode = FindNodeMessage.create(id, key);
        logger.info("{}", findNode);

        byte[] wire = findNode.getPacket();
        wire[64]++;

        FindNodeMessage findNode2 = (FindNodeMessage) Message.decode(wire);
        logger.info("{}", findNode2);

        assertEquals(findNode.toString(), findNode2.toString());
    }


    @Test
    public void test6() {

        byte[] id_1 = sha3("+++".getBytes(Charset.forName("UTF-8")));
        String host_1 = "85.65.19.231";
        int port_1 = 30303;

        Node node_1 = new Node(id_1, host_1 , port_1);
        Node node_2 = new Node(node_1.getRLP());

        byte[] id_2 = node_2.getId();
        String host_2 = node_2.getHost();
        int port_2 = node_2.getPort();

        assertEquals(Hex.toHexString(id_1), Hex.toHexString(id_2));
        assertEquals(host_1, host_2);
        assertTrue(port_1 == port_2);
    }


    @Test  // Neighbors parse data
    public void test7() {

        byte[] wire =
                Hex.decode("d5106e888eeca1e0b4a93bf17c325f912b43ca4176a000966619aa6a96ac9d5a60e66c73ed5629c13d4d0c806a3127379541e8d90d7fcb52c33c5e36557ad92dfed9619fcd3b92e42683aed89bd3c6eef6b59bd0237c36d83ebb0075a59903f50104f90200f901f8f8528c38352e36352e31392e32333182f310b840aeb2dd107edd996adf1bbf835fb3f9a11aabb7ed3dfef84c7a3c8767482bff522906a11e8cddee969153bf5944e64e37943db509bb4cc714c217f20483802ec0f8528c38352e36352e31392e32333182e5b4b840b70cdf8f23024a65afbf12110ca06fa5c37bd9fe4f6234a0120cdaaf16e8bb96d090d0164c316aaa18158d346e9b0a29ad9bfa0404ab4ee9906adfbacb01c21bf8528c38352e36352e31392e32333182df38b840ed8e01b5f5468f32de23a7524af1b35605ffd7cdb79af4eacd522c94f8ed849bb81dfed4992c179caeef0952ecad2d868503164a434c300356b369a33c159289f8528c38352e36352e31392e32333182df38b840136996f11c2c80f231987fc4f0cbd061cb021c63afaf5dd879e7c851a57be8d023af14bc201be81588ecab7971693b3f689a4854df74ad2e2334e88ae76aa122f8528c38352e36352e31392e32333182f303b840742eac32e1e2343b89c03a20fc051854ea6a3ff28ca918d1994fe1e32d6d77ab63352131db3ed0e7d6cc057d859c114b102f49052daee3d1c5f5fdaab972e655f8528c38352e36352e31392e32333182f310b8407d9e1f9ceb66fc21787b830554d604f933be203be9366710fb33355975e874a72b87837cf28b1b9ae171826b64e3c5d178326cbf71f89b3dec614816a1a40ce38454f6b578");

        NeighborsMessage msg1 = (NeighborsMessage) NeighborsMessage.decode(wire);

        ECKey key = ECKey.fromPrivate(BigInteger.TEN);
        NeighborsMessage msg2 = (NeighborsMessage) NeighborsMessage.create(msg1.getNodes(), key);

        NeighborsMessage msg3 = (NeighborsMessage) NeighborsMessage.decode(msg2.getPacket());

        for (int i = 0; i < msg1.getNodes().size(); ++i) {

            Node node_1 = msg1.getNodes().get(i);
            Node node_3 = msg3.getNodes().get(i);

            assertEquals(node_1.toString(), node_3.toString());
        }

        System.out.println(msg1);

    }


    @Test  // FindNodeMessage parse data
    public void test8() {

        byte[] wire =
                Hex.decode("3770d98825a42cb69edf70ffdf8d6d2b28a8c5499a7e3350e4a42c94652339cac3f8e9c3b5a181c8dd13e491ad9229f6a8bd018d786e1fb9e5264f43bbd6ce93af9bc85b468dee651bcd518561f83cb166da7aef7e506057dc2fbb2ea582bcc00003f847b84083fba54f6bb80ce31f6d5d1ec0a9a2e4685bc185115b01da6dcb70cd13116a6bd08b86ffe60b7d7ea56c6498848e3741113f8e70b9f0d12dbfe895680d03fd658454f6e772");

        FindNodeMessage msg1 = (FindNodeMessage) FindNodeMessage.decode(wire);

        ECKey key = ECKey.fromPrivate(BigInteger.TEN);
        FindNodeMessage msg2 = FindNodeMessage.create(msg1.getTarget(), key);

        FindNodeMessage msg3 = (FindNodeMessage) FindNodeMessage.decode(msg2.getPacket());

        Assert.assertEquals(Hex.toHexString(msg1.getTarget()), Hex.toHexString(msg3.getTarget()));
    }

    @Ignore //TODO #POC9
    @Test  // Ping parse data
    public void test9() {
//        wire: 4c62e1b75f4003ef77032006a142bbf31772936a1e5098566b28a04a5c71c73f1f2c9f539a85458c50a554de12da9d7e69fb2507f7c0788885508d385bbe7a9538fa675712aa1eaad29902bb46eee4531d00a10fd81179e4151929f60fec4dc50001ce87302e302e302e30808454f8483c
//        PingMessage: {mdc=4c62e1b75f4003ef77032006a142bbf31772936a1e5098566b28a04a5c71c73f, signature=1f2c9f539a85458c50a554de12da9d7e69fb2507f7c0788885508d385bbe7a9538fa675712aa1eaad29902bb46eee4531d00a10fd81179e4151929f60fec4dc500, type=01, data=ce87302e302e302e30808454f8483c}

        // FIXME: wire contains empty from data
        byte[] wire =
                Hex.decode("4c62e1b75f4003ef77032006a142bbf31772936a1e5098566b28a04a5c71c73f1f2c9f539a85458c50a554de12da9d7e69fb2507f7c0788885508d385bbe7a9538fa675712aa1eaad29902bb46eee4531d00a10fd81179e4151929f60fec4dc50001ce87302e302e302e30808454f8483c");

        PingMessage msg1 = (PingMessage)Message.decode(wire);

        ECKey key = ECKey.fromPrivate(BigInteger.TEN);
        Node node = Node.instanceOf(msg1.getToHost() + ":" + msg1.getToPort());
        PingMessage msg2 =  PingMessage.create(node, node, key);
        PingMessage msg3 = (PingMessage)Message.decode(msg2.getPacket());

        assertEquals(msg1.getToHost(), msg3.getToHost());
    }


    @Test  // Pong parse data
    public void test10(){
//        wire: 84db9bf6a1f7a3444f4d4946155da16c63a51abdd6822ac683d8243f260b99b265601b769acebfe3c76ddeb6e83e924f2bac2beca0c802ff0745d349bd58bc6662d62d38c2a3bb3e167a333d7d099496ebd35e096c5c1ee1587e9bd11f20e3d80002e6a079d49bdba3a7acfc9a2881d768d1aa246c2486ab166f0305a863bd47c5d21e0e8454f8483c
//        PongMessage: {mdc=84db9bf6a1f7a3444f4d4946155da16c63a51abdd6822ac683d8243f260b99b2, signature=65601b769acebfe3c76ddeb6e83e924f2bac2beca0c802ff0745d349bd58bc6662d62d38c2a3bb3e167a333d7d099496ebd35e096c5c1ee1587e9bd11f20e3d800, type=02, data=e6a079d49bdba3a7acfc9a2881d768d1aa246c2486ab166f0305a863bd47c5d21e0e8454f8483c}

        byte[] wire =
                Hex.decode("84db9bf6a1f7a3444f4d4946155da16c63a51abdd6822ac683d8243f260b99b265601b769acebfe3c76ddeb6e83e924f2bac2beca0c802ff0745d349bd58bc6662d62d38c2a3bb3e167a333d7d099496ebd35e096c5c1ee1587e9bd11f20e3d80002e6a079d49bdba3a7acfc9a2881d768d1aa246c2486ab166f0305a863bd47c5d21e0e8454f8483c");

        PongMessage msg1 = (PongMessage)Message.decode(wire);

        ECKey key = ECKey.fromPrivate(BigInteger.TEN);
        PongMessage msg2 = PongMessage.create(msg1.getToken(), key, 1448375807);

        PongMessage msg3 = (PongMessage) Message.decode(msg2.getPacket());
        assertEquals(Hex.toHexString(msg1.getToken()), Hex.toHexString(msg3.getToken()));
    }

    /**
     * Correct encoding of IP addresses according to official RLPx protocol documentation
     * https://github.com/ethereum/devp2p/blob/master/rlpx.md
     */
    @Test
    public void testCorrectIpPing() {
        //  {mdc=d7a3a7ce591180e2f6d6f8655ece88fe3d98fff2b9896578712f77aabb8394eb,
        //      signature=6a436c85ad30842cb64451f9a5705b96089b37ad7705cf28ee15e51be55a9b756fe178371d28961aa432ce625fb313fd8e6c8607a776107115bafdd591e89dab00,
        //      type=01, data=e804d7900000000000000000000000000000000082765f82765fc9843a8808ba8233d88084587328cd}
        byte[] wire =
                Hex.decode("d7a3a7ce591180e2f6d6f8655ece88fe3d98fff2b9896578712f77aabb8394eb6a436c85ad30842cb64451f9a5705b96089b37ad7705cf28ee15e51be55a9b756fe178371d28961aa432ce625fb313fd8e6c8607a776107115bafdd591e89dab0001e804d7900000000000000000000000000000000082765f82765fc9843a8808ba8233d88084587328cd");

        PingMessage msg1 = (PingMessage) Message.decode(wire);
        assertEquals(30303, msg1.getFromPort());
        assertEquals("0.0.0.0", msg1.getFromHost());
        assertEquals(13272, msg1.getToPort());
        assertEquals("58.136.8.186", msg1.getToHost());
    }
}










