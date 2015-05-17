package org.ethereum.net;

import org.ethereum.net.eth.BlockHashesMessage;
import org.ethereum.net.eth.EthMessageCodes;
import org.ethereum.net.eth.GetBlockHashesMessage;

import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertEquals;

public class GetBlockHashesMessageTest {

    /* BLOCK_HASHES_MESSAGE */
    private static final Logger logger = LoggerFactory.getLogger("test");


    @Test /* BlockHashesMessage 1 from network */
    public void test_1() {
        String blockHashesMessageRaw = "e4a05ad1c9caeade4cdf5798e796dc87939231d9c76c20a6a33fea6dab8e9d6dd009820100";

        byte[] payload = Hex.decode(blockHashesMessageRaw);
        GetBlockHashesMessage getBlockHashesMessage = new GetBlockHashesMessage(payload);
        System.out.println(getBlockHashesMessage);

        assertEquals(EthMessageCodes.GET_BLOCK_HASHES, getBlockHashesMessage.getCommand());
        assertEquals("5ad1c9caeade4cdf5798e796dc87939231d9c76c20a6a33fea6dab8e9d6dd009", Hex.toHexString(getBlockHashesMessage.getBestHash()));
        assertEquals(256, getBlockHashesMessage.getMaxBlocks());
        assertEquals(BlockHashesMessage.class, getBlockHashesMessage.getAnswerMessage());
    }


}
