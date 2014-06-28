package org.ethereum.vm;

import org.ethereum.db.Repository;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 03/06/2014 14:59
 */
public interface ProgramInvoke {

    public DataWord getOwnerAddress();
    public DataWord getBalance();
    public DataWord getOriginAddress();
    public DataWord getCallerAddress();
    public DataWord getMinGasPrice();
    public DataWord getGas();
    public DataWord getCallValue();
    public DataWord getDataSize();
    public DataWord getDataValue(DataWord indexData);
    public byte[]   getDataCopy(DataWord offsetData, DataWord lengthData);

    public DataWord getPrevHash();
    public DataWord getCoinbase();
    public DataWord getTimestamp();
    public DataWord getNumber();
    public DataWord getDifficulty();
    public DataWord getGaslimit();

    public Repository getRepository();

    public boolean byTransaction();
}
