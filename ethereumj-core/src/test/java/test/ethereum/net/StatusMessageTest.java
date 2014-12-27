package test.ethereum.net;

import org.ethereum.net.eth.StatusMessage;

import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertEquals;

public class StatusMessageTest {

    /* STATUS_MESSAGE */
    private static final Logger logger = LoggerFactory.getLogger("test");

    @Test
    public void test1() {

        String raw = "f84a1027808425c60144a0832056d3c93ff2739ace7199952e5365aa29f18805be05634c4db125c5340216a0955f36d073ccb026b78ab3424c15cf966a7563aa270413859f78702b9e8e22cb";
        byte[] payload = Hex.decode(raw);
        StatusMessage statusMessage = new StatusMessage(payload);

        logger.info(statusMessage.toString());

        assertEquals(39, statusMessage.getProtocolVersion());
        assertEquals("25c60144",
                Hex.toHexString(statusMessage.getTotalDifficulty()));
        assertEquals("832056d3c93ff2739ace7199952e5365aa29f18805be05634c4db125c5340216",
                Hex.toHexString(statusMessage.getBestHash()));
    }

}

