/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.net.message;

import org.ethereum.config.SystemProperties;
import org.ethereum.net.client.Capability;
import org.ethereum.net.client.ConfigCapabilities;
import org.ethereum.net.p2p.*;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains static values of messages on the network. These message
 * will always be the same and therefore don't need to be created each time.
 *
 * @author Roman Mandeleil
 * @since 13.04.14
 */
@Component
public class StaticMessages {

    @Autowired
    SystemProperties config;

    @Autowired
    ConfigCapabilities configCapabilities;

    public final static PingMessage PING_MESSAGE = new PingMessage();
    public final static PongMessage PONG_MESSAGE = new PongMessage();
    public final static GetPeersMessage GET_PEERS_MESSAGE = new GetPeersMessage();
    public final static DisconnectMessage DISCONNECT_MESSAGE = new DisconnectMessage(ReasonCode.REQUESTED);

    public static final byte[] SYNC_TOKEN = Hex.decode("22400891");

    public HelloMessage createHelloMessage(String peerId) {
        return createHelloMessage(peerId, config.listenPort());
    }
    public HelloMessage createHelloMessage(String peerId, int listenPort) {

        String helloAnnouncement = buildHelloAnnouncement();
        byte p2pVersion = (byte) config.defaultP2PVersion();
        List<Capability> capabilities = configCapabilities.getConfigCapabilities();

        return new HelloMessage(p2pVersion, helloAnnouncement,
                capabilities, listenPort, peerId);
    }

    private String buildHelloAnnouncement() {
        String version = config.projectVersion();
        String numberVersion = version;
        Pattern pattern = Pattern.compile("^\\d+(\\.\\d+)*");
        Matcher matcher = pattern.matcher(numberVersion);
        if (matcher.find()) {
            numberVersion = numberVersion.substring(matcher.start(), matcher.end());
        }
        String system = System.getProperty("os.name");
        if (system.contains(" "))
            system = system.substring(0, system.indexOf(" "));
        if (System.getProperty("java.vm.vendor").contains("Android"))
            system = "Android";
        String phrase = config.helloPhrase();

        return String.format("Ethereum(J)/v%s/%s/%s/Java/%s", numberVersion, system,
                config.projectVersionModifier().equalsIgnoreCase("release") ? "Release" : "Dev", phrase);
    }
}
