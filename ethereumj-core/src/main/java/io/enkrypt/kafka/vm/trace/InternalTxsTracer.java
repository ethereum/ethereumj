package io.enkrypt.kafka.vm.trace;

import java.util.stream.Stream;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.trace.Op;
import org.ethereum.vm.trace.OpActions;
import org.ethereum.vm.trace.ProgramTrace;

import static org.ethereum.vm.OpCode.CALL;
import static org.ethereum.vm.OpCode.CALLCODE;
import static org.ethereum.vm.OpCode.CREATE;
import static org.ethereum.vm.OpCode.DELEGATECALL;
import static org.ethereum.vm.OpCode.SUICIDE;

/**
 * This tracer just listen only for internal smart contract transactions.
 *
 * Also this tracer will add those transactions found when executing other smart contracts by DELEGATE or CREATE opcodes.
 */
public class InternalTxsTracer extends ProgramTrace {

  public static final Op EMPTY_OP = new Op();

  @Override public Op addOp(byte code, int pc, int deep, DataWord gas, OpActions actions) {
    if (Stream.of(CREATE, CALL, CALLCODE, DELEGATECALL, SUICIDE).anyMatch(opCode -> opCode.val() == code)) {
      super.addOp(code, pc, deep, gas, actions);
    }
    return EMPTY_OP;
  }
}
