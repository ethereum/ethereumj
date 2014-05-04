package org.ethereum.net.message;

import org.ethereum.net.rlp.RLPItem;
import org.ethereum.net.rlp.RLPList;
import org.ethereum.net.Command;
import static org.ethereum.net.Command.DISCONNECT;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 06/04/14 14:56
 */
public class DisconnectMessage extends Message {

    private byte reason;

    public static byte REASON_DISCONNECT_REQUESTED  = 0x00;
    public static byte REASON_TCP_ERROR             = 0x01;
    public static byte REASON_BAD_PROTOCOL          = 0x02;
    public static byte REASON_USELESS_PEER          = 0x03;
    public static byte REASON_TOO_MANY_PEERS        = 0x04;
    public static byte REASON_ALREADY_CONNECTED     = 0x05;
    public static byte REASON_WRONG_GENESIS         = 0x06;
    public static byte REASON_INCOMPATIBLE_PROTOCOL = 0x07;
    public static byte REASON_PEER_QUITING          = 0x08;

    public DisconnectMessage(RLPList rawData) {
        super(rawData);
    }

    @Override
    public void parseRLP() {

        RLPList paramsList = (RLPList) rawData.getElement(0);

        if (Command.fromInt(((RLPItem)(paramsList).getElement(0)).getData()[0]) != DISCONNECT){
            throw new Error("Disconnect: parsing for mal data");
        }

        byte[] reasonB = ((RLPItem)paramsList.getElement(1)).getData();
        if (reasonB == null){
            this.reason = 0;
        } else {
            this.reason = reasonB[0];
        }
        this.parsed = true;
        // todo: what to do when mal data ?
    }

    @Override
    public byte[] getPayload() {
        return null;
    }

    public byte getReason() {
        if (!parsed) parseRLP();
        return reason;
    }

    public String toString(){
        if (!parsed) parseRLP();
        return "Disconnect Message [ reason=" + reason + " ]";
    }
}

