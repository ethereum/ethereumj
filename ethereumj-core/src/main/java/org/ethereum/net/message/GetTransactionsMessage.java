package org.ethereum.net.message;

import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 06/04/14 14:56
 */
public class GetTransactionsMessage extends Message {

    public GetTransactionsMessage() {
        this.payload = Hex.decode("C116");
    }

    @Override
    public void parseRLP() {
    }

    public byte[] getPayload() {
        return payload;
    }

    @Override
    public String getMessageName(){
        return "GetTransactions";
    }

    @Override
    public Class getAnswerMessage() {
        return null;
    }
}

