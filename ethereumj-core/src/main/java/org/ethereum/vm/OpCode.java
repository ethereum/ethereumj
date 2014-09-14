package org.ethereum.vm;

import java.util.HashMap;
import java.util.Map;

/**
 * Instruction set for the Ethereum Virtual Machine
 * See Yellow Paper: http://www.gavwood.com/Paper.pdf 
 * - Appendix G. Virtual Machine Specification
 */
public enum OpCode {

    /** Halts execution (0x00) 					*/
	STOP(0x00),
	
	/*	Arithmetic Operations	*/
	
	/** (0x01) Addition operation				*/
	ADD(0x01),
	/** (0x02) Multiplication operation			*/
	MUL(0x02),
	/** (0x03) Subtraction operations			*/
	SUB(0x03),
	/** (0x04) Integer division operation		*/
	DIV(0x04),
	/** (0x05) Signed integer division operation*/
	SDIV(0x05),
	/** (0x06) Modulo remainder operation		*/
	MOD(0x06),
	/** (0x07) Signed modulo remainder operation*/
	SMOD(0x07),
	/** (0x08) Exponential operation			*/
	EXP(0x08),
	/** (0x09) Negation operation				*/
	NEG(0x09),
	/** (0x0a) Less-than comparison				*/
	LT(0X0a),
	/** (0x0b) Greater-than comparison			*/
    GT(0X0b),
    /** (0x0c) Signed less-than comparison		*/
    SLT(0X0c),
    /** (0x0d) Signed greater-than comparison	*/
    SGT(0X0d),
    /** (0x0e) Equality comparison				*/
	EQ(0X0e),
	/** (0x0f) Simple not operator				*/
	NOT(0X0f),

	/*	Bitwise Logic Operations	*/
	
	/** (0x10) Bitwise AND operation 			*/
	AND(0x10),
	/** (0x11) Bitwise OR operation 			*/
	OR(0x11),
	/** (0x12) Bitwise XOR operation			*/
	XOR(0x12),
	/** (0x13) Retrieve single byte from word 	*/
	BYTE(0x13),
	/** (0x14) Addition combined with modulo 
	 * remainder operation */
	ADDMOD(0x14),
	/** (0x15) Multiplication combined with modulo 
	 * remainder operation */
	MULMOD(0x15),
	
	/*	Cryptographic Operations	*/
	
	/** (0x20) Compute SHA3-256 hash			*/
	SHA3(0x20),
	
	/*	Environmental Information	*/
	
	/** (0x30)  Get address of currently 
	 * executing account 						*/
	ADDRESS(0x30),
	/** (0x31) Get balance of the given account	*/
	BALANCE(0x31),
	/** (0x32) Get execution origination address*/
	ORIGIN(0x32),
	/** (0x33) Get caller address				*/
	CALLER(0x33),
	/** (0x34) Get deposited value by the 
	 * instruction/transaction responsible 
	 * for this execution						*/
	CALLVALUE(0x34),
	/** (0x35) Get input data of current 
	 * environment								*/
	CALLDATALOAD(0x35),
	/** (0x36) Get size of input data in current
	 * environment								*/
	CALLDATASIZE(0x36),
	/** (0x37) Copy input data in current 
	 * environment to memory					*/
	CALLDATACOPY(0x37),
	/** (0x38) Get size of code running in 
	 * current environment						*/
	CODESIZE(0x38),
	/** (0x39) Copy code running in current 
	 * environment to memory					*/
	CODECOPY(0x39), // [len code_start mem_start CODECOPY]
	/** (0x3a) Get price of gas in current 
	 * environment								*/
	GASPRICE(0x3a),
	/** (0x3b) Copy code running in current 
	 * environment to memory with given offset	*/
	EXTCODECOPY(0x3b),
	/** (0x3c) Get size of code running in 
	 * current environment with given offset	*/
	EXTCODESIZE(0x3c),
	
	/*	Block Information	*/
	
