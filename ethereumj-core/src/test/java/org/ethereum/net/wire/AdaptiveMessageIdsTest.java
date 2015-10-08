package org.ethereum.net.wire;

import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.message.EthMessageCodes;
import org.ethereum.net.p2p.P2pMessageCodes;
import org.ethereum.net.rlpx.MessageCodesResolver;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.net.shh.ShhMessageCodes;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

import static org.ethereum.net.eth.EthVersion.*;

/**
 * @author Roman Mandeleil
 * @since 15.10.2014
 */
public class AdaptiveMessageIdsTest {

    private MessageCodesResolver messageCodesResolver;

    @Before
    public void setUp() {
        messageCodesResolver = new MessageCodesResolver();
    }

    @Test
    public void test1() {

        assertEquals(7, P2pMessageCodes.values().length);

        assertEquals(0, messageCodesResolver.withP2pOffset(P2pMessageCodes.HELLO.asByte()));
        assertEquals(1, messageCodesResolver.withP2pOffset(P2pMessageCodes.DISCONNECT.asByte()));
        assertEquals(2, messageCodesResolver.withP2pOffset(P2pMessageCodes.PING.asByte()));
        assertEquals(3, messageCodesResolver.withP2pOffset(P2pMessageCodes.PONG.asByte()));
        assertEquals(4, messageCodesResolver.withP2pOffset(P2pMessageCodes.GET_PEERS.asByte()));
        assertEquals(5, messageCodesResolver.withP2pOffset(P2pMessageCodes.PEERS.asByte()));
        assertEquals(15, messageCodesResolver.withP2pOffset(P2pMessageCodes.USER.asByte()));
    }

    @Test
    public void test2() {

        assertEquals(8, EthMessageCodes.values(V60).length);
        assertEquals(9, EthMessageCodes.values(V61).length);
        assertEquals(8, EthMessageCodes.values(V62).length);

        assertEquals(0, EthMessageCodes.STATUS.asByte());
        assertEquals(1, EthMessageCodes.NEW_BLOCK_HASHES.asByte());
        assertEquals(2, EthMessageCodes.TRANSACTIONS.asByte());
        assertEquals(3, EthMessageCodes.GET_BLOCK_HASHES.asByte());
        assertEquals(4, EthMessageCodes.BLOCK_HASHES.asByte());
        assertEquals(5, EthMessageCodes.GET_BLOCKS.asByte());
        assertEquals(6, EthMessageCodes.BLOCKS.asByte());
        assertEquals(7, EthMessageCodes.NEW_BLOCK.asByte());
        assertEquals(8, EthMessageCodes.GET_BLOCK_HASHES_BY_NUMBER.asByte());

        messageCodesResolver.setEthOffset(0x10);

        assertEquals(0x10 + 0, messageCodesResolver.withEthOffset(EthMessageCodes.STATUS.asByte()));
        assertEquals(0x10 + 1, messageCodesResolver.withEthOffset(EthMessageCodes.NEW_BLOCK_HASHES.asByte()));
        assertEquals(0x10 + 2, messageCodesResolver.withEthOffset(EthMessageCodes.TRANSACTIONS.asByte()));
        assertEquals(0x10 + 3, messageCodesResolver.withEthOffset(EthMessageCodes.GET_BLOCK_HASHES.asByte()));
        assertEquals(0x10 + 4, messageCodesResolver.withEthOffset(EthMessageCodes.BLOCK_HASHES.asByte()));
        assertEquals(0x10 + 5, messageCodesResolver.withEthOffset(EthMessageCodes.GET_BLOCKS.asByte()));
        assertEquals(0x10 + 6, messageCodesResolver.withEthOffset(EthMessageCodes.BLOCKS.asByte()));
        assertEquals(0x10 + 7, messageCodesResolver.withEthOffset(EthMessageCodes.NEW_BLOCK.asByte()));
        assertEquals(0x10 + 8, messageCodesResolver.withEthOffset(EthMessageCodes.GET_BLOCK_HASHES_BY_NUMBER.asByte()));
    }

