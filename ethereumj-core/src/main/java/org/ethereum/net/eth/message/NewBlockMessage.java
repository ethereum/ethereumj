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

import java.math.BigInteger;

/**
 * Wrapper around an Ethereum Blocks message on the network
 *
 * @see EthMessageCodes#NEW_BLOCK
 */
public class NewBlockMessage extends EthMessage {

    private Block block;
    private byte[] difficulty;

    public NewBlockMessage(byte[] encoded) {
        super(encoded);
    }

    public NewBlockMessage(Block block, byte[] difficulty) {
        this.block = block;
        this.difficulty = difficulty;
        this.parsed = true;
        encode();
    }

    private void encode() {
        byte[] block = this.block.getEncoded();
        byte[] diff = RLP.encodeElement(this.difficulty);

        this.encoded = RLP.encodeList(block, diff);
    }

    private synchronized void parse() {
        if (parsed) return;
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        RLPList blockRLP = ((RLPList) paramsList.get(0));
        block = new Block(blockRLP.getRLPData());
        difficulty = paramsList.get(1).getRLPData();

        parsed = true;
    }

    public Block getBlock() {
        parse();
        return block;
    }

    public byte[] getDifficulty() {
        parse();
        return difficulty;
    }

    public BigInteger getDifficultyAsBigInt() {
        return new BigInteger(1, difficulty);
    }

    @Override
    public byte[] getEncoded() {
        return encoded;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.NEW_BLOCK;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public String toString() {
        parse();

        String hash = this.getBlock().getShortHash();
        long number = this.getBlock().getNumber();
        return "NEW_BLOCK [ number: " + number + " hash:" + hash + " difficulty: " + Hex.toHexString(difficulty) + " ]";
    }
}