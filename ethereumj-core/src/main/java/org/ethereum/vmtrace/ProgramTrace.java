package org.ethereum.vmtrace;

import org.json.simple.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Mandeleil
 * @since 28.10.2014
 */
public class ProgramTrace {

    private byte[] txHash;
    private List<Op> ops = new ArrayList<>();

    public void setTxHash(byte[] txHash) {
        this.txHash = txHash;
    }

    public void addOp(Op op) {
        ops.add(op);
    }

    /**
     * Used for merging sub calls execution.
     */
    public void merge(ProgramTrace programTrace) {

        this.ops.addAll(programTrace.ops);
    }

    public String getJsonString() {
        return JSONArray.toJSONString(ops);
    }
}
