package org.ethereum.net.message;

import org.spongycastle.util.encoders.Hex;

import static org.ethereum.net.Command.HELLO;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;

import java.math.BigInteger;

/**
 * Wrapper around an Ethereum HelloMessage on the network 
 *
 * @see {@link org.ethereum.net.Command#HELLO}
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
    private byte capabilities;
    /** The port on which the peer is listening for an incoming connection */
    private short listenPort;
    /** The identity and public key of the peer */
    private byte[] peerId;

    public HelloMessage(RLPList rawData) {
        super(rawData);
    }

	public HelloMessage(byte p2pVersion, String clientId,
			byte capabilities, short listenPort, byte[] peerId) {
        this.p2pVersion = p2pVersion;
        this.clientId = clientId;
        this.capabilities = capabilities;
        this.listenPort = listenPort;
        this.peerId = peerId;
        this.parsed = true;
    }

    @Override
    public void parseRLP() {

        RLPList paramsList = (RLPList) rawData.get(0);

        // the message does no distinguish between the 0 and null so here I check command code for null
        // TODO: find out if it can be 00
		if (((RLPItem) paramsList.get(0)).getRLPData() != null)
            throw new Error("HelloMessage: parsing for mal data");
        
        this.p2pVersion			= ((RLPItem) paramsList.get(1)).getRLPData()[0];
        
        byte[] clientIdBytes	= ((RLPItem) paramsList.get(2)).getRLPData();
        this.clientId			= new String(clientIdBytes != null ? clientIdBytes : EMPTY_BYTE_ARRAY);
        
        this.capabilities		= ((RLPItem) paramsList.get(3)).getRLPData()[0];

        byte[] peerPortBytes	= ((RLPItem) paramsList.get(4)).getRLPData();
        this.listenPort         	= new BigInteger(peerPortBytes).shortValue();
        
        this.peerId           	= ((RLPItem) paramsList.get(5)).getRLPData();
        
        this.parsed = true;
        // TODO: what to do when mal data ?
    }

    public byte[] getPayload() {

        byte[] command			= RLP.encodeByte(HELLO.asByte());
        byte[] protocolVersion	= RLP.encodeByte(this.p2pVersion);
        byte[] clientId			= RLP.encodeString(this.clientId);
        byte[] capabilities		= RLP.encodeByte(this.capabilities);
        byte[] peerPort			= RLP.encodeShort(this.listenPort);
        byte[] peerId			= RLP.encodeElement(this.peerId);

		byte[] data = RLP.encodeList(command, protocolVersion, 
				clientId, capabilities, peerPort, peerId);

        return data;
    }

    public byte getCommandCode() {
        if (!parsed) parseRLP();
        return HELLO.asByte();
    }

    public byte getP2PVersion() {
        if (!parsed) parseRLP();
        return p2pVersion;
    }

    public String getClientId() {
        if (!parsed) parseRLP();
        return clientId;
    }

    public byte getCapabilities() {
        if (!parsed) parseRLP();
        return capabilities;
    }

    public short getListenPort() {
        if (!parsed) parseRLP();
        return listenPort;
    }

    public byte[] getPeerId() {
        if (!parsed) parseRLP();
        return peerId;
    }
    
	@Override
    public String getMessageName() {
        return "HelloMessage";
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public String toString() {
        if (!parsed) parseRLP();
        return "Hello Message [ command=" + HELLO.asByte() + " " +
                " p2pVersion=" + this.p2pVersion + " " +
                " clientId=" + this.clientId + " " +
                " capabilities=" + this.capabilities + " " +
                " peerPort=" + this.listenPort + " " +
                " peerId=" + Hex.toHexString(this.peerId) + " " +
                "]";
    }
}

