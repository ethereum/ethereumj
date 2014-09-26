package org.ethereum.net.message;

import static org.ethereum.net.Command.GET_BLOCKS;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.ethereum.net.Command;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

public class GetBlocksMessage extends Message {

    private List<byte[]> blockHashList = new ArrayList<>();
    private BigInteger blockNum;
    
	public GetBlocksMessage(byte[] encoded) {
		super(encoded);
	}

	// TODO: it get's byte for now. change it to int
    public GetBlocksMessage(byte number , byte[]... blockHashList) {

        byte[][] encodedElements = new byte[blockHashList.length + 2][];

        encodedElements[0] = new byte[]{GET_BLOCKS.asByte()};
        int i = 1;
        for (byte[] hash : blockHashList) {
            this.blockHashList.add(hash);
            byte[] element = RLP.encodeElement(hash);
            encodedElements[i] = element;
            ++i;
        }
        encodedElements[i] = RLP.encodeByte(number);

        this.encoded = RLP.encodeList(encodedElements);
        this.parsed = true;
    }
    
	public void parse() {
		RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        if ( (((RLPItem)(paramsList).get(0)).getRLPData()[0] & 0xFF) != GET_BLOCKS.asByte())
            throw new RuntimeException("Not a GetBlockssMessage command");
        
        int size = paramsList.size();
        for (int i = 1; i < size - 1; ++i) {
            blockHashList.add(((RLPItem) paramsList.get(i)).getRLPData());
        }

        // the last element is the num of requested blocks
		byte[] blockNumB = ((RLPItem) paramsList.get(size - 1)).getRLPData();
		this.blockNum = new BigInteger(blockNumB);

        this.parsed = true;
	}

	@Override
	public byte[] getEncoded() {
		return encoded;
	}

	@Override
	public Command getCommand() {
		return GET_BLOCKS;
	}

	@Override
	public Class<BlocksMessage> getAnswerMessage() {
		return BlocksMessage.class;
	}
	
    public String toString() {
        if (!parsed) parse();

        StringBuffer sb = new StringBuffer();
        for (byte[] blockHash : blockHashList) {
            sb.append("").append(Hex.toHexString(blockHash)).append("\n ");
        }
        sb.append(" blockNum=").append(blockNum);
        return "[command=" + this.getCommand().name() + " " + sb.toString() + " ]";
    }
}
