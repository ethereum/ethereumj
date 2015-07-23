package org.ethereum.vmtrace;

import org.ethereum.config.SystemProperties;
import org.ethereum.db.ContractDetails;
import org.ethereum.db.RepositoryTrack;
import org.ethereum.facade.Repository;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.OpCode;
import org.ethereum.vm.ProgramInvoke;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.lang.String.format;
import static org.ethereum.util.ByteUtil.toHexString;
import static org.ethereum.vmtrace.Serializers.serializeFieldsOnly;

public class ProgramTrace {

    private static final Logger LOGGER = LoggerFactory.getLogger("vm");

    private List<Op> ops = new ArrayList<>();
    private String result;
    private String error;
    private Map<String, String> initStorage = new HashMap<>();

    public List<Op> getOps() {
        return ops;
    }

    public void setOps(List<Op> ops) {
        this.ops = ops;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Map<String, String> getInitStorage() {
        return initStorage;
    }

    public void setInitStorage(Map<String, String> initStorage) {
        this.initStorage = initStorage;
    }

    public ProgramTrace initStorage(ProgramInvoke programInvoke) {
        if (SystemProperties.CONFIG.vmTrace()) {
            for (Map.Entry<DataWord, DataWord> entry : getStorage(programInvoke).entrySet()) {
                initStorage.put(entry.getKey().toString(), entry.getValue().toString());
            }
            if (!initStorage.isEmpty()) {
                LOGGER.info("{} entries loaded to transaction's initStorage", initStorage.size());
            }
        }

        return this;
    }

    private static Map<DataWord, DataWord> getStorage(ProgramInvoke programInvoke) {
        Repository repository = programInvoke.getRepository();
        if (repository instanceof RepositoryTrack) {
            repository = ((RepositoryTrack) repository).getOriginRepository();
        }

        byte[] address = programInvoke.getOwnerAddress().getLast20Bytes();
        ContractDetails details = repository.getContractDetails(address);

        return (details == null) ? Collections.EMPTY_MAP : details.getStorage();
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
        op.setGas(gas.value());
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

    public String asJsonString(boolean formatted) {
        return serializeFieldsOnly(this, formatted);
    }

    @Override
    public String toString() {
        return asJsonString(true);
    }
}
