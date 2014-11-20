package org.ethereum.vm;

import org.spongycastle.util.encoders.Hex;

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
    List<byte[]> topics;
    byte[] data;

    public LogInfo(byte[] address, List<byte[]> topics, byte[] data) {
        this.address = address;
        this.topics = topics;
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
