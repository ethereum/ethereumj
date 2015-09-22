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

        // we need to pick up Eth version used by sync
        // or the most recent one
        // if sync version isn't supported by remote peer
        Capability highestEth = null;
        for (Capability eth : eths) {

            if (eth.getVersion() == CONFIG.syncVersion()) {
                supported.add(eth);
                return supported;
            }

            if (highestEth == null || highestEth.getVersion() < eth.getVersion()) {
                highestEth = eth;
            }
        }

        supported.add(highestEth);
        return supported;
    }

}
