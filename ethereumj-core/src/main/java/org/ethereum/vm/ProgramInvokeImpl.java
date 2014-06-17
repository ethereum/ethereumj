package org.ethereum.vm;

import org.ethereum.db.TrackDatabase;
import org.ethereum.trie.TrackTrie;
import org.ethereum.util.ByteUtil;

import java.util.Arrays;
import java.util.Map;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 03/06/2014 15:00
 */

public class  ProgramInvokeImpl implements ProgramInvoke {

    /*** TRANSACTION  env ***/
    DataWord address;
    DataWord origin;
    DataWord caller;
    DataWord balance;
    DataWord gas;
    DataWord gasPrice;
    DataWord callValue;

    byte[] msgData;

    /*** BLOCK  env ***/
    DataWord prevHash;
    DataWord coinbase;
    DataWord timestamp;
    DataWord number;
    DataWord difficulty;
    DataWord gaslimit;

    Map<DataWord, DataWord> storage;

    TrackDatabase detaildDB;
    TrackDatabase chainDb;
    TrackTrie stateDb;
    private boolean byTransaction = true;

    public ProgramInvokeImpl(DataWord address, DataWord origin, DataWord caller, DataWord balance,
                             DataWord gasPrice, DataWord gas, DataWord callValue, byte[] msgData,
                             DataWord lastHash, DataWord coinbase, DataWord timestamp, DataWord number, DataWord difficulty,
                             DataWord gaslimit, Map<DataWord, DataWord> storage,
                             TrackDatabase detaildDB, TrackDatabase chainDb, TrackTrie stateDB) {

        // Transaction env
        this.address   = address;
        this.origin    = origin;
        this.caller    = caller;
        this.balance   = balance;
        this.gasPrice  = gasPrice;
        this.gas       = gas;
        this.callValue = callValue;
        this.msgData = msgData;

        // last Block env
        this.prevHash = lastHash;
        this.coinbase = coinbase;
        this.timestamp = timestamp;
        this.number = number;
        this.difficulty = difficulty;
        this.gaslimit   = gaslimit;

        this.storage = storage;

        this.detaildDB = detaildDB;
        this.chainDb = chainDb;
        this.stateDb = stateDB;
        this.byTransaction = false;

    }


    public ProgramInvokeImpl(byte[] address, byte[] origin, byte[] caller, byte[] balance,
                             byte[] gasPrice, byte[] gas, byte[] callValue, byte[] msgData,
                             byte[] lastHash, byte[] coinbase, long timestamp, long number, byte[] difficulty,
                             long gaslimit, Map<DataWord, DataWord> storage,
                             TrackDatabase detaildDB, TrackDatabase chainDb, TrackTrie stateDB) {

        // Transaction env
        this.address   = new DataWord(address);
        this.origin    = new DataWord(origin);
        this.caller    = new DataWord(caller);
        this.balance   = new DataWord(balance);
        this.gasPrice  = new DataWord(gasPrice);
        this.gas       = new DataWord(gas);
        this.callValue = new DataWord(callValue);
        this.msgData = msgData;

        // last Block env
        this.prevHash = new DataWord(lastHash);
        this.coinbase = new DataWord(coinbase);
        this.timestamp = new DataWord(timestamp);
        this.number = new DataWord(number);
        this.difficulty = new DataWord(difficulty);
        this.gaslimit   = new DataWord(gaslimit);

        this.storage = storage;

        this.detaildDB = detaildDB;
        this.chainDb = chainDb;
        this.stateDb = stateDB;
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

    /*           GAS op       */
    public DataWord getGas(){
        return gas;
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


    /*     PREVHASH op    */
    public DataWord getPrevHash() {
        return prevHash;
    }

    /*     COINBASE op    */
    public DataWord getCoinbase() {
        return coinbase;
    }

    /*     TIMESTAMP op    */
    public DataWord getTimestamp() {
        return timestamp;
    }

    /*     NUMBER op    */
    public DataWord getNumber() {
        return number;
    }

    /*     DIFFICULTY op    */
    public DataWord getDifficulty() {
        return difficulty;
    }

    /*     GASLIMIT op    */
    public DataWord getGaslimit() {
        return gaslimit;
    }

    /*  Storage */
    public Map<DataWord, DataWord> getStorage(){ return storage; }


    public TrackDatabase getDetaildDB() {
        return detaildDB;
    }

    public TrackDatabase getChainDb() {
        return chainDb;
    }

    public TrackTrie getStateDb() {
        return stateDb;
    }

    @Override
    public boolean byTransaction() {
        return byTransaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProgramInvokeImpl that = (ProgramInvokeImpl) o;

        if (byTransaction != that.byTransaction) return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (balance != null ? !balance.equals(that.balance) : that.balance != null) return false;
        if (callValue != null ? !callValue.equals(that.callValue) : that.callValue != null) return false;
        if (caller != null ? !caller.equals(that.caller) : that.caller != null) return false;
        if (chainDb != null ? !chainDb.equals(that.chainDb) : that.chainDb != null) return false;
        if (coinbase != null ? !coinbase.equals(that.coinbase) : that.coinbase != null) return false;
        if (detaildDB != null ? !detaildDB.equals(that.detaildDB) : that.detaildDB != null) return false;
        if (difficulty != null ? !difficulty.equals(that.difficulty) : that.difficulty != null) return false;
        if (gas != null ? !gas.equals(that.gas) : that.gas != null) return false;
        if (gasPrice != null ? !gasPrice.equals(that.gasPrice) : that.gasPrice != null) return false;
        if (gaslimit != null ? !gaslimit.equals(that.gaslimit) : that.gaslimit != null) return false;
        if (!Arrays.equals(msgData, that.msgData)) return false;
        if (number != null ? !number.equals(that.number) : that.number != null) return false;
        if (origin != null ? !origin.equals(that.origin) : that.origin != null) return false;
        if (prevHash != null ? !prevHash.equals(that.prevHash) : that.prevHash != null) return false;
        if (stateDb != null ? !stateDb.equals(that.stateDb) : that.stateDb != null) return false;
        if (storage != null ? !storage.equals(that.storage) : that.storage != null) return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + (origin != null ? origin.hashCode() : 0);
        result = 31 * result + (caller != null ? caller.hashCode() : 0);
        result = 31 * result + (balance != null ? balance.hashCode() : 0);
        result = 31 * result + (gas != null ? gas.hashCode() : 0);
        result = 31 * result + (gasPrice != null ? gasPrice.hashCode() : 0);
        result = 31 * result + (callValue != null ? callValue.hashCode() : 0);
        result = 31 * result + (msgData != null ? Arrays.hashCode(msgData) : 0);
        result = 31 * result + (prevHash != null ? prevHash.hashCode() : 0);
        result = 31 * result + (coinbase != null ? coinbase.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (difficulty != null ? difficulty.hashCode() : 0);
        result = 31 * result + (gaslimit != null ? gaslimit.hashCode() : 0);
        result = 31 * result + (storage != null ? storage.hashCode() : 0);
        result = 31 * result + (detaildDB != null ? detaildDB.hashCode() : 0);
        result = 31 * result + (chainDb != null ? chainDb.hashCode() : 0);
        result = 31 * result + (stateDb != null ? stateDb.hashCode() : 0);
        result = 31 * result + (byTransaction ? 1 : 0);
        return result;
    }
}
