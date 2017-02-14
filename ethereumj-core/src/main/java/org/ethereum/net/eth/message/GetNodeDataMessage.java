package org.ethereum.net.eth.message;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.ethereum.util.Utils;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around an Ethereum GetNodeData message on the network
 * Could contain:
 * - state roots
 * - accounts state roots
 * - accounts code hashes
 *
 * @see EthMessageCodes#GET_NODE_DATA
 */
public class GetNodeDataMessage extends EthMessage {

    /**
     * List of node hashes for which is state requested
     */
    private List<byte[]> nodeKeys;

    public GetNodeDataMessage(byte[] encoded) {
        super(encoded);
    }

    public GetNodeDataMessage(List<byte[]> nodeKeys) {
        this.nodeKeys = nodeKeys;
        this.parsed = true;
    }

    private synchronized void parse() {
        if (parsed) return;
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        this.nodeKeys = new ArrayList<>();
        for (int i = 0; i < paramsList.size(); ++i) {
            nodeKeys.add(paramsList.get(i).getRLPData());
        }

        this.parsed = true;
    }

    private void encode() {
        List<byte[]> encodedElements = new ArrayList<>();
        for (byte[] hash : nodeKeys)
            encodedElements.add(RLP.encodeElement(hash));
        byte[][] encodedElementArray = encodedElements.toArray(new byte[encodedElements.size()][]);

        this.encoded = RLP.encodeList(encodedElementArray);
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }


    @Override
    public Class<NodeDataMessage> getAnswerMessage() {
        return NodeDataMessage.class;
    }

    public List<byte[]> getNodeKeys() {
        parse();
        return nodeKeys;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.GET_NODE_DATA;
    }

    public String toString() {
        parse();

        StringBuilder payload = new StringBuilder();

        payload.append("count( ").append(nodeKeys.size()).append(" ) ");

        if (logger.isDebugEnabled()) {
            for (byte[] hash : nodeKeys) {
                payload.append(Hex.toHexString(hash).substring(0, 6)).append(" | ");
            }
            if (!nodeKeys.isEmpty()) {
                payload.delete(payload.length() - 3, payload.length());
            }
        } else {
            payload.append(Utils.getHashListShort(nodeKeys));
        }

        return "[" + getCommand().name() + " " + payload + "]";
    }
}
