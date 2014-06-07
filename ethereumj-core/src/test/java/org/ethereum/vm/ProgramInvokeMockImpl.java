package org.ethereum.vm;

import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 03/06/2014 15:00
 */

public class ProgramInvokeMockImpl implements ProgramInvoke {

    byte[] msgData;


    public ProgramInvokeMockImpl(byte[] msgDataRaw){
        this.msgData = msgDataRaw;
    }

    public ProgramInvokeMockImpl() {
    }

    /*           ADDRESS op         */
    public DataWord getOwnerAddress(){

        byte[] cowPrivKey = HashUtil.sha3("cow".getBytes());
        byte[] addr = ECKey.fromPrivate(cowPrivKey).getAddress();

        return new DataWord(addr);
    }

    /*           BALANCE op         */
    public DataWord getBalance(){
        byte[] balance = Hex.decode("0DE0B6B3A7640000");
        return new DataWord(balance);
    }


    /*           ORIGIN op         */
    public DataWord getOriginAddress(){

        byte[] cowPrivKey = HashUtil.sha3("horse".getBytes());
        byte[] addr = ECKey.fromPrivate(cowPrivKey).getAddress();

        return new DataWord(addr);
    }

    /*           CALLER op         */
    public DataWord getCallerAddress(){

        byte[] cowPrivKey = HashUtil.sha3("monkey".getBytes());
        byte[] addr = ECKey.fromPrivate(cowPrivKey).getAddress();

        return new DataWord(addr);
    }


    /*           GASPRICE op       */
    public DataWord getMinGasPrice(){

        byte[] minGasPrice = Hex.decode("09184e72a000");
        return new DataWord(minGasPrice);
    }


    /*          CALLVALUE op    */
    public DataWord getCallValue(){
        byte[] balance = Hex.decode("0DE0B6B3A7640000");
        return new DataWord(balance);
    }


      /*****************/
     /***  msg data ***/
    /*****************/

    /*     CALLDATALOAD  op   */
    public DataWord getDataValue(DataWord indexData){

        byte[] data = new byte[32];

        int index = indexData.value().intValue();
        int size = 32;

        if (msgData == null) return new DataWord(data);
        if (index > msgData.length) return new DataWord(data);
        if (index + 32 > msgData.length) size = msgData.length - index ;

        System.arraycopy(msgData, index, data, 0, size);

        return new DataWord(data);
    }

    /*  CALLDATASIZE */
    public DataWord getDataSize(){

        if (msgData == null || msgData.length == 0) return new DataWord(new byte[32]);
        int size = msgData.length;
        return new DataWord(size);
    }

    /*  CALLDATACOPY */
    public byte[] getDataCopy(DataWord offsetData, DataWord lengthData){

        int offset = offsetData.value().intValue();
        int length = lengthData.value().intValue();

        byte[] data = new byte[length];

        if (msgData == null) return data;
        if (offset > msgData.length) return data;
        if (offset + length > msgData.length) length = msgData.length - offset ;

        System.arraycopy(msgData, offset, data, 0, length);

        return data;
    }

}
