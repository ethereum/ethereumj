package org.ethereum.net.shh;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import static org.ethereum.net.shh.ShhMessageCodes.FILTER;

/**
 * @author by Konstantin Shabalin
 */
public class ShhFilterMessage extends ShhMessage {

    private byte[] bloomFilter;

    private ShhFilterMessage() {
    }

    public ShhFilterMessage(byte[] encoded) {
        super(encoded);
    }

    static ShhFilterMessage createFromFilter(byte[] bloomFilter) {
        ShhFilterMessage ret = new ShhFilterMessage();
        ret.bloomFilter = bloomFilter;
        return ret;
    }

    private void encode() {
        byte[] protocolVersion = RLP.encodeElement(this.bloomFilter);
        this.encoded = RLP.encodeList(protocolVersion);
    }

    private void parse() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);
        this.bloomFilter = paramsList.get(0).getRLPData();
        parsed = true;
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    public byte[] getBloomFilter() {
        return bloomFilter;
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
            " hash=" + bloomFilter + "]";
    }

}
