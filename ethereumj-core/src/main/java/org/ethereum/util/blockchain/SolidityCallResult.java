package org.ethereum.util.blockchain;

import org.ethereum.core.CallTransaction;
import org.ethereum.vm.LogInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 26.07.2016.
 */
public class SolidityCallResult extends TransactionResult {
    CallTransaction.Contract contract;
    CallTransaction.Function function;

    SolidityCallResult(CallTransaction.Contract contract, CallTransaction.Function function) {
        this.contract = contract;
        this.function = function;
    }

    public Object getReturnValue() {
        Object[] returnValues = getReturnValues();
        return isIncluded() && returnValues.length > 0 ? returnValues[0] : null;
    }

    public Object[] getReturnValues() {
        if (!isIncluded()) return null;
        byte[] executionResult = getReceipt().getExecutionResult();
        return function.decodeResult(executionResult);
    }

    public boolean isSuccessful() {
        return isIncluded() && getReceipt().isSuccessful();
    }

    public List<CallTransaction.Invocation> getEvents() {
        List<CallTransaction.Invocation> ret = new ArrayList<>();
        for (LogInfo logInfo : getReceipt().getLogInfoList()) {
            CallTransaction.Invocation event = contract.parseEvent(logInfo);
            if (event != null) ret.add(event);
        }
        return ret;
    }

    @Override
    public String toString() {
        String ret = "SolidityCallResult{" +
                function + ": " +
                (isIncluded() ? "EXECUTED" : "PENDING") + ", ";
        if (isIncluded()) {
            ret += isSuccessful() ? "SUCCESS" : ("ERR (" + getReceipt().getError() + ")");
            ret += ", ";
            if (isSuccessful()) {
                ret += "Ret: " + Arrays.toString(getReturnValues()) + ", ";
                ret += "Events: " + getEvents() + ", ";
            }
        }
        return ret + "}";
    }
}
