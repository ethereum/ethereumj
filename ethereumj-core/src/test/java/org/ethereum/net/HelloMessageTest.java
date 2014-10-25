package org.ethereum.net;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.p2p.P2pMessageCodes;
import org.ethereum.net.shh.ShhHandler;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class HelloMessageTest {

    /* HELLO_MESSAGE */

    @Test /* HelloMessage 1 from PeerServer */
    public void testPeer() {
    	String helloMessageRaw = "F8 80 80 80 B7 45 74 68 " +
    			"65 72 65 75 6D 28 2B 2B 29 2F 50 65 65 72 20 53 " +
    			"65 72 76 65 72 20 5A 65 72 6F 2F 76 30 2E 36 2E " +
    			"38 64 2F 52 65 6C 65 61 73 65 2F 4C 69 6E 75 78 " +
    			"2F 67 2B 2B C0 82 76 5F B8 40 20 17 B9 5D 55 86 " +
    			"AD D0 53 E7 C5 DC C8 11 2D B1 D5 57 ED 83 58 9C " +
    			"4E 0B D2 54 42 F3 9E 41 11 65 5A 48 72 57 AA 7E " +
    			"4E D3 09 E8 B4 D5 5B E5 FA 8D 8D 6E 97 B7 2C 67 " +
    			"D7 6A A0 3E B6 9A D9 81 ED 60";
    	    	
        byte[] payload = Hex.decode(helloMessageRaw);
        HelloMessage helloMessage = new HelloMessage(payload);
        System.out.println(helloMessage);

        assertEquals(P2pMessageCodes.HELLO, helloMessage.getCommand());
        assertEquals(0, helloMessage.getP2PVersion());
        assertEquals("Ethereum(++)/Peer Server Zero/v0.6.8d/Release/Linux/g++", helloMessage.getClientId());
        assertEquals(0, helloMessage.getCapabilities().size());
        assertEquals(30303, helloMessage.getListenPort());
        assertEquals(
            "2017B95D5586ADD053E7C5DCC8112DB1D557ED83589C4E0BD25442F39E4111655A487257AA7E4ED309E8B4D55BE5FA8D8D6E97B72C67D76AA03EB69AD981ED60",
            helloMessage.getPeerId().toUpperCase());
    }
    
    @Test /* HelloMessage 2 from Node */
    public void testNode() {
    	
    	String helloMessageRaw = "F8 80 80 02 AB 41 6C 65 "
    			+ "74 68 5A 65 72 6F 2F 76 30 2E 37 2E 34 2F 52 65 "
    			+ "6C 65 61 73 65 2D 78 36 34 2F 57 69 6E 64 6F 77 "
    			+ "73 2F 56 53 32 30 31 33 CC C5 83 65 74 68 23 C5 "
    			+ "83 73 68 68 01 82 76 5F B8 40 E1 01 2B 75 38 C4 "
    			+ "1D 31 9F B4 BE D7 DF E9 D7 ED C7 2B 82 F2 E6 BE "
    			+ "2D 20 F8 3C 60 14 11 51 5C 74 2B 3C E3 71 F5 61 "
    			+ "47 29 56 36 27 4D 34 91 D6 BC C5 1F 0A 09 20 EB "
    			+ "41 F2 C0 36 04 28 C9 A9 80 01";
    	
        byte[] payload = Hex.decode(helloMessageRaw);
        HelloMessage helloMessage = new HelloMessage(payload);
        System.out.println(helloMessage);

        assertEquals(P2pMessageCodes.HELLO, helloMessage.getCommand());
        assertEquals(2, helloMessage.getP2PVersion());
        assertEquals("AlethZero/v0.7.4/Release-x64/Windows/VS2013", helloMessage.getClientId());
        assertEquals(2, helloMessage.getCapabilities().size());
        assertEquals(Capability.ETH, helloMessage.getCapabilities().get(0).getName());
        assertEquals(35, helloMessage.getCapabilities().get(0).getVersion());
        assertEquals(Capability.SHH, helloMessage.getCapabilities().get(1).getName());
        assertEquals(1, helloMessage.getCapabilities().get(1).getVersion());
        
        assertEquals(30303, helloMessage.getListenPort());
		assertEquals("e1012b7538c41d319fb4bed7dfe9d7edc72b82f2e6be2d20f83c601411515c742b3ce371f56147295636274d3491d6bcc51f0a0920eb41f2c0360428c9a98001",
				helloMessage.getPeerId());
    }
    
    @Test /* HelloMessage 3 from new */
    public void testFromNew() {
        String helloAnnouncement = "Ethereum(J)/0.6.1/dev/Windows/Java";
        byte p2pVersion = 0x0;
		List<Capability> capabilities = Arrays.asList(
				new Capability(Capability.ETH, EthHandler.VERSION),
				new Capability(Capability.SHH, ShhHandler.VERSION));
        int listenPort = 30303;
        String peerId = "CAB0D93EEE1F44EF1286367101F1553450E3DDCE"
        		+ "EA45ABCAB0AC21E1EFB48A6610EBE88CE7317EB09229558311BA8B7250911D"
        		+ "7E49562C3988CA3143329DA3EA";

        HelloMessage helloMessage = new HelloMessage(p2pVersion, helloAnnouncement, 
				capabilities, listenPort, peerId);
        System.out.println(helloMessage);
        // rlp encoded hello message
        String expected = "F8778080A2457468657265756D284A292F302E362E312F6465762F5"
        		+ "7696E646F77732F4A617661CCC58365746824C5837368680182765FB840CAB0"
        		+ "D93EEE1F44EF1286367101F1553450E3DDCEEA45ABCAB0AC21E1EFB48A6610E"
        		+ "BE88CE7317EB09229558311BA8B7250911D7E49562C3988CA3143329DA3EA";
 
        assertEquals(P2pMessageCodes.HELLO, helloMessage.getCommand());
        assertEquals(expected, Hex.toHexString(helloMessage.getEncoded()).toUpperCase());
    }
}