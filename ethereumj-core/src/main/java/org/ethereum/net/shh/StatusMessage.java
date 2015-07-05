package org.ethereum.net.shh;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import static org.ethereum.net.shh.ShhMessageCodes.STATUS;

/**
 * @author by Konstantin Shabalin
 */
public class StatusMessage extends ShhMessage {

    private byte protocolVersion;

    public StatusMessage(byte[] encoded) {
        super(encoded);
    }

    public StatusMessage(byte protocolVersion) {
        this.protocolVersion = protocolVersion;
        this.parsed = true;
    }

    private void encode() {
        byte[] protocolVersion = RLP.encodeByte(this.protocolVersion);
        this.encoded = RLP.encodeList(protocolVersion);
    }

    private void parse() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);
        this.protocolVersion = paramsList.get(0).getRLPData()[0];
        parsed = true;
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

    @Override
    public ShhMessageCodes getCommand() {
        return STATUS;
    }

    @Override
    public String toString() {
        if (!parsed) parse();
        return "[" + this.getCommand().name() +
            " protocolVersion=" + this.protocolVersion + "]";
    }

}
