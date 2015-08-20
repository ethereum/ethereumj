package org.ethereum.net.client;

import org.ethereum.config.SystemProperties;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.net.swarm.bzz.BzzHandler;

import java.util.*;

/**
 * The protocols and versions of those protocols that this peer support
 */
public class Capability implements Comparable<Capability> {

    public final static String P2P = "p2p";
    public final static String ETH = "eth";
    public final static String SHH = "shh";
    public final static String BZZ = "bzz";

    private static SortedSet<Capability> AllCaps = new TreeSet<>();

    static {
        for (EthVersion v : EthVersion.supported()) {
            AllCaps.add(new Capability(Capability.ETH, v.getCode()));
        }

        AllCaps.add(new Capability(Capability.SHH, ShhHandler.VERSION));
        AllCaps.add(new Capability(Capability.BZZ, BzzHandler.VERSION));
    }

    /**
     * Gets the capabilities listed in 'peer.capabilities' config property
     * sorted by their names.
     */
    public static List<Capability> getConfigCapabilities() {
        List<Capability> ret = new ArrayList<>();
        List<String> caps = SystemProperties.CONFIG.peerCapabilities();
        for (Capability capability : AllCaps) {
            if (caps.contains(capability.getName())) {
                ret.add(capability);
            }
        }
        return ret;
    }

    private String name;
    private byte version;

    public Capability(String name, byte version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public byte getVersion() {
        return version;
    }

    public boolean isEth() {
        return ETH.equals(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Capability)) return false;

        Capability other = (Capability) obj;
        if (this.name == null)
            return other.name == null;
        else
            return this.name.equals(other.name) && this.version == other.version;
    }

    @Override
    public int compareTo(Capability o) {
        int cmp = this.name.compareTo(o.name);
        if (cmp != 0) {
            return cmp;
        } else {
            return Byte.valueOf(this.version).compareTo(o.version);
        }
    }

    public String toString() {
        return name + ":" + version;
    }
}