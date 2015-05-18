package org.ethereum.vmtrace;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.ethereum.vm.DataWord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ethereum.util.ByteUtil.toHexString;

public class OpActions {

    public enum ActionType {
        pop,
        push,
        swap,
        extend,
        write,
        put,
        remove,
        clear;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Action {

        private ActionType action;
        private Map<String, String> params;

        public void setAction(ActionType action) {
            this.action = action;
        }

        public void setParams(Map<String, String> params) {
            this.params = params;
        }

        Action addParam(String name, Object value) {
            if (value != null) {
                if (params == null) {
                    params = new HashMap<>();
                }
                params.put(name, value.toString());
            }
            return this;
        }
    }

    private List<Action> stack = new ArrayList<>();
    private List<Action> memory = new ArrayList<>();
    private List<Action> storage = new ArrayList<>();

    public void setStack(List<Action> stack) {
        this.stack = stack;
    }

    public void setMemory(List<Action> memory) {
        this.memory = memory;
    }

    public void setStorage(List<Action> storage) {
        this.storage = storage;
    }

    private static Action addAction(List<Action> container, ActionType type) {
        Action action = new Action();
        action.setAction(type);

        container.add(action);

        return action;
    }

    public Action addStackPop() {
        return addAction(stack, ActionType.pop);
    }

    public Action addStackPush(DataWord value) {
        return addAction(stack, ActionType.push)
                .addParam("value", value);
    }

    public Action addStackSwap(int from, int to) {
        return addAction(stack, ActionType.swap)
                .addParam("from", from)
                .addParam("to", to);
    }

    public Action addMemoryExtend(long delta) {
        return addAction(memory, ActionType.extend)
                .addParam("delta", delta);
    }

    public Action addMemoryWrite(int address, byte[] data) {
        return addAction(memory, ActionType.write)
                .addParam("address", address)
                .addParam("data", toHexString(data));
    }

    public Action addStoragePut(DataWord key, DataWord value) {
        return addAction(storage, ActionType.put)
                .addParam("key", key)
                .addParam("value", value);
    }

    public Action addStorageRemove(DataWord key) {
        return addAction(storage, ActionType.remove)
                .addParam("key", key);
    }

    public Action addStorageClear() {
        return addAction(storage, ActionType.clear);
    }
}
