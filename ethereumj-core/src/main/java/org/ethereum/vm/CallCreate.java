package org.ethereum.vm;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 03/07/2014 08:29
 */

public class CallCreate {

    byte[] data;
    byte[] destination;
    byte[] gasLimit;
    byte[] value;


    public CallCreate(byte[] data, byte[] destination, byte[] gasLimit, byte[] value) {
        this.data = data;
        this.destination = destination;
        this.gasLimit = gasLimit;
        this.value = value;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getDestination() {
        return destination;
    }

    public byte[] getGasLimit() {
        return gasLimit;
    }

    public byte[] getValue() {
        return value;
    }
}
