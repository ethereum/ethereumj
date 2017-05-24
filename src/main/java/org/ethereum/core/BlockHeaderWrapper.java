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
package org.ethereum.core;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.List;

/**
 * <p>Wraps {@link BlockHeader}</p>
 * Adds some additional data
 *
 * @author Mikhail Kalinin
 * @since 05.02.2016
 */
public class BlockHeaderWrapper {

    private BlockHeader header;
    private byte[] nodeId;

    public BlockHeaderWrapper(BlockHeader header, byte[] nodeId) {
        this.header = header;
        this.nodeId = nodeId;
    }

    public BlockHeaderWrapper(byte[] bytes) {
        parse(bytes);
    }

    public byte[] getBytes() {
        byte[] headerBytes = header.getEncoded();
        byte[] nodeIdBytes = RLP.encodeElement(nodeId);
        return RLP.encodeList(headerBytes, nodeIdBytes);
    }

    private void parse(byte[] bytes) {
        List<RLPElement> params = RLP.decode2(bytes);
        List<RLPElement> wrapper = (RLPList) params.get(0);

        byte[] headerBytes = wrapper.get(0).getRLPData();

        this.header= new BlockHeader(headerBytes);
        this.nodeId = wrapper.get(1).getRLPData();
    }

    public byte[] getNodeId() {
        return nodeId;
    }

    public byte[] getHash() {
        return header.getHash();
    }

    public long getNumber() {
        return header.getNumber();
    }

    public BlockHeader getHeader() {
        return header;
    }

    public String getHexStrShort() {
        return Hex.toHexString(header.getHash()).substring(0, 6);
    }

    public boolean sentBy(byte[] nodeId) {
        return Arrays.equals(this.nodeId, nodeId);
    }

    @Override
    public String toString() {
        return "BlockHeaderWrapper {" +
                "header=" + header +
                ", nodeId=" + Hex.toHexString(nodeId) +
                '}';
    }
}
