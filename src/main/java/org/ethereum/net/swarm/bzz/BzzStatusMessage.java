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
package org.ethereum.net.swarm.bzz;

import org.ethereum.net.client.Capability;
import org.ethereum.net.swarm.Util;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ethereum.net.swarm.Util.*;

/**
 * BZZ handshake message
 */
public class BzzStatusMessage extends BzzMessage {

    private long version;
    private String id;
    private PeerAddress addr;
    private long networkId;
    private List<Capability> capabilities;

    public BzzStatusMessage(byte[] encoded) {
        super(encoded);
    }

    public BzzStatusMessage(int version, String id, PeerAddress addr, long networkId, List<Capability> capabilities) {
        this.version = version;
        this.id = id;
        this.addr = addr;
        this.networkId = networkId;
        this.capabilities = capabilities;
        parsed = true;
    }

    @Override
    protected void decode() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        version = rlpDecodeLong(paramsList.get(0));
        id = rlpDecodeString(paramsList.get(1));
        addr = PeerAddress.parse((RLPList) paramsList.get(2));
        networkId = rlpDecodeInt(paramsList.get(3));

        capabilities = new ArrayList<>();
        RLPList caps = (RLPList) paramsList.get(4);
        for (RLPElement c : caps) {
            RLPList e = (RLPList) c;
            capabilities.add(new Capability(rlpDecodeString(e.get(0)), rlpDecodeByte(e.get(1))));
        }

        parsed = true;
    }

    private void encode() {
        byte[][] capabilities = new byte[this.capabilities.size()][];
        for (int i = 0; i < this.capabilities.size(); i++) {
            Capability capability = this.capabilities.get(i);
            capabilities[i] = rlpEncodeList(capability.getName(),capability.getVersion());
        }
        this.encoded = rlpEncodeList(version, id, addr.encodeRlp(), networkId, rlpEncodeList(capabilities));
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    /**
     * BZZ protocol version
     */
    public long getVersion() {
        return version;
    }

    /**
     * Gets the remote peer address
     */
    public PeerAddress getAddr() {
        return addr;
    }

    public long getNetworkId() {
        return networkId;
    }

    public List<Capability> getCapabilities() {
        return capabilities;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Override
    public BzzMessageCodes getCommand() {
        return BzzMessageCodes.STATUS;
    }


    @Override
    public String toString() {
        return "BzzStatusMessage{" +
                "version=" + version +
                ", id='" + id + '\'' +
                ", addr=" + addr +
                ", networkId=" + networkId +
                ", capabilities=" + capabilities +
                '}';
    }
}
