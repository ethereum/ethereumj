package org.ethereum.vm;

import org.ethereum.facade.Repository;

import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Mandeleil
 * @since 07.06.2014
 */
public class ProgramResult {

    private long gasUsed = 0;
    private ByteBuffer hReturn = null;
    private RuntimeException exception;
    private List<DataWord> deleteAccounts;
    private List<LogInfo> logInfoList;
    private long futureRefund = 0;

    private Repository repository = null;

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
        this.hReturn = ByteBuffer.allocate(hReturn.length);
        this.hReturn.put(hReturn);
    }

    public ByteBuffer getHReturn() {
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

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public void addDeleteAccount(DataWord address) {
        if (deleteAccounts == null)
            deleteAccounts = new ArrayList<>();
        deleteAccounts.add(address);
    }

    public void addLogInfo(LogInfo logInfo) {
        if (this.logInfoList == null) logInfoList = new ArrayList<>();
        this.logInfoList.add(logInfo);
    }

    public void addLogInfos(List<LogInfo> logInfos) {
        if (logInfos == null) return;
        if (this.logInfoList == null) logInfoList = new ArrayList<>();
        this.logInfoList.addAll(logInfos);
    }

    public List<LogInfo> getLogInfoList() {
        return logInfoList;
    }

    public void addDeleteAccounts(List<DataWord> accounts) {
        if (accounts == null) return;
        if (deleteAccounts == null)
            deleteAccounts = new ArrayList<>();
        deleteAccounts.addAll(accounts);
    }

    public List<DataWord> getDeleteAccounts() {
        return deleteAccounts;
    }

    public List<CallCreate> getCallCreateList() {
        return callCreateList;
    }

    public void addCallCreate(byte[] data, byte[] destination, byte[] gasLimit, byte[] value) {
        if (callCreateList == null)
            callCreateList = new ArrayList<>();
        callCreateList.add(new CallCreate(data, destination, gasLimit, value));
    }

    public void futureRefundGas(long gasValue) {
        futureRefund += gasValue;
    }

    public long getFutureRefund() {
        return futureRefund;
    }
}
