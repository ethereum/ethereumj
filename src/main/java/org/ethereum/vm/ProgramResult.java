package org.ethereum.vm;

import org.ethereum.db.Repository;
import org.ethereum.db.TrackDatabase;
import org.ethereum.trie.TrackTrie;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 07/06/2014 17:45
 */

public class ProgramResult {

    private int gasUsed = 0;
    private ByteBuffer  hReturn = null;
    private RuntimeException exception;

    Repository repository = null;

    public void spendGas(int gas){
        gasUsed += gas;
    }

    public void setHReturn(byte[] hReturn){

        this.hReturn = ByteBuffer.allocate(hReturn.length);
        this.hReturn.put(hReturn);
    }

    public ByteBuffer getHReturn() {
        return hReturn;
    }

    public RuntimeException getException() {
        return exception;
    }

    public int getGasUsed() {
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
}
