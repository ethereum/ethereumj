package org.ethereum.vm;

/**
 * @author Roman Mandeleil
 * @since 03.07.2014
 */
public class CallCreate {

    final byte[] data;
    final byte[] destination;
    final byte[] gasLimit;
    final byte[] value;


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
