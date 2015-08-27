package org.ethereum.net.eth.message;

import org.ethereum.core.Block;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Wrapper around an Ethereum Blocks message on the network
 *
 * @see EthMessageCodes#BLOCKS
 */
public class BlocksMessage extends EthMessage {

    private List<Block> blocks;

    public BlocksMessage(byte[] encoded) {
        super(encoded);
    }

    public BlocksMessage(List<Block> blocks) {
        this.blocks = blocks;
        parsed = true;
    }

    private void parse() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        blocks = new ArrayList<>();
        for (int i = 0; i < paramsList.size(); ++i) {
            RLPList rlpData = ((RLPList) paramsList.get(i));
            Block blockData = new Block(rlpData.getRLPData());
            blocks.add(blockData);
        }
        parsed = true;
    }

    private void encode() {

        List<byte[]> encodedElements = new Vector<>();

        for (Block block : blocks)
            encodedElements.add(block.getEncoded());

        byte[][] encodedElementArray = encodedElements
                .toArray(new byte[encodedElements.size()][]);

        this.encoded = RLP.encodeList(encodedElementArray);
    }


    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    public List<Block> getBlocks() {
        if (!parsed) parse();
        return blocks;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.BLOCKS;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public String toString() {
        if (!parsed) parse();

        StringBuilder sb = new StringBuilder();
        for (Block blockData : this.getBlocks()) {
            sb.append("\n   ").append(blockData.toFlatString());
        }
        return "[" + getCommand().name() + " count( " + blocks.size() + " )]";
    }
}