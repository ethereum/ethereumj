package org.ethereum.net.wire;

import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.eth.EthMessageCodes;
import org.ethereum.net.p2p.P2pHandler;
import org.ethereum.net.p2p.P2pMessageCodes;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.net.shh.ShhMessageCodes;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

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

    @After
    public void tearDown() {
        EthMessageCodes.setOffset((byte) 0x00);
        ShhMessageCodes.setOffset((byte) 0x00);
    }

    @Test
    public void test1() {

        assertEquals(7, P2pMessageCodes.values().length);

        assertEquals(0, P2pMessageCodes.HELLO.asByte());
        assertEquals(1, P2pMessageCodes.DISCONNECT.asByte());
        assertEquals(2, P2pMessageCodes.PING.asByte());
        assertEquals(3, P2pMessageCodes.PONG.asByte());
        assertEquals(4, P2pMessageCodes.GET_PEERS.asByte());
        assertEquals(5, P2pMessageCodes.PEERS.asByte());
        assertEquals(15, P2pMessageCodes.USER.asByte());
    }

    @Test
    public void test2() {

        assertEquals(8, EthMessageCodes.values().length);

        assertEquals(0, EthMessageCodes.STATUS.asByte());
        assertEquals(1, EthMessageCodes.GET_TRANSACTIONS.asByte());
        assertEquals(2, EthMessageCodes.TRANSACTIONS.asByte());
        assertEquals(3, EthMessageCodes.GET_BLOCK_HASHES.asByte());
        assertEquals(4, EthMessageCodes.BLOCK_HASHES.asByte());
        assertEquals(5, EthMessageCodes.GET_BLOCKS.asByte());
        assertEquals(6, EthMessageCodes.BLOCKS.asByte());
        assertEquals(7, EthMessageCodes.NEW_BLOCK.asByte());

        EthMessageCodes.setOffset((byte) 0x10);
        assertEquals(0x10 + 0, EthMessageCodes.STATUS.asByte());
        assertEquals(0x10 + 1, EthMessageCodes.GET_TRANSACTIONS.asByte());
        assertEquals(0x10 + 2, EthMessageCodes.TRANSACTIONS.asByte());
        assertEquals(0x10 + 3, EthMessageCodes.GET_BLOCK_HASHES.asByte());
        assertEquals(0x10 + 4, EthMessageCodes.BLOCK_HASHES.asByte());
        assertEquals(0x10 + 5, EthMessageCodes.GET_BLOCKS.asByte());
        assertEquals(0x10 + 6, EthMessageCodes.BLOCKS.asByte());
        assertEquals(0x10 + 7, EthMessageCodes.NEW_BLOCK.asByte());
    }

    @Test
    public void test3() {

        assertEquals(5, ShhMessageCodes.values().length);

        assertEquals(0, ShhMessageCodes.STATUS.asByte());
        assertEquals(1, ShhMessageCodes.MESSAGE.asByte());
        assertEquals(2, ShhMessageCodes.ADD_FILTER.asByte());
        assertEquals(3, ShhMessageCodes.REMOVE_FILTER.asByte());
        assertEquals(4, ShhMessageCodes.PACKET_COUNT.asByte());

        ShhMessageCodes.setOffset((byte) 0x20);
        assertEquals(0x20 + 0, ShhMessageCodes.STATUS.asByte());
        assertEquals(0x20 + 1, ShhMessageCodes.MESSAGE.asByte());
        assertEquals(0x20 + 2, ShhMessageCodes.ADD_FILTER.asByte());
        assertEquals(0x20 + 3, ShhMessageCodes.REMOVE_FILTER.asByte());
        assertEquals(0x20 + 4, ShhMessageCodes.PACKET_COUNT.asByte());
    }

    @Test
    public void test4() {

        P2pHandler p2pHandler = new P2pHandler();

        List<Capability> capabilities = Arrays.asList(
                new Capability(Capability.ETH, EthHandler.VERSION),
                new Capability(Capability.SHH, ShhHandler.VERSION));
        p2pHandler.adaptMessageIds(capabilities);

        assertEquals(0x10 + 0, EthMessageCodes.STATUS.asByte());
        assertEquals(0x10 + 1, EthMessageCodes.GET_TRANSACTIONS.asByte());
        assertEquals(0x10 + 2, EthMessageCodes.TRANSACTIONS.asByte());
        assertEquals(0x10 + 3, EthMessageCodes.GET_BLOCK_HASHES.asByte());
        assertEquals(0x10 + 4, EthMessageCodes.BLOCK_HASHES.asByte());
        assertEquals(0x10 + 5, EthMessageCodes.GET_BLOCKS.asByte());
        assertEquals(0x10 + 6, EthMessageCodes.BLOCKS.asByte());
        assertEquals(0x10 + 7, EthMessageCodes.NEW_BLOCK.asByte());

        assertEquals(0x19 + 0, ShhMessageCodes.STATUS.asByte());
        assertEquals(0x19 + 1, ShhMessageCodes.MESSAGE.asByte());
        assertEquals(0x19 + 2, ShhMessageCodes.ADD_FILTER.asByte());
        assertEquals(0x19 + 3, ShhMessageCodes.REMOVE_FILTER.asByte());
        assertEquals(0x19 + 4, ShhMessageCodes.PACKET_COUNT.asByte());
    }

    @Test // Capabilities should be read in alphabetical order
    public void test5() {

        P2pHandler p2pHandler = new P2pHandler();

        List<Capability> capabilities = Arrays.asList(
                new Capability(Capability.SHH, ShhHandler.VERSION),
                new Capability(Capability.ETH, EthHandler.VERSION));
        p2pHandler.adaptMessageIds(capabilities);

        assertEquals(0x10 + 0, EthMessageCodes.STATUS.asByte());
        assertEquals(0x10 + 1, EthMessageCodes.GET_TRANSACTIONS.asByte());
        assertEquals(0x10 + 2, EthMessageCodes.TRANSACTIONS.asByte());
        assertEquals(0x10 + 3, EthMessageCodes.GET_BLOCK_HASHES.asByte());
        assertEquals(0x10 + 4, EthMessageCodes.BLOCK_HASHES.asByte());
        assertEquals(0x10 + 5, EthMessageCodes.GET_BLOCKS.asByte());
        assertEquals(0x10 + 6, EthMessageCodes.BLOCKS.asByte());
        assertEquals(0x10 + 7, EthMessageCodes.NEW_BLOCK.asByte());

        assertEquals(0x19 + 0, ShhMessageCodes.STATUS.asByte());
        assertEquals(0x19 + 1, ShhMessageCodes.MESSAGE.asByte());
        assertEquals(0x19 + 2, ShhMessageCodes.ADD_FILTER.asByte());
        assertEquals(0x19 + 3, ShhMessageCodes.REMOVE_FILTER.asByte());
        assertEquals(0x19 + 4, ShhMessageCodes.PACKET_COUNT.asByte());
    }
}
