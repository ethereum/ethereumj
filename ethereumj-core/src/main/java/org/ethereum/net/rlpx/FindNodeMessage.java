package org.ethereum.net.rlpx;

import org.ethereum.crypto.ECKey;
import org.ethereum.util.RLP;

import static org.ethereum.util.ByteUtil.longToBytes;
import static org.ethereum.util.ByteUtil.stripLeadingZeroes;

public class FindNodeMessage extends Message {

    public static Message create(byte[] target, ECKey privKey) {

        long expiration = 3 + System.currentTimeMillis() / 1000;


        /* RLP Encode data */
        byte[] rlpToken = RLP.encodeElement(target);

        byte[] tmpExp = longToBytes(expiration);
        byte[] rlpExp = RLP.encodeElement(stripLeadingZeroes(tmpExp));

        byte[] type = new byte[]{3};
        byte[] data = RLP.encodeList(rlpToken, rlpExp);

        FindNodeMessage findNode = new FindNodeMessage();
        findNode.encode(type, data, privKey);

        return findNode;
    }


    @Override
    public String toString() {
        return "FindNodeMessage: " + super.toString();
    }

}
