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

import org.ethereum.sharding.processing.consensus.BeaconConstants;
import org.ethereum.sharding.processing.db.ValidatorSet;

import java.math.BigInteger;

/**
 * @author Mikhail Kalinin
 * @since 12.09.2018
 */
public class Dynasty {

    /* Set of validators */
    private final ValidatorSet validatorSet;
    /** What active validators are part of the attester set,
     * at what height, and in what shard.
     * Starts at slot {@link #startSlot} - {@link BeaconConstants#CYCLE_LENGTH} */
    private final Committee[] committees;
    /* The number of dynasty transitions including this one */
    private final long number;
    /* The next shard that crosslinking assignment will start from */
    private final long crosslinkingStartShard;
    /* Total balance of deposits in wei */
    private final BigInteger totalDeposits;
    /* Used to select the committees for each shard */
    private final byte[] seed;
    /* Last dynasty the seed was reset */
    private final long seedLastReset;
    /* Slot that current dynasty is stared from */
    private final long startSlot;

    public Dynasty(ValidatorSet validatorSet, Committee[] committees, long number, long crosslinkingStartShard,
                   BigInteger totalDeposits, byte[] seed, long seedLastReset, long startSlot) {
        this.validatorSet = validatorSet;
        this.committees = committees;
        this.number = number;
        this.crosslinkingStartShard = crosslinkingStartShard;
        this.totalDeposits = totalDeposits;
        this.seed = seed;
        this.seedLastReset = seedLastReset;
        this.startSlot = startSlot;
    }

    public ValidatorSet getValidatorSet() {
        return validatorSet;
    }

    public Committee[] getCommittees() {
        return committees;
    }

    public long getNumber() {
        return number;
    }

    public long getCrosslinkingStartShard() {
        return crosslinkingStartShard;
    }

    public BigInteger getTotalDeposits() {
        return totalDeposits;
    }

    public byte[] getSeed() {
        return seed;
    }

    public long getSeedLastReset() {
        return seedLastReset;
    }

    public long getStartSlot() {
        return startSlot;
    }

    public Dynasty withValidatorSet(ValidatorSet validatorSet) {
        return new Dynasty(validatorSet, committees, number, crosslinkingStartShard,
                totalDeposits, seed, seedLastReset, startSlot);
    }

    public Dynasty withNumber(long number) {
        return new Dynasty(validatorSet, committees, number, crosslinkingStartShard,
                totalDeposits, seed, seedLastReset, startSlot);
    }

    public Dynasty withCrosslinkingStartShard(long crosslinkingStartShard) {
        return new Dynasty(validatorSet, committees, number, crosslinkingStartShard,
                totalDeposits, seed, seedLastReset, startSlot);
    }

    public Dynasty withTotalDepositsIncrement(BigInteger addition) {
        return new Dynasty(validatorSet, committees, number, crosslinkingStartShard,
                totalDeposits.add(addition), seed, seedLastReset, startSlot);
    }
}
