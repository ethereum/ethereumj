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
package org.ethereum.sharding.domain;

import org.ethereum.datasource.Serializer;
import org.ethereum.sharding.processing.state.AttestationRecord;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import static org.ethereum.crypto.HashUtil.blake2b;

/**
 * Beacon chain block structure.
 *
 * @author Mikhail Kalinin
 * @since 14.08.2018
 */
public class Beacon {

    /* Hash of the parent block */
    private byte[] parentHash;
    /* Randao commitment reveal */
    private byte[] randaoReveal;
    /* Reference to main chain block */
    private byte[] mainChainRef;
    /* Hash of the state */
    private byte[] stateHash;
    /* Slot number */
    private long slotNumber;
    /* Attestations */
    private AttestationRecord[] attestations;

    public Beacon(byte[] parentHash, byte[] randaoReveal, byte[] mainChainRef, byte[] stateHash,
                  long slotNumber, AttestationRecord[] attestations) {
        this.parentHash = parentHash;
        this.randaoReveal = randaoReveal;
        this.mainChainRef = mainChainRef;
        this.stateHash = stateHash;
        this.slotNumber = slotNumber;
        this.attestations = attestations;
    }

    public Beacon(byte[] rlp) {
        RLPList items = RLP.unwrapList(rlp);
        this.parentHash = items.get(0).getRLPData();
        this.randaoReveal = items.get(1).getRLPData();
        this.mainChainRef = items.get(2).getRLPData();
        this.stateHash = items.get(3).getRLPData();
        this.slotNumber = ByteUtil.bytesToBigInteger(items.get(4).getRLPData()).longValue();

        RLPList attestationsRlp = RLP.unwrapList(items.get(5).getRLPData());
        this.attestations = new AttestationRecord[attestationsRlp.size()];
        for (int i = 0; i < attestationsRlp.size(); ++i) {
            attestations[i] = new AttestationRecord(attestationsRlp.get(i).getRLPData());
        }
    }

    public byte[] getEncoded() {
        return RLP.wrapList(parentHash, randaoReveal, mainChainRef, stateHash,
                BigInteger.valueOf(slotNumber).toByteArray());
    }

    public byte[] getHash() {
        return blake2b(getEncoded());
    }

    public byte[] getParentHash() {
        return parentHash;
    }

    public byte[] getRandaoReveal() {
        return randaoReveal;
    }

    public byte[] getMainChainRef() {
        return mainChainRef;
    }

    public byte[] getStateHash() {
        return stateHash;
    }

    public long getSlotNumber() {
        return slotNumber;
    }

    public AttestationRecord[] getAttestations() {
        return attestations;
    }

    public boolean isParentOf(Beacon other) {
        return FastByteComparisons.equal(this.getHash(), other.getParentHash());
    }

    public void setStateHash(byte[] stateHash) {
        this.stateHash = stateHash;
    }

    public void setAttestations(AttestationRecord[] attestations) {
        this.attestations = attestations;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || !(other instanceof Beacon)) return false;

        return FastByteComparisons.equal(this.getHash(), ((Beacon) other).getHash());
    }

    @Override
    public String toString() {
        return "#" + getSlotNumber() + " (" + Hex.toHexString(getHash()).substring(0,6) + " <~ "
                + Hex.toHexString(getParentHash()).substring(0,6) + "; mainChainRef: " +
                Hex.toHexString(mainChainRef).substring(0,6) + ")";
    }

    public boolean isGenesis() {
        return this.slotNumber == 0L;
    }

    public static final Serializer<Beacon, byte[]> Serializer = new Serializer<Beacon, byte[]>() {
        @Override
        public byte[] serialize(Beacon block) {
            return block == null ? null : block.getEncoded();
        }

        @Override
        public Beacon deserialize(byte[] stream) {
            return stream == null ? null : new Beacon(stream);
        }
    };
}
