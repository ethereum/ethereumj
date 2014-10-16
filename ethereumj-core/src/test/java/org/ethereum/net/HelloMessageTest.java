package org.ethereum.net;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.p2p.P2pMessageCodes;
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
    	String helloMessageRaw = "F8 7B 80 80 AE 4E 45 74 68 65 72 65 "
    			+ "75 6D 28 2B 2B 29 2F 5A 65 72 6F 47 6F 78 2F 76 30 "
    			+ "2E 36 2E 39 2F 6E 63 75 72 73 65 73 2F 4C 69 6E 75 "
    			+ "78 2F 67 2B 2B C4 83 65 74 68 82 76 5F B8 40 CA DF "
    			+ "B9 3D 2B B5 FB E2 94 35 84 D9 3E D9 0E 37 46 67 C9 "
    			+ "E8 B2 50 2E 97 46 93 CC C6 B3 D3 70 BD 4C DE 77 38 "
    			+ "D0 B6 26 E3 D2 F3 CA EC C5 9E 13 02 D1 71 1B F5 95 "
    			+ "71 10 60 D7 B4 92 1E 18 B9 76 56";
    	
        byte[] payload = Hex.decode(helloMessageRaw);
        HelloMessage helloMessage = new HelloMessage(payload);
        System.out.println(helloMessage);

        assertEquals(P2pMessageCodes.HELLO, helloMessage.getCommand());
        assertEquals(0, helloMessage.getP2PVersion());
        assertEquals("NEthereum(++)/ZeroGox/v0.6.9/ncurses/Linux/g++", helloMessage.getClientId());
        assertEquals(1, helloMessage.getCapabilities().size());
        assertEquals("eth", helloMessage.getCapabilities().get(0));
        assertEquals(30303, helloMessage.getListenPort());
		assertEquals("cadfb93d2bb5fbe2943584d93ed90e374667c9e8b2502e974693ccc6b3d370bd4cde7738d0b626e3d2f3caecc59e1302d1711bf595711060d7b4921e18b97656",
				helloMessage.getPeerId());
    }
    
    @Test /* HelloMessage 3 from new */
    public void testFromNew() {
        String helloAnnouncement = "Ethereum(J)/0.6.1/dev/Windows/Java";
        byte p2pVersion = 0x00;
        List<String> capabilities = new ArrayList<>(Arrays.asList("eth", "shh"));
        int listenPort = 30303;
        String peerId = "CAB0D93EEE1F44EF1286367101F1553450E3DDCE"
        		+ "EA45ABCAB0AC21E1EFB48A6610EBE88CE7317EB09229558311BA8B7250911D"
        		+ "7E49562C3988CA3143329DA3EA";

        HelloMessage helloMessage = new HelloMessage(p2pVersion, helloAnnouncement, 
				capabilities, listenPort, peerId);
        System.out.println(helloMessage);
        // rlp encoded hello message
        String expected = "F8738080A2457468657265756D284A292F302E362E312F6465762F5"
        		+ "7696E646F77732F4A617661C8836574688373686882765FB840CAB0D93EEE1F"
        		+ "44EF1286367101F1553450E3DDCEEA45ABCAB0AC21E1EFB48A6610EBE88CE73"
        		+ "17EB09229558311BA8B7250911D7E49562C3988CA3143329DA3EA";
 
        assertEquals(P2pMessageCodes.HELLO, helloMessage.getCommand());
        assertEquals(expected, Hex.toHexString(helloMessage.getEncoded()).toUpperCase());
    }
}