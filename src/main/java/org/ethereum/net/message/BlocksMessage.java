package org.ethereum.net.message;

import org.ethereum.net.RLP;
import org.ethereum.net.rlp.RLPItem;
import org.ethereum.net.rlp.RLPList;
import org.ethereum.net.vo.BlockData;
import org.ethereum.net.vo.TransactionData;

import java.util.ArrayList;
import java.util.List;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 06/04/14 14:56
 */
public class BlocksMessage extends Message {

    private final byte commandCode = 0x13;

    private List<BlockData> blockDataList = new ArrayList<BlockData>();


    public BlocksMessage(RLPList rawData) {
        super(rawData);
    }



    public void parseRLP() {

        RLPList paramsList = (RLPList) rawData.getElement(0);

        if (((RLPItem)(paramsList).getElement(0)).getData()[0] != commandCode){

            throw new Error("BlocksMessage: parsing for mal data");
        }

        for (int i = 1; i < paramsList.size(); ++i){

            RLPList rlpData = ((RLPList)paramsList.getElement(i));
            BlockData blockData = new BlockData(rlpData);
            this.blockDataList.add(blockData);
        }

        parsed = true;
    }


    @Override
    public byte[] getPayload() {
        return null;
    }


    public List<BlockData> getBlockDataList() {
        if (!parsed) parseRLP();
        return blockDataList;
    }

    public String toString(){

        StringBuffer sb = new StringBuffer();
        for (BlockData blockData : this.getBlockDataList()){
            sb.append("   ").append( blockData.toString() ).append("\n");

            List<TransactionData> transactions = blockData.getTransactionsList();
            for (TransactionData transactionData : transactions){

                sb.append("[").append(transactionData).append("]\n");
            }
        }

        return "Blocks Message [\n" +
                  sb.toString()
                + " ]";

    }
}