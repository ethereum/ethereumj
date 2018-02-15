package org.ethereum.vm.trace;

import java.util.List;

import org.ethereum.vm.DataWord;

public interface ProgramTrace {

    List<Op> getOps();

    void setOps(List<Op> ops);

    String getResult();

    void setResult(String result);

    String getError();

    void setError(String error);

    String getContractAddress();

    void setContractAddress(String contractAddress);

    ProgramTrace result(byte[] result);

    ProgramTrace error(Exception error);

    Op addOp(byte code, int pc, int deep, DataWord gas, OpActions actions);

    void merge(ProgramTrace programTrace);

    String asJsonString(boolean formatted);

}