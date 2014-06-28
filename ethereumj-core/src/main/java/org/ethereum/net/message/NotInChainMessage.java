package org.ethereum.net.message;

import static org.ethereum.net.Command.NOT_IN_CHAIN;

import org.ethereum.net.Command;
import org.ethereum.net.message.Message;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 06/04/14 14:56
 */
public class NotInChainMessage extends Message {

    private byte[] hash;

    public NotInChainMessage(RLPList rawData) {
        super(rawData);
    }

    @Override
    public void parseRLP() {
        RLPList paramsList = (RLPList) rawData.get(0);

        if (Command.fromInt(((RLPItem)(paramsList).get(0)).getRLPData()[0] & 0xFF) != NOT_IN_CHAIN) {
            throw new Error("NotInChain Message: parsing for mal data");
        }
        hash = ((RLPItem)paramsList.get(1)).getRLPData();
    }

    @Override
    public byte[] getPayload() {
        return null;
    }

    public byte[] getHash() {
        return hash;
    }

    public String toString() {
        if (!parsed) 
        	parseRLP();
        return "NotInChain Message [" + Hex.toHexString(hash) + "]";
    }
}
