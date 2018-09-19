/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.sharding.processing.state;

import org.ethereum.datasource.Serializer;
import org.ethereum.sharding.processing.consensus.BeaconConstants;
import org.ethereum.sharding.processing.db.ValidatorSet;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import static org.ethereum.crypto.HashUtil.blake2b;
import static org.ethereum.util.ByteUtil.byteArrayToLong;
import static org.ethereum.util.ByteUtil.isSingleZero;
import static org.ethereum.util.ByteUtil.longToBytesNoLeadZeroes;

/**
 * @author Mikhail Kalinin
 * @since 06.09.2018
 */
public class CrystallizedState {

    /* Slot number that the state was calculated at */
    private final long lastStateRecalc;
    /* Current dynasty */
    private final Dynasty dynasty;
    /* Current finality state */
    private final Finality finality;
    /* The most recent crosslinks for each shard */
    private final Crosslink[] crosslinks;

    public CrystallizedState(long lastStateRecalc, Dynasty dynasty, Finality finality, Crosslink[] crosslinks) {
        this.lastStateRecalc = lastStateRecalc;
        this.dynasty = dynasty;
        this.finality = finality;
        this.crosslinks = crosslinks;
    }

    public Flattened flatten() {
        return new Flattened(dynasty.getValidatorSet().getHash(), lastStateRecalc, dynasty.getCommittees(),
                finality.getLastJustifiedSlot(), finality.getJustifiedStreak(), finality.getLastFinalizedSlot(),
                dynasty.getNumber(), crosslinks, dynasty.getSeed(), dynasty.getSeedLastReset(), dynasty.getStartSlot());
    }

    public byte[] getHash() {
        return flatten().getHash();
    }

    public long getLastStateRecalc() {
        return lastStateRecalc;
    }

    public Dynasty getDynasty() {
        return dynasty;
    }

    public Finality getFinality() {
        return finality;
    }

    public CrystallizedState withDynasty(Dynasty dynasty) {
        return new CrystallizedState(lastStateRecalc, dynasty, finality, crosslinks);
    }

    public CrystallizedState withLastStateRecalc(long lastStateRecalc) {
        return new CrystallizedState(lastStateRecalc, dynasty, finality, crosslinks);
    }

    public CrystallizedState withLastStateRecalcIncrement(long addition) {
        return new CrystallizedState(lastStateRecalc + addition, dynasty, finality, crosslinks);
    }

    public CrystallizedState withCrosslinks(Crosslink[] crosslinks) {
        return new CrystallizedState(lastStateRecalc, dynasty, finality, crosslinks);
    }

    public CrystallizedState withFinality(Finality finality) {
        return new CrystallizedState(lastStateRecalc, dynasty, finality, crosslinks);
    }

    public static class Flattened {
        /* Hash of the validator set */
        private final byte[] validatorSetHash;
        /* Slot number that the state was calculated at */
        private final long lastStateRecalc;
        /** What active validators are part of the attester set,
         * at what height, and in what shard.
         * Starts at slot {@link #lastStateRecalc} - {@link BeaconConstants#CYCLE_LENGTH} */
        private final Committee[] committees;
        /* The last justified slot */
        private final long lastJustifiedSlot;
        /* Number of consecutive justified slots ending at this one */
        private final long justifiedStreak;
        /* The last finalized slot */
        private final long lastFinalizedSlot;
        /* The number of dynasty transitions including this one */
        private final long currentDynasty;
        /* The most recent crosslinks for each shard */
        private final Crosslink[] crosslinks;
        /* Used to select the committees for each shard */
        private final byte[] dynastySeed;
        /* Last dynasty the seed was reset */
        private final long dynastySeedLastReset;
        /* Slot that current dynasty is stared from */
        private final long dynastyStart;

        public Flattened(byte[] validatorSetHash, long lastStateRecalc, Committee[] committees, long lastJustifiedSlot,
                         long justifiedStreak, long lastFinalizedSlot, long currentDynasty, Crosslink[] crosslinks,
                         byte[] dynastySeed, long dynastySeedLastReset, long dynastyStart) {
            this.validatorSetHash = validatorSetHash;
            this.lastStateRecalc = lastStateRecalc;
            this.committees = committees;
            this.lastJustifiedSlot = lastJustifiedSlot;
            this.justifiedStreak = justifiedStreak;
            this.lastFinalizedSlot = lastFinalizedSlot;
            this.currentDynasty = currentDynasty;
            this.crosslinks = crosslinks;
            this.dynastySeed = dynastySeed;
            this.dynastySeedLastReset = dynastySeedLastReset;
            this.dynastyStart = dynastyStart;
        }

