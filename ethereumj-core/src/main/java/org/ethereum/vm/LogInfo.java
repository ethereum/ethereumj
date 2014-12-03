package org.ethereum.vm;

import org.ethereum.core.BlockHeader;
import org.ethereum.core.Bloom;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.RLP;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * www.etherj.com
 *
 * @author: Roman Mandeleil
 * Created on: 19/11/2014 22:03
 */

public class LogInfo {

    byte[] address;
    List<byte[]> topics = new ArrayList<>();
    byte[] data;

    /* Log info in encoded form */
    private byte[] rlpEncoded;

    public LogInfo(byte[] address, List<byte[]> topics, byte[] data) {
        this.address = address;
        this.topics = (topics == null) ? new ArrayList<byte[]>() : topics;
        this.data = data;
    }

    public byte[] getAddress() {
        return address;
    }

    public List<byte[]> getTopics() {
        return topics;
    }

    public byte[] getData() {
        return data;
    }

    /*  [address, [topic, topic ...] data] */
    public byte[] getEncoded() {


        byte[] addressEncoded   = RLP.encodeElement(this.address);

        byte[][] topicsEncoded = null;
        if (topics != null){
            topicsEncoded = new byte[topics.size()][];
            int i = 0;
            for( byte[] topic : topics ){
                topicsEncoded[i] = topic;
                ++i;
            }
        }

        byte[] dataEncoded = RLP.encodeElement(data);
        return RLP.encodeList(addressEncoded, RLP.encodeList(topicsEncoded), dataEncoded);
    }

    public Bloom getBloom() {
        Bloom ret = Bloom.create(HashUtil.sha3(address));
        for(byte[] topic:topics) {
            ret.or(Bloom.create(HashUtil.sha3(topic)));
        }

        return ret;
    }

    @Override
    public String toString() {

        StringBuffer topicsStr = new StringBuffer();
        topicsStr.append("[");

        for (byte[] topic: topics){
            String topicStr = Hex.toHexString(topic);
            topicsStr.append(topicStr).append(" ");
        }
        topicsStr.append("]");


        return "LogInfo{" +
                "address=" + Hex.toHexString(address) +
                ", topics=" + topicsStr +
                ", data=" + Hex.toHexString(data) +
                '}';
    }


}
