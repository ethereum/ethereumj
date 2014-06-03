package org.ethereum.vm;

import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.ByteBuffer;

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

    // todo: add can be done in more efficient way
    // todo      without BigInteger quick hack
    public void add2(DataWord word){

        BigInteger result = value().add( word.value() );
        byte[] bytes = result.toByteArray();

        ByteBuffer data    =  ByteBuffer.allocate(32);
        System.arraycopy(bytes, 0, data.array(), 32 - bytes.length, bytes.length);
        this.data = data.array();
    }
    
    // By	: Holger
    // From	: http://stackoverflow.com/a/24023466/459349
    public void add(DataWord word) {
		if (this.data.length != 32 || word.data.length != 32)
			throw new IllegalArgumentException();
		byte[] result = new byte[32];
		for (int i = 31, overflow = 0; i >= 0; i--) {
			int v = (this.data[i] & 0xff) + (word.data[i] & 0xff) + overflow;
			result[i] = (byte) v;
			overflow = v >>> 8;
		}
		this.data = result;
    }

    // todo: mull can be done in more efficient way
    // todo:     with shift left shift right trick
    // todo      without BigInteger quick hack
    public void mull(DataWord word){

        BigInteger result = value().multiply( word.value() );
        byte[] bytes = result.toByteArray();

        ByteBuffer data    =  ByteBuffer.allocate(32);
        System.arraycopy(bytes, 0, data.array(), 32 - bytes.length, bytes.length);
        this.data = data.array();
    }

    // todo: improve with no BigInteger
    public void div(DataWord word){

        if (word.isZero()){
            this.and(ZERO);
            return;
        }

        BigInteger result = value().divide( word.value() );
        byte[] bytes = result.toByteArray();

        ByteBuffer data    =  ByteBuffer.allocate(32);
        System.arraycopy(bytes, 0, data.array(), 32 - bytes.length, bytes.length);
        this.data = data.array();
    }

    // todo: improve with no BigInteger
    public void sub(DataWord word){

        BigInteger result = value().subtract( word.value() );
        byte[] bytes = result.toByteArray();

        ByteBuffer data    =  ByteBuffer.allocate(32);
        System.arraycopy(bytes, 0, data.array(), 32 - bytes.length, bytes.length);
        this.data = data.array();
    }

    // todo: improve with no BigInteger
    public void exp(DataWord word){

        BigInteger result = value().pow( word.value().intValue() );
        byte[] bytes = result.toByteArray();

        ByteBuffer data    =  ByteBuffer.allocate(32);
        System.arraycopy(bytes, 0, data.array(), 32 - bytes.length, bytes.length);
        this.data = data.array();
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
