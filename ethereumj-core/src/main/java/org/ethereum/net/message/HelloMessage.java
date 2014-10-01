package org.ethereum.net.message;

import static org.ethereum.net.message.Command.HELLO;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;

import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around an Ethereum HelloMessage on the network 
 *
 * @see {@link org.ethereum.net.message.Command#HELLO}
 * 
 * @author Roman Mandeleil
 */
public class HelloMessage extends Message {

	/** The implemented version of the P2P protocol. */
    private byte p2pVersion;
    /** The underlying client. A user-readable string. */
    private String clientId;
    /** A peer-network capability code, readable ASCII and 3 letters. 
     * Currently only "eth" and "shh" are known.  */
    private List<String> capabilities;
    /** The port on which the peer is listening for an incoming connection */
    private int listenPort;
    /** The identity and public key of the peer */
    private byte[] peerId;

    public HelloMessage(byte[] encoded) {
        super(encoded);
    }

	public HelloMessage(byte p2pVersion, String clientId,
			List<String> capabilities, int listenPort, byte[] peerId) {
        this.p2pVersion = p2pVersion;
        this.clientId = clientId;
        this.capabilities = capabilities;
        this.listenPort = listenPort;
        this.peerId = peerId;
        this.parsed = true;
    }

    private void parse() {

		RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);
      
        // TODO: find out if it can be 00. Do we need to check for this?
        // The message does not distinguish between 0 and null, 
        // so we check command code for null.
		if (((RLPItem)paramsList.get(0)).getRLPData() != null)
            throw new Error("HelloMessage: parsing for mal data"); 
        
        byte[] p2pVersionBytes	= ((RLPItem) paramsList.get(1)).getRLPData();
        this.p2pVersion			= p2pVersionBytes != null ? p2pVersionBytes[0] : 0;
        
        byte[] clientIdBytes	= ((RLPItem) paramsList.get(2)).getRLPData();
        this.clientId			= new String(clientIdBytes != null ? clientIdBytes : EMPTY_BYTE_ARRAY);
        
        RLPList capabilityList 	= (RLPList) paramsList.get(3);
        this.capabilities = new ArrayList<>();
        for (int i = 0; i < capabilityList.size(); i++) {
        	this.capabilities.add(new String(capabilityList.get(i).getRLPData()));
        }
        
        byte[] peerPortBytes	= ((RLPItem) paramsList.get(4)).getRLPData();
        this.listenPort         = ByteUtil.byteArrayToInt(peerPortBytes);
        
        this.peerId           	= ((RLPItem) paramsList.get(5)).getRLPData();
        
        this.parsed = true;
    }

    private void encode() {
        byte[] command			= RLP.encodeByte(HELLO.asByte());
        byte[] p2pVersion		= RLP.encodeByte(this.p2pVersion);
        byte[] clientId			= RLP.encodeString(this.clientId);
        byte[][] capabilities 	= new byte[this.capabilities.size()][];
        for (int i = 0; i < this.capabilities.size(); i++) {
			capabilities[i] = RLP.encode(this.capabilities.get(i).getBytes());
		}
        byte[] capabilityList	= RLP.encodeList(capabilities);
        byte[] peerPort			= RLP.encodeInt(this.listenPort);
        byte[] peerId			= RLP.encodeElement(this.peerId);

		this.encoded = RLP.encodeList(command, p2pVersion, 
				clientId, capabilityList, peerPort, peerId);
	}
    
    @Override
    public byte[] getEncoded() {
    	if (encoded == null) this.encode();
        return encoded;
    }
    
	@Override
	public Command getCommand() {
		return HELLO;
	}
    
    public byte getP2PVersion() {
        if (!parsed) parse();
        return p2pVersion;
    }

    public String getClientId() {
        if (!parsed) parse();
        return clientId;
    }

    public List<String> getCapabilities() {
        if (!parsed) parse();
        return capabilities;
    }

    public int getListenPort() {
        if (!parsed) parse();
        return listenPort;
    }

    public byte[] getPeerId() {
        if (!parsed) parse();
        return peerId;
    }
    
    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public String toString() {
        if (!parsed) parse();
        return "[" + this.getCommand().name() +
        		" p2pVersion=" + this.p2pVersion +
                " clientId=" + this.clientId +
                " capabilities=[" + Joiner.on(" ").join(this.capabilities) + "]" +
                " peerPort=" + this.listenPort +
                " peerId=" + Hex.toHexString(this.peerId) +
                "]";
    }
}