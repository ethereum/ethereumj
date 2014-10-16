package org.ethereum.net;

import static org.junit.Assert.assertEquals;

import org.ethereum.net.eth.BlockHashesMessage;
import org.ethereum.net.eth.EthMessageCodes;
import org.ethereum.net.eth.GetBlockHashesMessage;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class GetBlockHashesMessageTest {

    /* BLOCK_HASHES_MESSAGE */
    
    @Test /* BlockHashesMessage 1 from network */
    public void test_1() {
    	String blockHashesMessageRaw = "e513a05ad1c9caeade4cdf5798e796dc87939231d9c76c20a6a33fea6dab8e9d6dd009820100";
    	
        byte[] payload = Hex.decode(blockHashesMessageRaw);
        GetBlockHashesMessage getBlockHashesMessage = new GetBlockHashesMessage(payload);
        System.out.println(getBlockHashesMessage);
        
        assertEquals(EthMessageCodes.GET_BLOCK_HASHES, getBlockHashesMessage.getCommand());
        assertEquals("5ad1c9caeade4cdf5798e796dc87939231d9c76c20a6a33fea6dab8e9d6dd009", Hex.toHexString(getBlockHashesMessage.getBestHash()));
        assertEquals(256, getBlockHashesMessage.getMaxBlocks());
        assertEquals(BlockHashesMessage.class, getBlockHashesMessage.getAnswerMessage());
    }
    
    @Test /* GetBlockHashesMessage 2 from new */
    public void test_2() {
    	byte[] bestHash = Hex.decode("455408387e6c5b029b0d51f7d617a4d1dc4895fa6eda09455cc2ee62c08d907e"); 
        GetBlockHashesMessage getBlockHashesMessage = new GetBlockHashesMessage(bestHash, 128);
        System.out.println(getBlockHashesMessage);
        
        String expected = "e413a0455408387e6c5b029b0d51f7d617a4d1dc4895fa6eda09455cc2ee62c08d907e8180";
    	assertEquals(expected, Hex.toHexString(getBlockHashesMessage.getEncoded()));
    	
        assertEquals(EthMessageCodes.GET_BLOCK_HASHES, getBlockHashesMessage.getCommand());
        assertEquals(Hex.toHexString(bestHash), Hex.toHexString(getBlockHashesMessage.getBestHash()));
        assertEquals(128, getBlockHashesMessage.getMaxBlocks());
        assertEquals(BlockHashesMessage.class, getBlockHashesMessage.getAnswerMessage());
    }
    
	
}
