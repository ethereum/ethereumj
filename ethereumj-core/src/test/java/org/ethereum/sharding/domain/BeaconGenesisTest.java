package org.ethereum.sharding.domain;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Mikhail Kalinin
 * @since 03.09.2018
 */
public class BeaconGenesisTest {

    @Test
    public void testLoadFromJson() {
        BeaconGenesis.Json json = BeaconGenesis.Json.fromResource("genesis/beacon-genesis-parse-test.json");
        BeaconGenesis genesis = new BeaconGenesis(json);

        assertArrayEquals(Hex.decode("d4e56740f876aef8c010b86a40d5f56745a118d0906a34e69aec8c0db1cb8fa3"), genesis.getParentHash());
        assertArrayEquals(Hex.decode("c85ef7d79691fe79573b1a7064c19c1a9819ebdbd1faaab1a8ec92344438aaf4"), genesis.getRandaoReveal());
        assertArrayEquals(Hex.decode("41791102999c339c844880b23950704cc43aa840f3739e365323cda4dfa89e7a"), genesis.getMainChainRef());
        assertEquals(1535474832000L, genesis.getTimestamp());
    }
}
