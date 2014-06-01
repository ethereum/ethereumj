package org.ethereum.vm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
import java.math.BigInteger;

import static org.ethereum.vm.OpCode.PUSH1;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 01/06/2014 10:44
 */

public class VM {

    static private BigInteger _32_ = BigInteger.valueOf(32);

    Logger logger = LoggerFactory.getLogger("VM");


    public void step(Program program){


        byte op = program.getCurrentOp();
        logger.debug("Op: {}" ,OpCode.code(op).name());

        switch (OpCode.code(op)){


            /**
             * Stop and Arithmetic Operations
             */

//            case STOP:
//                break;
//            case ADD:
//                break;
//            case MUL:
//                break;
//            case SUB:
//                break;
//            case DIV:
//                break;
//            case SDIV:
//                break;
//            case MOD:
//                break;
//            case SMOD:
//                break;
//            case EXP:
//                break;
            case NEG:{
                DataWord word1 = program.stackPull();
                word1.negate();
                program.stackPush(word1);
                program.step();
            }break;
            case LT:{
                DataWord word1 = program.stackPull();
                DataWord word2 = program.stackPull();
                if (word1.value().compareTo(word2.value()) == -1){
                    word1.and(DataWord.ZERO);
                    word1.getData()[31] = 1;
                } else {
                    word1.and(DataWord.ZERO);
                }
                program.stackPush(word1);
                program.step();
            }break;
//            case SLT:
//                break;
//            case SGT:
//                break;
            case GT:{
                DataWord word1 = program.stackPull();
                DataWord word2 = program.stackPull();
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
                DataWord word1 = program.stackPull();
                DataWord word2 = program.stackPull();
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
                DataWord word1 = program.stackPull();
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
                DataWord word1 = program.stackPull();
                DataWord word2 = program.stackPull();
                word1.and(word2);
                program.stackPush(word1);
                program.step();
            }
            break;
            case OR: {
                DataWord word1 = program.stackPull();
                DataWord word2 = program.stackPull();
                word1.or(word2);
                program.stackPush(word1);
                program.step();
            }
            break;
            case XOR: {
                DataWord word1 = program.stackPull();
                DataWord word2 = program.stackPull();
                word1.xor(word2);
                program.stackPush(word1);
                program.step();
            }
            break;
            case BYTE:{

                DataWord word1 = program.stackPull();
                DataWord word2 = program.stackPull();

                DataWord result = null;
                if (word1.value().compareTo(_32_) == -1){
                    byte tmp = word2.getData()[word1.value().intValue()];
                    word2.and(DataWord.ZERO);
                    word2.getData()[31] = tmp;
                    result = word2;
                } else
                    result = new DataWord();

                program.stackPush(result);
                program.step();
            }
            break;

            /**
             * SHA3
             */

//            case SHA3:
//                break;

            /**
             * Environmental Information
             */

            case ADDRESS:
                break;
            case BALANCE:
                break;
            case ORIGIN:
                break;
            case CALLER:
                break;
            case CALLVALUE:
                break;
            case CALLDATALOAD:
                break;
            case CALLDATASIZE:
                break;
            case CALLDATACOPY:
                break;
            case CODESIZE:
                break;
            case CODECOPY:
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


            case POP:
                break;
            case DUP:
                break;
            case SWAP:
                break;
            case MLOAD:
                break;
            case MSTORE:
                break;
            case MSTORE8:
                break;
            case SLOAD:
                break;
            case SSTORE:
                break;
            case JUMP:
                break;
            case JUMPI:
                break;
            case PC:
                break;
            case MEMSIZE:
                break;
            case GAS:
                break;

            case PUSH1:
            case PUSH2:
            case PUSH3:
            case PUSH4:
            case PUSH5:
            case PUSH6:
            case PUSH7:
            case PUSH8:
            case PUSH9:
            case PUSH10:
            case PUSH11:
            case PUSH12:
            case PUSH13:
            case PUSH14:
            case PUSH15:
            case PUSH16:
            case PUSH17:
            case PUSH18:
            case PUSH19:
            case PUSH20:
            case PUSH21:
            case PUSH22:
            case PUSH23:
            case PUSH24:
            case PUSH25:
            case PUSH26:
            case PUSH27:
            case PUSH28:
            case PUSH29:
            case PUSH30:
            case PUSH31:
            case PUSH32:

                program.step();
                int nPush = op - PUSH1.val() + 1;

                byte[] data = program.sweep(nPush);
                program.stackPush(data);
                break;
            case CREATE:
                break;
            case CALL:
                break;
            case RETURN:
                break;
            case SUICIDE:
                break;
            default:

        }
        program.fullTrace();





    }

}
