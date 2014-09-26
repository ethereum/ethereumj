package org.ethereum.net;

import static org.junit.Assert.assertEquals;

import org.ethereum.net.message.StatusMessage;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class StatusMessageTest {

    /* STATUS_MESSAGE */
    
    @Test /* StatusMessage 1 from network */
    public void test_1() {
    	String statusMessageRaw = "";
    	
        byte[] payload = Hex.decode(statusMessageRaw);
        StatusMessage statusMessage = new StatusMessage(payload);
        System.out.println(statusMessage);
        
        assertEquals(0, statusMessage.getProtocolVersion());
        assertEquals(0, statusMessage.getNetworkId());
        assertEquals("", Hex.toHexString(statusMessage.getTotalDifficulty()));
        assertEquals("", Hex.toHexString(statusMessage.getBestHash()));
        assertEquals("", Hex.toHexString(statusMessage.getGenesisHash()));
    }
    
    @Test /* StatusMessage 1 from new */
    public void test_2() {
    	byte protocolVersion = 0, networkId = 0;
    	byte[] totalDifficulty = Hex.decode("ff");
    	byte[] bestHash = Hex.decode("ff");
    	byte[] genesisHash = Hex.decode("ff");
    	
        StatusMessage statusMessage = new StatusMessage(protocolVersion, 
        		networkId, totalDifficulty, bestHash, genesisHash);
        System.out.println(statusMessage);
        
    	String expected = "ff";
    	assertEquals(expected, statusMessage.getEncoded());
    	
        assertEquals(0, statusMessage.getProtocolVersion());
        assertEquals(0, statusMessage.getNetworkId());
        assertEquals("", Hex.toHexString(statusMessage.getTotalDifficulty()));
        assertEquals("", Hex.toHexString(statusMessage.getBestHash()));
        assertEquals("", Hex.toHexString(statusMessage.getGenesisHash()));
    }
}

