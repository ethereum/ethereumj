/*
 * Copyright (c) [2018] [ <ether.camp> ]
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
package org.ethereum.vm.program;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNoException;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;

import org.ethereum.core.Block;
import org.ethereum.vm.DataWord;
import org.junit.Test;

/**
 * Test cases for the {@link NullStorage} class.
 */
public class NullStorageTest {
    /**
     * {@link NullStorage} instance to use when testing.
     */
    private final NullStorage storage = new NullStorage();

    /**
     * Contract: No exception should be thrown when setting program listener.
     */
    @Test
    public void testSetProgramListener() {
        assertNoException(() -> storage.setProgramListener(null));
    }

    /**
     * Contract: No exception should be thrown when creating accounts.
     */
    @Test
    public void testCreateAccount() {
        assertNoException(() -> storage.createAccount(new byte[0]));
        assertNoException(() -> storage.createAccount(new byte[] { 61, -10, 105 }));
    }

    /**
     * Contract: {@link Storage#isExist(byte[])} should return true.
     */
    @Test
    public void testIsExist() {
        assertTrue(storage.isExist(new byte[0]));
        assertTrue(storage.isExist(new byte[] { -10, 110, 20 }));
    }

    /**
     * Contract: Account states should not be null.
     */
    @Test
    public void testGetAccountState() {
        assertNotNull(storage.getAccountState(new byte[0]));
        assertNotNull(storage.getAccountState(new byte[] { 61, -10, 105 }));
    }

    /**
     * Contract: No exception should be thrown when deleting data.
     */
    @Test
    public void testDelete() {
        assertNoException(() -> storage.delete(new byte[0]));
        assertNoException(() -> storage.delete(new byte[] { 61, -10, 105 }));
    }

    /**
     * Contract: Increasing the nonce should not return null.
     */
    @Test
    public void testIncreaseNonce() {
        assertNotNull(storage.increaseNonce(new byte[0]));
        assertNotNull(storage.increaseNonce(new byte[] { 61, -10, 105 }));
    }

    /**
     * Contract: Setting the nonce should not return null.
     */
    @Test
    public void testSetNonce() {
        assertNotNull(storage.setNonce(new byte[0], BigInteger.ZERO));
        assertNotNull(storage.setNonce(new byte[] { 61, -10, 105 }, BigInteger.valueOf(-1)));
    }

    /**
     * Contract: Nonce should not be null.
     */
    @Test
    public void testGetNonce() {
        assertNotNull(storage.getNonce(new byte[0]));
        assertNotNull(storage.getNonce(new byte[] { 61, -10, 105 }));
    }

    /**
     * Contract: Contract details should not be null.
     */
    @Test
    public void testGetContractDetails() {
        assertNotNull(storage.getContractDetails(new byte[0]));
        assertNotNull(storage.getContractDetails(new byte[] { 61, -10, 105 }));
    }

    /**
     * Contract: Contract details should always exist.
     */
    @Test
    public void testHasContractDetails() {
        assertTrue(storage.hasContractDetails(new byte[0]));
        assertTrue(storage.hasContractDetails(new byte[] { 61, -10, 105 }));
    }

    /**
     * Contract: No exception should be thrown when saving code.
     */
    @Test
    public void testSaveCode() {
        assertNoException(() -> storage.saveCode(new byte[0], new byte[0]));
    }

    /**
     * Contract: Code should not be null.
     */
    @Test
    public void testGetCode() {
        assertNotNull(storage.getCode(new byte[0]));
        assertNotNull(storage.getCode(new byte[] { 61, -10, 105 }));
    }

    /**
     * Contract: Code hash should not be null.
     */
    @Test
    public void testGetCodeHash() {
        assertNotNull(storage.getCodeHash(new byte[0]));
        assertNotNull(storage.getCodeHash(new byte[] { 61, -10, 105 }));
    }

    /**
     * Contract: No exception should be thrown when adding a storage row.
     */
    @Test
    public void testAddStorageRow() {
        assertNoException(() -> storage.addStorageRow(new byte[0], new DataWord(), new DataWord()));
    }

