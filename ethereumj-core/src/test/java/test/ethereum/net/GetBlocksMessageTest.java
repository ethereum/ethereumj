package test.ethereum.net;

import org.ethereum.net.eth.BlocksMessage;
import org.ethereum.net.eth.EthMessageCodes;
import org.ethereum.net.eth.GetBlocksMessage;

import org.junit.Test;

import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GetBlocksMessageTest {

    /* GET_BLOCKS */

    @Test /* GetBlocks message parsing */
    public void test_1() {

        String getBlocksMessageRaw = "f8a615a0497dcbd12fa99ced7b27cda6611f64eb13ab50e20260eec5ee6b7190e7206d54a00959bdfba5e54fcc9370e86b7996fbe32a277bab65c31a0102226f83c4d3e0f2a001a333c156485880776e929e84c26c9778c1e9b4dcb5cd3bff8ad0aeff385df0a0690e13595c9e8e4fa9a621dfed6ad828a6e8e591479af6897c979a83daf73084a0b20f253d2b62609e932c13f3bca59a22913ea5b1e532d8a707976997461ec143";

        byte[] payload = Hex.decode(getBlocksMessageRaw);
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

    @Test /* GetBlocks from new */
    public void test_2() {

        List<byte[]> hashList = Arrays.asList(
                Hex.decode("497dcbd12fa99ced7b27cda6611f64eb13ab50e20260eec5ee6b7190e7206d54"),
                Hex.decode("0959bdfba5e54fcc9370e86b7996fbe32a277bab65c31a0102226f83c4d3e0f2"),
                Hex.decode("01a333c156485880776e929e84c26c9778c1e9b4dcb5cd3bff8ad0aeff385df0"),
                Hex.decode("690e13595c9e8e4fa9a621dfed6ad828a6e8e591479af6897c979a83daf73084"),
                Hex.decode("b20f253d2b62609e932c13f3bca59a22913ea5b1e532d8a707976997461ec143"));

        GetBlocksMessage getBlocksMessage = new GetBlocksMessage(hashList);
        System.out.println(getBlocksMessage);

        String expected = "f8a605a0497dcbd12fa99ced7b27cda6611f64eb13ab50e20260eec5ee6b7190e7206d54a00959bdfba5e54fcc9370e86b7996fbe32a277bab65c31a0102226f83c4d3e0f2a001a333c156485880776e929e84c26c9778c1e9b4dcb5cd3bff8ad0aeff385df0a0690e13595c9e8e4fa9a621dfed6ad828a6e8e591479af6897c979a83daf73084a0b20f253d2b62609e932c13f3bca59a22913ea5b1e532d8a707976997461ec143";
        assertEquals(expected, Hex.toHexString(getBlocksMessage.getEncoded()));

        assertEquals(EthMessageCodes.GET_BLOCKS, getBlocksMessage.getCommand());
        assertEquals(5, getBlocksMessage.getBlockHashes().size());
        assertEquals(BlocksMessage.class, getBlocksMessage.getAnswerMessage());
    }
}
