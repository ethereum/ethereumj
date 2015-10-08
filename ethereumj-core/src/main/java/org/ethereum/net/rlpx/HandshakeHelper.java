package org.ethereum.net.rlpx;

import org.ethereum.net.client.Capability;
import org.ethereum.net.p2p.HelloMessage;

import java.util.ArrayList;
import java.util.List;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
public class HandshakeHelper {

    public static List<Capability> getSupportedCapabilities(HelloMessage hello) {
        List<Capability> configCaps = Capability.getConfigCapabilities();
        List<Capability> supported = new ArrayList<>();

        List<Capability> eths = new ArrayList<>();

        for (Capability cap : hello.getCapabilities()) {
            if (configCaps.contains(cap)) {
                if (cap.isEth()) {
                    eths.add(cap);
                } else {
                    supported.add(cap);
                }
            }
        }

        if (eths.isEmpty()) {
            return supported;
        }

        // we need to pick up
        // the most recent Eth version
        Capability highest = null;
        for (Capability eth : eths) {
            if (highest == null || highest.getVersion() < eth.getVersion()) {
                highest = eth;
            }
        }

        supported.add(highest);
        return supported;
    }

}
