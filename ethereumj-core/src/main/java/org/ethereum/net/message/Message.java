package org.ethereum.net.message;

import org.ethereum.net.rlp.RLPList;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 06/04/14 14:58
 */
public abstract class Message {

    RLPList rawData;
    boolean parsed = false;

	public Message() {}

    public Message(RLPList rawData) {
        this.rawData = rawData;
        parsed = false;
    }

    public abstract void parseRLP();
    public abstract byte[] getPayload();

}
