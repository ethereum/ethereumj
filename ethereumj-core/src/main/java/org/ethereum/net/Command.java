package org.ethereum.net;

import java.util.HashMap;
import java.util.Map;

public enum Command {

	HELLO(0x00),
	DISCONNECT(0x01),
	PING(0x02),
	PONG(0x03),
	GET_PEERS(0x10),
	PEERS(0x11),
	TRANSACTIONS(0x12),
	BLOCKS(0x13),
	GET_CHAIN(0x14),
	NOT_IN_CHAIN(0x15),
	GET_TRANSACTIONS(0x16),
	UNKNOWN(0xFF);

    private int cmd;
    
    private static final Map<Integer, Command> intToTypeMap = new HashMap<Integer, Command>();
    static {
        for (Command type : Command.values()) {
            intToTypeMap.put(type.cmd, type);
        }
    }
    
    private Command(int cmd) {
        this.cmd = cmd;
    }

    public static Command fromInt(int i) {
    	Command type = intToTypeMap.get(Integer.valueOf(i));
        if (type == null) 
            return Command.UNKNOWN;
        return type;
    }
    
    public byte asByte() {
    	return (byte) cmd;
    }
}
