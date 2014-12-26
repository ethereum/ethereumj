package org.ethereum.vmtrace;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.OpCode;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Data to for one program step to save.
 *
 *   {
 *    'op': 'CODECOPY'
 *    'storage': {},
 *    'gas': '99376',
 *    'pc': '9',
 *    'memory': '',
 *    'stack': ['15', '15', '14', '0'],
 *   }
 *
 * www.etherj.com
 *
 * @author Roman Mandeleil
 * @since 28.10.2014
 */

public class Op {

    byte op;
    int pc;
    DataWord gas;
    Map<String, String> storage;
    byte[] memory;
    List<String> stack;

    public void setOp(byte op) {
        this.op = op;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }

    public void saveGas(DataWord gas) {
        this.gas = gas;
    }

    public void saveStorageMap(Map<DataWord, DataWord> storage) {

        this.storage = new HashMap<>();
        List<DataWord> keys = new ArrayList<>(storage.keySet());
        Collections.sort(keys);
        for (DataWord key : keys) {
            DataWord value = storage.get(key);
            this.storage.put(Hex.toHexString(key.getNoLeadZeroesData()),
                    Hex.toHexString(value.getNoLeadZeroesData()));
        }
    }

    public void saveMemory(ByteBuffer memory) {
        if (memory != null)
            this.memory = Arrays.copyOf(memory.array(), memory.array().length);
    }

    public void saveStack(Stack<DataWord> stack) {

        this.stack = new ArrayList<>();

        for (DataWord element : stack) {
            this.stack.add(0, Hex.toHexString(element.getNoLeadZeroesData()));
        }
    }

    public String toString() {

        Map<Object, Object> jsonData = new LinkedHashMap<>();

        jsonData.put("op", OpCode.code(op).name());
        jsonData.put("pc", Long.toString(pc));
        jsonData.put("gas", gas.value().toString());
        jsonData.put("stack", stack);
        jsonData.put("memory", memory == null ? "" : Hex.toHexString(memory));
        jsonData.put("storage", new JSONObject(storage));

        return JSONValue.toJSONString(jsonData);
    }


}