    @Test
    public void test3() {

        assertEquals(3, ShhMessageCodes.values().length);

        assertEquals(0, ShhMessageCodes.STATUS.asByte());
        assertEquals(1, ShhMessageCodes.MESSAGE.asByte());
        assertEquals(2, ShhMessageCodes.FILTER.asByte());

        messageCodesResolver.setShhOffset(0x20);

        assertEquals(0x20 + 0, messageCodesResolver.withShhOffset(ShhMessageCodes.STATUS.asByte()));
        assertEquals(0x20 + 1, messageCodesResolver.withShhOffset(ShhMessageCodes.MESSAGE.asByte()));
        assertEquals(0x20 + 2, messageCodesResolver.withShhOffset(ShhMessageCodes.FILTER.asByte()));
    }

    @Test
    public void test4() {

        List<Capability> capabilities = Arrays.asList(
                new Capability(Capability.ETH, EthVersion.V61.getCode()),
                new Capability(Capability.SHH, ShhHandler.VERSION));

        messageCodesResolver.init(capabilities);

        assertEquals(0x10 + 0, messageCodesResolver.withEthOffset(EthMessageCodes.STATUS.asByte()));
        assertEquals(0x10 + 1, messageCodesResolver.withEthOffset(EthMessageCodes.NEW_BLOCK_HASHES.asByte()));
        assertEquals(0x10 + 2, messageCodesResolver.withEthOffset(EthMessageCodes.TRANSACTIONS.asByte()));
        assertEquals(0x10 + 3, messageCodesResolver.withEthOffset(EthMessageCodes.GET_BLOCK_HASHES.asByte()));
        assertEquals(0x10 + 4, messageCodesResolver.withEthOffset(EthMessageCodes.BLOCK_HASHES.asByte()));
        assertEquals(0x10 + 5, messageCodesResolver.withEthOffset(EthMessageCodes.GET_BLOCKS.asByte()));
        assertEquals(0x10 + 6, messageCodesResolver.withEthOffset(EthMessageCodes.BLOCKS.asByte()));
        assertEquals(0x10 + 7, messageCodesResolver.withEthOffset(EthMessageCodes.NEW_BLOCK.asByte()));
        assertEquals(0x10 + 8, messageCodesResolver.withEthOffset(EthMessageCodes.GET_BLOCK_HASHES_BY_NUMBER.asByte()));

        assertEquals(0x19 + 0, messageCodesResolver.withShhOffset(ShhMessageCodes.STATUS.asByte()));
        assertEquals(0x19 + 1, messageCodesResolver.withShhOffset(ShhMessageCodes.MESSAGE.asByte()));
        assertEquals(0x19 + 2, messageCodesResolver.withShhOffset(ShhMessageCodes.FILTER.asByte()));
    }

    @Test // Capabilities should be read in alphabetical order
    public void test5() {

        List<Capability> capabilities = Arrays.asList(
                new Capability(Capability.SHH, ShhHandler.VERSION),
                new Capability(Capability.ETH, EthVersion.V61.getCode()));

        messageCodesResolver.init(capabilities);

        assertEquals(0x10 + 0, messageCodesResolver.withEthOffset(EthMessageCodes.STATUS.asByte()));
        assertEquals(0x10 + 1, messageCodesResolver.withEthOffset(EthMessageCodes.NEW_BLOCK_HASHES.asByte()));
        assertEquals(0x10 + 2, messageCodesResolver.withEthOffset(EthMessageCodes.TRANSACTIONS.asByte()));
        assertEquals(0x10 + 3, messageCodesResolver.withEthOffset(EthMessageCodes.GET_BLOCK_HASHES.asByte()));
        assertEquals(0x10 + 4, messageCodesResolver.withEthOffset(EthMessageCodes.BLOCK_HASHES.asByte()));
        assertEquals(0x10 + 5, messageCodesResolver.withEthOffset(EthMessageCodes.GET_BLOCKS.asByte()));
        assertEquals(0x10 + 6, messageCodesResolver.withEthOffset(EthMessageCodes.BLOCKS.asByte()));
        assertEquals(0x10 + 7, messageCodesResolver.withEthOffset(EthMessageCodes.NEW_BLOCK.asByte()));
        assertEquals(0x10 + 8, messageCodesResolver.withEthOffset(EthMessageCodes.GET_BLOCK_HASHES_BY_NUMBER.asByte()));

        assertEquals(0x19 + 0, messageCodesResolver.withShhOffset(ShhMessageCodes.STATUS.asByte()));
        assertEquals(0x19 + 1, messageCodesResolver.withShhOffset(ShhMessageCodes.MESSAGE.asByte()));
        assertEquals(0x19 + 2, messageCodesResolver.withShhOffset(ShhMessageCodes.FILTER.asByte()));
    }
}
