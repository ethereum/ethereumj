package org.ethereum.net.message;

import org.ethereum.config.SystemProperties;
import org.ethereum.net.client.Capability;
import org.ethereum.net.p2p.*;
import org.spongycastle.util.encoders.Hex;

import java.util.List;

/**
 * This class contains static values of messages on the network. These message
 * will always be the same and therefore don't need to be created each time.
 *
 * @author Roman Mandeleil
 * @since 13.04.14
 */
public class StaticMessages {

    public final static PingMessage PING_MESSAGE = new PingMessage();
    public final static PongMessage PONG_MESSAGE = new PongMessage();
    public final static GetPeersMessage GET_PEERS_MESSAGE = new GetPeersMessage();
    public final static DisconnectMessage DISCONNECT_MESSAGE = new DisconnectMessage(ReasonCode.REQUESTED);

    public static final byte[] SYNC_TOKEN = Hex.decode("22400891");

    public static HelloMessage createHelloMessage(String peerId) {
        return createHelloMessage(peerId, SystemProperties.CONFIG.listenPort());
    }
    public static HelloMessage createHelloMessage(String peerId, int listenPort) {

        String helloAnnouncement = buildHelloAnnouncement();
        byte p2pVersion = P2pHandler.VERSION;
        List<Capability> capabilities = Capability.getConfigCapabilities();

        return new HelloMessage(p2pVersion, helloAnnouncement,
                capabilities, listenPort, peerId);
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
