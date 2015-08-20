package org.ethereum.net.eth.message;

import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

/**
 * Wrapper for Ethereum V60 STATUS message. <br>
 *
 * @see EthMessageCodes#STATUS
 *
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
public class StatusMessage60 extends StatusMessage {

    public StatusMessage60(byte[] encoded) {
        super(encoded);
    }

    public StatusMessage60(byte protocolVersion, int networkId,
                           byte[] totalDifficulty, byte[] bestHash, byte[] genesisHash) {
        super(protocolVersion, networkId, totalDifficulty, bestHash, genesisHash);
    }

    @Override
    protected void parse() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        this.protocolVersion = paramsList.get(0).getRLPData()[0];
        byte[] networkIdBytes = paramsList.get(1).getRLPData();
        this.networkId = networkIdBytes == null ? 0 : ByteUtil.byteArrayToInt(networkIdBytes);

        byte[] diff = paramsList.get(2).getRLPData();
        this.totalDifficulty = (diff == null) ? ByteUtil.ZERO_BYTE_ARRAY : diff;
        this.bestHash = paramsList.get(3).getRLPData();
        this.genesisHash = paramsList.get(4).getRLPData();

        parsed = true;
    }

    @Override
    protected void encode() {
        byte[] protocolVersion = RLP.encodeByte(this.protocolVersion);
        byte[] networkId = RLP.encodeInt(this.networkId);
        byte[] totalDifficulty = RLP.encodeElement(this.totalDifficulty);
        byte[] bestHash = RLP.encodeElement(this.bestHash);
        byte[] genesisHash = RLP.encodeElement(this.genesisHash);

        this.encoded = RLP.encodeList( protocolVersion, networkId,
                totalDifficulty, bestHash, genesisHash);
    }
}
