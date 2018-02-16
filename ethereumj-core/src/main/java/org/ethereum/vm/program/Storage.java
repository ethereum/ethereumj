package org.ethereum.vm.program;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.ethereum.core.Repository;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.listener.ProgramListenerAware;

public interface Storage extends Repository, ProgramListenerAware {

    int getStorageSize(byte[] addr);

    Set<DataWord> getStorageKeys(byte[] addr);

    Map<DataWord, DataWord> getStorage(byte[] addr, Collection<DataWord> keys);

}