        public Flattened(byte[] encoded) {
            RLPList list = RLP.unwrapList(encoded);

            this.validatorSetHash = list.get(0).getRLPData();
            this.lastStateRecalc = byteArrayToLong(list.get(1).getRLPData());
            this.lastJustifiedSlot = byteArrayToLong(list.get(2).getRLPData());
            this.justifiedStreak = byteArrayToLong(list.get(3).getRLPData());
            this.lastFinalizedSlot = byteArrayToLong(list.get(4).getRLPData());
            this.currentDynasty = byteArrayToLong(list.get(5).getRLPData());
            this.dynastySeed = list.get(6).getRLPData();
            this.dynastySeedLastReset = byteArrayToLong(list.get(7).getRLPData());
            this.dynastyStart = byteArrayToLong(list.get(8).getRLPData());

            if (!isSingleZero(list.get(9).getRLPData())) {
                RLPList committeeList = RLP.unwrapList(list.get(9).getRLPData());
                this.committees = new Committee[committeeList.size()];
                for (int i = 0; i < committeeList.size(); i++)
                    this.committees[i] = new Committee(committeeList.get(i).getRLPData());
            } else {
                this.committees = new Committee[0];
            }

            if (!isSingleZero(list.get(10).getRLPData())) {
                RLPList crosslinkList = RLP.unwrapList(list.get(10).getRLPData());
                this.crosslinks = new Crosslink[crosslinkList.size()];
                for (int i = 0; i < crosslinkList.size(); i++)
                    this.crosslinks[i] = new Crosslink(crosslinkList.get(i).getRLPData());
            } else {
                this.crosslinks = new Crosslink[0];
            }
        }

        public byte[] encode() {
            byte[][] encodedCommittees = new byte[committees.length][];
            byte[][] encodedCrosslinks = new byte[crosslinks.length][];

            if (committees.length > 0) {
                for (int i = 0; i < committees.length; i++)
                    encodedCommittees[i] = committees[i].getEncoded();
            }

            if (crosslinks.length > 0) {
                for (int i = 0; i < crosslinks.length; i++)
                    encodedCrosslinks[i] = crosslinks[i].getEncoded();
            }

            return RLP.wrapList(validatorSetHash, longToBytesNoLeadZeroes(lastStateRecalc),
                    longToBytesNoLeadZeroes(lastJustifiedSlot), longToBytesNoLeadZeroes(justifiedStreak),
                    longToBytesNoLeadZeroes(lastFinalizedSlot), longToBytesNoLeadZeroes(currentDynasty),
                    longToBytesNoLeadZeroes(dynastyStart), dynastySeed, longToBytesNoLeadZeroes(dynastySeedLastReset),
                    encodedCommittees.length > 0 ? RLP.wrapList(encodedCommittees) : ByteUtil.ZERO_BYTE_ARRAY,
                    encodedCrosslinks.length > 0 ? RLP.wrapList(encodedCrosslinks) : ByteUtil.ZERO_BYTE_ARRAY);
        }

        public byte[] getHash() {
            return blake2b(encode());
        }

        public byte[] getValidatorSetHash() {
            return validatorSetHash;
        }

        public long getLastStateRecalc() {
            return lastStateRecalc;
        }

        public Committee[] getCommittees() {
            return committees;
        }

        public long getLastJustifiedSlot() {
            return lastJustifiedSlot;
        }

        public long getJustifiedStreak() {
            return justifiedStreak;
        }

        public long getLastFinalizedSlot() {
            return lastFinalizedSlot;
        }

        public long getCurrentDynasty() {
            return currentDynasty;
        }

        public Crosslink[] getCrosslinks() {
            return crosslinks;
        }

        public byte[] getDynastySeed() {
            return dynastySeed;
        }

        public long getDynastySeedLastReset() {
            return dynastySeedLastReset;
        }

        public long getDynastyStart() {
            return dynastyStart;
        }

        public static org.ethereum.datasource.Serializer<Flattened, byte[]> getSerializer() {
            return Serializer;
        }

        public static Flattened empty() {
            return new Flattened(ValidatorSet.EMPTY_HASH, 0, new Committee[0], 0L, 0L, 0L, 0L,
                    new Crosslink[0], new byte[32], 0L, 0L);
        }

        public static final org.ethereum.datasource.Serializer<Flattened, byte[]> Serializer = new Serializer<Flattened, byte[]>() {
            @Override
            public byte[] serialize(Flattened state) {
                return state == null ? null : state.encode();
            }

            @Override
            public Flattened deserialize(byte[] stream) {
                return stream == null ? null : new Flattened(stream);
            }
        };
    }
}
