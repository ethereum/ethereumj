package org.ethereum.util;


/**
 * www.ethereumJ.com 
 * @author: Roman Mandeleil
 * Created on: 21/04/14 16:26
 */
public class RLPItem implements RLPElement {

	byte[] rlpData;
	
	public RLPItem(byte[] rlpData) {
		this.rlpData = rlpData;
	}
	
	public byte[] getRLPData() {
		if (rlpData.length == 0)
			return null;
		return rlpData;
	}
}
