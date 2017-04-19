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
package org.ethereum.net;

import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.p2p.P2pHandler;
import org.ethereum.net.p2p.P2pMessageCodes;
import org.ethereum.net.shh.ShhHandler;

import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HelloMessageTest {

    /* HELLO_MESSAGE */
    private static final Logger logger = LoggerFactory.getLogger("test");

    //Parsing from raw bytes
    @Test
    public void test1() {
        String helloMessageRaw = "f87902a5457468657265756d282b2b292f76302e372e392f52656c656173652f4c696e75782f672b2bccc58365746827c583736868018203e0b8401fbf1e41f08078918c9f7b6734594ee56d7f538614f602c71194db0a1af5a77f9b86eb14669fe7a8a46a2dd1b7d070b94e463f4ecd5b337c8b4d31bbf8dd5646";

        byte[] payload = Hex.decode(helloMessageRaw);
        HelloMessage helloMessage = new HelloMessage(payload);
        logger.info(helloMessage.toString());

        assertEquals(P2pMessageCodes.HELLO, helloMessage.getCommand());
        assertEquals(2, helloMessage.getP2PVersion());
        assertEquals("Ethereum(++)/v0.7.9/Release/Linux/g++", helloMessage.getClientId());
        assertEquals(2, helloMessage.getCapabilities().size());
        assertEquals(992, helloMessage.getListenPort());
        assertEquals(
                "1fbf1e41f08078918c9f7b6734594ee56d7f538614f602c71194db0a1af5a77f9b86eb14669fe7a8a46a2dd1b7d070b94e463f4ecd5b337c8b4d31bbf8dd5646",
                helloMessage.getPeerId());
    }

    //Instantiate from constructor
    @Test
    public void test2() {

        //Init
        byte version = 2;
        String clientStr = "Ethereum(++)/v0.7.9/Release/Linux/g++";
        List<Capability> capabilities = Arrays.asList(
                new Capability(Capability.ETH, EthVersion.UPPER),
                new Capability(Capability.SHH, ShhHandler.VERSION),
                new Capability(Capability.P2P, P2pHandler.VERSION));
        int listenPort = 992;
        String peerId = "1fbf1e41f08078918c9f7b6734594ee56d7f538614f602c71194db0a1af5a";

        HelloMessage helloMessage = new HelloMessage(version, clientStr, capabilities, listenPort, peerId);
        logger.info(helloMessage.toString());

        assertEquals(P2pMessageCodes.HELLO, helloMessage.getCommand());
        assertEquals(version, helloMessage.getP2PVersion());
        assertEquals(clientStr, helloMessage.getClientId());
        assertEquals(3, helloMessage.getCapabilities().size());
        assertEquals(listenPort, helloMessage.getListenPort());
        assertEquals(peerId, helloMessage.getPeerId());
    }

    //Fail test
    @Test
    public void test3() {
        //Init
        byte version = -1; //invalid version
        String clientStr = ""; //null id
        List<Capability> capabilities = Arrays.asList(
                new Capability(null, (byte) 0),
                new Capability(null, (byte) 0),
                null, //null here causes NullPointerException when using toString
                new Capability(null, (byte) 0)); //encoding null capabilities
        int listenPort = 99999; //invalid port
        String peerId = ""; //null id

        HelloMessage helloMessage = new HelloMessage(version, clientStr, capabilities, listenPort, peerId);

        assertEquals(P2pMessageCodes.HELLO, helloMessage.getCommand());
        assertEquals(version, helloMessage.getP2PVersion());
        assertEquals(clientStr, helloMessage.getClientId());
        assertEquals(4, helloMessage.getCapabilities().size());
        assertEquals(listenPort, helloMessage.getListenPort());
        assertEquals(peerId, helloMessage.getPeerId());
    }
}
