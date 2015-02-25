package test.ethereum.net;

import org.ethereum.net.rlpx.*;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.util.ByteUtil.merge;
import static org.junit.Assert.assertEquals;

public class RLPXTest {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("test");

    @Test // ping test
    public void test1(){

        String ip = "85.65.19.231";
        int port = 30303;
        long expiration = System.currentTimeMillis();

        Message ping = PingMessage.create(ip, port);
        logger.info("{}", ping);

        byte[] wire = ping.getPacket();
        PingMessage ping2 = (PingMessage)Message.decode(wire);
        logger.info("{}", ping2);

        assertEquals(ping.toString(), ping2.toString());
    }

    @Test // pong test
    public void test2(){

        byte[] token = sha3("+++".getBytes(Charset.forName("UTF-8")));

        Message pong = PongMessage.create(token);
        logger.info("{}", pong);

        byte[] wire = pong.getPacket();
        PongMessage pong2 = (PongMessage)Message.decode(wire);
        logger.info("{}", pong);

        assertEquals(pong.toString(), pong2.toString());
    }

    @Test // neighbors message
    public void test3(){

        String ip = "85.65.19.231";
        int port = 30303;

        byte[] part1 = sha3("007".getBytes(Charset.forName("UTF-8")));
        byte[] part2 = sha3("007".getBytes(Charset.forName("UTF-8")));
        byte[] id = merge(part1, part2);

        Node node = new Node(id, ip, port);

        List<Node> nodes = Arrays.asList(node);
        Message neighbors = NeighborsMessage.create(nodes);
        logger.info("{}", neighbors);

        byte[] wire = neighbors.getPacket();
        NeighborsMessage neighbors2 = (NeighborsMessage)Message.decode(wire);
        logger.info("{}", neighbors2);

        assertEquals(neighbors.toString(), neighbors2.toString());
    }

    @Test // find node message
    public void test4(){

        byte[] id = sha3("+++".getBytes(Charset.forName("UTF-8")));

        Message findNode = FindNodeMessage.create(id);
        logger.info("{}", findNode);

        byte[] wire = findNode.getPacket();
        FindNodeMessage findNode2 = (FindNodeMessage)Message.decode(wire);
        logger.info("{}", findNode2);

        assertEquals(findNode.toString(), findNode2.toString());
    }


    @Test (expected = Error.class)// failure on MDC
    public void test5(){

        byte[] id = sha3("+++".getBytes(Charset.forName("UTF-8")));

        Message findNode = FindNodeMessage.create(id);
        logger.info("{}", findNode);

        byte[] wire = findNode.getPacket();
        wire[64] = 0;

        FindNodeMessage findNode2 = (FindNodeMessage)Message.decode(wire);
        logger.info("{}", findNode2);

        assertEquals(findNode.toString(), findNode2.toString());
    }

}
