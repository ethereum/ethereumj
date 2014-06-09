package org.ethereum.vm;

import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 03/06/2014 15:00
 */

public class    ProgramInvokeImpl implements ProgramInvoke {

    DataWord address;
    DataWord origin;
    DataWord caller;
    DataWord balance;
    DataWord gas;
    DataWord gasPrice;
    DataWord callValue;

    byte[] msgData;


    public ProgramInvokeImpl(byte[] msgDataRaw){
        this.msgData = msgDataRaw;
    }

    public ProgramInvokeImpl(byte[] address, byte[] origin, byte[] caller, byte[] balance,
                             byte[] gasPrice, byte[] gas, byte[] callValue, byte[] msgData) {

        this.address   = new DataWord(address);
        this.origin    = new DataWord(origin);
        this.caller    = new DataWord(caller);
        this.balance   = new DataWord(balance);
        this.gasPrice  = new DataWord(gasPrice);
        this.gas       = new DataWord(gas);
        this.callValue = new DataWord(callValue);

        this.msgData = msgData;
    }

    /*           ADDRESS op         */
    public DataWord getOwnerAddress(){

        return address;
    }

    /*           BALANCE op         */
    public DataWord getBalance(){
        return balance;
    }

    /*           ORIGIN op         */
    public DataWord getOriginAddress(){
        return origin;
    }

    /*           CALLER op         */
    public DataWord getCallerAddress(){
        return caller;
    }

    /*           GASPRICE op       */
    public DataWord getMinGasPrice(){
        return gasPrice;
    }


    /*          CALLVALUE op    */
    public DataWord getCallValue(){
        return callValue;
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
