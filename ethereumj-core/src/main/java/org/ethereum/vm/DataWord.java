package org.ethereum.vm;

import org.ethereum.util.ByteUtil;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * DataWord is the 32-byte array representation of a 256-bit number
 * Calculations can be done on this word with other DataWords
 *
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 01/06/2014 10:45
 */
public class DataWord {

    public static final DataWord ZERO = new DataWord(new byte[32]);      // don't push it in to the stack

    byte[] data = new byte[32];

	public DataWord() {
		data = new byte[32];
	}

	public DataWord(int num) {
		ByteBuffer bInt = ByteBuffer.allocate(4).putInt(num);
		ByteBuffer data = ByteBuffer.allocate(32);
		System.arraycopy(bInt.array(), 0, data.array(), 28, 4);
		this.data = data.array();
	}

    public DataWord(long num) {
        ByteBuffer bLong = ByteBuffer.allocate(8).putLong(num);
        ByteBuffer data = ByteBuffer.allocate(32);
        System.arraycopy(bLong.array(), 0, data.array(), 24, 8);
        this.data = data.array();
    }


	public DataWord(byte[] data) {

        if (data == null){
            this.data = new byte[]{};
            return;
        }

		if (data.length > 32)
			throw new RuntimeException("Data word can't exit 32 bytes: " + data);
		System.arraycopy(data, 0, this.data, 32 - data.length, data.length);
	}

    public byte[] getData() {
        return data;
    }

    public byte[] getNoLeadZeroesData() {
        return ByteUtil.stripLeadingZeroes(data);
    }

    public BigInteger value(){
        return new BigInteger(1, data);
    }
    public int intValue(){
        return new BigInteger(1, data).intValue();
    }
    public long longValue(){
        return new BigInteger(1, data).longValue();
    }

    public BigInteger sValue(){
        return new BigInteger(data);
    }


    public boolean isZero(){

        byte result = 0;
        for (byte tmp : data){
            result |= tmp;
        }
        return result == 0;
    }

    // only in case of signed operation
    // when the number is explicit defined
    // as negative
    public boolean isNegative(){
        int result = data[0] & 0x80;
        return result == 0x80;
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

    // By	: Holger
    // From	: http://stackoverflow.com/a/24023466/459349
    public void add(DataWord word) {
		byte[] result = new byte[32];
		for (int i = 31, overflow = 0; i >= 0; i--) {
			int v = (this.data[i] & 0xff) + (word.data[i] & 0xff) + overflow;
			result[i] = (byte) v;
			overflow = v >>> 8;
		}
		this.data = result;
    }
    
    // old add-method with BigInteger quick hack
    public void add2(DataWord word){

        BigInteger result = value().add( word.value() );
        byte[] bytes = result.toByteArray();

        ByteBuffer data    =  ByteBuffer.allocate(32);
        System.arraycopy(bytes, 0, data.array(), 32 - bytes.length, bytes.length);
        this.data = data.array();
    }
    
    // todo: mul can be done in more efficient way
    // todo:     with shift left shift right trick
    // todo      without BigInteger quick hack
    public void mul(DataWord word){

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
    public void sDiv(DataWord word){

        if (word.isZero()){
            this.and(ZERO);
            return;
        }

        BigInteger result = sValue().divide( word.sValue() );
        byte[] bytes = result.toByteArray();

        ByteBuffer data    =  ByteBuffer.allocate(32);
        if (result.compareTo(BigInteger.ZERO) == -1)
            Arrays.fill(data.array(), (byte) 0xFF);

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

    // todo: improve with no BigInteger
    public void mod(DataWord word){

        if (word.isZero()){
            this.and(ZERO);
            return;
        }

        BigInteger result = value().mod(word.value());
        byte[] bytes = result.toByteArray();

        ByteBuffer data    =  ByteBuffer.allocate(32);
        System.arraycopy(bytes, 0, data.array(), 32 - bytes.length, bytes.length);
        this.data = data.array();
    }

    // todo: improve with no BigInteger
    public void sMod(DataWord word){

        if (word.isZero() || word.isNegative()){
            this.and(ZERO);
            return;
        }

        BigInteger result = sValue().mod( word.sValue());
        byte[] bytes = result.toByteArray();

        ByteBuffer data    =  ByteBuffer.allocate(32);
        if (result.compareTo(BigInteger.ZERO) == -1)
            Arrays.fill(data.array(), (byte) 0xFF);

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
