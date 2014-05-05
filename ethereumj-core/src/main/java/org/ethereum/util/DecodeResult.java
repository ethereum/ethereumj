package org.ethereum.util;

public class DecodeResult {

	private int pos;
	private Object decoded;
		
	public DecodeResult(int pos, Object decoded) {
		this.pos = pos;
		this.decoded = decoded;
	}
	
	public int getPos() {
		return pos;
	}
	public Object getDecoded() {
		return decoded;
	}
}
