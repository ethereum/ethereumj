package org.ethereum.net;

import org.ethereum.core.Block;
import org.ethereum.net.eth.NewBlockMessage;

import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.Ignore;
import org.spongycastle.util.encoders.Hex;

public class NewBlockMessageTest {

    private static final Logger logger = LoggerFactory.getLogger("test");


    /* NEW_BLOCK */

    @Test
    public void test_1() {

        byte[] payload = Hex.decode("f90144f9013Bf90136a0d8faffbc4c4213d35db9007de41cece45d95db7fd6c0f129e158baa888c48eefa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794baedba0480e1b882b606cd302d8c4f5701cabac7a0c7d4565fb7b3d98e54a0dec8b76f8c001a784a5689954ce0aedcc1bbe8d13095a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b8400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000083063477825fc88609184e72a0008301e8488084543ffee680a00de0b9d4a0f0c23546d31f1f70db00d25cf6a7af79365b4e058e4a6a3b69527bc0c0850177ddbebe");

        NewBlockMessage newBlockMessage = new NewBlockMessage(payload);
        logger.trace("{}", newBlockMessage);
    }


}
