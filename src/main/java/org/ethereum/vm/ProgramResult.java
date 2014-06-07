package org.ethereum.vm;

import java.nio.ByteBuffer;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 07/06/2014 17:45
 */

public class ProgramResult {

    private int gasUsed = 0;
    private ByteBuffer  hReturn = null;
    private RuntimeException exception;

    public void spendGas(int gas){
        gasUsed += gas;
    }

    public void setHReturn(byte[] hReturn){

        this.hReturn = ByteBuffer.allocate(hReturn.length);
        this.hReturn.put(hReturn);
    }

    public ByteBuffer gethReturn() {
        return hReturn;
    }

    public RuntimeException getException() {
        return exception;
    }

    public void setException(RuntimeException exception) {
        this.exception = exception;
    }


}
