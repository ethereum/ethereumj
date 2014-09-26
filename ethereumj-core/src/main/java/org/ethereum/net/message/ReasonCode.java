package org.ethereum.net.message;

import java.util.HashMap;
import java.util.Map;

/**
 * Reason is an optional integer specifying one 
 * of a number of reasons for disconnect 
 */
public enum ReasonCode {

    REQUESTED(0x00),
    TCP_ERROR(0x01),
    BAD_PROTOCOL(0x02),
    USELESS_PEER(0x03),
    TOO_MANY_PEERS(0x04),
    ALREADY_CONNECTED(0x05),
    WRONG_GENESIS(0x06),
    INCOMPATIBLE_PROTOCOL(0x07),
    PEER_QUITING(0x08),
	UNKNOWN(0xFF);

    private int reason;
    
    private static final Map<Integer, ReasonCode> intToTypeMap = new HashMap<>();
    static {
        for (ReasonCode type : ReasonCode.values()) {
            intToTypeMap.put(type.reason, type);
        }
    }
    
    private ReasonCode(int reason) {
        this.reason = reason;
    }
        
    public static ReasonCode fromInt(int i) {
    	ReasonCode type = intToTypeMap.get(Integer.valueOf(i));
        if (type == null) 
            return ReasonCode.UNKNOWN;
        return type;
    }
    
    public byte asByte() {
    	return (byte) reason;
    }
}
