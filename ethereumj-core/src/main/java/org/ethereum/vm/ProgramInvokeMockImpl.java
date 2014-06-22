package org.ethereum.vm;

import org.ethereum.core.ContractDetails;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.db.TrackDatabase;
import org.ethereum.manager.WorldManager;
import org.ethereum.trie.TrackTrie;
import org.spongycastle.util.encoders.Hex;

import java.util.Map;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 03/06/2014 15:00
 */

public class ProgramInvokeMockImpl implements ProgramInvoke {

    byte[] msgData;

    TrackTrie stateDB       = null;
    TrackDatabase chainDb   = null;
    TrackDatabase detaildDB = null;

    ContractDetails details = null;

    String ownerAddress;


    public ProgramInvokeMockImpl(byte[] msgDataRaw){
        this.msgData = msgDataRaw;
    }

    public ProgramInvokeMockImpl() {
    }

    /*           ADDRESS op         */
    public DataWord getOwnerAddress(){

        byte[] addr = Hex.decode(ownerAddress);
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

    /*           GAS op       */
    public DataWord getGas() {
        byte[] minGasPrice = Hex.decode("0F4240");
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


    @Override
    public DataWord getPrevHash() {
        byte[] prevHash = Hex.decode("961CB117ABA86D1E596854015A1483323F18883C2D745B0BC03E87F146D2BB1C");
        return new DataWord(prevHash);
    }

    @Override
    public DataWord getCoinbase() {
        byte[] coinBase = Hex.decode("E559DE5527492BCB42EC68D07DF0742A98EC3F1E");
        return new DataWord(coinBase);
    }

    @Override
    public DataWord getTimestamp() {
        long timestamp = 1401421348;
        return new DataWord(timestamp);
    }

    @Override
    public DataWord getNumber() {
        long number = 33;
        return new DataWord(number);
    }

    @Override
    public DataWord getDifficulty() {
        byte[] difficulty = Hex.decode("3ED290");
        return new DataWord(difficulty);
    }

    @Override
    public DataWord getGaslimit() {
        long gasLimit = 1000000;
        return new DataWord(gasLimit);
    }


    public void setStateDB(TrackTrie stateDB) {
        this.stateDB = stateDB;
    }

    public void setChainDb(TrackDatabase chainDb) {
        this.chainDb = chainDb;
    }

    public void setDetaildDB(TrackDatabase detaildDB) {
        this.detaildDB = detaildDB;
    }

    public void setDetails(ContractDetails details) {
        this.details = details;
    }

    public void setOwnerAddress(String ownerAddress) {
        this.ownerAddress = ownerAddress;
    }


    @Override
    public Map<DataWord, DataWord> getStorage() {
        if (details == null) return null;
        return details.getStorage();
    }

    @Override
    public TrackDatabase getDetaildDB() {
        return detaildDB;
    }

    @Override
    public TrackDatabase getChainDb() {
        return chainDb;
    }

    @Override
    public TrackTrie getStateDb() {
        return stateDB;
    }

    @Override
    public boolean byTransaction() {
        return true;
    }
}
