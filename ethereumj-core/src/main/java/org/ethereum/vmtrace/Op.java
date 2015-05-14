package org.ethereum.vmtrace;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.OpCode;

public class Op {

    private OpCode code;
    private int deep;
    private int pc;
    @JsonSerialize(using = Serializers.DataWordSerializer.class)
    private DataWord gas;
    private OpActions actions;

    public void setCode(OpCode code) {
        this.code = code;
    }

    public void setDeep(int deep) {
        this.deep = deep;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }

    public void setGas(DataWord gas) {
        this.gas = gas;
    }

    public void setActions(OpActions actions) {
        this.actions = actions;
    }
}
