package org.ethereum.vm;

import org.ethereum.crypto.HashUtil;
import org.ethereum.vm.Program.OutOfGasException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import static org.ethereum.vm.OpCode.PUSH1;

/**
 * The Ethereum Virtual Machine (EVM) is responsible for initialization
 * and executing a transaction on a contract.
 * 
 * It is a quasi-Turing-complete machine; the quasi qualification 
 * comes from the fact that the computation is intrinsically bounded 
 * through a parameter, gas, which limits the total amount of computation done.
 *
 * The EVM is a simple stack-based architecture. The word size of the machine 
 * (and thus size of stack item) is 256-bit. This was chosen to facilitate 
 * the SHA3-256 hash scheme and  elliptic-curve computations. The memory model 
 * is a simple word-addressed byte array. The stack has an unlimited size. 
 * The machine also has an independent storage model; this is similar in concept 
 * to the memory but rather than a byte array, it is a word-addressable word array. 
 * 
 * Unlike memory, which is volatile, storage is non volatile and is 
 * maintained as part of the system state. All locations in both storage 
 * and memory are well-defined initially as zero.
 * 
 * The machine does not follow the standard von Neumann architecture. 
 * Rather than storing program code in generally-accessible memory or storage, 
 * it is stored separately in a virtual ROM interactable only though 
 * a specialised instruction.
 * 
 * The machine can have exceptional execution for several reasons, 
 * including stack underflows and invalid instructions. These unambiguously 
 * and validly result in immediate halting of the machine with all state changes 
 * left intact. The one piece of exceptional execution that does not leave 
 * state changes intact is the out-of-gas (OOG) exception. 
 * 
 * Here, the machine halts immediately and reports the issue to 
 * the execution agent (either the transaction processor or, recursively, 
 * the spawning execution environment) and which will deal with it separately.
 *
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 01/06/2014 10:44
 */
public class VM {

	private Logger logger = LoggerFactory.getLogger("VM");
	private static BigInteger _32_ = BigInteger.valueOf(32);

