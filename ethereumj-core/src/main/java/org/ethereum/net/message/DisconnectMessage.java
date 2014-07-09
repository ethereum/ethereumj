package org.ethereum.net.message;

import org.ethereum.net.Command;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;

import static org.ethereum.net.Command.DISCONNECT;
import static org.ethereum.net.message.ReasonCode.DISCONNECT_REQUESTED;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 06/04/14 14:56
 */
public class DisconnectMessage extends Message {

    private ReasonCode reason;

    public DisconnectMessage(byte[] payload) {
        super(RLP.decode2(payload));
        this.payload = payload;
    }


    public DisconnectMessage(RLPList rawData) {
        super(rawData);
    }

    @Override
    public void parseRLP() {

        RLPList paramsList = (RLPList) rawData.get(0);

        if (Command.fromInt(((RLPItem)(paramsList).get(0)).getRLPData()[0]) != DISCONNECT) {
            throw new Error("Disconnect: parsing for mal data");
        }

        byte[] reasonB = ((RLPItem)paramsList.get(1)).getRLPData();
        if (reasonB == null) {
            this.reason = DISCONNECT_REQUESTED;
        } else {
            this.reason = ReasonCode.fromInt(reasonB[0]);
        }
        this.parsed = true;
        // TODO: what to do when mal data ?
    }

    @Override
    public byte[] getPayload() {
        return null;
    }

    public ReasonCode getReason() {
        if (!parsed) parseRLP();
        return reason;
    }

    @Override
    public String getMessageName() {
        return "Disconnect";
    }

    @Override
    public Class getAnswerMessage() {
        return null;
    }

    public String toString() {
        if (!parsed) parseRLP();
        return "Disconnect Message [ reason=" + reason + " ]";
    }
}

