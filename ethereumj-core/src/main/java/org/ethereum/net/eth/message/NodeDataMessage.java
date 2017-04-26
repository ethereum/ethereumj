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

import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.ethereum.util.Value;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around an Ethereum NodeData message on the network
 *
 * @see EthMessageCodes#NODE_DATA
 */
public class NodeDataMessage extends EthMessage {

    private List<Value> dataList;

    public NodeDataMessage(byte[] encoded) {
        super(encoded);
        parse();
    }

    public NodeDataMessage(List<Value> dataList) {
        this.dataList = dataList;
        parsed = true;
    }

    private void parse() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        dataList = new ArrayList<>();
        for (int i = 0; i < paramsList.size(); ++i) {
            // Need it AS IS
            dataList.add(Value.fromRlpEncoded(paramsList.get(i).getRLPData()));
        }
        parsed = true;
    }

    private void encode() {
        List<byte[]> dataListRLP = new ArrayList<>();
        for (Value value: dataList) {
            if (value == null) continue; // Bad sign
            dataListRLP.add(RLP.encodeElement(value.getData()));
        }
        byte[][] encodedElementArray = dataListRLP.toArray(new byte[dataListRLP.size()][]);
        this.encoded = RLP.encodeList(encodedElementArray);
    }


    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    public List<Value> getDataList() {
        return dataList;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.NODE_DATA;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public String toString() {

        StringBuilder payload = new StringBuilder();

        payload.append("count( ").append(dataList.size()).append(" )");

        if (logger.isTraceEnabled()) {
            payload.append(" ");
            for (Value value : dataList) {
                payload.append(value).append(" | ");
            }
            if (!dataList.isEmpty()) {
                payload.delete(payload.length() - 3, payload.length());
            }
        }

        return "[" + getCommand().name() + " " + payload + "]";
    }
}
