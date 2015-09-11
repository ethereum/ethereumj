package org.ethereum.net;

import org.ethereum.net.eth.message.BlockHashesMessage;
import org.ethereum.net.eth.message.EthMessageCodes;
import org.ethereum.net.eth.message.GetBlockHashesByNumberMessage;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertEquals;

/**
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
public class GetBlockHashesByNumberMessageTest {

    @Test /* GetBlockHashesByNumberMessage 1 from network */
    public void test_1() {
        String blockHashesMessageRaw = "c464822710";

        byte[] payload = Hex.decode(blockHashesMessageRaw);
        GetBlockHashesByNumberMessage msg = new GetBlockHashesByNumberMessage(payload);
        System.out.println(msg);

        assertEquals(EthMessageCodes.GET_BLOCK_HASHES_BY_NUMBER, msg.getCommand());
        assertEquals(100, msg.getBlockNumber());
        assertEquals(10000, msg.getMaxBlocks());
        assertEquals(BlockHashesMessage.class, msg.getAnswerMessage());
    }

    @Test /* GetBlockHashesByNumberMessage 2 from new */
    public void test_2() {

        GetBlockHashesByNumberMessage msg = new GetBlockHashesByNumberMessage(100, 10000);

        String expected = "c464822710";

        assertEquals(EthMessageCodes.GET_BLOCK_HASHES_BY_NUMBER, msg.getCommand());
        assertEquals(expected, Hex.toHexString(msg.getEncoded()));
        assertEquals(BlockHashesMessage.class, msg.getAnswerMessage());
    }

}
