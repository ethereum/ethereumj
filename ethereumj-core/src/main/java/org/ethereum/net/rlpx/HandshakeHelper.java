package org.ethereum.net.rlpx;

import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.util.CollectionUtils;
import org.ethereum.util.Functional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
public class HandshakeHelper {

    public static List<Capability> getSupportedCapabilities(HelloMessage hello) {
        List<Capability> configCaps = Capability.getConfigCapabilities();
        List<Capability> supported = new ArrayList<>();

        Capability eth = null;

        for (Capability cap : hello.getCapabilities()) {
            if (configCaps.contains(cap)) {

                if (cap.isEth()) {

                    // we need to pick up the most recent Eth version from the list of supported ones
                    if (EthVersion.isSupported(cap.getVersion())) {
                        if (eth == null || eth.getVersion() < cap.getVersion()) {
                            eth = cap;
                        }
                    }

                } else {
                    supported.add(cap);
                }
            }
        }

        if (eth != null) {
            supported.add(eth);
        }

        return supported;
    }

}
