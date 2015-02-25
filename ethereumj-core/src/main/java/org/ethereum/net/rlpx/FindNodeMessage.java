package org.ethereum.net.rlpx;

import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;

public class FindNodeMessage extends Message {

    public static Message create(byte[] target) {

        long expiration = System.currentTimeMillis();

        /* RLP Encode data */
        byte[] rlpToken = RLP.encodeElement(target);
        byte[] rlpExp = RLP.encodeElement(ByteUtil.longToBytes(expiration));

        byte[] type = new byte[]{3};
        byte[] data = RLP.encodeList(rlpToken, rlpExp);

        FindNodeMessage findNode = new FindNodeMessage();
        findNode.encode(type, data);

        return findNode;
    }


    @Override
    public String toString() {
        return "FindNodeMessage: " + super.toString();
    }

}
