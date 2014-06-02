package org.ethereum.vm;

import com.fasterxml.jackson.databind.ser.std.ArraySerializerBase;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import static java.util.Arrays.*;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 01/06/2014 19:47
 */

public class DataWord {

    static DataWord ZERO = new DataWord(new byte[32]);      // don't push it in to the stack

    byte[] data = new byte[32];

    public DataWord(){
        data = new byte[32];
    }

    public DataWord(int num){
        ByteBuffer bInt   = ByteBuffer.allocate(4).putInt(num);
        ByteBuffer data    =  ByteBuffer.allocate(32);
        System.arraycopy(bInt.array(), 0, data.array(), 28, 4);
        this.data = data.array();
    }

    public DataWord(byte[] data){
        if (data == null || data.length > 32)
            throw new RuntimeException("bad push data: " +  data);

        System.arraycopy(data, 0, this.data, 32 - data.length,  data.length);
    }

    public byte[] getData() {
        return data;
    }

    public BigInteger value(){
        return new BigInteger(1, data);
    }

    public boolean isZero(){

        byte result = 0;
        for (byte tmp : data){
            result |= tmp;
        }
        return result == 0;
    }

    public DataWord and(DataWord w2){

        for (int i = 0; i < this.data.length; ++i){
            this.data[i] &= w2.data[i];
        }

        return this;
    }

    public DataWord or(DataWord w2){

        for (int i = 0; i < this.data.length; ++i){
            this.data[i] |= w2.data[i];
        }

        return this;
    }

    public DataWord xor(DataWord w2){

        for (int i = 0; i < this.data.length; ++i){
            this.data[i] ^= w2.data[i];
        }

        return this;
    }

    public void negate(){

        if (this.isZero()) return;

        for (int i = 0; i < this.data.length; ++i){
            this.data[i] = (byte) ~this.data[i];
        }

        for (int i = this.data.length - 1; i >= 0 ; --i){

            this.data[i] = (byte)  (1 + this.data[i] & 0xFF);
            if (this.data[i] != 0) break;
        }
    }


    public String toString(){
        return Hex.toHexString(data);
    }

    public DataWord clone(){
        return new DataWord(Arrays.clone(data));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataWord dataWord = (DataWord) o;

        if (!java.util.Arrays.equals(data, dataWord.data)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return java.util.Arrays.hashCode(data);
    }
}
