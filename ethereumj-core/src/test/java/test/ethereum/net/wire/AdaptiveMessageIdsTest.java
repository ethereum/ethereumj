package test.ethereum.net.wire;

import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.eth.EthMessageCodes;
import org.ethereum.net.p2p.P2pHandler;
import org.ethereum.net.p2p.P2pMessageCodes;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.net.shh.ShhMessageCodes;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Roman Mandeleil
 * @since 15.10.2014
 */
public class AdaptiveMessageIdsTest {

    @Before
    public void setUp() {
        EthMessageCodes.setOffset((byte) 0x00);
        ShhMessageCodes.setOffset((byte) 0x00);
    }

    @Test
    public void test1() {

        Assert.assertEquals(7, P2pMessageCodes.values().length);

        Assert.assertEquals(0, P2pMessageCodes.HELLO.asByte());
        Assert.assertEquals(1, P2pMessageCodes.DISCONNECT.asByte());
        Assert.assertEquals(2, P2pMessageCodes.PING.asByte());
        Assert.assertEquals(3, P2pMessageCodes.PONG.asByte());
        Assert.assertEquals(4, P2pMessageCodes.GET_PEERS.asByte());
        Assert.assertEquals(5, P2pMessageCodes.PEERS.asByte());
        Assert.assertEquals(15, P2pMessageCodes.USER.asByte());
    }

    @Test
    public void test2() {

        Assert.assertEquals(9, EthMessageCodes.values().length);

        Assert.assertEquals(0, EthMessageCodes.STATUS.asByte());
        Assert.assertEquals(1, EthMessageCodes.GET_TRANSACTIONS.asByte());
        Assert.assertEquals(2, EthMessageCodes.TRANSACTIONS.asByte());
        Assert.assertEquals(3, EthMessageCodes.GET_BLOCK_HASHES.asByte());
        Assert.assertEquals(4, EthMessageCodes.BLOCK_HASHES.asByte());
        Assert.assertEquals(5, EthMessageCodes.GET_BLOCKS.asByte());
        Assert.assertEquals(6, EthMessageCodes.BLOCKS.asByte());
        Assert.assertEquals(7, EthMessageCodes.NEW_BLOCK.asByte());
        Assert.assertEquals(8, EthMessageCodes.PACKET_COUNT.asByte());

        EthMessageCodes.setOffset((byte) 0x10);
        Assert.assertEquals(0x10 + 0, EthMessageCodes.STATUS.asByte());
        Assert.assertEquals(0x10 + 1, EthMessageCodes.GET_TRANSACTIONS.asByte());
        Assert.assertEquals(0x10 + 2, EthMessageCodes.TRANSACTIONS.asByte());
        Assert.assertEquals(0x10 + 3, EthMessageCodes.GET_BLOCK_HASHES.asByte());
        Assert.assertEquals(0x10 + 4, EthMessageCodes.BLOCK_HASHES.asByte());
        Assert.assertEquals(0x10 + 5, EthMessageCodes.GET_BLOCKS.asByte());
        Assert.assertEquals(0x10 + 6, EthMessageCodes.BLOCKS.asByte());
        Assert.assertEquals(0x10 + 7, EthMessageCodes.NEW_BLOCK.asByte());
        Assert.assertEquals(0x10 + 8, EthMessageCodes.PACKET_COUNT.asByte());
    }

    @Test
    public void test3() {

        Assert.assertEquals(5, ShhMessageCodes.values().length);

        Assert.assertEquals(0, ShhMessageCodes.STATUS.asByte());
        Assert.assertEquals(1, ShhMessageCodes.MESSAGE.asByte());
        Assert.assertEquals(2, ShhMessageCodes.ADD_FILTER.asByte());
        Assert.assertEquals(3, ShhMessageCodes.REMOVE_FILTER.asByte());
        Assert.assertEquals(4, ShhMessageCodes.PACKET_COUNT.asByte());

        ShhMessageCodes.setOffset((byte) 0x20);
        Assert.assertEquals(0x20 + 0, ShhMessageCodes.STATUS.asByte());
        Assert.assertEquals(0x20 + 1, ShhMessageCodes.MESSAGE.asByte());
        Assert.assertEquals(0x20 + 2, ShhMessageCodes.ADD_FILTER.asByte());
        Assert.assertEquals(0x20 + 3, ShhMessageCodes.REMOVE_FILTER.asByte());
        Assert.assertEquals(0x20 + 4, ShhMessageCodes.PACKET_COUNT.asByte());
    }

