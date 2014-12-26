package test.ethereum.net;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.p2p.P2pMessageCodes;
import org.ethereum.net.shh.ShhHandler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

public class HelloMessageTest {

    /* HELLO_MESSAGE */
    private static final Logger logger = LoggerFactory.getLogger("test");


    @Test
    public void test1() {
        String helloMessageRaw = "f87a8002a5457468657265756d282b2b292f76302e372e392f52656c656173652f4c696e75782f672b2bccc58365746827c583736868018203e0b8401fbf1e41f08078918c9f7b6734594ee56d7f538614f602c71194db0a1af5a77f9b86eb14669fe7a8a46a2dd1b7d070b94e463f4ecd5b337c8b4d31bbf8dd5646";

        byte[] payload = Hex.decode(helloMessageRaw);
        HelloMessage helloMessage = new HelloMessage(payload);
        logger.info(helloMessage.toString());

        assertEquals(P2pMessageCodes.HELLO, helloMessage.getCommand());
        assertEquals(2, helloMessage.getP2PVersion());
        assertEquals("Ethereum(++)/v0.7.9/Release/Linux/g++", helloMessage.getClientId());
        assertEquals(2,   helloMessage.getCapabilities().size());
        assertEquals(992, helloMessage.getListenPort());
        assertEquals(
            "1fbf1e41f08078918c9f7b6734594ee56d7f538614f602c71194db0a1af5a77f9b86eb14669fe7a8a46a2dd1b7d070b94e463f4ecd5b337c8b4d31bbf8dd5646",
            helloMessage.getPeerId());


    }


}