package org.ethereum.net.client;

/**
 * The protocols and versions of those protocols that this peer support
 */
public class Capability implements Comparable<Capability> {

    public final static String P2P = "p2p";
    public final static String ETH = "eth";
    public final static String PAR = "par";
    public final static String SHH = "shh";
    public final static String BZZ = "bzz";

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

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (int) version;
        return result;
    }

    public String toString() {
        return name + ":" + version;
    }
}