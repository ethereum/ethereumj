package org.ethereum.vm.program.listener;

import org.ethereum.vm.DataWord;

import java.util.ArrayList;
import java.util.List;


public class CompositeProgramListener implements ProgramListener {

    private List<ProgramListener> listeners = new ArrayList<>();

    @Override
    public void onMemoryExtend(int delta) {
        for (ProgramListener listener : listeners) {
            listener.onMemoryExtend(delta);
        }
    }

    @Override
    public void onMemoryWrite(int address, byte[] data, int size) {
        for (ProgramListener listener : listeners) {
            listener.onMemoryWrite(address, data, size);
        }
    }

    @Override
    public void onStackPop() {
        for (ProgramListener listener : listeners) {
            listener.onStackPop();
        }
    }

    @Override
    public void onStackPush(DataWord value) {
        for (ProgramListener listener : listeners) {
            listener.onStackPush(value);
        }
    }

    @Override
    public void onStackSwap(int from, int to) {
        for (ProgramListener listener : listeners) {
            listener.onStackSwap(from, to);
        }
    }

    @Override
    public void onStoragePut(DataWord key, DataWord value) {
        for (ProgramListener listener : listeners) {
            listener.onStoragePut(key, value);
        }
    }

    @Override
    public void onStorageClear() {
        for (ProgramListener listener : listeners) {
            listener.onStorageClear();
        }
    }

    public void addListener(ProgramListener listener) {
        listeners.add(listener);
    }

    public boolean isEmpty() {
        return listeners.isEmpty();
    }
}
