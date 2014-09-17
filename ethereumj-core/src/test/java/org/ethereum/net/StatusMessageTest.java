package org.ethereum.net;

import static org.junit.Assert.assertEquals;

import org.ethereum.net.message.HelloMessage;
import org.ethereum.net.message.StatusMessage;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class StatusMessageTest {

    /* STATUS_MESSAGE */

    @Test /* StatusMessage 1 from PeerServer */
    public void testPeer() {
    	String statusMessageRaw = "";
    	
        byte[] payload = Hex.decode(statusMessageRaw);

        StatusMessage statusMessage = new StatusMessage(payload);
        System.out.println(statusMessage);

        assertEquals(0, statusMessage.getGenesisHash());
        assertEquals(0, statusMessage.getNetworkId());
        assertEquals(0, statusMessage.getProtocolVersion());
        assertEquals(0, statusMessage.getTotalDifficulty());
        assertEquals(0, statusMessage.getBestHash());
    }
    
    @Test /* HelloMessage 2 from Node */
    public void testNode() {
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
        helloMessage.parse();
        System.out.println(helloMessage);

        assertEquals(0, helloMessage.getP2PVersion());
        assertEquals("Ethereum(++)/Nick/v0.6.8d/Release/Linux/g++", helloMessage.getClientId());
        assertEquals(3, helloMessage.getCapabilities().size());
        assertEquals(30303, helloMessage.getListenPort());
        assertEquals(
            "2017B95D5586ADD053E7C5DCC8112DB1D557ED83589C4E0BD25442F39E4111655A487257AA7E4ED309E8B4D55BE5FA8D8D6E97B72C67D76AA03EB69AD981ED60",
            Hex.toHexString(helloMessage.getPeerId()).toUpperCase() );
    }
}

