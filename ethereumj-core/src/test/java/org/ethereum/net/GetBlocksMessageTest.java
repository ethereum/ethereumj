package org.ethereum.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.ethereum.net.message.BlocksMessage;
import org.ethereum.net.message.Command;
import org.ethereum.net.message.GetBlocksMessage;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class GetBlocksMessageTest {

    /* GET_BLOCKS */

    @Test /* GetBlocks message parsing */
    public void test_1() {

    	fail("Not yet implemented");
    	String getBlocksMessageRaw = "";
    	
        byte[] payload = Hex.decode(getBlocksMessageRaw);
        GetBlocksMessage getBlocksMessage = new GetBlocksMessage(payload);
        System.out.println(getBlocksMessage);
        
        assertEquals(Command.GET_BLOCKS, getBlocksMessage.getCommand());
        assertEquals(0, getBlocksMessage.getBlockHashes().size());
        assertEquals(BlocksMessage.class, getBlocksMessage.getAnswerMessage());
    }
	
    @Test /* GetBlocks from new */
    public void test_2() {

    	fail("Not yet implemented");
    	List<byte[]> hashList = new ArrayList<>();
        GetBlocksMessage getBlocksMessage = new GetBlocksMessage(hashList);
        System.out.println(getBlocksMessage);
        
        String expected = "e413a0455408387e6c5b029b0d51f7d617a4d1dc4895fa6eda09455cc2ee62c08d907e8180";
    	assertEquals(expected, Hex.toHexString(getBlocksMessage.getEncoded()));
    	
        assertEquals(Command.GET_BLOCKS, getBlocksMessage.getCommand());
        assertEquals(128, getBlocksMessage.getBlockHashes().size());
        assertEquals(BlocksMessage.class, getBlocksMessage.getAnswerMessage());
    }
}
