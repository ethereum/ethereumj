package org.ethereum.net.rlpx;

import org.ethereum.net.client.Capability;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.util.CollectionUtils;

import java.util.List;

import static org.ethereum.net.message.StaticMessages.HELLO_MESSAGE;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
public class HandshakeHelper {

    public static List<Capability> getSupportedCapabilities(HelloMessage hello) {
        return CollectionUtils.selectList(hello.getCapabilities(), new CollectionUtils.Predicate<Capability>() {
            @Override
            public boolean evaluate(Capability cap) {
                return HELLO_MESSAGE.getCapabilities().contains(cap);
            }
        });
    }

}
