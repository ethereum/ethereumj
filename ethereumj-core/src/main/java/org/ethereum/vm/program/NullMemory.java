package org.ethereum.vm.program;

import java.util.ArrayList;
import java.util.List;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.listener.ProgramListener;

/**
 * A null implementation of the {@link Memory} interface. This is good for
 * passing around if memory is not used, or when testing.
 */
public class NullMemory implements Memory {

    @Override
    public void setProgramListener(ProgramListener traceListener) {
	// Intentionally left blank
    }

    @Override
    public byte[] read(int address, int size) {
	return new byte[size];
    }

    @Override
    public void write(int address, byte[] data, int dataSize, boolean limited) {
	// Intentionally left blank
    }

    @Override
    public void extendAndWrite(int address, int allocSize, byte[] data) {
	// Intentionally left blank
    }

    @Override
    public void extend(int address, int size) {
	// Intentionally left blank
    }

    @Override
    public DataWord readWord(int address) {
	return new DataWord();
    }

    @Override
    public byte readByte(int address) {
	return 0;
    }

    @Override
    public int size() {
	return 0;
    }

    @Override
    public int internalSize() {
	return 0;
    }

    @Override
    public List<byte[]> getChunks() {
	return new ArrayList<>(0);
    }

}
