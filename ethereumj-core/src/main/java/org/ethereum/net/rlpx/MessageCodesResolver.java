package org.ethereum.net.rlpx;

import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.message.EthMessageCodes;
import org.ethereum.net.p2p.P2pMessageCodes;
import org.ethereum.net.shh.ShhMessageCodes;
import org.ethereum.net.swarm.bzz.BzzMessageCodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
public class MessageCodesResolver {

    private Map<String, Integer> offsets = new HashMap<>();

    public MessageCodesResolver() {
    }

    public MessageCodesResolver(List<Capability> caps) {
        init(caps);
    }

    public void init(List<Capability> caps) {
        Collections.sort(caps);
        int offset = P2pMessageCodes.USER.asByte() + 1;

        for (Capability capability : caps) {
            if (capability.getName().equals(Capability.ETH)) {
                setEthOffset(offset);
                offset += EthMessageCodes.values().length;
            }

            if (capability.getName().equals(Capability.SHH)) {
                setShhOffset(offset);
                offset += ShhMessageCodes.values().length;
            }

            if (capability.getName().equals(Capability.BZZ)) {
                setBzzOffset(offset);
                offset += BzzMessageCodes.values().length + 4;
                // FIXME: for some reason Go left 4 codes between BZZ and ETH message codes
            }
        }
    }

    public byte withP2pOffset(byte code) {
        return withOffset(code, Capability.P2P);
    }

    public byte withBzzOffset(byte code) {
        return withOffset(code, Capability.BZZ);
    }

    public byte withEthOffset(byte code) {
        return withOffset(code, Capability.ETH);
    }

    public byte withShhOffset(byte code) {
        return withOffset(code, Capability.SHH);
    }

    public byte withOffset(byte code, String cap) {
        byte offset = getOffset(cap);
        return (byte)(code + offset);
    }

    public byte resolveP2p(byte code) {
        return resolve(code, Capability.P2P);
    }

    public byte resolveBzz(byte code) {
        return resolve(code, Capability.BZZ);
    }

    public byte resolveEth(byte code) {
        return resolve(code, Capability.ETH);
    }

    public byte resolveShh(byte code) {
        return resolve(code, Capability.SHH);
    }

    private byte resolve(byte code, String cap) {
        byte offset = getOffset(cap);
        return (byte)(code - offset);
    }

    private byte getOffset(String cap) {
        Integer offset = offsets.get(cap);
        return offset == null ? 0 : offset.byteValue();
    }

    public void setBzzOffset(int offset) {
        setOffset(Capability.BZZ, offset);
    }

    public void setEthOffset(int offset) {
        setOffset(Capability.ETH, offset);
    }

    public void setShhOffset(int offset) {
        setOffset(Capability.SHH, offset);
    }

    private void setOffset(String cap, int offset) {
        offsets.put(cap, offset);
    }
}
