/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.vm.program.invoke;

import org.ethereum.core.Repository;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.db.RepositoryRoot;
import org.ethereum.db.BlockStore;
import org.ethereum.db.BlockStoreDummy;
import org.ethereum.vm.DataWord;

import org.spongycastle.util.encoders.Hex;

/**
 * @author Roman Mandeleil
 * @since 03.06.2014
 */
public class ProgramInvokeMockImpl implements ProgramInvoke {

    private byte[] msgData;

    private Repository repository;
    private Repository origRepository;
    private byte[] ownerAddress = Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826");
    private final byte[] contractAddress = Hex.decode("471fd3ad3e9eeadeec4608b92d16ce6b500704cc");

    // default for most tests. This can be overwritten by the test
    private long gasLimit = 1000000;

    public ProgramInvokeMockImpl(byte[] msgDataRaw) {
        this();
        this.msgData = msgDataRaw;
    }

    public ProgramInvokeMockImpl() {


        this.repository = new RepositoryRoot(new HashMapDB<byte[]>());
        this.repository.createAccount(ownerAddress);

        this.repository.createAccount(contractAddress);
        this.repository.saveCode(contractAddress,
                Hex.decode("385E60076000396000605f556014600054601e60"
                        + "205463abcddcba6040545b51602001600a525451"
                        + "6040016014525451606001601e52545160800160"
                        + "28525460a052546016604860003960166000f260"
                        + "00603f556103e75660005460005360200235"));
        this.origRepository = this.repository.clone();
    }

    public ProgramInvokeMockImpl(boolean defaults) {


    }

    /*           ADDRESS op         */
    public DataWord getOwnerAddress() {
        return DataWord.of(ownerAddress);
    }

    /*           BALANCE op         */
    public DataWord getBalance() {
        byte[] balance = Hex.decode("0DE0B6B3A7640000");
        return DataWord.of(balance);
    }

    /*           ORIGIN op         */
    public DataWord getOriginAddress() {

        byte[] cowPrivKey = HashUtil.sha3("horse".getBytes());
        byte[] addr = ECKey.fromPrivate(cowPrivKey).getAddress();

        return DataWord.of(addr);
    }

    /*           CALLER op         */
    public DataWord getCallerAddress() {

        byte[] cowPrivKey = HashUtil.sha3("monkey".getBytes());
        byte[] addr = ECKey.fromPrivate(cowPrivKey).getAddress();

        return DataWord.of(addr);
    }

    /*           GASPRICE op       */
    public DataWord getMinGasPrice() {

        byte[] minGasPrice = Hex.decode("09184e72a000");
        return DataWord.of(minGasPrice);
    }

    /*           GAS op       */
    public DataWord getGas() {

        return DataWord.of(gasLimit);
    }

    @Override
    public long getGasLong() {
        return gasLimit;
    }

    public void setGas(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    /*          CALLVALUE op    */
    public DataWord getCallValue() {
        byte[] balance = Hex.decode("0DE0B6B3A7640000");
        return DataWord.of(balance);
    }

    /*****************/
    /***  msg data ***/
    /**
     * *************
     */

    /*     CALLDATALOAD  op   */
    public DataWord getDataValue(DataWord indexData) {

        byte[] data = new byte[32];

        int index = indexData.value().intValue();
        int size = 32;

        if (msgData == null) return DataWord.of(data);
        if (index > msgData.length) return DataWord.of(data);
        if (index + 32 > msgData.length) size = msgData.length - index;

        System.arraycopy(msgData, index, data, 0, size);

        return DataWord.of(data);
    }

    /*  CALLDATASIZE */
    public DataWord getDataSize() {

        if (msgData == null || msgData.length == 0) return DataWord.of(new byte[32]);
        int size = msgData.length;
        return DataWord.of(size);
    }

    /*  CALLDATACOPY */
    public byte[] getDataCopy(DataWord offsetData, DataWord lengthData) {

        int offset = offsetData.value().intValue();
        int length = lengthData.value().intValue();

        byte[] data = new byte[length];

        if (msgData == null) return data;
        if (offset > msgData.length) return data;
        if (offset + length > msgData.length) length = msgData.length - offset;

        System.arraycopy(msgData, offset, data, 0, length);

        return data;
    }

    @Override
    public DataWord getPrevHash() {
        byte[] prevHash = Hex.decode("961CB117ABA86D1E596854015A1483323F18883C2D745B0BC03E87F146D2BB1C");
        return DataWord.of(prevHash);
    }

    @Override
    public DataWord getCoinbase() {
        byte[] coinBase = Hex.decode("E559DE5527492BCB42EC68D07DF0742A98EC3F1E");
        return DataWord.of(coinBase);
    }

    @Override
    public DataWord getTimestamp() {
        long timestamp = 1401421348;
        return DataWord.of(timestamp);
    }

    @Override
    public DataWord getNumber() {
        long number = 33;
        return DataWord.of(number);
    }

    @Override
    public DataWord getDifficulty() {
        byte[] difficulty = Hex.decode("3ED290");
        return DataWord.of(difficulty);
    }

    @Override
    public DataWord getGaslimit() {
        return DataWord.of(gasLimit);
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public void setOwnerAddress(byte[] ownerAddress) {
        this.ownerAddress = ownerAddress;
    }

    @Override
    public boolean byTransaction() {
        return true;
    }

    @Override
    public boolean isStaticCall() {
        return false;
    }

    @Override
    public boolean byTestingSuite() {
        return false;
    }

    @Override
    public Repository getRepository() {
        return this.repository;
    }

    @Override
    public Repository getOrigRepository() {
        return this.origRepository;
    }

    @Override
    public BlockStore getBlockStore() {
        return new BlockStoreDummy();
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public void setOrigRepository(Repository repository) {
        this.origRepository = repository.clone();
    }

    @Override
    public int getCallDeep() {
        return 0;
    }
}
