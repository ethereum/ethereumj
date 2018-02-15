package org.ethereum.vm.trace;

import java.util.ArrayList;
import java.util.List;

import org.ethereum.vm.DataWord;

/**
 * Null implementation of the {@link ProgramTrace} interface to be used if
 * program trace is not used
 *
 */
public class NullProgramTrace implements ProgramTrace {

    @Override
    public List<Op> getOps() {
	return new ArrayList<>();
    }

    @Override
    public void setOps(List<Op> ops) {
	// blank intentionally
    }

    @Override
    public String getResult() {
	return new String();
    }

    @Override
    public void setResult(String result) {
	// blank intentionally
    }

    @Override
    public String getError() {
	return new String();
    }

    @Override
    public void setError(String error) {
	// blank intentionally
    }

    @Override
    public String getContractAddress() {
	return new String();
    }

    @Override
    public void setContractAddress(String contractAddress) {
	// blank intentionally
    }

    @Override
    public ProgramTrace result(byte[] result) {
	return this;
    }

    @Override
    public ProgramTrace error(Exception error) {
	return this;
    }

    @Override
    public Op addOp(byte code, int pc, int deep, DataWord gas, OpActions actions) {
	return new Op();
    }

    @Override
    public void merge(ProgramTrace programTrace) {
	// blank intentionally
    }

    @Override
    public String asJsonString(boolean formatted) {
	return "{}";
    }

    @Override
    public String toString() {
        return asJsonString(false);
    }
}
