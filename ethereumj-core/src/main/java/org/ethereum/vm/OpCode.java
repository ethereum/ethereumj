package org.ethereum.vm;

import java.util.HashMap;
import java.util.Map;

/**
 * Instruction set for the Ethereum Virtual Machine
 * See Yellow Paper: http://www.gavwood.com/Paper.pdf 
 * - Appendix G. Virtual Machine Specification
 */
public enum OpCode {

    /** Halts execution (0x00)                  */
    STOP(0x00,   0),

    /*  Arithmetic Operations   */

    /** (0x01) Addition operation               */
    ADD(0x01, 2),
    /** (0x02) Multiplication operation         */
    MUL(0x02, 2),
    /** (0x03) Subtraction operations           */
    SUB(0x03, 2),
    /** (0x04) Integer division operation       */
    DIV(0x04, 2),
    /** (0x05) Signed integer division operation*/
    SDIV(0x05, 2),
    /** (0x06) Modulo remainder operation       */
    MOD(0x06, 2),
    /** (0x07) Signed modulo remainder operation*/
    SMOD(0x07, 2),
    /** (0x08) Addition combined with modulo 
     * remainder operation */
    ADDMOD(0x08, 3),
    /** (0x09) Multiplication combined with modulo 
     * remainder operation */
    MULMOD(0x09, 3),
    /** (0x0a) Exponential operation            */
    EXP(0x0a, 2),
    /** (0x0b) Extend length of signed integer  */
    SIGNEXTEND(0x0b, 2),

    /*  Bitwise Logic & Comparison Operations   */

    /** (0x10) Less-than comparison             */
    LT(0X10, 2),
    /** (0x11) Greater-than comparison          */
    GT(0X11, 2),
    /** (0x12) Signed less-than comparison      */
    SLT(0X12, 2),
    /** (0x13) Signed greater-than comparison   */
    SGT(0X13, 2),
    /** (0x14) Equality comparison              */
    EQ(0X14, 2),
    /** (0x15) Negation operation               */
    ISZERO(0x15, 1),
    /** (0x16) Bitwise AND operation            */
    AND(0x16, 2),
    /** (0x17) Bitwise OR operation             */
    OR(0x17, 2),
    /** (0x18) Bitwise XOR operation            */
    XOR(0x18, 2),
    /** (0x19) Bitwise NOT operationr           */
    NOT(0x19, 1),
    /** (0x1a) Retrieve single byte from word   */
    BYTE(0x1a, 2),

    /*  Cryptographic Operations    */

    /** (0x20) Compute SHA3-256 hash            */
    SHA3(0x20,  2),

    /*  Environmental Information   */

    /** (0x30)  Get address of currently 
     * executing account                        */
    ADDRESS(0x30, 0),
    /** (0x31) Get balance of the given account */
    BALANCE(0x31, 1),
    /** (0x32) Get execution origination address*/
    ORIGIN(0x32, 0),
    /** (0x33) Get caller address               */
    CALLER(0x33, 0),
    /** (0x34) Get deposited value by the 
     * instruction/transaction responsible 
     * for this execution                       */
    CALLVALUE(0x34, 0),
    /** (0x35) Get input data of current 
     * environment                              */
    CALLDATALOAD(0x35, 1),
    /** (0x36) Get size of input data in current
     * environment                              */
    CALLDATASIZE(0x36, 0),
    /** (0x37) Copy input data in current 
     * environment to memory                    */
    CALLDATACOPY(0x37, 3),
    /** (0x38) Get size of code running in 
     * current environment                      */
    CODESIZE(0x38, 0),
    /** (0x39) Copy code running in current 
     * environment to memory                    */
    CODECOPY(0x39, 3), // [len code_start mem_start CODECOPY]
    /** (0x3a) Get price of gas in current 
     * environment                              */
    GASPRICE(0x3a, 0),
    /** (0x3b) Get size of code running in 
     * current environment with given offset    */
    EXTCODESIZE(0x3b, 1),
    /** (0x3c) Copy code running in current 
     * environment to memory with given offset  */
    EXTCODECOPY(0x3c, 4),

    /*  Block Information   */

    /** (0x40) Get hash of most recent 
     * complete block                           */
    PREVHASH(0x40, 0),
    /** (0x41) Get the block’s coinbase address   */
    COINBASE(0x41, 0),
    /** (x042) Get the block’s timestamp      */
    TIMESTAMP(0x42, 0),
    /** (0x43) Get the block’s number         */
    NUMBER(0x43, 0),
    /** (0x44) Get the block’s difficulty     */
    DIFFICULTY(0x44, 0),
    /** (0x45) Get the block’s gas limit      */
    GASLIMIT(0x45, 0),

