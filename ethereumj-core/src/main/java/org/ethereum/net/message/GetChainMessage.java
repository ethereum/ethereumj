package org.ethereum.net.message;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.ethereum.net.Command.GET_CHAIN;

import org.ethereum.net.Command;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.ethereum.util.Utils;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 06/04/14 14:56
 */
public class GetChainMessage extends Message {

    List<byte[]> blockHashList = new ArrayList<byte[]>();
    BigInteger blockNum;

    public GetChainMessage(RLPList rawData) {
        super(rawData);
    }

    @Override
    public void parseRLP() {

        RLPList paramsList = (RLPList) rawData.get(0);

        if (Command.fromInt(((RLPItem)(paramsList).get(0)).getData()[0]) != GET_CHAIN){
            throw new Error("GetChain: parsing for mal data");
        }

        int size = paramsList.size();
        for (int i = 1; i < size - 1; ++i){
            blockHashList.add(((RLPItem) paramsList.get(i)).getData());
        }

        // the last element is the num of requested blocks
        byte[] blockNumB = ((RLPItem)paramsList.get(size - 1)).getData();
        this.blockNum = new BigInteger(blockNumB);

        this.parsed = true;
        // todo: what to do when mal data ?
    }

    @Override
    public byte[] getPayload() {
        return null;
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
            sb.append("").append(Utils.toHexString(blockHash)).append(", ");
        }

        sb.append(" blockNum=").append(blockNum);
        return "GetChain Message [" + sb.toString() + " ]";
    }
}
