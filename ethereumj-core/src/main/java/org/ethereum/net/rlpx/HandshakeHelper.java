package org.ethereum.net.rlpx;

import org.ethereum.net.client.Capability;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.util.CollectionUtils;
import org.ethereum.util.Functional;

import java.util.List;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
public class HandshakeHelper {

    public static List<Capability> getSupportedCapabilities(HelloMessage hello) {
        final List<Capability> configCaps = Capability.getConfigCapabilities();
        return CollectionUtils.selectList(hello.getCapabilities(), new Functional.Predicate<Capability>() {
            @Override
            public boolean test(Capability cap) {
                return configCaps.contains(cap);
            }
        });
    }

}
