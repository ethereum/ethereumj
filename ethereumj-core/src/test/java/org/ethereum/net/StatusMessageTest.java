package org.ethereum.net;

import static org.junit.Assert.assertEquals;

import org.ethereum.net.message.Command;
import org.ethereum.net.message.StatusMessage;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class StatusMessageTest {

    /* STATUS_MESSAGE */
    
    @Test /* StatusMessage 1 from network */
    public void test_1() {
    	String statusMessageRaw = "f84b102180850157f8b482a0455408387e6c5b029b0d51f7d617a4d1dc4895fa6eda09455cc2ee62c08d907ea008436a4d33c77e6acf013e586a3333ad152f25d31df8b68749d85046810e1f4b";

        byte[] payload = Hex.decode(statusMessageRaw);
        StatusMessage statusMessage = new StatusMessage(payload);
        System.out.println(statusMessage);
        
        assertEquals(Command.STATUS, statusMessage.getCommand());
        assertEquals(33, statusMessage.getProtocolVersion());
        assertEquals(0, statusMessage.getNetworkId());
        assertEquals("0157f8b482", Hex.toHexString(statusMessage.getTotalDifficulty()));
        assertEquals("455408387e6c5b029b0d51f7d617a4d1dc4895fa6eda09455cc2ee62c08d907e", Hex.toHexString(statusMessage.getBestHash()));
        assertEquals("08436a4d33c77e6acf013e586a3333ad152f25d31df8b68749d85046810e1f4b", Hex.toHexString(statusMessage.getGenesisHash()));
    }
    
    @Test /* StatusMessage 2 from new */
    public void test_2() {
    	byte protocolVersion = 33, networkId = 0;
    	byte[] totalDifficulty = new byte[0];
    	byte[] bestHash = Hex.decode("08436a4d33c77e6acf013e586a3333ad152f25d31df8b68749d85046810e1f4b");
    	byte[] genesisHash = Hex.decode("08436a4d33c77e6acf013e586a3333ad152f25d31df8b68749d85046810e1f4b");
    	
        StatusMessage statusMessage = new StatusMessage(protocolVersion, 
        		networkId, totalDifficulty, bestHash, genesisHash);
        System.out.println(statusMessage);
        
        String expected = "f84610218080a008436a4d33c77e6acf013e586a3333ad152f25d31df8b68749d85046810e1f4ba008436a4d33c77e6acf013e586a3333ad152f25d31df8b68749d85046810e1f4b";
    	assertEquals(expected, Hex.toHexString(statusMessage.getEncoded()));
    	
    	assertEquals(Command.STATUS, statusMessage.getCommand());
        assertEquals(33, statusMessage.getProtocolVersion());
        assertEquals(0, statusMessage.getNetworkId());
        assertEquals(Hex.toHexString(totalDifficulty), Hex.toHexString(statusMessage.getTotalDifficulty()));
        assertEquals(Hex.toHexString(bestHash), Hex.toHexString(statusMessage.getBestHash()));
        assertEquals(Hex.toHexString(genesisHash), Hex.toHexString(statusMessage.getGenesisHash()));
    }
}

