package org.ethereum.net.swarm.bzz;

import org.ethereum.net.swarm.Key;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BzzRetrieveReqMessage extends BzzMessage {

    private Key key;

    // optional
    long maxSize = -1;
    long maxPeers = -1;
    long timeout = -1;

    public BzzRetrieveReqMessage(byte[] encoded) {
        super(encoded);
    }

    public BzzRetrieveReqMessage(Key key) {
        this.key = key;
    }

    public BzzRetrieveReqMessage(long id, Key key) {
        this.key = key;
        this.id = id;
    }

    @Override
    protected void decode() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        key = new Key(paramsList.get(0).getRLPData());

        if (paramsList.size() > 1) {
            id = ByteUtil.byteArrayToLong(paramsList.get(1).getRLPData());
        }
        if (paramsList.size() > 2) {
            maxSize = ByteUtil.byteArrayToLong(paramsList.get(2).getRLPData());
        }
        if (paramsList.size() > 3) {
            maxPeers = ByteUtil.byteArrayToLong(paramsList.get(3).getRLPData());
        }
        if (paramsList.size() > 4) {
            timeout = ByteUtil.byteArrayToLong(paramsList.get(3).getRLPData());
        }

        parsed = true;
    }

    private void encode() {
//        this.encoded = RLP.encodeList(RLP.encodeElement(key.getBytes()));
        List<byte[]> elems = new ArrayList<>();
        elems.add(RLP.encodeElement(key.getBytes()));
//        elems.add(RLP.encodeElement(data));
//        if (id >= 0) {
            elems.add(RLP.encodeElement(ByteUtil.longToBytes(id)));
//        }
//        if (maxSize >= 0) {
            elems.add(RLP.encodeElement(ByteUtil.longToBytes(maxSize)));
//        }
//        if (maxPeers >= 0) {
            elems.add(RLP.encodeElement(ByteUtil.longToBytes(maxPeers)));
            elems.add(RLP.encodeElement(ByteUtil.longToBytes(timeout)));
//        }
        this.encoded = RLP.encodeList(elems.toArray(new byte[0][]));

    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    public BzzProtocol getPeer() {
        return peer;
    }

    public Key getKey() {
        return key;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public long getMaxPeers() {
        return maxPeers;
    }

    public long getTimeout() {
        return timeout;
    }


    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Override
    public BzzMessageCodes getCommand() {
        return BzzMessageCodes.RETRIEVE_REQUEST;
    }

    @Override
    public String toString() {
        return "BzzRetrieveReqMessage{" +
                "key=" + key +
                ", id=" + id +
                ", maxSize=" + maxSize +
                ", maxPeers=" + maxPeers +
                '}';
    }
}
