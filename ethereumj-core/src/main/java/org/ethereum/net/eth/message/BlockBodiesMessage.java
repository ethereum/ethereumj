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

import org.ethereum.core.Block;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around an Ethereum BlockBodies message on the network
 *
 * @see EthMessageCodes#BLOCK_BODIES
 *
 * @author Mikhail Kalinin
 * @since 04.09.2015
 */
public class BlockBodiesMessage extends EthMessage {

    private List<byte[]> blockBodies;

    public BlockBodiesMessage(byte[] encoded) {
        super(encoded);
    }

    public BlockBodiesMessage(List<byte[]> blockBodies) {
        this.blockBodies = blockBodies;
        parsed = true;
    }

    private synchronized void parse() {
        if (parsed) return;
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        blockBodies = new ArrayList<>();
        for (int i = 0; i < paramsList.size(); ++i) {
            RLPList rlpData = ((RLPList) paramsList.get(i));
            blockBodies.add(rlpData.getRLPData());
        }
        parsed = true;
    }

    private void encode() {

        byte[][] encodedElementArray = blockBodies
                .toArray(new byte[blockBodies.size()][]);

        this.encoded = RLP.encodeList(encodedElementArray);
    }


    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    public List<byte[]> getBlockBodies() {
        parse();
        return blockBodies;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.BLOCK_BODIES;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public String toString() {
        parse();

        StringBuilder payload = new StringBuilder();

        payload.append("count( ").append(blockBodies.size()).append(" )");

        if (logger.isTraceEnabled()) {
            payload.append(" ");
            for (byte[] body : blockBodies) {
                payload.append(Hex.toHexString(body)).append(" | ");
            }
            if (!blockBodies.isEmpty()) {
                payload.delete(payload.length() - 3, payload.length());
            }
        }

        return "[" + getCommand().name() + " " + payload + "]";
    }
}
