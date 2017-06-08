package org.ethereum.util.blockchain;

import org.ethereum.core.CallTransaction;
import org.ethereum.vm.LogInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Arsalan on 2017-04-21.
 */
public class SolidityCallResultImpl extends SolidityCallResult {
    SolidityContractImpl contract;
    CallTransaction.Function function;

    SolidityCallResultImpl(SolidityContractImpl contract, CallTransaction.Function function) {
        this.contract = contract;
        this.function = function;
    }

    @Override
    public CallTransaction.Function getFunction() {
        return function;
    }

    public List<CallTransaction.Invocation> getEvents() {
        List<CallTransaction.Invocation> ret = new ArrayList<>();
        for (LogInfo logInfo : getReceipt().getLogInfoList()) {
            for (CallTransaction.Contract c : contract.relatedContracts) {
                CallTransaction.Invocation event = c.parseEvent(logInfo);
                if (event != null) ret.add(event);
            }
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

