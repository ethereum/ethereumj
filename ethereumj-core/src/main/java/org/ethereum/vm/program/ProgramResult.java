/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.vm.program;

import org.ethereum.util.ByteArraySet;
import org.ethereum.vm.CallCreate;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private boolean revert;

    private Set<DataWord> deleteAccounts;
    private ByteArraySet touchedAccounts = new ByteArraySet();
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

    public void setRevert() {
        this.revert = true;
    }

    public boolean isRevert() {
        return revert;
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

    public Set<DataWord> getDeleteAccounts() {
        if (deleteAccounts == null) {
            deleteAccounts = new HashSet<>();
        }
        return deleteAccounts;
    }

    public void addDeleteAccount(DataWord address) {
        getDeleteAccounts().add(address);
    }

    public void addDeleteAccounts(Set<DataWord> accounts) {
        if (!isEmpty(accounts)) {
            getDeleteAccounts().addAll(accounts);
        }
    }

    public void addTouchAccount(byte[] addr) {
        touchedAccounts.add(addr);
    }

    public Set<byte[]> getTouchedAccounts() {
        return touchedAccounts;
    }

    public void addTouchAccounts(Set<byte[]> accounts) {
        if (!isEmpty(accounts)) {
            getTouchedAccounts().addAll(accounts);
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
        if (another.getException() == null && !another.isRevert()) {
            addDeleteAccounts(another.getDeleteAccounts());
            addLogInfos(another.getLogInfoList());
            addFutureRefund(another.getFutureRefund());
            addTouchAccounts(another.getTouchedAccounts());
        }
    }
    
    public static ProgramResult createEmpty() {
        ProgramResult result = new ProgramResult();
        result.setHReturn(EMPTY_BYTE_ARRAY);
        return result;
    }
}
