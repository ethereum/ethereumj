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
package org.ethereum.db;

import org.ethereum.core.Genesis;

import org.ethereum.core.Repository;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.datasource.NoDeleteSource;
import org.ethereum.datasource.Source;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.DataWord;

import org.junit.*;
import org.junit.runners.MethodSorters;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @author Roman Mandeleil
 * @since 17.11.2014
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RepositoryTest {

    private RepositoryRoot repository;
    private byte[] accountKey = Hex.decode("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826");
    private DataWord storageKey = new DataWord(Hex.decode("A1A2A3"));
    private DataWord storageValue = new DataWord(Hex.decode("A4A5A6"));
    private byte[] associatedCode = Hex.decode("A1A2A3");

    @Before
    public void beforeEach() {
        repository = new RepositoryRoot(new HashMapDB());
    }

    @After
    public void afterEach() {
        repository.close();
    }

    @Test
    public void accountAttributesShouldBeCorrectlySetAtCreationTime() {
        assertEquals(BigInteger.ZERO, repository.getNonce(accountKey));
        assertEquals(BigInteger.ZERO, repository.getBalance(accountKey));
        assertEquals(HashUtil.EMPTY_DATA_HASH, repository.getCodeHash(accountKey));
        assertEquals(true, repository.hasContractDetails(accountKey));
    }

    @Test
    public void increaseNonceShouldAddOneToGivenAccountNonce() {
        repository.increaseNonce(accountKey);

        assertEquals(BigInteger.ONE, repository.getNonce(accountKey));
    }

    @Test
    public void addBalanceShouldAddGivenAmountToExistingBalanceValue() {
        BigInteger balance = new BigInteger("42");
        repository.addBalance(accountKey, balance);

        assertEquals(balance, repository.getBalance(accountKey));
    }

    @Test
    public void saveCodeShouldCorrectlyAssociateTheNewCodeToTheGivenAccount() {
        byte[] accountNewCode = Hex.decode("A1A2A3");
        repository.saveCode(accountKey, accountNewCode);

        assertArrayEquals(accountNewCode, repository.getCode(accountKey));
    }

    @Test
    public void addStorageRowShouldCorrectlyPutGivenValueWithKeyInGivenAccount() {
        repository.addStorageRow(accountKey, storageKey, storageValue);

        assertEquals(storageValue, repository.getStorageValue(accountKey, storageKey));
    }

    @Test
    public void commitShouldPushNewChangesToParentRepository() {
        Repository track = repository.startTracking();

        populateTrack(track);
        track.commit();

        assertEquals(BigInteger.ONE, repository.getNonce(accountKey));
        assertEquals(BigInteger.TEN, repository.getBalance(accountKey));
        assertEquals(storageValue, repository.getStorageValue(accountKey, storageKey));
        assertArrayEquals(associatedCode, repository.getCode(accountKey));
    }

    @Test
    public void rollbackShouldPreserveParentRepositoryState() {
        Repository track = repository.startTracking();

        populateTrack(track);
        track.rollback();

        assertThatParentRepositoryIsSetToInitialState();
    }

    private void assertThatParentRepositoryIsSetToInitialState() {
        assertEquals(BigInteger.ZERO, repository.getNonce(accountKey));
        assertEquals(BigInteger.ZERO, repository.getBalance(accountKey));
        assertEquals(HashUtil.EMPTY_DATA_HASH, repository.getCodeHash(accountKey));
        assertEquals(ByteUtil.EMPTY_BYTE_ARRAY, repository.getCode(accountKey));
    }

    @Test
    public void GenesisPopulateRepositoryShouldCorrectlySetRootState() {
        Genesis.populateRepository(repository, (Genesis)Genesis.getInstance());

        assertArrayEquals(Genesis.getInstance().getStateRoot(), repository.getRoot());
    }

    @Test
    public void deleteShouldCorrectlyRemoveAssociatedKeyFromGivenAccount() {
        byte[] newAccountKey = Hex.decode("13978AEE95F38490E9769C39B2773ED763D9CD5F");
        byte[] associatedCode = Hex.decode("B1B2B3");

        repository.saveCode(newAccountKey, associatedCode);
        repository.delete(newAccountKey);

        assertEquals(false, repository.isExist(newAccountKey));
    }

    @Test
    public void getContractDetailsShouldCorrectlyBuildContractDetailObject() {
        populateTrack(repository);
        ContractDetails contractDetail = repository.getContractDetails(accountKey);

        assertArrayEquals(accountKey, contractDetail.getAddress());
        assertEquals(storageValue, contractDetail.get(storageKey));
        assertArrayEquals(associatedCode, contractDetail.getCode());
    }

    private void populateTrack(Repository track) {
        track.increaseNonce(accountKey);
        track.addBalance(accountKey, BigInteger.TEN);
        track.addStorageRow(accountKey, storageKey, storageValue);
        track.saveCode(accountKey, associatedCode);
    }

    @Test
    public void valueInAGivenSnapshotShouldNotBeImpactedByValuesInAMoreRecentSnapshot() {
        Source<byte[], byte[]> stateDB = new NoDeleteSource<>(new HashMapDB<byte[]>());
        RepositoryRoot repository = new RepositoryRoot(stateDB);

        DataWord storageKey1 = new DataWord("c1");
        DataWord storageValue1 = new DataWord("c1a1");

        DataWord storageKey2 = new DataWord("c2");
        DataWord storageValue2 = new DataWord("c2a2");

        byte[] snapshot0Root = repository.getRoot();
        assertNull(getValueFromSnapShot(stateDB, storageKey1, snapshot0Root));
        assertNull(getValueFromSnapShot(stateDB, storageKey2, snapshot0Root));

        byte[] snapshot1Root = buildNewSnapShot(repository, storageKey1, storageValue1);
        assertEquals(storageValue1, getValueFromSnapShot(stateDB, storageKey1, snapshot1Root));
        assertNull(getValueFromSnapShot(stateDB, storageKey2, snapshot1Root));

        byte[] snapshot2Root = buildNewSnapShot(repository, storageKey2, storageValue2);
        assertEquals(storageValue1, getValueFromSnapShot(stateDB, storageKey1, snapshot2Root));
        assertEquals(storageValue2, getValueFromSnapShot(stateDB, storageKey2, snapshot2Root));
    }

    private DataWord getValueFromSnapShot(Source<byte[], byte[]> stateDB, DataWord cowKey1, byte[] snapshot1Root) {
        ContractDetails details;
        Repository snapshot1 = new RepositoryRoot(stateDB, snapshot1Root);
        details = snapshot1.getContractDetails(accountKey);
        return details.get(cowKey1);
    }

    private byte[] buildNewSnapShot(RepositoryRoot repository, DataWord key, DataWord value) {
        Repository track = repository.startTracking();
        track.addStorageRow(accountKey, key, value);
        track.commit();
        repository.commit();
        return repository.getRoot();
    }

    private boolean running = true;

    @Test
    public void testMultiThread() throws InterruptedException {
        final DataWord newStorageKey = new DataWord("c2");
        final DataWord newStorageValue = new DataWord("c2a2");
        final CountDownLatch failSema = new CountDownLatch(1);

        Repository track = repository.startTracking();
        track.addStorageRow(accountKey, newStorageKey, newStorageValue);
        track.commit();
        repository.flush();

        for (int i = 0; i < 10; ++i) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int cnt = 1;
                        while (running) {
                            Repository snap = repository.getSnapshotTo(repository.getRoot()).startTracking();
                            snap.addBalance(accountKey, BigInteger.TEN);
                            snap.addStorageRow(accountKey, storageKey, new DataWord(cnt));
                            snap.rollback();
                            cnt++;
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        failSema.countDown();
                    }
                }
            }).start();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                int cnt = 1;
                try {
                    while(running) {
                        Repository track = repository.startTracking();
                        DataWord cVal = new DataWord(cnt);
                        track.addStorageRow(accountKey, storageKey, cVal);
                        track.addBalance(accountKey, BigInteger.ONE);
                        track.commit();

                        repository.flush();

                        assertEquals(BigInteger.valueOf(cnt), repository.getBalance(accountKey));
                        assertEquals(cVal, repository.getStorageValue(accountKey, storageKey));
                        assertEquals(newStorageValue, repository.getStorageValue(accountKey, newStorageKey));
                        cnt++;
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    try {
                        repository.addStorageRow(accountKey, storageKey, new DataWord(123));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    failSema.countDown();
                }
            }
        }).start();

        failSema.await(10, TimeUnit.SECONDS);
        running = false;

        if (failSema.getCount() == 0) {
            throw new RuntimeException("Test failed.");
        }
    }
}
