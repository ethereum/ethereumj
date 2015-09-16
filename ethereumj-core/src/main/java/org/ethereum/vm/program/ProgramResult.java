package org.ethereum.vm.program;

import org.ethereum.vm.CallCreate;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.size;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;

/**
 * @author Roman Mandeleil
 * @since 07.06.2014
 */
public class ProgramResult {

    private long gasUsed;
    private byte[] hReturn = EMPTY_BYTE_ARRAY;
    private RuntimeException exception;

    private List<DataWord> deleteAccounts;
    private List<InternalTransaction> internalTransactions;
    private List<LogInfo> logInfoList;
    private long futureRefund = 0;

    /*
     * for testing runs ,
     * call/create is not executed
     * but dummy recorded
     */
    private List<CallCreate> callCreateList;

    public void spendGas(long gas) {
        gasUsed += gas;
    }

    public void refundGas(long gas) {
        gasUsed -= gas;
    }

    public void setHReturn(byte[] hReturn) {
        this.hReturn = hReturn;

    }

    public byte[] getHReturn() {
        return hReturn;
    }

    public RuntimeException getException() {
        return exception;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public void setException(RuntimeException exception) {
        this.exception = exception;
    }

    public List<DataWord> getDeleteAccounts() {
        if (deleteAccounts == null) {
            deleteAccounts = new ArrayList<>();
        }
        return deleteAccounts;
    }

    public void addDeleteAccount(DataWord address) {
        getDeleteAccounts().add(address);
    }

    public void addDeleteAccounts(List<DataWord> accounts) {
        if (!isEmpty(accounts)) {
            getDeleteAccounts().addAll(accounts);
        }
    }

    public List<LogInfo> getLogInfoList() {
        if (logInfoList == null) {
            logInfoList = new ArrayList<>();
        }
        return logInfoList;
    }

    public void addLogInfo(LogInfo logInfo) {
        getLogInfoList().add(logInfo);
    }

    public void addLogInfos(List<LogInfo> logInfos) {
        if (!isEmpty(logInfos)) {
            getLogInfoList().addAll(logInfos);
        }
    }

    public List<CallCreate> getCallCreateList() {
        if (callCreateList == null) {
            callCreateList = new ArrayList<>();
        }
        return callCreateList;
    }

    public void addCallCreate(byte[] data, byte[] destination, byte[] gasLimit, byte[] value) {
        getCallCreateList().add(new CallCreate(data, destination, gasLimit, value));
    }

    public List<InternalTransaction> getInternalTransactions() {
        if (internalTransactions == null) {
            internalTransactions = new ArrayList<>();
        }
        return internalTransactions;
    }

    public InternalTransaction addInternalTransaction(byte[] parentHash, int deep, byte[] nonce, DataWord gasPrice, DataWord gasLimit,
                                                      byte[] senderAddress, byte[] receiveAddress, byte[] value, byte[] data, String note) {
        InternalTransaction transaction = new InternalTransaction(parentHash, deep, size(internalTransactions), nonce, gasPrice, gasLimit, senderAddress, receiveAddress, value, data, note);
        getInternalTransactions().add(transaction);
        return transaction;
    }

    public void addInternalTransactions(List<InternalTransaction> internalTransactions) {
        getInternalTransactions().addAll(internalTransactions);
    }

    public void rejectInternalTransactions() {
        for (InternalTransaction internalTx : getInternalTransactions()) {
            internalTx.reject();
        }
    }

    public void addFutureRefund(long gasValue) {
        futureRefund += gasValue;
    }

    public long getFutureRefund() {
        return futureRefund;
    }

    public void resetFutureRefund() {
        futureRefund = 0;
    }

    public void merge(ProgramResult another) {
        addInternalTransactions(another.getInternalTransactions());
        addDeleteAccounts(another.getDeleteAccounts());
        addLogInfos(another.getLogInfoList());
        addFutureRefund(another.getFutureRefund());
    }
    
    public static ProgramResult empty() {
        ProgramResult result = new ProgramResult();
        result.setHReturn(EMPTY_BYTE_ARRAY);
        return result;
    }
}
