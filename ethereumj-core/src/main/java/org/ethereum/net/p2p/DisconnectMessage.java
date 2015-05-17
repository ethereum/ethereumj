package org.ethereum.net.p2p;

import org.ethereum.net.message.ReasonCode;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import static org.ethereum.net.message.ReasonCode.REQUESTED;
import static org.ethereum.net.p2p.P2pMessageCodes.DISCONNECT;

/**
 * Wrapper around an Ethereum Disconnect message on the network
 *
 * @see org.ethereum.net.p2p.P2pMessageCodes#DISCONNECT
 */
public class DisconnectMessage extends P2pMessage {

    private ReasonCode reason;

    public DisconnectMessage(byte[] encoded) {
        super(encoded);
    }

    public DisconnectMessage(ReasonCode reason) {
        this.reason = reason;
        parsed = true;
    }

    private void parse() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        byte[] reasonBytes = paramsList.get(0).getRLPData();
        if (reasonBytes == null)
            this.reason = REQUESTED;
        else
            this.reason = ReasonCode.fromInt(reasonBytes[0]);

        parsed = true;
    }

    private void encode() {
        byte[] encodedReason = RLP.encodeByte(this.reason.asByte());
        this.encoded = RLP.encodeList(encodedReason);
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    @Override
    public P2pMessageCodes getCommand() {
        return P2pMessageCodes.DISCONNECT;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public ReasonCode getReason() {
        if (!parsed) parse();
        return reason;
    }

    public String toString() {
        if (!parsed) parse();
        return "[" + this.getCommand().name() + " reason=" + reason + "]";
    }
}