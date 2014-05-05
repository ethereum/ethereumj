package org.ethereum.net;

import java.util.HashMap;
import java.util.Map;

public enum ReasonCode {

    REASON_DISCONNECT_REQUESTED(0x00),
    REASON_TCP_ERROR(0x01),
    REASON_BAD_PROTOCOL(0x02),
    REASON_USELESS_PEER(0x03),
    REASON_TOO_MANY_PEERS(0x04),
    REASON_ALREADY_CONNECTED(0x05),
    REASON_WRONG_GENESIS(0x06),
    REASON_INCOMPATIBLE_PROTOCOL(0x07),
    REASON_PEER_QUITING(0x08),
	UNKNOWN(0xFF);

    private int reason;
    
    private static final Map<Integer, ReasonCode> intToTypeMap = new HashMap<Integer, ReasonCode>();
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