    /*  Memory, Storage and Flow Operations */

    /** (0x50) Remove item from stack           */
    POP(0x50,   1),
    /** (0x51) Load word from memory            */
    MLOAD(0x51, 1),
    /** (0x52) Save word to memory              */
    MSTORE(0x52, 2),
    /** (0x53) Save byte to memory              */
    MSTORE8(0x53, 2),
    /** (0x54) Load word from storage           */
    SLOAD(0x54, 1),
    /** (0x55) Save word to storage             */
    SSTORE(0x55, 2),
    /** (0x56) Alter the program counter        */
    JUMP(0x56, 1),
    /** (0x57) Conditionally alter the program 
     * counter                                  */
    JUMPI(0x57, 2),
    /** (0x58) Get the program counter          */
    PC(0x58, 0),
    /** (0x59) Get the size of active memory    */
    MSIZE(0x59, 0),
    /** (0x5a) Get the amount of available gas  */
    GAS(0x5a, 0),
    /** (0x5b) */
    JUMPDEST(0x5b, 0),

    /*  Push Operations */

    /** (0x60) Place 1-byte item on stack       */
    PUSH1(0x60, 0),
    /** (0x61) Place 2-byte item on stack       */
    PUSH2(0x61, 0),
    /** (0x62) Place 3-byte item on stack       */
    PUSH3(0x62, 0),
    /** (0x63) Place 4-byte item on stack       */
    PUSH4(0x63, 0),
    /** (0x64) Place 5-byte item on stack       */
    PUSH5(0x64, 0),
    /** (0x65) Place 6-byte item on stack       */
    PUSH6(0x65, 0),
    /** (0x66) Place 7-byte item on stack       */
    PUSH7(0x66, 0),
    /** (0x67) Place 8-byte item on stack       */
    PUSH8(0x67, 0),
    /** (0x68) Place 9-byte item on stack       */
    PUSH9(0x68, 0),
    /** (0x69) Place 10-byte item on stack      */
    PUSH10(0x69, 0),
    /** (0x6a) Place 11-byte item on stack      */
    PUSH11(0x6a, 0),
    /** (0x6b) Place 12-byte item on stack      */
    PUSH12(0x6b, 0),
    /** (0x6c) Place 13-byte item on stack      */
    PUSH13(0x6c, 0),
    /** (0x6d) Place 14-byte item on stack      */
    PUSH14(0x6d, 0),
    /** (0x6e) Place 15-byte item on stack      */
    PUSH15(0x6e, 0),
    /** (0x6f) Place 16-byte item on stack      */
    PUSH16(0x6f, 0),
    /** (0x70) Place 17-byte item on stack      */
    PUSH17(0x70, 0),
    /** (0x71) Place 18-byte item on stack      */
    PUSH18(0x71, 0),
    /** (0x72) Place 19-byte item on stack      */
    PUSH19(0x72, 0),
    /** (0x73) Place 20-byte item on stack      */
    PUSH20(0x73, 0),
    /** (0x74) Place 21-byte item on stack      */
    PUSH21(0x74, 0),
    /** (0x75) Place 22-byte item on stack      */
    PUSH22(0x75, 0),
    /** (0x76) Place 23-byte item on stack      */
    PUSH23(0x76, 0),
    /** (0x77) Place 24-byte item on stack      */
    PUSH24(0x77, 0),
    /** (0x78) Place 25-byte item on stack      */
    PUSH25(0x78, 0),
    /** (0x79) Place 26-byte item on stack      */
    PUSH26(0x79, 0),
    /** (0x7a) Place 27-byte item on stack      */
    PUSH27(0x7a, 0),
    /** (0x7b) Place 28-byte item on stack      */
    PUSH28(0x7b, 0),
    /** (0x7c) Place 29-byte item on stack      */
    PUSH29(0x7c, 0),
    /** (0x7d) Place 30-byte item on stack      */
    PUSH30(0x7d, 0),
    /** (0x7e) Place 31-byte item on stack      */
    PUSH31(0x7e, 0),
    /** (0x7f) Place 32-byte (full word) 
     * item on stack                            */
    PUSH32(0x7f, 0),

    /*  Duplicate Nth item from the stack   */

