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

import org.ethereum.core.BlockIdentifier;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around an Ethereum NewBlockHashes message on the network<br>
 *
 * @see EthMessageCodes#NEW_BLOCK_HASHES
 *
 * @author Mikhail Kalinin
 * @since 05.09.2015
 */
public class NewBlockHashesMessage extends EthMessage {

    /**
     * List of identifiers holding hash and number of the blocks
     */
    private List<BlockIdentifier> blockIdentifiers;

    public NewBlockHashesMessage(byte[] payload) {
        super(payload);
    }

    public NewBlockHashesMessage(List<BlockIdentifier> blockIdentifiers) {
        this.blockIdentifiers = blockIdentifiers;
        parsed = true;
    }

    private synchronized void parse() {
        if (parsed) return;
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        blockIdentifiers = new ArrayList<>();
        for (int i = 0; i < paramsList.size(); ++i) {
            RLPList rlpData = ((RLPList) paramsList.get(i));
            blockIdentifiers.add(new BlockIdentifier(rlpData));
        }
        parsed = true;
    }

    private void encode() {
        List<byte[]> encodedElements = new ArrayList<>();
        for (BlockIdentifier identifier : blockIdentifiers)
            encodedElements.add(identifier.getEncoded());
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

    public List<BlockIdentifier> getBlockIdentifiers() {
        parse();
        return blockIdentifiers;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.NEW_BLOCK_HASHES;
    }

    @Override
    public String toString() {
        parse();

        return "[" + this.getCommand().name() + "] (" + blockIdentifiers.size() + ")";
    }

}