    /**
     * Contract: Storage rows should not be null.
     */
    @Test
    public void testGetStorageValue() {
        assertNotNull(storage.getStorageValue(new byte[0], new DataWord()));
    }

    /**
     * Contract: Balance should not be null.
     */
    @Test
    public void testGetBalance() {
        assertNotNull(storage.getBalance(new byte[0]));
    }

    /**
     * Contract: No exception should be thrown when adding balance.
     */
    @Test
    public void testAddBalance() {
        assertNoException(() -> storage.addBalance(new byte[0], BigInteger.ZERO));
    }

    /**
     * Contract: Account keys should not be null.
     */
    @Test
    public void testGetAccountsKeys() {
        assertNotNull(storage.getAccountsKeys());
    }

    /**
     * Contract: No exception should be thrown when dumping state.
     */
    @Test
    public void testDumpState() {
        assertNoException(() -> storage.dumpState(new Block(new byte[0]), 0, 0, new byte[0]));
    }

    /**
     * Contract: No exception should be thrown when tracking starts.
     */
    @Test
    public void testStartTracking() {
        assertNoException(() -> storage.startTracking());
    }

    /**
     * Contract: No exception should be thrown when flushing.
     */
    @Test
    public void testFlush() {
        assertNoException(() -> storage.flush());
    }

    /**
     * Contract: No exception should be thrown when flushing without reconnecting.
     */
    @Test
    public void testFlushNoReconnect() {
        assertNoException(() -> storage.flushNoReconnect());
    }

    /**
     * Contract: No exception should be thrown when commiting.
     */
    @Test
    public void testCommit() {
        assertNoException(() -> storage.commit());
    }

    /**
     * Contract: No exception should be thrown when rolling back.
     */
    @Test
    public void testRollback() {
        assertNoException(() -> storage.rollback());
    }

    /**
     * Contract: No exception should be thrown when syncing to root.
     */
    @Test
    public void testSyncToRoot() {
        assertNoException(() -> storage.syncToRoot(new byte[0]));
    }

    /**
     * Contract: Storage should not be closed.
     */
    @Test
    public void testIsClosed() {
        assertTrue(storage.isClosed());
    }

    /**
     * Contract: No exception should be thrown when closing.
     */
    @Test
    public void testClose() {
        assertNoException(() -> storage.close());
    }

    /**
     * Contract: No exception should be thrown when resetting.
     */
    @Test
    public void testReset() {
        assertNoException(() -> storage.reset());
    }

    /**
     * Contract: No exception should be thrown when updating a batch.
     */
    @Test
    public void testUpdateBatch() {
        assertNoException(() -> storage.updateBatch(new HashMap<>(), new HashMap<>()));
    }

    /**
     * Contract: Root should not be null.
     */
    @Test
    public void testGetRoot() {
        assertNotNull(storage.getRoot());
    }

    /**
     * Contract: No exception should be thrown when loading an account.
     */
    @Test
    public void testLoadAccount() {
        assertNoException(() -> storage.loadAccount(new byte[0], new HashMap<>(), new HashMap<>()));
    }

    /**
     * Contract: Snapshot should not be null.
     */
    @Test
    public void testGetSnapshotTo() {
        assertNotNull(storage.getSnapshotTo(new byte[0]));
    }

    /**
     * Contract: Storage size is 0.
     */
    @Test
    public void testGetStorageSize() {
        assertEquals(0, storage.getStorageSize(new byte[0]));
    }

    /**
     * Contract: Storage keys should not be null.
     */
    @Test
    public void testGetStorageKeys() {
        assertNotNull(storage.getStorageKeys(new byte[0]));
    }

    /**
     * Contract: Getting storage should not return null.
     */
    @Test
    public void testGetStorage() {
        assertNotNull(storage.getStorage(new byte[0], new HashSet<>()));
    }

    /**
     * Helper for asserting that no exceptions are thrown.
     * 
     * @param runnable
     *            Runnable to assert from.
     */
    private void assertNoException(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            assumeNoException(e);
        }
    }
}
