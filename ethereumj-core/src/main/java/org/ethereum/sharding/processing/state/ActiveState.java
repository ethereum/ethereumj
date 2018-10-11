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

import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import static org.ethereum.crypto.HashUtil.blake2b;
import static org.ethereum.sharding.processing.consensus.BeaconConstants.CYCLE_LENGTH;
import static org.ethereum.util.ByteUtil.toHexString;

/**
 * Active beacon chain state
 */
public class ActiveState {
    // Attestations that have not yet been processed
    private final AttestationRecord[] pendingAttestations;
    // Most recent 2 * CYCLE_LENGTH block hashes, older to newer
    private final byte[][] recentBlockHashes;
    // RANDAO state
    private final byte[] randaoMix;

    // TODO: Add pending_specials


    public ActiveState(AttestationRecord[] pendingAttestations, byte[][] recentBlockHashes, byte[] randaoMix) {
        this.pendingAttestations = pendingAttestations;
        this.recentBlockHashes = recentBlockHashes;
        this.randaoMix = randaoMix;
    }

    /**
     * Creates active state with empty pending attestations
     * and block hashes filled with `00`*32
     */
    public static ActiveState createEmpty() {
        AttestationRecord[] pendingAttestations = new AttestationRecord[0];
        int size = CYCLE_LENGTH * 2;
        byte[][] recentBlockHashes = new byte[size][];
        for (int i = 0; i < size; ++i) {
            recentBlockHashes[i] = new byte[32];
        }

        return new ActiveState(pendingAttestations, recentBlockHashes, new byte[32]);
    }

    public ActiveState(byte[] encoded) {
        RLPList list = RLP.unwrapList(encoded);

        RLPList attestationList = RLP.unwrapList(list.get(0).getRLPData());
        this.pendingAttestations = new AttestationRecord[attestationList.size()];
        for (int i = 0; i < attestationList.size(); i++)
            pendingAttestations[i] = new AttestationRecord(attestationList.get(i).getRLPData());

        RLPList hashesList = RLP.unwrapList(list.get(1).getRLPData());
        this.recentBlockHashes = new byte[hashesList.size()][];
        for (int i = 0; i < hashesList.size(); i++)
            recentBlockHashes[i] = hashesList.get(i).getRLPData();

        this.randaoMix = list.get(2).getRLPData();
    }

    public AttestationRecord[] getPendingAttestations() {
        return pendingAttestations;
    }

    public byte[][] getRecentBlockHashes() {
        return recentBlockHashes;
    }

    public byte[] getRandaoMix() {
        return randaoMix;
    }

    public byte[] getEncoded() {
        byte[][] encodedAttestations = new byte[pendingAttestations.length][];

        for (int i = 0; i < pendingAttestations.length; i++)
            encodedAttestations[i] = pendingAttestations[i].getEncoded();

        return RLP.wrapList(
                pendingAttestations.length > 0 ? RLP.wrapList(encodedAttestations) : ByteUtil.ZERO_BYTE_ARRAY,
                recentBlockHashes.length > 0 ? RLP.wrapList(recentBlockHashes) : ByteUtil.ZERO_BYTE_ARRAY,
                RLP.encodeElement(randaoMix)
        );
    }

    public byte[] getHash() {
        return blake2b(getEncoded());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append("ActiveState{")
                .append("pendingAttestations=[").append(pendingAttestations.length).append(" item(s)]")
                .append(", recentBlockHashes=[...");

        for (int i = Math.max(0, recentBlockHashes.length - 3); i < recentBlockHashes.length; ++i) {
            builder.append(", ").append(toHexString(recentBlockHashes[i]));
        }
        builder.append("]");

        builder.append(", randaoMix=")
                .append(ByteUtil.toHexString(randaoMix))
                .append('}');

        return builder.toString();
    }
}
