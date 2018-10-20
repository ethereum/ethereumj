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
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;

import java.util.ArrayList;
import java.util.List;

import static org.ethereum.crypto.HashUtil.blake2b;
import static org.ethereum.sharding.processing.consensus.BeaconConstants.CYCLE_LENGTH;
import static org.ethereum.util.ByteUtil.isSingleZero;
import static org.ethereum.util.ByteUtil.toHexString;

/**
 * Active beacon chain state
 */
public class ActiveState {
    // Attestations that have not yet been processed
    private final List<AttestationRecord> pendingAttestations;
    // Special objects that have not yet been processed
    private final List<SpecialRecord> pendingSpecials;
    // Most recent 2 * CYCLE_LENGTH block hashes, older to newer
    private final List<byte[]> recentBlockHashes;
    // RANDAO state
    private final byte[] randaoMix;

    // TODO: Add pending_specials


    public ActiveState(List<AttestationRecord> pendingAttestations, List<SpecialRecord> pendingSpecials,
                       List<byte[]> recentBlockHashes, byte[] randaoMix) {
        this.pendingAttestations = pendingAttestations;
        this.pendingSpecials = pendingSpecials;
        this.recentBlockHashes = recentBlockHashes;
        this.randaoMix = randaoMix;
    }

    /**
     * Creates active state with empty pending attestations
     * and block hashes filled with `00`*32
     */
    public static ActiveState createEmpty() {
        List<AttestationRecord> pendingAttestations = new ArrayList<>();
        List<SpecialRecord> pendingSpecials = new ArrayList<>();
        List<byte[]> recentBlockHashes = new ArrayList<>();
        for (int i = 0; i < (CYCLE_LENGTH * 2); ++i) {
            recentBlockHashes.add(new byte[32]);
        }

        return new ActiveState(pendingAttestations, pendingSpecials, recentBlockHashes, new byte[32]);
    }

    public ActiveState(byte[] encoded) {
        RLPList list = RLP.unwrapList(encoded);

        this.pendingAttestations = new ArrayList<>();
        if (!isSingleZero(list.get(0).getRLPData())) {
            RLPList attestationList = RLP.unwrapList(list.get(0).getRLPData());
            for (RLPElement attestationRlp : attestationList)
                pendingAttestations.add(new AttestationRecord(attestationRlp.getRLPData()));
        }

        this.pendingSpecials = new ArrayList<>();
        if (!isSingleZero(list.get(1).getRLPData())) {
            RLPList specialsList = RLP.unwrapList(list.get(1).getRLPData());
            for (RLPElement specialRlp : specialsList)
                pendingSpecials.add(new SpecialRecord(specialRlp.getRLPData()));
        }

        this.recentBlockHashes = new ArrayList<>();
        if (!isSingleZero(list.get(2).getRLPData())) {
            RLPList hashesList = RLP.unwrapList(list.get(2).getRLPData());
            for (RLPElement hashRlp : hashesList)
                recentBlockHashes.add(hashRlp.getRLPData());
        }

        this.randaoMix = list.get(3).getRLPData();
    }

    public List<AttestationRecord> getPendingAttestations() {
        return pendingAttestations;
    }

    public List<SpecialRecord> getPendingSpecials() {
        return pendingSpecials;
    }

    public List<byte[]> getRecentBlockHashes() {
        return recentBlockHashes;
    }

    public byte[] getRandaoMix() {
        return randaoMix;
    }

    public byte[] getEncoded() {
        byte[][] encodedAttestations = new byte[pendingAttestations.size()][];
        for (int i = 0; i < pendingAttestations.size(); i++)
            encodedAttestations[i] = pendingAttestations.get(i).getEncoded();

        byte[][] encodedSpecials = new byte[pendingSpecials.size()][];
        for (int i = 0; i < pendingSpecials.size(); i++)
            encodedSpecials[i] = pendingSpecials.get(i).getEncoded();

        byte[][] encodedHashes = new byte[recentBlockHashes.size()][];
        for (int i = 0; i < recentBlockHashes.size(); i++)
            encodedHashes[i] = RLP.encodeElement(recentBlockHashes.get(i));

        return RLP.wrapList(
                pendingAttestations.size() > 0 ? RLP.wrapList(encodedAttestations) : ByteUtil.ZERO_BYTE_ARRAY,
                pendingSpecials.size() > 0 ? RLP.wrapList(encodedSpecials) : ByteUtil.ZERO_BYTE_ARRAY,
                recentBlockHashes.size() > 0 ? RLP.wrapList(encodedHashes) : ByteUtil.ZERO_BYTE_ARRAY,
                RLP.encodeElement(randaoMix)
        );
    }

    public byte[] getHash() {
        return blake2b(getEncoded());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append("ActiveState{")
                .append("pendingAttestations=[").append(pendingAttestations.size()).append(" item(s)]")
                .append("pendingSpecials=[").append(pendingSpecials.size()).append(" item(s)]")
                .append(", recentBlockHashes=[...");

        for (int i = Math.max(0, recentBlockHashes.size() - 3); i < recentBlockHashes.size(); ++i) {
            builder.append(", ").append(toHexString(recentBlockHashes.get(i)));
        }
        builder.append("]");

        builder.append(", randaoMix=")
                .append(ByteUtil.toHexString(randaoMix))
                .append('}');

        return builder.toString();
    }

    public static final org.ethereum.datasource.Serializer<ActiveState, byte[]> Serializer = new Serializer<ActiveState, byte[]>() {
        @Override
        public byte[] serialize(ActiveState state) {
            return state == null ? null : state.getEncoded();
        }

        @Override
        public ActiveState deserialize(byte[] stream) {
            return stream == null ? null : new ActiveState(stream);
        }
    };
}
