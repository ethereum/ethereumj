package org.ethereum.vmtrace;

import org.ethereum.vm.DataWord;

import static org.ethereum.config.SystemProperties.CONFIG;

public class ProgramTraceListener {

    private final boolean enabled = CONFIG.vmTrace();
    private OpActions actions = new OpActions();

    public void onMemoryExtend(int delta) {
        if (enabled) actions.addMemoryExtend(delta);
    }

    public void onMemoryWrite(int address, byte[] data) {
        if (enabled) actions.addMemoryWrite(address, data);
    }
    
    public void onStackPop() {
        if (enabled) actions.addStackPop();
    }
    
    public void onStackPush(DataWord value) {
        if (enabled) actions.addStackPush(value);
    }

    public void onStackSwap(int from, int to) {
        if (enabled) actions.addStackSwap(from, to);
    }

    public void onStoragePut(DataWord key, DataWord value) {
        if (enabled) actions.addStoragePut(key, value);
    }

    public void onStorageRemove(DataWord key) {
        if (enabled) actions.addStorageRemove(key);
    }

    public OpActions resetActions() {
        OpActions actions = this.actions;
        this.actions = new OpActions();
        return actions;
    }
}
