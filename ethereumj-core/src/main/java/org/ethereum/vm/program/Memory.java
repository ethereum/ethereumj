package org.ethereum.vm.program;

import java.util.List;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.listener.ProgramListener;
import org.ethereum.vm.program.listener.ProgramListenerAware;

public interface Memory extends ProgramListenerAware {

    void setProgramListener(ProgramListener traceListener);

    byte[] read(int address, int size);

    void write(int address, byte[] data, int dataSize, boolean limited);

    void extendAndWrite(int address, int allocSize, byte[] data);

    void extend(int address, int size);

    DataWord readWord(int address);

    // just access expecting all data valid
    byte readByte(int address);

    String toString();

    int size();

    int internalSize();

    List<byte[]> getChunks();

}
