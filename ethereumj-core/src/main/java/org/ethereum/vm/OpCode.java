package org.ethereum.vm;

import java.util.*;

/**
 * Instruction set for the Ethereum Virtual Machine
 */
public enum OpCode {

    /**
	 * Stop and Arithmetic Operations
	 */
	
	STOP(0x00),
	ADD(0x01),
	MUL(0x02),
	SUB(0x03),
	DIV(0x04),
	SDIV(0x05),
	MOD(0x06),
	SMOD(0x07),
	EXP(0x08),
	NEG(0x09),
	LT(0X0A),
    GT(0X0B),
    SLT(0X0C),
    SGT(0X0D),
	EQ(0X0E),
	NOT(0X0F),



	/**
	 * Bitwise Logic Operations
	 */
	
	AND(0x10),
	OR(0x11),
	XOR(0x12),
	BYTE(0x13),
	
	/**
	 * SHA3
	 */
	
	SHA3(0x20),
	
	/**
	 * Environmental Information
	 */

	ADDRESS(0x30),
	BALANCE(0x31),
	ORIGIN(0x32),
	CALLER(0x33),
	CALLVALUE(0x34),
	CALLDATALOAD(0x35),
	CALLDATASIZE(0x36),
	CALLDATACOPY(0x37),
	CODESIZE(0x38),
	CODECOPY(0x39), // [len code_start mem_start CODECOPY]
	GASPRICE(0x3a),
	
	/**
	 * Block Information
	 */

	PREVHASH(0x40),
	COINBASE(0x41),
	TIMESTAMP(0x42),
	NUMBER(0x43),
	DIFFICULTY(0x44),
	GASLIMIT(0x45),
	
	/**
	 * Memory, Storage and Flow Operations
	 */

	POP(0x50),
	DUP(0x51),
	SWAP(0x52),
	MLOAD(0x53),
	MSTORE(0x54),
	MSTORE8(0x55),
	SLOAD(0x56),
	SSTORE(0x57),
	JUMP(0x58),
	JUMPI(0x59),
	PC(0x5a),
	MSIZE(0x5b),
	GAS(0x5c),
	
	/**
	 * Push Operations
	 */
		
	PUSH1(0x60),
	PUSH2(0x61),
	PUSH3(0x62),
	PUSH4(0x63),
	PUSH5(0x64),
	PUSH6(0x65),
	PUSH7(0x66),
	PUSH8(0x67),
	PUSH9(0x68),
	PUSH10(0x69),
	PUSH11(0x6a),
	PUSH12(0x6b),
	PUSH13(0x6c),
	PUSH14(0x6d),
	PUSH15(0x6e),
	PUSH16(0x6f),
	PUSH17(0x70),
	PUSH18(0x71),
	PUSH19(0x72),
	PUSH20(0x73),
	PUSH21(0x74),
	PUSH22(0x75),
	PUSH23(0x76),
	PUSH24(0x77),
	PUSH25(0x78),
	PUSH26(0x79),
	PUSH27(0x7a),
	PUSH28(0x7b),
	PUSH29(0x7c),
	PUSH30(0x7d),
	PUSH31(0x7e),
	PUSH32(0x7f),
	
	/**
	 * System operations
	 */
	
	CREATE(0xf0),
	CALL(0xf1),
	RETURN(0xf2),
	SUICIDE(0xff);
	
	private byte opcode;
    
    private static final Map<Byte, OpCode> intToTypeMap = new HashMap<Byte, OpCode>();
    private static final Map<String, Byte> stringToByteMap = new HashMap<String, Byte>();


    static {
        for (OpCode type : OpCode.values()) {
            intToTypeMap.put(type.opcode, type);
            stringToByteMap.put(type.name(), type.opcode);
        }
    }
    
    private OpCode(int op) {
        this.opcode = (byte) op;
    }

    public static OpCode fromInt(int i) {
    	OpCode type = intToTypeMap.get(i);
        if (type == null)
            return OpCode.STOP;
        return type;
    }

    public byte val(){
        return opcode;
    }

    public int asInt() {
    	return opcode;
    }

    public static boolean contains(String code){

       return stringToByteMap.containsKey(code.trim());
    }

    public static byte byteVal(String code){
        return stringToByteMap.get(code);
    }

    public static OpCode code(byte op){
        return intToTypeMap.get(op);
    }
}
