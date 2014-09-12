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
	LT(0X0a),
    GT(0X0b),
    SLT(0X0c),
    SGT(0X0d),
	EQ(0X0e),
	NOT(0X0f),

	/**
	 * Bitwise Logic Operations
	 */
	AND(0x10),
	OR(0x11),
	XOR(0x12),
	BYTE(0x13),
	ADDMOD(0x14),
	MULMOD(0x15),
	
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
	EXTCODECOPY(0x3b),
	EXTCODESIZE(0x3c),
	
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
	 * Duplicate Nth item from the stack
	 */
	DUP1(0x80),
	DUP2(0x81),
	DUP3(0x82),
	DUP4(0x83),
	DUP5(0x84),
	DUP6(0x85),
	DUP7(0x86),
	DUP8(0x87),
	DUP9(0x88),
	DUP10(0x89),
	DUP11(0x8a),
	DUP12(0x8b),
	DUP13(0x8c),
	DUP14(0x8d),
	DUP15(0x8e),
	DUP16(0x8f),
	
	/**
	 * Swap the Nth item from the stack with the top
	 */
	SWAP1(0x90),
	SWAP2(0x91),
	SWAP3(0x92),
	SWAP4(0x93),
	SWAP5(0x94),
	SWAP6(0x95),
	SWAP7(0x96),
	SWAP8(0x97),
	SWAP9(0x98),
	SWAP10(0x99),
	SWAP11(0x9a),
	SWAP12(0x9b),
	SWAP13(0x9c),
	SWAP14(0x9d),
	SWAP15(0x9e),
	SWAP16(0x9f),
	
	/**
	 * System operations
	 */
	CREATE(0xf0),   //       [in_size] [in_offs] [gas_val] CREATE
	CALL(0xf1),     //       [out_data_size] [out_data_start] [in_data_size] [in_data_start] [value] [to_addr] [gas] CALL
	RETURN(0xf2),
	POST(0xf3),
	CALLSTATELESS(0xf4),
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

    public byte val() {
        return opcode;
    }

    public int asInt() {
    	return opcode;
    }

    public static boolean contains(String code) {
       return stringToByteMap.containsKey(code.trim());
    }

    public static byte byteVal(String code) {
        return stringToByteMap.get(code);
    }

    public static OpCode code(byte op) {
        return intToTypeMap.get(op);
    }
}