	/** (0x40) Get hash of most recent 
	 * complete block							*/
	PREVHASH(0x40),
	/** (0x41) Get the block’s coinbase address	*/
	COINBASE(0x41),
	/** (x042) Get the block’s timestamp		*/
	TIMESTAMP(0x42),
	/** (0x43) Get the block’s number			*/
	NUMBER(0x43),
	/** (0x44) Get the block’s difficulty		*/
	DIFFICULTY(0x44),
	/** (0x45) Get the block’s gas limit		*/
	GASLIMIT(0x45),
	
	/*	Memory, Storage and Flow Operations	*/
	
	/** (0x50) Remove item from stack			*/
	POP(0x50),
	/** (0x53) Load word from memory			*/
	MLOAD(0x53),
	/** (0x54) Save word to memory				*/
	MSTORE(0x54),
	/** (0x55) Save byte to memory				*/
	MSTORE8(0x55),
	/** (0x56) Load word from storage			*/
	SLOAD(0x56),
	/** (0x57) Save word to storage				*/
	SSTORE(0x57),
	/** (0x58) Alter the program counter		*/
	JUMP(0x58),
	/** (0x59) Conditionally alter the program 
	 * counter									*/
	JUMPI(0x59),
	/** (0x5a) Get the program counter			*/
	PC(0x5a),
	/** (0x5b) Get the size of active memory	*/
	MSIZE(0x5b),
	/** (0x5c) Get the amount of available gas	*/
	GAS(0x5c),
	
	/*	Push Operations	*/
	
	/** (0x60) Place 1-byte item on stack		*/
	PUSH1(0x60),
	/** (0x61) Place 2-byte item on stack		*/
	PUSH2(0x61),
	/** (0x62) Place 3-byte item on stack		*/
	PUSH3(0x62),
	/** (0x63) Place 4-byte item on stack		*/
	PUSH4(0x63),
	/** (0x64) Place 5-byte item on stack		*/
	PUSH5(0x64),
	/** (0x65) Place 6-byte item on stack		*/
	PUSH6(0x65),
	/** (0x66) Place 7-byte item on stack		*/
	PUSH7(0x66),
	/** (0x67) Place 8-byte item on stack		*/
	PUSH8(0x67),
	/** (0x68) Place 9-byte item on stack		*/
	PUSH9(0x68),
	/** (0x69) Place 10-byte item on stack		*/
	PUSH10(0x69),
	/** (0x6a) Place 11-byte item on stack		*/
	PUSH11(0x6a),
	/** (0x6b) Place 12-byte item on stack		*/
	PUSH12(0x6b),
	/** (0x6c) Place 13-byte item on stack		*/
	PUSH13(0x6c),
	/** (0x6d) Place 14-byte item on stack		*/
	PUSH14(0x6d),
	/** (0x6e) Place 15-byte item on stack		*/
	PUSH15(0x6e),
	/** (0x6f) Place 16-byte item on stack		*/
	PUSH16(0x6f),
	/** (0x70) Place 17-byte item on stack		*/
	PUSH17(0x70),
	/** (0x71) Place 18-byte item on stack		*/
	PUSH18(0x71),
	/** (0x72) Place 19-byte item on stack		*/
	PUSH19(0x72),
	/** (0x73) Place 20-byte item on stack		*/
	PUSH20(0x73),
	/** (0x74) Place 21-byte item on stack		*/
	PUSH21(0x74),
	/** (0x75) Place 22-byte item on stack		*/
	PUSH22(0x75),
	/** (0x76) Place 23-byte item on stack		*/
	PUSH23(0x76),
	/** (0x77) Place 24-byte item on stack		*/
	PUSH24(0x77),
	/** (0x78) Place 25-byte item on stack		*/
	PUSH25(0x78),
	/** (0x79) Place 26-byte item on stack		*/
	PUSH26(0x79),
	/** (0x7a) Place 27-byte item on stack		*/
	PUSH27(0x7a),
	/** (0x7b) Place 28-byte item on stack		*/
	PUSH28(0x7b),
	/** (0x7c) Place 29-byte item on stack		*/
	PUSH29(0x7c),
	/** (0x7d) Place 30-byte item on stack		*/
	PUSH30(0x7d),
	/** (0x7e) Place 31-byte item on stack		*/
	PUSH31(0x7e),
	/** (0x7f) Place 32-byte (full word) 
	 * item on stack							*/
	PUSH32(0x7f),
	
