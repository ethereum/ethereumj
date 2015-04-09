package org.ethereum.vmtrace;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.ethereum.vmtrace.Serializers.serializeFieldsOnly;

/**
 * @author Roman Mandeleil
 * @since 28.10.2014
 */
public class ProgramTrace {

    private static final Logger LOGGER = LoggerFactory.getLogger("vmtrace");
    @JsonIgnore
    private byte[] txHash;
    private List<Op> ops = new ArrayList<>();
    private String result;
    private String error;

    public void setTxHash(byte[] txHash) {
        this.txHash = txHash;
    }

    public void setResult(ByteBuffer result) {
        this.result = Hex.toHexString(result.array());
    }

    public void setError(Exception error) {
        this.error = (error == null) ? "" : format("%s: %s", error.getClass(), error.getMessage());
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

    public String asJsonString() {
        return serializeFieldsOnly(this, true);
    }
}
