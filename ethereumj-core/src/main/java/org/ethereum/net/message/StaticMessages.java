package org.ethereum.net.message;

import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.HashUtil;
import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.eth.GetTransactionsMessage;
import org.ethereum.net.p2p.*;
import org.ethereum.net.shh.ShhHandler;

import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.List;

/**
 * This class contains static values of messages on the network. These message
 * will always be the same and therefore don't need to be created each time.
 *
 * @author Roman Mandeleil
 * @since 13.04.14
 */
public class StaticMessages {

	public static final String PEER_ID = Hex.toHexString(HashUtil.randomPeerId());
	
	public final static PingMessage PING_MESSAGE 						= new PingMessage();
	public final static PongMessage PONG_MESSAGE 						= new PongMessage();
	public final static HelloMessage HELLO_MESSAGE 						= generateHelloMessage();
	public final static GetPeersMessage GET_PEERS_MESSAGE 				= new GetPeersMessage();
	public final static GetTransactionsMessage GET_TRANSACTIONS_MESSAGE = new GetTransactionsMessage();
	public final static DisconnectMessage DISCONNECT_MESSAGE = new DisconnectMessage(ReasonCode.REQUESTED);

	public static final byte[] SYNC_TOKEN = Hex.decode("22400891");

	private static HelloMessage generateHelloMessage() {
		String helloAnnouncement = buildHelloAnnouncement();
		byte p2pVersion = P2pHandler.VERSION;
		List<Capability> capabilities = Arrays.asList(
				new Capability(Capability.ETH, EthHandler.VERSION),
				new Capability(Capability.SHH, ShhHandler.VERSION));
		int listenPort = SystemProperties.CONFIG.listenPort();

		return new HelloMessage(p2pVersion, helloAnnouncement, 
				capabilities, listenPort, PEER_ID);
	}

	private static String buildHelloAnnouncement() {
		String version = SystemProperties.CONFIG.projectVersion();
		String system = System.getProperty("os.name");
		if (system.contains(" "))
			system = system.substring(0, system.indexOf(" "));
		if (System.getProperty("java.vm.vendor").contains("Android"))
			system = "Android";
		String phrase = SystemProperties.CONFIG.helloPhrase();

		return String.format("Ethereum(J)/v%s/%s/%s/Java", version, phrase, system);
	}
}
