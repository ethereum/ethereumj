package org.ethereum.net;

import org.ethereum.core.Block;
import org.ethereum.net.eth.BlocksMessage;

import org.junit.Ignore;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;

import java.net.URISyntaxException;
import java.net.URL;

import java.nio.file.Files;

import java.util.List;

import static org.junit.Assert.assertEquals;

@Ignore
public class BlocksMessageTest {

    private static final Logger logger = LoggerFactory.getLogger("test");

    /* BLOCKS */

    @Test
    public void test_1() {

        byte[] payload = Hex.decode("f901fff901fcf901f7a0fbce9f78142b5d76c2787d89d574136573f62dce21dd7bcf27c7c68ab407ccc3a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d493479415caa04a9407a2f242b2859005a379655bfb9b11a0689e7e862856d619e32ec5d949711164b447e0df7e55f4570d9fa27f33ca31a2a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000830201c008832fefd880845504456b80a05b0400eac058e0243754f4149f14e5c84cef1c33a79d83e21c80f590b953fd60881b4ef00c7a4dae1fc0c0");

        BlocksMessage blocksMessage = new BlocksMessage(payload);
        List<Block> list = blocksMessage.getBlocks();
        logger.info(blocksMessage.toString());

        Block block = list.get(0);
        assertEquals(0, block.getTransactionsList().size());
        assertEquals(8, block.getNumber());
        assertEquals("2bff4626b9854e88c72ccc5b47621a0a4e47ef5d97e1fa7c00560f7cd57543c5",
                Hex.toHexString(block.getHash()));
        assertEquals("689e7e862856d619e32ec5d949711164b447e0df7e55f4570d9fa27f33ca31a2",
                Hex.toHexString(block.getStateRoot()));
    }

}

