package org.ethereum.net.message;

import org.ethereum.util.RLPList;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 06/04/14 14:58
 */
public abstract class Message {

    RLPList rawData;
    boolean parsed = false;
    byte[] payload;

	public Message() {}

    public Message(RLPList rawData) {
        this.rawData = rawData;
        parsed = false;
    }

    public abstract void parseRLP();
    public abstract byte[] getPayload();

    public abstract String getMessageName();

    public abstract Class getAnswerMessage();


}