    @Test
    public void test4() {

        P2pHandler p2pHandler = new P2pHandler();

        List<Capability> capabilities = Arrays.asList(
                new Capability(Capability.ETH, EthHandler.VERSION),
                new Capability(Capability.SHH, ShhHandler.VERSION));
        p2pHandler.adaptMessageIds(capabilities);

        Assert.assertEquals(0x10 + 0, EthMessageCodes.STATUS.asByte());
        Assert.assertEquals(0x10 + 1, EthMessageCodes.GET_TRANSACTIONS.asByte());
        Assert.assertEquals(0x10 + 2, EthMessageCodes.TRANSACTIONS.asByte());
        Assert.assertEquals(0x10 + 3, EthMessageCodes.GET_BLOCK_HASHES.asByte());
        Assert.assertEquals(0x10 + 4, EthMessageCodes.BLOCK_HASHES.asByte());
        Assert.assertEquals(0x10 + 5, EthMessageCodes.GET_BLOCKS.asByte());
        Assert.assertEquals(0x10 + 6, EthMessageCodes.BLOCKS.asByte());
        Assert.assertEquals(0x10 + 7, EthMessageCodes.NEW_BLOCK.asByte());
        Assert.assertEquals(0x10 + 8, EthMessageCodes.PACKET_COUNT.asByte());

        Assert.assertEquals(0x19 + 0, ShhMessageCodes.STATUS.asByte());
        Assert.assertEquals(0x19 + 1, ShhMessageCodes.MESSAGE.asByte());
        Assert.assertEquals(0x19 + 2, ShhMessageCodes.ADD_FILTER.asByte());
        Assert.assertEquals(0x19 + 3, ShhMessageCodes.REMOVE_FILTER.asByte());
        Assert.assertEquals(0x19 + 4, ShhMessageCodes.PACKET_COUNT.asByte());
    }

    @Test // Capabilities should be read in alphabetical order
    public void test5() {

        P2pHandler p2pHandler = new P2pHandler();

        List<Capability> capabilities = Arrays.asList(
                new Capability(Capability.SHH, ShhHandler.VERSION),
                new Capability(Capability.ETH, EthHandler.VERSION));
        p2pHandler.adaptMessageIds(capabilities);

        Assert.assertEquals(0x10 + 0, EthMessageCodes.STATUS.asByte());
        Assert.assertEquals(0x10 + 1, EthMessageCodes.GET_TRANSACTIONS.asByte());
        Assert.assertEquals(0x10 + 2, EthMessageCodes.TRANSACTIONS.asByte());
        Assert.assertEquals(0x10 + 3, EthMessageCodes.GET_BLOCK_HASHES.asByte());
        Assert.assertEquals(0x10 + 4, EthMessageCodes.BLOCK_HASHES.asByte());
        Assert.assertEquals(0x10 + 5, EthMessageCodes.GET_BLOCKS.asByte());
        Assert.assertEquals(0x10 + 6, EthMessageCodes.BLOCKS.asByte());
        Assert.assertEquals(0x10 + 7, EthMessageCodes.NEW_BLOCK.asByte());
        Assert.assertEquals(0x10 + 8, EthMessageCodes.PACKET_COUNT.asByte());

        Assert.assertEquals(0x19 + 0, ShhMessageCodes.STATUS.asByte());
        Assert.assertEquals(0x19 + 1, ShhMessageCodes.MESSAGE.asByte());
        Assert.assertEquals(0x19 + 2, ShhMessageCodes.ADD_FILTER.asByte());
        Assert.assertEquals(0x19 + 3, ShhMessageCodes.REMOVE_FILTER.asByte());
        Assert.assertEquals(0x19 + 4, ShhMessageCodes.PACKET_COUNT.asByte());
    }
}