	/*	Duplicate Nth item from the stack	*/
	
	/** (0x80) Duplicate 1st item on stack		*/
	DUP1(0x80),
	/** (0x81) Duplicate 2nd item on stack		*/
	DUP2(0x81),
	/** (0x82) Duplicate 3rd item on stack		*/
	DUP3(0x82),
	/** (0x83) Duplicate 4th item on stack		*/
	DUP4(0x83),
	/** (0x84) Duplicate 5th item on stack		*/
	DUP5(0x84),
	/** (0x85) Duplicate 6th item on stack		*/
	DUP6(0x85),
	/** (0x86) Duplicate 7th item on stack		*/
	DUP7(0x86),
	/** (0x87) Duplicate 8th item on stack		*/
	DUP8(0x87),
	/** (0x88) Duplicate 9th item on stack		*/
	DUP9(0x88),
	/** (0x89) Duplicate 10th item on stack		*/
	DUP10(0x89),
	/** (0x8a) Duplicate 11th item on stack		*/
	DUP11(0x8a),
	/** (0x8b) Duplicate 12th item on stack		*/
	DUP12(0x8b),
	/** (0x8c) Duplicate 13th item on stack		*/
	DUP13(0x8c),
	/** (0x8d) Duplicate 14th item on stack		*/
	DUP14(0x8d),
	/** (0x8e) Duplicate 15th item on stack		*/
	DUP15(0x8e),
	/** (0x8f) Duplicate 16th item on stack		*/
	DUP16(0x8f),
	
	/*	Swap the Nth item from the stack with the top	*/
	
	/** (0x90) Exchange 2nd item from stack with the top */
	SWAP1(0x90),
	/** (0x91) Exchange 3rd item from stack with the top */
	SWAP2(0x91),
	/** (0x92) Exchange 4th item from stack with the top */
	SWAP3(0x92),
	/** (0x93) Exchange 5th item from stack with the top */
	SWAP4(0x93),
	/** (0x94) Exchange 6th item from stack with the top */
	SWAP5(0x94),
	/** (0x95) Exchange 7th item from stack with the top */
	SWAP6(0x95),
	/** (0x96) Exchange 8th item from stack with the top */
	SWAP7(0x96),
	/** (0x97) Exchange 9th item from stack with the top */
	SWAP8(0x97),
	/** (0x98) Exchange 10th item from stack with the top */
	SWAP9(0x98),
	/** (0x99) Exchange 11th item from stack with the top */
	SWAP10(0x99),
	/** (0x9a) Exchange 12th item from stack with the top */
	SWAP11(0x9a),
	/** (0x9b) Exchange 13th item from stack with the top */
	SWAP12(0x9b),
	/** (0x9c) Exchange 14th item from stack with the top */
	SWAP13(0x9c),
	/** (0x9d) Exchange 15th item from stack with the top */
	SWAP14(0x9d),
	/** (0x9e) Exchange 16th item from stack with the top */
	SWAP15(0x9e),
	/** (0x9f) Exchange 17th item from stack with the top */
	SWAP16(0x9f),
	
	/*	System operations	*/
	
	/** (0xf0) Create a new account with associated code	*/
	CREATE(0xf0),   //       [in_size] [in_offs] [gas_val] CREATE
	/** (cxf1) Message-call into an account 				*/
	CALL(0xf1),     //       [out_data_size] [out_data_start] [in_data_size] [in_data_start] [value] [to_addr] [gas] CALL
	/** (0xf2) Halt execution returning output data			*/
	RETURN(0xf2),
	/** (0xf3) Same as call, except 5 arguments in 
	 * and 0 arguments out, and instead of immediately 
	 * calling it adds the call to a postqueue, to be 
	 * executed after everything else 
	 * (including prior-created posts) within the scope 
	 * of that transaction execution is executed */
	POST(0xf3),
	/** (0xf4) Calls self, but grabbing the code from the 
	 * TO argument instead of from one's own address 		*/
	CALLSTATELESS(0xf4),
	/** (0xff) Halt execution and register account for 
	 * later deletion */
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