    public void step(Program program) {

        try {

            byte op = program.getCurrentOp();
            program.setLastOp(op);
            logger.debug("[ {} ] Op: {}" ,program.getPC(), OpCode.code(op).name());

            int oldMemSize = program.getMemSize();

            switch (OpCode.code(op)) {
                case SHA3:
                   program.spendGas(GasCost.SHA3, OpCode.code(op).name());
                   break;
                case SLOAD:
                    program.spendGas(GasCost.SLOAD, OpCode.code(op).name());
                    break;
                case BALANCE:
                    program.spendGas(GasCost.BALANCE, OpCode.code(op).name());
                    break;
                case CREATE:
                    program.spendGas(GasCost.CREATE, OpCode.code(op).name());
                    break;
                case CALL:
                    program.spendGas(GasCost.CALL, OpCode.code(op).name());
                    break;
                case SSTORE: case STOP: case SUICIDE:
                    // The ops that doesn't charged by step, or
                    // charged in the following section
                    break;
                default:
                    program.spendGas(GasCost.STEP, OpCode.code(op).name());
                    break;
            }

            switch (OpCode.code(op)) {
                /**
                 * Stop and Arithmetic Operations
                 */
                case STOP:{
                    program.setHReturn(ByteBuffer.allocate(0));
                    program.stop();
                }	break;
                case ADD:{
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.add(word2);
                    program.stackPush(word1);
                    program.step();
                }	break;
                case MUL:{
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.mul(word2);
                    program.stackPush(word1);
                    program.step();
                }	break;
                case SUB:{
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.sub(word2);
                    program.stackPush(word1);
                    program.step();
                }	break;
                case DIV:{
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.div(word2);
                    program.stackPush(word1);
                    program.step();
                }	break;
                case SDIV:{
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.sDiv(word2);
                    program.stackPush(word1);
                    program.step();
                }	break;
                case MOD:{
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.mod(word2);
                    program.stackPush(word1);
                    program.step();
                }	break;
                case SMOD:{
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.sMod(word2);
                    program.stackPush(word1);
                    program.step();
                }	break;
                case EXP:{
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.exp(word2);
                    program.stackPush(word1);
                    program.step();
                }	break;
                case NEG:{
                    DataWord word1 = program.stackPop();
                    word1.negate();
                    program.stackPush(word1);
                    program.step();
                }	break;
                case LT:{
                    // TODO: can be improved by not using BigInteger
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    if (word1.value().compareTo(word2.value()) == -1) {
                        word1.and(DataWord.ZERO);
                        word1.getData()[31] = 1;
                    } else {
                        word1.and(DataWord.ZERO);
                    }
                    program.stackPush(word1);
                    program.step();
                }	break;
                case SLT:{
                    // TODO: can be improved by not using BigInteger
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    if (word1.sValue().compareTo(word2.sValue()) == -1) {
                        word1.and(DataWord.ZERO);
                        word1.getData()[31] = 1;
                    } else {
                        word1.and(DataWord.ZERO);
                    }
                    program.stackPush(word1);
                    program.step();
                }	break;
                case SGT:{
                    // TODO: can be improved by not using BigInteger
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    if (word1.sValue().compareTo(word2.sValue()) == 1) {
                        word1.and(DataWord.ZERO);
                        word1.getData()[31] = 1;
                    } else {
                        word1.and(DataWord.ZERO);
                    }
                    program.stackPush(word1);
                    program.step();
                }	break;
                case GT:{
                    // TODO: can be improved by not using BigInteger
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    if (word1.value().compareTo(word2.value()) == 1) {
                        word1.and(DataWord.ZERO);
                        word1.getData()[31] = 1;
                    } else {
                        word1.and(DataWord.ZERO);
                    }
                    program.stackPush(word1);
                    program.step();
                }	break;
                case EQ:{
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    if (word1.xor(word2).isZero()) {
                        word1.and(DataWord.ZERO);
                        word1.getData()[31] = 1;
                    } else {
                        word1.and(DataWord.ZERO);
                    }
                    program.stackPush(word1);
                    program.step();
                }	break;
                case NOT: {
                    DataWord word1 = program.stackPop();
                    if (word1.isZero()) {
                        word1.getData()[31] = 1;
                    } else {
                        word1.and(DataWord.ZERO);
                    }
                    program.stackPush(word1);
                    program.step();
                }	break;

                /**
                 * Bitwise Logic Operations
                 */
                case AND:{
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.and(word2);
                    program.stackPush(word1);
                    program.step();
                }	break;
                case OR: {
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.or(word2);
                    program.stackPush(word1);
                    program.step();
                }	break;
                case XOR: {
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.xor(word2);
                    program.stackPush(word1);
                    program.step();
                }	break;
                case BYTE:{
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    DataWord result = null;
                    if (word1.value().compareTo(_32_) == -1) {
                        byte tmp = word2.getData()[word1.value().intValue()];
                        word2.and(DataWord.ZERO);
                        word2.getData()[31] = tmp;
                        result = word2;
                    } else {
                        result = new DataWord();
                    }
                    program.stackPush(result);
                    program.step();
                }	break;

                /**
                 * SHA3
                 */
                case SHA3:{
                    DataWord memOffsetData  = program.stackPop();
                    DataWord lengthData     = program.stackPop();
                    ByteBuffer buffer = program.memoryChunk(memOffsetData, lengthData);

                    byte[] encoded = HashUtil.sha3(buffer.array());
                    DataWord word = new DataWord(encoded);

                    program.stackPush(word);
                    program.step();
                }	break;

                /**
                 * Environmental Information
                 */
                case ADDRESS:{
                    DataWord address = program.getOwnerAddress();
                    program.stackPush(address);
                    program.step();
                }	break;
                case BALANCE:{
                    DataWord balance = program.getBalance();
                    program.stackPush(balance);
                    program.step();
                }	break;
                case ORIGIN:{
                    DataWord originAddress = program.getOriginAddress();
                    program.stackPush(originAddress);
                    program.step();
                }	break;
                case CALLER:{
                    DataWord callerAddress = program.getCallerAddress();
                    program.stackPush(callerAddress);
                    program.step();
                }	break;
                case CALLVALUE:{
                    DataWord callValue = program.getCallValue();
                    program.stackPush(callValue);
                    program.step();
                }	break;
                case CALLDATALOAD:{
                    DataWord dataOffs  = program.stackPop();
                    DataWord value = program.getDataValue(dataOffs);

                    program.stackPush(value);
                    program.step();
                }	break;
                case CALLDATASIZE:{
                    DataWord dataSize = program.getDataSize();
                    program.stackPush(dataSize);
                    program.step();
                }	break;
                case CALLDATACOPY:{
                    DataWord memOffsetData  = program.stackPop();
                    DataWord dataOffsetData = program.stackPop();
                    DataWord lengthData     = program.stackPop();

                    byte[] msgData = program.getDataCopy(dataOffsetData, lengthData);
                    program.memorySave(memOffsetData.data, msgData);
                    program.step();
                }	break;
                case CODESIZE:{
                    DataWord length = new DataWord(program.ops.length);
                    program.stackPush(length);
                    program.step();
                }	break;
                case CODECOPY:{
                    DataWord memOffsetData  = program.stackPop();
                    DataWord codeOffsetData = program.stackPop();
                    DataWord lengthData     = program.stackPop();

                    int length     = lengthData.value().intValue();
                    int codeOffset = codeOffsetData.value().intValue();

                    if (program.ops.length < length + codeOffset) {
                        program.stop();
                        break;
                    }

                    byte[] code = new byte[length];
                    System.arraycopy(program.ops, codeOffset, code, 0, length);

                    program.memorySave(memOffsetData.getData(), code);
                    program.step();
                }	break;
                case GASPRICE:{
                    DataWord gasPrice = program.getGasPrice();
                    program.stackPush(gasPrice);
                    program.step();
                }   break;

                /**
                 * Block Information
                 */
                case PREVHASH: {
                    DataWord prevHash = program.getPrevHash();
                    program.stackPush(prevHash);
                    program.step();
                }   break;
                case COINBASE: {
                    DataWord coinbase = program.getCoinbase();
                    program.stackPush(coinbase);
                    program.step();
                }   break;
                case TIMESTAMP:{
                    DataWord timestamp = program.getTimestamp();
                    program.stackPush(timestamp);
                    program.step();
                }   break;
                case NUMBER:{
                    DataWord number = program.getNumber();
                    program.stackPush(number);
                    program.step();
                }   break;
                case DIFFICULTY:{
                    DataWord difficulty = program.getDifficulty();
                    program.stackPush(difficulty);
                    program.step();
                }   break;
                case GASLIMIT:{
                    DataWord gaslimit = program.getGaslimit();
                    program.stackPush(gaslimit);
                    program.step();
                }   break;
                case POP:{
                    program.stackPop();
                    program.step();
                }	break;
                case DUP:{
                    DataWord word_1 =  program.stackPop();
                    DataWord word_2 = word_1.clone();
                    program.stackPush(word_1);
                    program.stackPush(word_2);
                    program.step();
                }	break;
                case SWAP:{
                    DataWord word_1 =  program.stackPop();
                    DataWord word_2 =  program.stackPop();
                    program.stackPush(word_1);
                    program.stackPush(word_2);
                    program.step();
                }	break;
                case MLOAD:{
                    DataWord addr =  program.stackPop();
                    DataWord data =  program.memoryLoad(addr);
                    program.stackPush(data);
                    program.step();
                }	break;
                case MSTORE:{
                    DataWord addr  =  program.stackPop();
                    DataWord value =  program.stackPop();
                    program.memorySave(addr, value);
                    program.step();
                }	break;
                case MSTORE8:{
                    DataWord addr  =  program.stackPop();
                    DataWord value =  program.stackPop();
                    byte[] byteVal = {value.getData()[31]};
                    program.memorySave(addr.getData(), byteVal);
                    program.step();
                }	break;
                case SLOAD:{
                    DataWord key =  program.stackPop();
                    DataWord val = program.storageLoad(key);
                    if (val == null) {
                        val = key.and(DataWord.ZERO);
                    }
                    program.stackPush(val);
                    program.step();
                }	break;
                case SSTORE:{
                    DataWord addr  =  program.stackPop();
                    DataWord value =  program.stackPop();

                    // for gas calculations [YP 9.2]
                    DataWord oldValue =  program.storageLoad(addr);
                    program.storageSave(addr, value);
                    if (oldValue == null && !value.isZero()) {
                        program.spendGas(GasCost.SSTORE * 2, OpCode.code(op).name());
                    } else if (oldValue != null && value.isZero()) {
                        program.spendGas(GasCost.SSTORE * 0, OpCode.code(op).name());
                    } else
                        program.spendGas(GasCost.SSTORE, OpCode.code(op).name());
                    program.step();
                }	break;
                case JUMP:{
                    DataWord pos  =  program.stackPop();
                    program.setPC(pos);
                }	break;
                case JUMPI:{
                    DataWord pos   =  program.stackPop();
                    DataWord cond  =  program.stackPop();

                    if (!cond.isZero()) {
                        program.setPC(pos);
                    } else{
                        program.step();
                    }
                }	break;
                case PC:{
                    int pc = program.getPC();
                    DataWord pcWord = new DataWord(pc);
                    program.stackPush(pcWord);
                    program.step();
                }	break;
                case MSIZE:{
                    int memSize = program.getMemSize();
                    DataWord wordMemSize = new DataWord(memSize);
                    program.stackPush(wordMemSize);
                    program.step();
                }	break;
                case GAS:{
                    DataWord gas = program.getGas();
                    program.stackPush(gas);
                    program.step();
                }   break;

                case PUSH1:  case PUSH2:  case PUSH3:  case PUSH4:  case PUSH5:  case PUSH6:  case PUSH7:  case PUSH8:
                case PUSH9:  case PUSH10: case PUSH11: case PUSH12: case PUSH13: case PUSH14: case PUSH15: case PUSH16:
                case PUSH17: case PUSH18: case PUSH19: case PUSH20: case PUSH21: case PUSH22: case PUSH23: case PUSH24:
                case PUSH25: case PUSH26: case PUSH27: case PUSH28: case PUSH29: case PUSH30: case PUSH31: case PUSH32:{
                    program.step();
                    int nPush = op - PUSH1.val() + 1;

                    byte[] data = program.sweep(nPush);
                    program.stackPush(data);
                }	break;
                case CREATE:{
                    DataWord gas        =  program.stackPop();
                    DataWord inOffset   =  program.stackPop();
                    DataWord inSize     =  program.stackPop();

                    program.createContract(gas, inOffset, inSize);

                    program.step();
                }	break;
                case CALL:{
                    DataWord gas        =  program.stackPop();
                    DataWord toAddress  =  program.stackPop();
                    DataWord value      =  program.stackPop();

                    DataWord inDataOffs =  program.stackPop();
                    DataWord inDataSize =  program.stackPop();

                    DataWord outDataOffs =  program.stackPop();
                    DataWord outDataSize =  program.stackPop();

                    program.callToAddress(gas, toAddress, value, inDataOffs, inDataSize,outDataOffs, outDataSize);

                    program.step();
                }	break;
                case RETURN:{
                    DataWord offset   =  program.stackPop();
                    DataWord size     =  program.stackPop();

                    ByteBuffer hReturn = program.memoryChunk(offset, size);
                    program.setHReturn(hReturn);

                    program.step();
                    program.stop();
                }	break;
                case SUICIDE:{
                    DataWord address =  program.stackPop();
                    program.suicide(address);
                    program.stop();
                }	break;
                default:{
                }
            }

            // memory gas calc
            int newMemSize = program.getMemSize();
            int memoryUsage = (newMemSize - oldMemSize) /32;

            if (memoryUsage > 0)
                program.spendGas(GasCost.MEMORY * memoryUsage, OpCode.code(op).name() + " (memory usage)");

            program.fullTrace();
        } catch (RuntimeException e) {
            program.stop();
            if(e instanceof OutOfGasException)
            	logger.warn("OutOfGasException occurred", e);
            else
            	logger.error("VM halted", e);
            throw e;
        }
    }

    public void play(Program program) {
        try {
            // In case the program invoked by wire got
            // transaction, this will be the gas cost,
            // otherwise the call done by other contract
            // charged by CALL op
            if (program.invokeData.byTransaction()) {
                program.spendGas(GasCost.TRANSACTION, "TRANSACTION");
                program.spendGas(GasCost.TXDATA * program.invokeData.getDataSize().intValue(), "DATA");
            }
            while(!program.isStopped())
                this.step(program);
        } catch (RuntimeException e) {
            program.setRuntimeFailure(e);
        }
    }
}
