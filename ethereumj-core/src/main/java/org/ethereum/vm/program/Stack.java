package org.ethereum.vm.program;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.listener.ProgramListener;
import org.ethereum.vm.program.listener.ProgramListenerAware;

public interface Stack extends ProgramListenerAware{

    void setProgramListener(ProgramListener listener);

    DataWord pop();

    DataWord push(DataWord item);

    void swap(int from, int to);

}