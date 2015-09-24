package org.ethereum.net.shh;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import static org.ethereum.net.shh.ShhMessageCodes.FILTER;

/**
 * @author by Konstantin Shabalin
 */
public class ShhFilterMessage extends ShhMessage {

    private ByteArrayWrapper bloomFilterHash;

    public ShhFilterMessage(byte[] encoded) {
        super(encoded);
    }

    public ShhFilterMessage(ByteArrayWrapper bloomFilterHash) {
        this.bloomFilterHash = bloomFilterHash;
        this.parsed = true;
    }

    private void encode() {
        byte[] protocolVersion = RLP.encodeElement(this.bloomFilterHash.getData());
        this.encoded = RLP.encodeList(protocolVersion);
    }

    private void parse() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);
        this.bloomFilterHash = new ByteArrayWrapper(paramsList.get(0).getRLPData());
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
        return FILTER;
    }

    @Override
    public String toString() {
        if (!parsed) parse();
        return "[" + this.getCommand().name() +
            " hash=" + bloomFilterHash + "]";
    }

}
