package org.ethereum.vm;

import org.ethereum.vm.program.Program;

/**
 * Created by Anton Nashatyrev on 15.02.2016.
 */
public interface VMHook {
    void startPlay(Program program);
    void step(Program program, OpCode opcode);
    void stopPlay(Program program);
}
