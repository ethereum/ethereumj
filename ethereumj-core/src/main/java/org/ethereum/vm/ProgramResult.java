package org.ethereum.vm;

import org.ethereum.db.RepositoryImpl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 07/06/2014 17:45
 */
public class ProgramResult {

    private long gasUsed = 0;
    private ByteBuffer  hReturn = null;
    private RuntimeException exception;
    private List<DataWord> deleteAccounts;

    private RepositoryImpl repository = null;

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

    public RepositoryImpl getRepository() {
        return repository;
    }

    public void setRepository(RepositoryImpl repository) {
        this.repository = repository;
    }

    public void addDeleteAccount(DataWord address){

        if (deleteAccounts == null){
            deleteAccounts = new ArrayList<>();
        }

        deleteAccounts.add(address);
    }

    public void addDeleteAccounts(List<DataWord> accounts){

        if (accounts == null) return;

        if (deleteAccounts == null){
            deleteAccounts = new ArrayList<>();
        }

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
}
