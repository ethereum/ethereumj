package org.ethereum.net.client;

import org.ethereum.config.SystemProperties;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.net.swarm.bzz.BzzHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The protocols and versions of those protocols that this peer support
 */
public class Capability implements Comparable<Capability> {

    public final static String P2P = "p2p";
    public final static String ETH = "eth";
    public final static String SHH = "shh";
    public final static String BZZ = "bzz";

    private static SortedMap<String, Capability> AllCaps = new TreeMap<>();

    static {
        for (EthVersion v : EthVersion.values()) {
            AllCaps.put(ETH, new Capability(Capability.ETH, v.getCode()));
        }

        AllCaps.put(SHH, new Capability(Capability.SHH, ShhHandler.VERSION));
        AllCaps.put(BZZ, new Capability(Capability.BZZ, BzzHandler.VERSION));
    }

    /**
     * Gets the capabilities listed in 'peer.capabilities' config property
     * sorted by their names.
     */
    public static List<Capability> getConfigCapabilities() {
        List<Capability> ret = new ArrayList<>();
        List<String> caps = SystemProperties.CONFIG.peerCapabilities();
        for (Capability capability : AllCaps.values()) {
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
        return this.name.compareTo(o.name);
    }

    public String toString() {
        return name + ":" + version;
    }
}