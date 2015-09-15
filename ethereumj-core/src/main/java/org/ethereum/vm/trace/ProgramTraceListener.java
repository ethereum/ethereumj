package org.ethereum.vm.trace;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.listener.ProgramListenerAdaptor;

import static org.ethereum.config.SystemProperties.CONFIG;

public class ProgramTraceListener extends ProgramListenerAdaptor {

    private final boolean enabled = CONFIG.vmTrace();
    private OpActions actions = new OpActions();

    @Override
    public void onMemoryExtend(int delta) {
        if (enabled) actions.addMemoryExtend(delta);
    }

    @Override
    public void onMemoryWrite(int address, byte[] data, int size) {
        if (enabled) actions.addMemoryWrite(address, data, size);
    }

    @Override
    public void onStackPop() {
        if (enabled) actions.addStackPop();
    }

    @Override
    public void onStackPush(DataWord value) {
        if (enabled) actions.addStackPush(value);
    }

    @Override
    public void onStackSwap(int from, int to) {
        if (enabled) actions.addStackSwap(from, to);
    }

    @Override
    public void onStoragePut(DataWord key, DataWord value) {
        if (enabled) {
            if (value.equals(DataWord.ZERO)) {
                actions.addStorageRemove(key);
            } else {
                actions.addStoragePut(key, value);
            }
        }
    }

    @Override
    public void onStorageClear() {
        if (enabled) actions.addStorageClear();
    }

    public OpActions resetActions() {
        OpActions actions = this.actions;
        this.actions = new OpActions();
        return actions;
    }
}
