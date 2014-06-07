package org.ethereum.vm;

import org.ethereum.crypto.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import static org.ethereum.vm.OpCode.PUSH1;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 01/06/2014 10:44
 */

public class VM {

    static private BigInteger _32_ = BigInteger.valueOf(32);

    Logger logger = LoggerFactory.getLogger("VM");


    public void step(Program program) {


        try {

            byte op = program.getCurrentOp();
            logger.debug("Op: {}" ,OpCode.code(op).name());

            int oldMemSize = program.getMemSize();

            switch (OpCode.code(op)) {
                case SHA3:
                   program.spendGas(GasCost.SHA3);
                break;
                case SLOAD:
                    program.spendGas(GasCost.SLOAD);
                break;
                case SSTORE:
                    // todo: calc gas in the execution
                    // todo: according to the size
                break;
                case BALANCE:
                    program.spendGas(GasCost.BALANCE);
                break;
                case CREATE:
                    program.spendGas(GasCost.CREATE);
                break;
                case CALL:
                    program.spendGas(GasCost.CALL);
                break;
                case MSTORE8:
                case MSTORE:
                    // todo: calc gas in the execution
                    // todo: according to the size
                break;
                default:
                    program.spendGas(GasCost.STEP);
                break;
            }


            switch (OpCode.code(op)) {


                /**
                 * Stop and Arithmetic Operations
                 */

                case STOP:{
                    program.setHReturn(ByteBuffer.allocate(0));
                    program.stop();
                }
                break;
                case ADD:{

                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.add(word2);
                    program.stackPush(word1);
                    program.step();
                }
                break;
                case MUL:{

                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.mul(word2);
                    program.stackPush(word1);
                    program.step();
                }
                break;
                case SUB:{

                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.sub(word2);
                    program.stackPush(word1);
                    program.step();
                }
                break;
                case DIV:{

                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.div(word2);
                    program.stackPush(word1);
                    program.step();
                }
                break;
                case SDIV:{

                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.sDiv(word2);
                    program.stackPush(word1);
                    program.step();
                }
                break;
                case MOD:{

                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.mod(word2);
                    program.stackPush(word1);
                    program.step();
                }
                break;
                case SMOD:{

                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.sMod(word2);
                    program.stackPush(word1);
                    program.step();
                }
                break;
                case EXP:{

                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.exp(word2);
                    program.stackPush(word1);
                    program.step();
                }
                break;
                case NEG:{
                    DataWord word1 = program.stackPop();
                    word1.negate();
                    program.stackPush(word1);
                    program.step();
                }
                break;
                case LT:{

                    // todo: can be improved by not using BigInteger
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    if (word1.value().compareTo(word2.value()) == -1){
                        word1.and(DataWord.ZERO);
                        word1.getData()[31] = 1;
                    } else {
                        word1.and(DataWord.ZERO);
                    }
                    program.stackPush(word1);
                    program.step();
                }
                break;
                case SLT:{

                    // todo: can be improved by not using BigInteger
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    if (word1.sValue().compareTo(word2.sValue()) == -1){
                        word1.and(DataWord.ZERO);
                        word1.getData()[31] = 1;
                    } else {
                        word1.and(DataWord.ZERO);
                    }
                    program.stackPush(word1);
                    program.step();
                }break;
                case SGT:{

                    // todo: can be improved by not using BigInteger
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    if (word1.sValue().compareTo(word2.sValue()) == 1){
                        word1.and(DataWord.ZERO);
                        word1.getData()[31] = 1;
                    } else {
                        word1.and(DataWord.ZERO);
                    }
                    program.stackPush(word1);
                    program.step();
                }break;
                case GT:{

                    // todo: can be improved by not using BigInteger
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    if (word1.value().compareTo(word2.value()) == 1){
                        word1.and(DataWord.ZERO);
                        word1.getData()[31] = 1;
                    } else {
                        word1.and(DataWord.ZERO);
                    }
                    program.stackPush(word1);
                    program.step();
                }break;
                case EQ:{
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    if (word1.xor(word2).isZero()){
                        word1.and(DataWord.ZERO);
                        word1.getData()[31] = 1;
                    } else {
                        word1.and(DataWord.ZERO);
                    }
                    program.stackPush(word1);
                    program.step();
                }
                break;
                case NOT: {
                    DataWord word1 = program.stackPop();
                    if (word1.isZero()){
                        word1.getData()[31] = 1;
                    } else {
                        word1.and(DataWord.ZERO);
                    }
                    program.stackPush(word1);
                    program.step();
                }
                break;

                /**
                 * Bitwise Logic Operations
                 */

                case AND:{
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.and(word2);
                    program.stackPush(word1);
                    program.step();
                }
                break;
                case OR: {
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.or(word2);
                    program.stackPush(word1);
                    program.step();
                }
                break;
                case XOR: {
                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();
                    word1.xor(word2);
                    program.stackPush(word1);
                    program.step();
                }
                break;
                case BYTE:{

                    DataWord word1 = program.stackPop();
                    DataWord word2 = program.stackPop();

                    DataWord result = null;
                    if (word1.value().compareTo(_32_) == -1){
                        byte tmp = word2.getData()[word1.value().intValue()];
                        word2.and(DataWord.ZERO);
                        word2.getData()[31] = tmp;
                        result = word2;
                    } else {
                        result = new DataWord();
                    }
                    program.stackPush(result);
                    program.step();
                }
                break;

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
                }
                break;

                /**
                 * Environmental Information
                 */

                case ADDRESS:{
                    DataWord address = program.getOwnerAddress();
                    program.stackPush(address);
                    program.step();
                }
                break;
                case BALANCE:{
                    DataWord balance = program.getBalance();
                    program.stackPush(balance);
                    program.step();
                }
                break;
                case ORIGIN:{
                    DataWord originAddress = program.getOriginAddress();
                    program.stackPush(originAddress);
                    program.step();
                }
                break;
                case CALLER:{
                    DataWord callerAddress = program.getCallerAddress();
                    program.stackPush(callerAddress);
                    program.step();
                }
                break;
                case CALLVALUE:{
                    DataWord callValue = program.getCallValue();
                    program.stackPush(callValue);
                    program.step();
                }
                break;
                case CALLDATALOAD:{
                    DataWord dataOffs  = program.stackPop();
                    DataWord value = program.getDataValue(dataOffs);

                    program.stackPush(value);
                    program.step();
                }
                break;
                case CALLDATASIZE:{
                    DataWord dataSize = program.getDataSize();

                    program.stackPush(dataSize);
                    program.step();
                }
                break;
                case CALLDATACOPY:{

                    DataWord memOffsetData  = program.stackPop();
                    DataWord dataOffsetData = program.stackPop();
                    DataWord lengthData     = program.stackPop();

                    byte[] msgData = program.getDataCopy(dataOffsetData, lengthData);
                    program.memorySave(memOffsetData.data, msgData);
                    program.step();

                } break;
                case CODESIZE:{

                    DataWord length = new DataWord(program.ops.length);
                    program.stackPush(length);
                    program.step();
                }
                break;
                case CODECOPY:{
                    DataWord memOffsetData  = program.stackPop();
                    DataWord codeOffsetData = program.stackPop();
                    DataWord lengthData     = program.stackPop();

                    int length     = lengthData.value().intValue();
                    int codeOffset = codeOffsetData.value().intValue();
                    int memOffset  = memOffsetData.value().intValue();

                    if (program.ops.length < length + codeOffset){

                        program.stop();
                        break;
                    }

                    byte[] code = new byte[length];
                    System.arraycopy(program.ops, codeOffset, code, memOffset, length);

                    program.memorySave(memOffsetData.getData(), code);
                    program.step();
                }
                break;
                case GASPRICE:
                    break;

                /**
                 * Block Information
                 */

                case PREVHASH:
                    break;
                case COINBASE:
                    break;
                case TIMESTAMP:
                    break;
                case NUMBER:
                    break;
                case DIFFICULTY:
                    break;
                case GASLIMIT:
                    break;


                case POP:{
                    program.stackPop();
                    program.step();
                }
                break;
                case DUP:{
                    DataWord word_1 =  program.stackPop();
                    DataWord word_2 = word_1.clone();
                    program.stackPush(word_1);
                    program.stackPush(word_2);
                    program.step();
                }
                break;
                case SWAP:{
                    DataWord word_1 =  program.stackPop();
                    DataWord word_2 =  program.stackPop();
                    program.stackPush(word_1);
                    program.stackPush(word_2);
                    program.step();
                }
                break;
                case MLOAD:{
                    DataWord addr =  program.stackPop();
                    DataWord data =  program.memoryLoad(addr);
                    program.stackPush(data);
                    program.step();
                }
                break;
                case MSTORE:{
                    DataWord addr  =  program.stackPop();
                    DataWord value =  program.stackPop();
                    program.memorySave(addr, value);
                    program.step();
                }
                break;
                case MSTORE8:{

                    DataWord addr  =  program.stackPop();
                    DataWord value =  program.stackPop();
                    byte[] byteVal = {value.getData()[31]};
                    program.memorySave(addr.getData(), byteVal);
                    program.step();
                }
                break;
                case SLOAD:{
                    DataWord key =  program.stackPop();
                    DataWord val = program.storageLoad(key);
                    if (val == null) {
                        val = key.and(DataWord.ZERO);
                    }
                    program.stackPush(val);
                    program.step();
                }
                break;
                case SSTORE:{
                    DataWord addr  =  program.stackPop();
                    DataWord value =  program.stackPop();

                    // for gas calculations [YP 9.2]
                    DataWord oldValue =  program.storageLoad(addr);
                    program.storageSave(addr, value);
                    if (oldValue == null && !value.isZero()){
                        program.spendGas(GasCost.SSTORE * 2);
                    } else if (oldValue != null && value.isZero()){
                        program.spendGas(GasCost.SSTORE * 0);
                    } else
                        program.spendGas(GasCost.SSTORE);

                    program.step();
                }
                break;
                case JUMP:{
                    DataWord pos  =  program.stackPop();
                    program.setPC(pos);
                }
                break;
                case JUMPI:{
                    DataWord pos   =  program.stackPop();
                    DataWord cond  =  program.stackPop();

                    if (!cond.isZero()){
                        program.setPC(pos);
                    } else{
                        program.step();
                    }
                }
                break;
                case PC:{
                    int pc = program.getPC();
                    DataWord pcWord = new DataWord(pc);
                    program.stackPush(pcWord);
                    program.step();
                }
                break;
                case MSIZE:{

                    int memSize = program.getMemSize();
                    DataWord wordMemSize = new DataWord(memSize);
                    program.stackPush(wordMemSize);
                    program.step();
                }
                break;
                case GAS:
                    break;

                case PUSH1:  case PUSH2:  case PUSH3:  case PUSH4:  case PUSH5:  case PUSH6:  case PUSH7:  case PUSH8:
                case PUSH9:  case PUSH10: case PUSH11: case PUSH12: case PUSH13: case PUSH14: case PUSH15: case PUSH16:
                case PUSH17: case PUSH18: case PUSH19: case PUSH20: case PUSH21: case PUSH22: case PUSH23: case PUSH24:
                case PUSH25: case PUSH26: case PUSH27: case PUSH28: case PUSH29: case PUSH30: case PUSH31: case PUSH32:{

                    program.step();
                    int nPush = op - PUSH1.val() + 1;

                    byte[] data = program.sweep(nPush);
                    program.stackPush(data);
                }
                break;
                case CREATE:
                    break;
                case CALL:
                    break;
                case RETURN:{

                    DataWord offset   =  program.stackPop();
                    DataWord size     =  program.stackPop();

                    ByteBuffer hReturn = program.memoryChunk(offset, size);
                    program.hReturn = hReturn;

                    program.step();
                    program.stop();
                }
                break;
                case SUICIDE:{
                    DataWord address   =  program.stackPop();
                    // todo: transfer left balance to the address
                    program.stop();
                }
                break;
                default:{
                }

                // memory gas calc
                int newMemSize = program.getMemSize();
                program.spendGas(GasCost.MEMORY * (newMemSize - oldMemSize) /32);
            }
            program.fullTrace();
        } catch (RuntimeException e) {
            program.stop();
            throw e;
        }

    }

}
