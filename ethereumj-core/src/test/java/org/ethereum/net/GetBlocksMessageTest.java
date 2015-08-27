package org.ethereum.net;

import org.ethereum.net.eth.message.BlocksMessage;
import org.ethereum.net.eth.message.EthMessageCodes;
import org.ethereum.net.eth.message.GetBlocksMessage;

import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertEquals;

public class GetBlocksMessageTest {

    /* GET_BLOCKS */
    private static final Logger logger = LoggerFactory.getLogger("test");


    @Test /* GetBlocks message parsing */
    public void test_1() {

        byte[] payload = Hex.decode("f8a5a0497dcbd12fa99ced7b27cda6611f64eb13ab50e20260eec5ee6b7190e7206d54a00959bdfba5e54fcc9370e86b7996fbe32a277bab65c31a0102226f83c4d3e0f2a001a333c156485880776e929e84c26c9778c1e9b4dcb5cd3bff8ad0aeff385df0a0690e13595c9e8e4fa9a621dfed6ad828a6e8e591479af6897c979a83daf73084a0b20f253d2b62609e932c13f3bca59a22913ea5b1e532d8a707976997461ec143");
        GetBlocksMessage getBlocksMessage = new GetBlocksMessage(payload);
        System.out.println(getBlocksMessage);

        assertEquals(EthMessageCodes.GET_BLOCKS, getBlocksMessage.getCommand());
        assertEquals(5, getBlocksMessage.getBlockHashes().size());
        String hash1 = "497dcbd12fa99ced7b27cda6611f64eb13ab50e20260eec5ee6b7190e7206d54";
        String hash4 = "b20f253d2b62609e932c13f3bca59a22913ea5b1e532d8a707976997461ec143";
        assertEquals(hash1, Hex.toHexString(getBlocksMessage.getBlockHashes().get(0)));
        assertEquals(hash4, Hex.toHexString(getBlocksMessage.getBlockHashes().get(4)));

        assertEquals(BlocksMessage.class, getBlocksMessage.getAnswerMessage());
    }

}