    /** (0x80) Duplicate 1st item on stack      */
    DUP1(0x80, 1),
    /** (0x81) Duplicate 2nd item on stack      */
    DUP2(0x81, 2),
    /** (0x82) Duplicate 3rd item on stack      */
    DUP3(0x82, 3),
    /** (0x83) Duplicate 4th item on stack      */
    DUP4(0x83, 4),
    /** (0x84) Duplicate 5th item on stack      */
    DUP5(0x84, 5),
    /** (0x85) Duplicate 6th item on stack      */
    DUP6(0x85, 6),
    /** (0x86) Duplicate 7th item on stack      */
    DUP7(0x86, 7),
    /** (0x87) Duplicate 8th item on stack      */
    DUP8(0x87, 8),
    /** (0x88) Duplicate 9th item on stack      */
    DUP9(0x88, 9),
    /** (0x89) Duplicate 10th item on stack     */
    DUP10(0x89, 10),
    /** (0x8a) Duplicate 11th item on stack     */
    DUP11(0x8a, 11),
    /** (0x8b) Duplicate 12th item on stack     */
    DUP12(0x8b, 12),
    /** (0x8c) Duplicate 13th item on stack     */
    DUP13(0x8c, 13),
    /** (0x8d) Duplicate 14th item on stack     */
    DUP14(0x8d, 14),
    /** (0x8e) Duplicate 15th item on stack     */
    DUP15(0x8e, 15),
    /** (0x8f) Duplicate 16th item on stack     */
    DUP16(0x8f, 16),

    /*  Swap the Nth item from the stack with the top   */

    /** (0x90) Exchange 2nd item from stack with the top */
    SWAP1(0x90, 2),
    /** (0x91) Exchange 3rd item from stack with the top */
    SWAP2(0x91, 3),
    /** (0x92) Exchange 4th item from stack with the top */
    SWAP3(0x92, 4),
    /** (0x93) Exchange 5th item from stack with the top */
    SWAP4(0x93, 5),
    /** (0x94) Exchange 6th item from stack with the top */
    SWAP5(0x94, 6),
    /** (0x95) Exchange 7th item from stack with the top */
    SWAP6(0x95, 7),
    /** (0x96) Exchange 8th item from stack with the top */
    SWAP7(0x96, 8),
    /** (0x97) Exchange 9th item from stack with the top */
    SWAP8(0x97, 9),
    /** (0x98) Exchange 10th item from stack with the top */
    SWAP9(0x98, 10),
    /** (0x99) Exchange 11th item from stack with the top */
    SWAP10(0x99, 11),
    /** (0x9a) Exchange 12th item from stack with the top */
    SWAP11(0x9a, 12),
    /** (0x9b) Exchange 13th item from stack with the top */
    SWAP12(0x9b, 13),
    /** (0x9c) Exchange 14th item from stack with the top */
    SWAP13(0x9c, 14),
    /** (0x9d) Exchange 15th item from stack with the top */
    SWAP14(0x9d, 15),
    /** (0x9e) Exchange 16th item from stack with the top */
    SWAP15(0x9e, 16),
    /** (0x9f) Exchange 17th item from stack with the top */
    SWAP16(0x9f, 17),

    /** (0xa[n]) log some data for some addres with 0..n tags [addr [tag0..tagn] data]  */
    LOG0(0xa0, 2),
    LOG1(0xa1, 3),
    LOG2(0xa2, 4),
    LOG3(0xa3, 5),
    LOG4(0xa4, 6),

    /*  System operations   */

    /** (0xf0) Create a new account with associated code    */
    CREATE(0xf0, 3),   //       [in_size] [in_offs] [gas_val] CREATE
    /** (cxf1) Message-call into an account                 */
    CALL(0xf1, 7),     //       [out_data_size] [out_data_start] [in_data_size] [in_data_start] [value] [to_addr] [gas] CALL
    /** (0xf2) Calls self, but grabbing the code from the
     * TO argument instead of from one's own address        */
    CALLCODE(0xf2, 7),
    /** (0xf3) Halt execution returning output data         */
    RETURN(0xf3, 2),
    /** (0xff) Halt execution and register account for
     * later deletion */
    SUICIDE(0xff, 1);

    private byte opcode;
    private int require;
    
    private static final Map<Byte, OpCode> intToTypeMap = new HashMap<>();
    private static final Map<String, Byte> stringToByteMap = new HashMap<>();

    static {
        for (OpCode type : OpCode.values()) {
            intToTypeMap.put(type.opcode, type);
            stringToByteMap.put(type.name(), type.opcode);
        }
    }
    
    private OpCode(int op, int require) {
        this.opcode = (byte) op;
        this.require = require;
    }
    
    public byte val() {
        return opcode;
    }
    
    /**
     * Returns the mininum amount of items required on the stack for this operation
     * 
     * @return minimum amount of expected items on the stack
     */
    public int require() {
        return require;
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

    public static OpCode code(byte code) {
        return intToTypeMap.get(code);
    }
}
