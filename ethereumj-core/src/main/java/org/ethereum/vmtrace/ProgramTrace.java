package org.ethereum.vmtrace;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.ethereum.db.ContractDetails;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.OpCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.ethereum.util.ByteUtil.toHexString;
import static org.ethereum.vmtrace.Serializers.serializeFieldsOnly;

public class ProgramTrace {

    @JsonIgnore
    private byte[] txHash;
    private List<Op> ops = new ArrayList<>();
    private String result;
    private String error;
    private Map<String, String> initStorage = new HashMap<>();

    public void setTxHash(byte[] txHash) {
        this.txHash = txHash;
    }

    public void setOps(List<Op> ops) {
        this.ops = ops;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setInitStorage(Map<String, String> initStorage) {
        this.initStorage = initStorage;
    }

    public ProgramTrace initStorage(ContractDetails details) {
        initStorage = new HashMap<>();
        for (Map.Entry<DataWord, DataWord> entry : details.getStorage().entrySet()) {
            initStorage.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return this;
    }
    
    public ProgramTrace result(byte[] result) {
        setResult(toHexString(result));
        return this;
    }

    public ProgramTrace error(Exception error) {
        setError(error == null ? "" : format("%s: %s", error.getClass(), error.getMessage()));
        return this;
    }

    public Op addOp(byte code, int pc, int deep, DataWord gas, OpActions actions) {
        Op op = new Op();
        op.setActions(actions);
        op.setCode(OpCode.code(code));
        op.setDeep(deep);
        op.setGas(gas);
        op.setPc(pc);
        
        ops.add(op);
        
        return op;
    }

    /**
     * Used for merging sub calls execution.
     */
    public void merge(ProgramTrace programTrace) {
        this.ops.addAll(programTrace.ops);
    }

    public String asJsonString(boolean needFormat) {
        return serializeFieldsOnly(this, true);
    }
}
