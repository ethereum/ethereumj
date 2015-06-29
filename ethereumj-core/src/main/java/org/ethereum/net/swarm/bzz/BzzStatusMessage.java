package org.ethereum.net.swarm.bzz;

import org.ethereum.net.client.Capability;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BzzStatusMessage extends BzzMessage {

    private long version;
    private String id;
    private byte[] nodeId;
    private PeerAddress addr;
    private long networkId;
    private List<Capability> capabilities;

    public BzzStatusMessage(byte[] encoded) {
        super(encoded);
    }

    public BzzStatusMessage(int version, String id, byte[] nodeId, PeerAddress addr, long networkId, List<Capability> capabilities) {
        this.version = version;
        this.id = id;
        this.nodeId = nodeId;
        this.addr = addr;
        this.networkId = networkId;
        this.capabilities = capabilities;
        parsed = true;
    }

    @Override
    protected void decode() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        version = ByteUtil.byteArrayToLong(paramsList.get(0).getRLPData());
        id = new String(paramsList.get(1).getRLPData());
        nodeId = paramsList.get(2).getRLPData();
        addr = PeerAddress.parse((RLPList) paramsList.get(3));
        networkId = ByteUtil.byteArrayToLong(paramsList.get(4).getRLPData());

        capabilities = new ArrayList<>();
        RLPList caps = (RLPList) paramsList.get(5);
        for (RLPElement c : caps) {
            RLPList e = (RLPList) c;
            capabilities.add(new Capability(new String(e.get(0).getRLPData()),e.get(1).getRLPData()[0]));
        }

        parsed = true;
    }

    private void encode() {
        byte[] bVersion = RLP.encodeElement(ByteUtil.longToBytes(version));
        byte[] bId = RLP.encodeString(id);
        byte[] bAddr = addr.encodeRlp();
        byte[] bNetId = RLP.encodeElement(ByteUtil.longToBytes(networkId));
        byte[][] capabilities = new byte[this.capabilities.size()][];
        for (int i = 0; i < this.capabilities.size(); i++) {
            Capability capability = this.capabilities.get(i);
            capabilities[i] = RLP.encodeList(
                    RLP.encodeElement(capability.getName().getBytes()),
                    RLP.encodeByte(capability.getVersion()));
        }
        byte[] bCapabilityList = RLP.encodeList(capabilities);

        this.encoded = RLP.encodeList( bVersion, bId, RLP.encodeElement(nodeId), bAddr, bNetId, bCapabilityList);
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    public long getVersion() {
        return version;
    }

    public String getId() {
        return id;
    }

    public byte[] getNodeId() {
        return nodeId;
    }

    public PeerAddress getAddr() {
        return addr;
    }

    public long getNetworkId() {
        return networkId;
    }

    public List<Capability> getCapabilities() {
        return capabilities;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Override
    public BzzMessageCodes getCommand() {
        return BzzMessageCodes.STATUS;
    }


    @Override
    public String toString() {
        return "BzzStatusMessage{" +
                "version=" + version +
                ", id='" + id + '\'' +
                ", nodeId=" + Arrays.toString(nodeId) +
                ", addr=" + addr +
                ", networkId=" + networkId +
                ", capabilities=" + capabilities +
                '}';
    }
}
