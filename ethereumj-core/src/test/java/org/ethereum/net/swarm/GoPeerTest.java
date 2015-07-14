package org.ethereum.net.swarm;

import org.ethereum.Start;
import org.junit.Ignore;
import org.junit.Test;

import static org.ethereum.crypto.HashUtil.sha3;

/**
 * Created by Admin on 06.07.2015.
 */
public class GoPeerTest {

    @Ignore
    @Test
    // TODO to be done at some point: run Go peer and connect to it
    public void putTest() throws Exception {
        System.out.println("Starting Java peer...");
        Start.main(new String[]{});
        System.out.println("Warming up...");
        Thread.sleep(5000);
        System.out.println("Sending a chunk...");

        Key key = new Key(sha3(new byte[]{0x22, 0x33}));
//            stdout.setFilter(Hex.toHexString(key.getBytes()));
        Chunk chunk = new Chunk(key, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 77, 88});

        NetStore.getInstance().put(chunk);
    }
}
