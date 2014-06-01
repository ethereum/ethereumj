package org.ethereum.vm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ethereum.vm.OpCode.PUSH1;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 01/06/2014 10:44
 */

public class VM {

    Logger logger = LoggerFactory.getLogger("VM");


    public void step(Program program){


        byte op = program.getCurrentOp();

        switch (OpCode.code(op)){


            /**
             * Stop and Arithmetic Operations
             */

            case STOP:
                break;
            case ADD:
                break;
            case MUL:
                break;
            case SUB:
                break;
            case DIV:
                break;
            case SDIV:
                break;
            case MOD:
                break;
            case SMOD:
                break;
            case EXP:
                break;
            case NEG:
                break;
            case LT:
                break;
            case SLT:
                break;
            case SGT:
                break;
            case GT:
                break;
            case EQ:
                break;
            case NOT:
                break;



            /**
             * Bitwise Logic Operations
             */

            case AND:
                break;
            case OR:
                break;
            case XOR:
                break;
            case BYTE:
                break;

            /**
             * SHA3
             */

            case SHA3:
                break;

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

                logger.debug("Op: {}" ,OpCode.code(op).name());
                program.step();
                int nPush = op - PUSH1.val() + 1;

                byte[] data = program.sweep(nPush);
                program.stackPush(data);
                program.fullTrace();
                break;
            default:

        }





    }

}
