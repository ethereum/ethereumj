package org.ethereum.net.message;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.ethereum.net.Command.GET_CHAIN;

import org.ethereum.net.Command;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 06/04/14 14:56
 */
public class GetChainMessage extends Message {

    List<byte[]> blockHashList = new ArrayList<byte[]>();
    BigInteger blockNum;

    public GetChainMessage(RLPList rawData) {
        super(rawData);
    }

    // todo: it get's byte for now change it to int
    public GetChainMessage(byte number , byte[]... blockHashList){

        byte[][] encodedElements = new byte[blockHashList.length + 2][];

        encodedElements[0] = new byte[]{0x14};
        int i = 1;
        for (byte[] hash : blockHashList){
            this.blockHashList.add(hash);
            byte[] element = RLP.encodeElement(hash);
            encodedElements[i] = element;
            ++i;
        }
        encodedElements[i] = RLP.encodeByte(number);

        this.payload = RLP.encodeList(encodedElements);
        this.parsed = true;

    }

    @Override
    public void parseRLP() {

        RLPList paramsList = (RLPList) rawData.get(0);

        if (Command.fromInt(((RLPItem)(paramsList).get(0)).getRLPData()[0]) != GET_CHAIN){
            throw new Error("GetChain: parsing for mal data");
        }

        int size = paramsList.size();
        for (int i = 1; i < size - 1; ++i){
            blockHashList.add(((RLPItem) paramsList.get(i)).getRLPData());
        }

        // the last element is the num of requested blocks
        byte[] blockNumB = ((RLPItem)paramsList.get(size - 1)).getRLPData();
        this.blockNum = new BigInteger(blockNumB);

        this.parsed = true;
        // todo: what to do when mal data ?
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }

    public List<byte[]> getBlockHashList() {
        if (!parsed) parseRLP();
        return blockHashList;
    }

    public BigInteger getBlockNum() {
        if (!parsed) parseRLP();
        return blockNum;
    }

    public String toString(){

        if (!parsed) parseRLP();

        StringBuffer sb = new StringBuffer();
        for (byte[] blockHash : blockHashList){
            sb.append("").append(Hex.toHexString(blockHash)).append(", ");
        }

        sb.append(" blockNum=").append(blockNum);
        return "GetChain Message [" + sb.toString() + " ]";
    }
}
