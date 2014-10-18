package org.ethereum.net.client;

/**
 * The protocols and versions of those protocols that this peer support
 */
public class Capability {
	
	public final static String ETH = "eth";
	public final static String SHH = "shh";
	
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
	
	public boolean equals(Object o) {
		if(o instanceof Capability) {
			Capability cap = (Capability) o;
			if (cap.getName() == null)
				return this.name == null;
			else
				return cap.getName().equals(this.name) && cap.getVersion() == this.version;
		}
		return false;
	}
	
	public String toString() {
		return name + ":" + version;
	}
}