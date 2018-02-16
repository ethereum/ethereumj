package org.ethereum.vm.program;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.listener.ProgramListener;

/**
 * A null implementation of the {@link Stack} interface. This is good for
 * passing around if stack is not used, or when testing.
 */
public class NullStack implements Stack {	

    @Override
    public void setProgramListener(ProgramListener listener) {
	// Intentionally left blank
    }

    @Override
    public synchronized DataWord pop() {
	return new DataWord();
    }

    @Override
    public DataWord push(DataWord item) {
	return new DataWord();
    }

    @Override
    public void swap(int from, int to) {
	// Intentionally left blank
    }
}
