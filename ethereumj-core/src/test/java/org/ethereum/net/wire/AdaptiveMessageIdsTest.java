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
package org.ethereum.net.wire;

import static org.ethereum.net.eth.EthVersion.V62;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;

import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.message.EthMessageCodes;
import org.ethereum.net.p2p.P2pMessageCodes;
import org.ethereum.net.rlpx.MessageCodesResolver;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.net.shh.ShhMessageCodes;
import org.ethereum.net.swarm.bzz.BzzHandler;
import org.ethereum.net.swarm.bzz.BzzMessageCodes;
import org.junit.Before;
import org.junit.Test;

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

        assertEquals(8, EthMessageCodes.values(V62).length);

        assertEquals(0, EthMessageCodes.STATUS.asByte());
        assertEquals(1, EthMessageCodes.NEW_BLOCK_HASHES.asByte());
        assertEquals(2, EthMessageCodes.TRANSACTIONS.asByte());
        assertEquals(3, EthMessageCodes.GET_BLOCK_HEADERS.asByte());
        assertEquals(4, EthMessageCodes.BLOCK_HEADERS.asByte());
        assertEquals(5, EthMessageCodes.GET_BLOCK_BODIES.asByte());
        assertEquals(6, EthMessageCodes.BLOCK_BODIES.asByte());
        assertEquals(7, EthMessageCodes.NEW_BLOCK.asByte());

        messageCodesResolver.setEthOffset(0x10);

        assertEquals(0x10 + 0, messageCodesResolver.withEthOffset(EthMessageCodes.STATUS.asByte()));
        assertEquals(0x10 + 1, messageCodesResolver.withEthOffset(EthMessageCodes.NEW_BLOCK_HASHES.asByte()));
        assertEquals(0x10 + 2, messageCodesResolver.withEthOffset(EthMessageCodes.TRANSACTIONS.asByte()));
        assertEquals(0x10 + 3, messageCodesResolver.withEthOffset(EthMessageCodes.GET_BLOCK_HEADERS.asByte()));
        assertEquals(0x10 + 4, messageCodesResolver.withEthOffset(EthMessageCodes.BLOCK_HEADERS.asByte()));
        assertEquals(0x10 + 5, messageCodesResolver.withEthOffset(EthMessageCodes.GET_BLOCK_BODIES.asByte()));
        assertEquals(0x10 + 6, messageCodesResolver.withEthOffset(EthMessageCodes.BLOCK_BODIES.asByte()));
        assertEquals(0x10 + 7, messageCodesResolver.withEthOffset(EthMessageCodes.NEW_BLOCK.asByte()));
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
                new Capability(Capability.ETH, EthVersion.V62.getCode()),
                new Capability(Capability.SHH, ShhHandler.VERSION));

        messageCodesResolver.init(capabilities);

        int ethOffset = P2pMessageCodes.USER.asByte() + 1;
        int shhOffset = ethOffset + EthMessageCodes.NEW_BLOCK.asByte() + 1;

        assertEquals(ethOffset + 0, messageCodesResolver.withEthOffset(EthMessageCodes.STATUS.asByte()));
        assertEquals(ethOffset + 1, messageCodesResolver.withEthOffset(EthMessageCodes.NEW_BLOCK_HASHES.asByte()));
        assertEquals(ethOffset + 2, messageCodesResolver.withEthOffset(EthMessageCodes.TRANSACTIONS.asByte()));
        assertEquals(ethOffset + 3, messageCodesResolver.withEthOffset(EthMessageCodes.GET_BLOCK_HEADERS.asByte()));
        assertEquals(ethOffset + 4, messageCodesResolver.withEthOffset(EthMessageCodes.BLOCK_HEADERS.asByte()));
        assertEquals(ethOffset + 5, messageCodesResolver.withEthOffset(EthMessageCodes.GET_BLOCK_BODIES.asByte()));
        assertEquals(ethOffset + 6, messageCodesResolver.withEthOffset(EthMessageCodes.BLOCK_BODIES.asByte()));
        assertEquals(ethOffset + 7, messageCodesResolver.withEthOffset(EthMessageCodes.NEW_BLOCK.asByte()));

        assertEquals(shhOffset + 0, messageCodesResolver.withShhOffset(ShhMessageCodes.STATUS.asByte()));
        assertEquals(shhOffset + 1, messageCodesResolver.withShhOffset(ShhMessageCodes.MESSAGE.asByte()));
        assertEquals(shhOffset + 2, messageCodesResolver.withShhOffset(ShhMessageCodes.FILTER.asByte()));
    }

    @Test // Capabilities should be read in alphabetical order
    public void test5() {

        EthVersion ethVersion = EthVersion.V62;
        List<Capability> capabilities = Arrays.asList(
                new Capability(Capability.SHH, ShhHandler.VERSION),
                new Capability(Capability.ETH, ethVersion.getCode()));

        messageCodesResolver.init(capabilities);

        int ethOffset = P2pMessageCodes.USER.asByte() + 1;
        int shhOffset = ethOffset + EthMessageCodes.NEW_BLOCK.asByte() + 1;

        assertEquals(ethOffset + 0, messageCodesResolver.withEthOffset(EthMessageCodes.STATUS.asByte()));
        assertEquals(ethOffset + 1, messageCodesResolver.withEthOffset(EthMessageCodes.NEW_BLOCK_HASHES.asByte()));
        assertEquals(ethOffset + 2, messageCodesResolver.withEthOffset(EthMessageCodes.TRANSACTIONS.asByte()));
        assertEquals(ethOffset + 3, messageCodesResolver.withEthOffset(EthMessageCodes.GET_BLOCK_HEADERS.asByte()));
        assertEquals(ethOffset + 4, messageCodesResolver.withEthOffset(EthMessageCodes.BLOCK_HEADERS.asByte()));
        assertEquals(ethOffset + 5, messageCodesResolver.withEthOffset(EthMessageCodes.GET_BLOCK_BODIES.asByte()));
        assertEquals(ethOffset + 6, messageCodesResolver.withEthOffset(EthMessageCodes.BLOCK_BODIES.asByte()));
        assertEquals(ethOffset + 7, messageCodesResolver.withEthOffset(EthMessageCodes.NEW_BLOCK.asByte()));

        assertFalse(EthMessageCodes.inRange((byte)(shhOffset + 0), ethVersion));
        assertEquals(shhOffset + 0, messageCodesResolver.withShhOffset(ShhMessageCodes.STATUS.asByte()));
        assertEquals(shhOffset + 1, messageCodesResolver.withShhOffset(ShhMessageCodes.MESSAGE.asByte()));
        assertEquals(shhOffset + 2, messageCodesResolver.withShhOffset(ShhMessageCodes.FILTER.asByte()));
    }

    @Test // Verify that eth63 offset is working correctly
    public void test6() {

        EthVersion ethVersion = EthVersion.V63;
        List<Capability> capabilities = Arrays.asList(
                new Capability(Capability.SHH, ShhHandler.VERSION),
                new Capability(Capability.ETH, ethVersion.getCode()));

        messageCodesResolver.init(capabilities);

        int ethOffset = P2pMessageCodes.USER.asByte() + 1;
        int shhOffset = ethOffset + EthMessageCodes.RECEIPTS.asByte() + 1;
        // Verify Eth Offset Lower Boundary
        byte ethCode = messageCodesResolver.resolveEth(messageCodesResolver.withP2pOffset(P2pMessageCodes.USER.asByte()));
        assertFalse(EthMessageCodes.inRange(ethCode, ethVersion));
        assertEquals(ethOffset + 0, messageCodesResolver.withEthOffset(EthMessageCodes.STATUS.asByte()));
        assertEquals(ethOffset + 1, messageCodesResolver.withEthOffset(EthMessageCodes.NEW_BLOCK_HASHES.asByte()));
        assertEquals(ethOffset + 2, messageCodesResolver.withEthOffset(EthMessageCodes.TRANSACTIONS.asByte()));
        assertEquals(ethOffset + 3, messageCodesResolver.withEthOffset(EthMessageCodes.GET_BLOCK_HEADERS.asByte()));
        assertEquals(ethOffset + 4, messageCodesResolver.withEthOffset(EthMessageCodes.BLOCK_HEADERS.asByte()));
        assertEquals(ethOffset + 5, messageCodesResolver.withEthOffset(EthMessageCodes.GET_BLOCK_BODIES.asByte()));
        assertEquals(ethOffset + 6, messageCodesResolver.withEthOffset(EthMessageCodes.BLOCK_BODIES.asByte()));
        assertEquals(ethOffset + 7, messageCodesResolver.withEthOffset(EthMessageCodes.NEW_BLOCK.asByte()));
        assertEquals(ethOffset + 13, messageCodesResolver.withEthOffset(EthMessageCodes.GET_NODE_DATA.asByte()));
        assertEquals(ethOffset + 14, messageCodesResolver.withEthOffset(EthMessageCodes.NODE_DATA.asByte()));
        assertEquals(ethOffset + 15, messageCodesResolver.withEthOffset(EthMessageCodes.GET_RECEIPTS.asByte()));
        assertEquals(ethOffset + 16, messageCodesResolver.withEthOffset(EthMessageCodes.RECEIPTS.asByte()));

        // Verify Eth Upper boundary
        ethCode = messageCodesResolver.resolveEth((byte)(shhOffset + 0));
        assertFalse(EthMessageCodes.inRange(ethCode, ethVersion));

        // Verify Shh Lower Boundary
        byte shhCode = messageCodesResolver.resolveShh((byte)(ethOffset + 16));
        assertFalse(ShhMessageCodes.inRange(shhCode));
        assertEquals(shhOffset + 0, messageCodesResolver.withShhOffset(ShhMessageCodes.STATUS.asByte()));
        assertEquals(shhOffset + 1, messageCodesResolver.withShhOffset(ShhMessageCodes.MESSAGE.asByte()));
        assertEquals(shhOffset + 2, messageCodesResolver.withShhOffset(ShhMessageCodes.FILTER.asByte()));
    }

    @Test // Checks that offsets works correctly when all capabilities are activated
    public void test7() {
        EthVersion ethVersion = EthVersion.V63;
        List<Capability> capabilities = Arrays.asList(
                new Capability(Capability.ETH, ethVersion.getCode()),
                new Capability(Capability.SHH, ShhHandler.VERSION),
                new Capability(Capability.BZZ, BzzHandler.VERSION));

        messageCodesResolver.init(capabilities);

        int bzzOffset = P2pMessageCodes.USER.asByte() + 1;
        int ethOffset = bzzOffset + BzzMessageCodes.PEERS.asByte() + 1;
        int shhOffset = ethOffset + EthMessageCodes.RECEIPTS.asByte() + 1;
        // Verify Bzz Offset Lower Boundary
        byte bzzCode = messageCodesResolver.resolveBzz(messageCodesResolver.withP2pOffset(P2pMessageCodes.USER.asByte()));
        assertFalse(BzzMessageCodes.inRange(bzzCode));
        assertEquals(bzzOffset + 0, messageCodesResolver.withBzzOffset(BzzMessageCodes.STATUS.asByte()));
        assertEquals(bzzOffset + 1, messageCodesResolver.withBzzOffset(BzzMessageCodes.STORE_REQUEST.asByte()));
        assertEquals(bzzOffset + 2, messageCodesResolver.withBzzOffset(BzzMessageCodes.RETRIEVE_REQUEST.asByte()));
        assertEquals(bzzOffset + 3, messageCodesResolver.withBzzOffset(BzzMessageCodes.PEERS.asByte()));
        // Verify Bzz Offset Upper Boundary
        bzzCode = messageCodesResolver.resolveBzz((byte) (ethOffset + 0));
        assertFalse(BzzMessageCodes.inRange(bzzCode));
        // Verify Eth Offset Lower Boundary
        byte ethCode = messageCodesResolver.resolveEth((byte)(bzzOffset + 3));
        assertFalse(EthMessageCodes.inRange(ethCode, ethVersion));
        assertEquals(ethOffset + 0, messageCodesResolver.withEthOffset(EthMessageCodes.STATUS.asByte()));
        assertEquals(ethOffset + 1, messageCodesResolver.withEthOffset(EthMessageCodes.NEW_BLOCK_HASHES.asByte()));
        assertEquals(ethOffset + 2, messageCodesResolver.withEthOffset(EthMessageCodes.TRANSACTIONS.asByte()));
        assertEquals(ethOffset + 3, messageCodesResolver.withEthOffset(EthMessageCodes.GET_BLOCK_HEADERS.asByte()));
        assertEquals(ethOffset + 4, messageCodesResolver.withEthOffset(EthMessageCodes.BLOCK_HEADERS.asByte()));
        assertEquals(ethOffset + 5, messageCodesResolver.withEthOffset(EthMessageCodes.GET_BLOCK_BODIES.asByte()));
        assertEquals(ethOffset + 6, messageCodesResolver.withEthOffset(EthMessageCodes.BLOCK_BODIES.asByte()));
        assertEquals(ethOffset + 7, messageCodesResolver.withEthOffset(EthMessageCodes.NEW_BLOCK.asByte()));
        // Verify Eth Offset Upper Boundary
        ethCode = messageCodesResolver.resolveEth((byte)(shhOffset + 0));
        assertFalse(EthMessageCodes.inRange(ethCode, ethVersion));
        // Verify Shh Offset Lower Boundary
        byte shhCode = messageCodesResolver.resolveShh((byte)(ethOffset + 7));
        assertFalse(ShhMessageCodes.inRange(shhCode));
        assertEquals(shhOffset + 0, messageCodesResolver.withShhOffset(ShhMessageCodes.STATUS.asByte()));
        assertEquals(shhOffset + 1, messageCodesResolver.withShhOffset(ShhMessageCodes.MESSAGE.asByte()));
        assertEquals(shhOffset + 2, messageCodesResolver.withShhOffset(ShhMessageCodes.FILTER.asByte()));
    }
}
