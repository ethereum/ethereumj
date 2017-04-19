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
package org.ethereum.core;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.ContractDetails;
import org.ethereum.vm.DataWord;

import java.math.BigInteger;

import java.util.HashMap;
import java.util.Set;

/**
 * @author Roman Mandeleil
 * @since 08.09.2014
 */
public interface Repository extends org.ethereum.facade.Repository{

    /**
     * Create a new account in the database
     *
     * @param addr of the contract
     * @return newly created account state
     */
    AccountState createAccount(byte[] addr);


    /**
     * @param addr - account to check
     * @return - true if account exist,
     *           false otherwise
     */
    boolean isExist(byte[] addr);

    /**
     * Retrieve an account
     *
     * @param addr of the account
     * @return account state as stored in the database
     */
    AccountState getAccountState(byte[] addr);

    /**
     * Deletes the account
     *
     * @param addr of the account
     */
    void delete(byte[] addr);

    /**
     * Increase the account nonce of the given account by one
     *
     * @param addr of the account
     * @return new value of the nonce
     */
    BigInteger increaseNonce(byte[] addr);

    /**
     * Sets the account nonce of the given account
     *
     * @param addr of the account
     * @param nonce new nonce
     * @return new value of the nonce
     */
    BigInteger setNonce(byte[] addr, BigInteger nonce);

    /**
     * Get current nonce of a given account
     *
     * @param addr of the account
     * @return value of the nonce
     */
    BigInteger getNonce(byte[] addr);

    /**
     * Retrieve contract details for a given account from the database
     *
     * @param addr of the account
     * @return new contract details
     */
    ContractDetails getContractDetails(byte[] addr);

    boolean hasContractDetails(byte[] addr);

    /**
     * Store code associated with an account
     *
     * @param addr for the account
     * @param code that will be associated with this account
     */
    void saveCode(byte[] addr, byte[] code);

    /**
     * Retrieve the code associated with an account
     *
     * @param addr of the account
     * @return code in byte-array format
     */
    byte[] getCode(byte[] addr);

    /**
     * Retrieve the code hash associated with an account
     *
     * @param addr of the account
     * @return code hash
     */
    byte[] getCodeHash(byte[] addr);

    /**
     * Put a value in storage of an account at a given key
     *
     * @param addr of the account
     * @param key of the data to store
     * @param value is the data to store
     */
    void addStorageRow(byte[] addr, DataWord key, DataWord value);


    /**
     * Retrieve storage value from an account for a given key
     *
     * @param addr of the account
     * @param key associated with this value
     * @return data in the form of a <code>DataWord</code>
     */
    DataWord getStorageValue(byte[] addr, DataWord key);


    /**
     * Retrieve balance of an account
     *
     * @param addr of the account
     * @return balance of the account as a <code>BigInteger</code> value
     */
    BigInteger getBalance(byte[] addr);

    /**
     * Add value to the balance of an account
     *
     * @param addr of the account
     * @param value to be added
     * @return new balance of the account
     */
    BigInteger addBalance(byte[] addr, BigInteger value);

    /**
     * @return Returns set of all the account addresses
     */
    Set<byte[]> getAccountsKeys();


    /**
     * Dump the full state of the current repository into a file with JSON format
     * It contains all the contracts/account, their attributes and
     *
     * @param block of the current state
     * @param gasUsed the amount of gas used in the block until that point
     * @param txNumber is the number of the transaction for which the dump has to be made
     * @param txHash is the hash of the given transaction.
     * If null, the block state post coinbase reward is dumped.
     */
    void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash);

    /**
     * Save a snapshot and start tracking future changes
     *
     * @return the tracker repository
     */
    Repository startTracking();

    void flush();
    void flushNoReconnect();


    /**
     * Store all the temporary changes made
     * to the repository in the actual database
     */
    void commit();

    /**
     * Undo all the changes made so far
     * to a snapshot of the repository
     */
    void rollback();

    /**
     * Return to one of the previous snapshots
     * by moving the root.
     *
     * @param root - new root
     */
    void syncToRoot(byte[] root);

    /**
     * Check to see if the current repository has an open connection to the database
     *
     * @return <tt>true</tt> if connection to database is open
     */
    boolean isClosed();

    /**
     * Close the database
     */
    void close();

    /**
     * Reset
     */
    void reset();

    void updateBatch(HashMap<ByteArrayWrapper, AccountState> accountStates,
                            HashMap<ByteArrayWrapper, ContractDetails> contractDetailes);


    byte[] getRoot();

    void loadAccount(byte[] addr, HashMap<ByteArrayWrapper, AccountState> cacheAccounts,
                     HashMap<ByteArrayWrapper, ContractDetails> cacheDetails);

    Repository getSnapshotTo(byte[] root);
}
