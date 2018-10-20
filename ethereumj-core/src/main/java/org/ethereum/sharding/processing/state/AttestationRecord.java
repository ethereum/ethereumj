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

import org.ethereum.sharding.crypto.Sign;
import org.ethereum.sharding.util.Bitfield;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ethereum.util.ByteUtil.bigIntegerToBytes;
import static org.ethereum.util.ByteUtil.byteArrayToLong;
import static org.ethereum.util.ByteUtil.bytesToBigInteger;
import static org.ethereum.util.ByteUtil.intToBytesNoLeadZeroes;
import static org.ethereum.util.ByteUtil.longToBytesNoLeadZeroes;
import static org.ethereum.util.ByteUtil.toHexString;

/**
 * Slot attestation data
 */
public class AttestationRecord {

    // Slot number
    private final long slot;
    // Shard ID
    private final int shardId;
    // List of block hashes that this signature is signing over that
    // are NOT part of the current chain, in order of oldest to newest
    private final List<byte[]> obliqueParentHashes;
    // Block hash in the shard that we are attesting to
    private final byte[] shardBlockHash;
    // Who is participating
    private final Bitfield attesterBitfield;
    // Last justified block
    private final long justifiedSlot;
    private final byte[] justifiedBlockHash;
    // The actual signature
    private final Sign.Signature aggregateSig;

    public AttestationRecord(long slot, int shardId, List<byte[]> obliqueParentHashes, byte[] shardBlockHash,
                             Bitfield attesterBitfield, long justifiedSlot, byte[] justifiedBlockHash,
                             Sign.Signature aggregateSig) {
        this.slot = slot;
        this.shardId = shardId;
        this.obliqueParentHashes = obliqueParentHashes;
        this.shardBlockHash = shardBlockHash;
        this.attesterBitfield = attesterBitfield;
        this.justifiedSlot = justifiedSlot;
        this.justifiedBlockHash = justifiedBlockHash;
        this.aggregateSig = aggregateSig;
    }

    public AttestationRecord(byte[] encoded) {
        RLPList list = RLP.unwrapList(encoded);

        this.slot = byteArrayToLong(list.get(0).getRLPData());
        this.shardId = bytesToBigInteger(list.get(1).getRLPData()).shortValue();

        RLPList hashesList = RLP.unwrapList(list.get(2).getRLPData());
        this.obliqueParentHashes = new ArrayList<>(hashesList.size());
        for (RLPElement hashRlp : hashesList)
            this.obliqueParentHashes.add(hashRlp.getRLPData());

        this.shardBlockHash = list.get(3).getRLPData();
        this.attesterBitfield = new Bitfield(list.get(4).getRLPData());
        this.justifiedSlot = byteArrayToLong(list.get(5).getRLPData());
        this.justifiedBlockHash = list.get(6).getRLPData();

        RLPList sigList = RLP.unwrapList(list.get(7).getRLPData());
        this.aggregateSig = new Sign.Signature();
        this.aggregateSig.r = ByteUtil.bytesToBigInteger(sigList.get(0).getRLPData());
        this.aggregateSig.s = ByteUtil.bytesToBigInteger(sigList.get(1).getRLPData());
    }

    public long getSlot() {
        return slot;
    }

    public int getShardId() {
        return shardId;
    }

    public List<byte[]> getObliqueParentHashes() {
        return obliqueParentHashes;
    }

    public byte[] getShardBlockHash() {
        return shardBlockHash;
    }

    public Bitfield getAttesterBitfield() {
        return attesterBitfield;
    }

    public long getJustifiedSlot() {
        return justifiedSlot;
    }

    public byte[] getJustifiedBlockHash() {
        return justifiedBlockHash;
    }

    public Sign.Signature getAggregateSig() {
        return aggregateSig;
    }

    public byte[] getEncoded() {
        byte[][] encodedAggSig = new byte[2][];
        encodedAggSig[0] = bigIntegerToBytes(aggregateSig.r);
        encodedAggSig[1] = bigIntegerToBytes(aggregateSig.s);

        byte[][] encodedHashes = new byte[obliqueParentHashes.size()][];
        for (int i = 0; i < obliqueParentHashes.size(); i++)
            encodedHashes[i] = RLP.encodeElement(obliqueParentHashes.get(i));

        return RLP.wrapList(longToBytesNoLeadZeroes(slot),
                intToBytesNoLeadZeroes(shardId),
                obliqueParentHashes.size() > 0 ? RLP.wrapList(encodedHashes) : ByteUtil.ZERO_BYTE_ARRAY,
                shardBlockHash,
                attesterBitfield.getData(),
                longToBytesNoLeadZeroes(justifiedSlot),
                justifiedBlockHash,
                RLP.wrapList(encodedAggSig));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttestationRecord that = (AttestationRecord) o;
        return Arrays.equals(this.getEncoded(), that.getEncoded());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append("AttestationRecord{")
                .append("slot=").append(slot)
                .append(", shardId=").append(shardId)
                .append(", obliqueParentHashes=[").append(obliqueParentHashes.size()).append(" item(s)]")
                .append(", shardBlockHash=").append(toHexString(shardBlockHash))
                .append(", attesterBitfield=").append(attesterBitfield)
                .append(", justifiedSlot=").append(justifiedSlot)
                .append(", justifiedBlockHash=").append(toHexString(justifiedBlockHash))
                .append(", aggregateSig=[").append(aggregateSig).append("}");

        return builder.toString();
    }
}
