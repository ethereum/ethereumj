package org.ethereum.vm.program.listener;

import org.ethereum.vm.DataWord;

import java.util.HashMap;
import java.util.Map;

public class ProgramStorageChangeListener extends ProgramListenerAdaptor {

    private Map<DataWord, DataWord> diff = new HashMap<>();

    @Override
    public void onStoragePut(DataWord key, DataWord value) {
        diff.put(key, value);
    }

    @Override
    public void onStorageClear() {
        // TODO: ...
    }

    public Map<DataWord, DataWord> getDiff() {
        return new HashMap<>(diff);
    }

    public void merge(Map<DataWord, DataWord> diff) {
        this.diff.putAll(diff);
    }
}
