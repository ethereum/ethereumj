package org.ethereum.sharding.processing.state;

import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.sharding.processing.consensus.BeaconConstants;
import org.ethereum.sharding.processing.db.TrieValidatorSet;
import org.ethereum.sharding.processing.db.ValidatorSet;
import org.junit.Test;

import java.util.Random;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Mikhail Kalinin
 * @since 21.09.2018
 */
public class CrystallizedStateTest {

    @Test
    public void testSerialize() {
        CrystallizedState expected = getRandomState();

        CrystallizedState actual = fromEncoded(expected.flatten().encode());
        assertStateEquals(expected, actual);

        // with empty crosslinks and committees
        expected = getRandomState();
        expected.withDynasty(expected.getDynasty().withCommittees(new Committee[0][]))
                .withCrosslinks(new Crosslink[0]);
        actual = fromEncoded(expected.flatten().encode());

        assertStateEquals(expected, actual);

        // with empty slots in committee
        expected = getRandomState();
        Committee[][] committees = expected.getDynasty().getCommittees();
        committees[0] = committees[committees.length / 2] = committees[committees.length - 1] = new Committee[0];
        expected.withDynasty(expected.getDynasty().withCommittees(committees));

        actual = fromEncoded(expected.flatten().encode());
        assertStateEquals(expected, actual);
    }

    CrystallizedState fromEncoded(byte[] encoded) {
        CrystallizedState.Flattened flattened = new CrystallizedState.Flattened(encoded);
        ValidatorSet validatorSet = new TrieValidatorSet(new HashMapDB<>(), new HashMapDB<>(),
                flattened.getValidatorSetHash());
        Dynasty dynasty = new Dynasty(validatorSet, flattened.getCommittees(),
                flattened.getCurrentDynasty(), flattened.getDynastySeed(),
                flattened.getDynastySeedLastReset(), flattened.getDynastyStart());

        Finality finality = new Finality(flattened.getLastJustifiedSlot(),
                flattened.getJustifiedStreak(), flattened.getLastFinalizedSlot());

        return new CrystallizedState(flattened.getLastStateRecalc(),
                dynasty, finality, flattened.getCrosslinks());
    }

    CrystallizedState getRandomState() {
        Random rnd = new Random();

        ValidatorSet validatorSet = new TrieValidatorSet(new HashMapDB<>(), new HashMapDB<>());
        Committee[][] committees = new Committee[abs(rnd.nextInt()) % 1000 + 1][];
        for (int i = 0; i < committees.length; i++) {
            Committee[] slot = new Committee[abs(rnd.nextInt()) % 18 + 1];
            committees[i] = slot;
            for (int j = 0; j < slot.length; j++) {
                int[] validators = IntStream.range(1, abs(rnd.nextInt()) % (2 * BeaconConstants.MIN_COMMITTEE_SIZE - 2) + 2)
                        .toArray();
                slot[j] = new Committee((short) (abs(rnd.nextInt()) % BeaconConstants.SHARD_COUNT), validators);
            }
        }
        Dynasty dynasty = new Dynasty(validatorSet, committees, abs(rnd.nextInt()) % 10 + 1, HashUtil.randomHash(),
                abs(rnd.nextInt()) % 100 + 1, abs(rnd.nextInt()) % 100 + 1);
        Finality finality = new Finality(abs(rnd.nextInt()) % 100 + 1, abs(rnd.nextInt()) % 100 + 1, abs(rnd.nextInt()) % 100 + 1);

        Crosslink[] links = new Crosslink[BeaconConstants.SHARD_COUNT];
        for (int i = 0; i < links.length; i++) {
            links[i] = new Crosslink(abs(rnd.nextInt()) % 100 + 1, abs(rnd.nextInt()) % 1000 + 1, HashUtil.randomHash());
        }

        return new CrystallizedState(abs(rnd.nextInt()), dynasty, finality, links);
    }
    
    void assertStateEquals(CrystallizedState expected, CrystallizedState actual) {
        assertEquals(expected.getLastStateRecalc(), actual.getLastStateRecalc());
        assertEquals(expected.getCrosslinks().length, actual.getCrosslinks().length);
        for (int i = 0; i < expected.getCrosslinks().length; i++) {
            Crosslink e = expected.getCrosslinks()[i]; Crosslink a = actual.getCrosslinks()[i];
            assertEquals(e.getDynasty(), a.getDynasty());
            assertEquals(e.getSlot(), a.getSlot());
            assertArrayEquals(e.getHash(), a.getHash());
        }

        assertEquals(expected.getFinality().getJustifiedStreak(), actual.getFinality().getJustifiedStreak());
        assertEquals(expected.getFinality().getLastJustifiedSlot(), actual.getFinality().getLastJustifiedSlot());
        assertEquals(expected.getFinality().getLastFinalizedSlot(), actual.getFinality().getLastFinalizedSlot());

        assertArrayEquals(expected.getDynasty().getValidatorSet().getHash(),
                actual.getDynasty().getValidatorSet().getHash());
        assertArrayEquals(expected.getDynasty().getSeed(), actual.getDynasty().getSeed());
        assertEquals(expected.getDynasty().getNumber(), actual.getDynasty().getNumber());
        assertEquals(expected.getDynasty().getSeedLastReset(), actual.getDynasty().getSeedLastReset());
        assertEquals(expected.getDynasty().getStartSlot(), actual.getDynasty().getStartSlot());

        assertEquals(expected.getDynasty().getCommittees().length, actual.getDynasty().getCommittees().length);
        for (int i = 0; i < expected.getDynasty().getCommittees().length; i++) {
            for (int j = 0; j < expected.getDynasty().getCommittees()[i].length; j++) {
                assertEquals(expected.getDynasty().getCommittees()[i][j].getShardId(),
                        actual.getDynasty().getCommittees()[i][j].getShardId());
                assertArrayEquals(expected.getDynasty().getCommittees()[i][j].getValidators(),
                        actual.getDynasty().getCommittees()[i][j].getValidators());
            }
        }
    }
}
