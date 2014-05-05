package org.ethereum.net.message;

import java.util.ArrayList;
import java.util.List;

import static org.ethereum.net.Command.BLOCKS;

import org.ethereum.net.Command;
import org.ethereum.net.rlp.RLPItem;
import org.ethereum.net.rlp.RLPList;
import org.ethereum.net.vo.Block;
import org.ethereum.net.vo.Transaction;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 06/04/14 14:56
 */
public class BlocksMessage extends Message {

    private List<Block> blockDataList = new ArrayList<Block>();

    public BlocksMessage(RLPList rawData) {
        super(rawData);
    }

    public void parseRLP() {

        RLPList paramsList = (RLPList) rawData.getElement(0);

        if ( Command.fromInt(((RLPItem)(paramsList).getElement(0)).getData()[0]) != BLOCKS){
            throw new Error("BlocksMessage: parsing for mal data");
        }

        for (int i = 1; i < paramsList.size(); ++i){
            RLPList rlpData = ((RLPList)paramsList.getElement(i));
            Block blockData = new Block(rlpData);
            this.blockDataList.add(blockData);
        }
        parsed = true;
    }

    @Override
    public byte[] getPayload() {
        return null;
    }

    public List<Block> getBlockDataList() {
        if (!parsed) parseRLP();
        return blockDataList;
    }

	public String toString() {

        StringBuffer sb = new StringBuffer();
        for (Block blockData : this.getBlockDataList()){
            sb.append("   ").append( blockData.toString() ).append("\n");

            List<Transaction> transactions = blockData.getTransactionsList();
            for (Transaction transactionData : transactions){
                sb.append("[").append(transactionData).append("]\n");
            }
        }

        return "Blocks Message [\n" +
                  sb.toString()
                + " ]";
    }
}