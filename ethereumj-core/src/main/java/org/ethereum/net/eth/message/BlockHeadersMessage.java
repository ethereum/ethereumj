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
package org.ethereum.net.eth.message;

import org.ethereum.core.IBlockHeader;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around an Ethereum BlockHeaders message on the network
 *
 * @see EthMessageCodes#BLOCK_HEADERS
 *
 * @author Mikhail Kalinin
 * @since 04.09.2015
 */
public class BlockHeadersMessage extends EthMessage {

    /**
     * List of block headers from the peer
     */
    private List<IBlockHeader> IBlockHeaders;

    public BlockHeadersMessage(byte[] encoded) {
        super(encoded);
    }

    public BlockHeadersMessage(List<IBlockHeader> headers) {
        this.IBlockHeaders = headers;
        parsed = true;
    }

    private synchronized void parse() {
        if (parsed) return;
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        IBlockHeaders = new ArrayList<>();
        for (int i = 0; i < paramsList.size(); ++i) {
            RLPList rlpData = ((RLPList) paramsList.get(i));
            IBlockHeaders.add(IBlockHeader.Factory.decodeBlockHeader(rlpData));
        }
        parsed = true;
    }

    private void encode() {
        List<byte[]> encodedElements = new ArrayList<>();
        for (IBlockHeader IBlockHeader : IBlockHeaders)
            encodedElements.add(IBlockHeader.getEncoded());
        byte[][] encodedElementArray = encodedElements.toArray(new byte[encodedElements.size()][]);
        this.encoded = RLP.encodeList(encodedElementArray);
    }


    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public List<IBlockHeader> getBlockHeaders() {
        parse();
        return IBlockHeaders;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.BLOCK_HEADERS;
    }

    @Override
    public String toString() {
        parse();

        StringBuilder payload = new StringBuilder();

        payload.append("count( ").append(IBlockHeaders.size()).append(" )");

        if (logger.isTraceEnabled()) {
            payload.append(" ");
            for (IBlockHeader header : IBlockHeaders) {
                payload.append(Hex.toHexString(header.getHash()).substring(0, 6)).append(" | ");
            }
            if (!IBlockHeaders.isEmpty()) {
                payload.delete(payload.length() - 3, payload.length());
            }
        } else {
            if (IBlockHeaders.size() > 0) {
                payload.append("#").append(IBlockHeaders.get(0).getNumber()).append(" (")
                        .append(Hex.toHexString(IBlockHeaders.get(0).getHash()).substring(0, 8)).append(")");
            }
            if (IBlockHeaders.size() > 1) {
                payload.append(" ... #").append(IBlockHeaders.get(IBlockHeaders.size() - 1).getNumber()).append(" (")
                        .append(Hex.toHexString(IBlockHeaders.get(IBlockHeaders.size() - 1).getHash()).substring(0, 8)).append(")");
            }
        }

        return "[" + getCommand().name() + " " + payload + "]";
    }
}
