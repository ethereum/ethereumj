package test.ethereum.net;

import org.ethereum.net.eth.StatusMessage;

import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

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

    @Test //from constructor
    public void test2() {
        //Init
        byte version = 39;
        byte netId = 0;
        byte[] difficulty = new BigInteger("25c60144", 16).toByteArray();
        byte[] bestHash =
                new BigInteger("832056d3c93ff2739ace7199952e5365aa29f18805be05634c4db125c5340216", 16).toByteArray();
        byte[] genesisHash =
                new BigInteger("955f36d073ccb026b78ab3424c15cf966a7563aa270413859f78702b9e8e22cb", 16).toByteArray();

        StatusMessage statusMessage = new StatusMessage(version, netId, difficulty, bestHash, genesisHash);

        logger.info(statusMessage.toString());

        assertEquals(39, statusMessage.getProtocolVersion());
        assertEquals("25c60144", Hex.toHexString(statusMessage.getTotalDifficulty()));
        assertEquals("00832056d3c93ff2739ace7199952e5365aa29f18805be05634c4db125c5340216",
                Hex.toHexString(statusMessage.getBestHash()));
        assertEquals("00955f36d073ccb026b78ab3424c15cf966a7563aa270413859f78702b9e8e22cb",
                Hex.toHexString(statusMessage.getGenesisHash()));
    }

    @Test //fail test
    public void test3() {
        //Init
        byte version = -1; //invalid version
        byte netId = -1;  //invalid netid
        byte[] difficulty = new BigInteger("-1000000", 16).toByteArray(); //negative difficulty
        byte[] bestHash = new BigInteger("-100000000000000000000000000", 16).toByteArray(); //invalid hash
        byte[] genesisHash = new BigInteger("-1000000000000000000000000000000", 16).toByteArray(); //invalid hash

        StatusMessage statusMessage = new StatusMessage(version, netId, difficulty, bestHash, genesisHash);

        logger.info(statusMessage.toString());

        assertEquals(-1, statusMessage.getProtocolVersion());
        assertEquals("ff000000", Hex.toHexString(statusMessage.getTotalDifficulty()));
        assertEquals("ff00000000000000000000000000", Hex.toHexString(statusMessage.getBestHash()));
        assertEquals("ff000000000000000000000000000000", Hex.toHexString(statusMessage.getGenesisHash()));
    }
}

