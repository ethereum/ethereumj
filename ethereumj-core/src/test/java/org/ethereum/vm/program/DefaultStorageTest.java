package org.ethereum.vm.program;

import static org.ethereum.jsontestsuite.suite.Utils.parseData;
import static org.ethereum.util.ByteUtil.wrap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.HashMap;

import org.ethereum.core.AccountState;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.ContractDetails;
import org.ethereum.db.RepositoryRoot;
import org.ethereum.jsontestsuite.suite.ContractDetailsCacheImpl;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.invoke.ProgramInvokeMockImpl;
import org.ethereum.vm.program.listener.CompositeProgramListener;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DefaultStorageTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void createAccountTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        byte[] addr = { 0 };
        AccountState as = storage.createAccount(addr);
        assertEquals(as, storage.getAccountState(addr));
    }

    @Test
    public void isExistTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        byte[] addr = { 0 };
        storage.createAccount(addr);
        assertEquals(true, storage.isExist(addr));
    }

    @Test
    public void deleteTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        byte[] addr = { 0 };
        storage.createAccount(addr);
        storage.delete(addr);
        assertEquals(false, storage.isExist(addr));
    }

    @Test
    public void deleteWithListenerTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);
        storage.setProgramListener(new CompositeProgramListener());

        /* Test. */
        byte[] addr = { 0 };
        storage.createAccount(addr);
        storage.delete(addr);
        assertEquals(false, storage.isExist(addr));
    }

    @Test
    public void deleteWithSameAddressTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        DataWord addr = new DataWord(1);
        programInvoke.setOwnerAddress(addr.getData());
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        storage.createAccount(addr.getData());
        storage.delete(addr.getData());
        assertEquals(false, storage.isExist(addr.getData()));
    }

    @Test
    public void deleteWithListenerWithSameAdressTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        DataWord addr = new DataWord(1);
        programInvoke.setOwnerAddress(addr.getData());
        Storage storage = new DefaultStorage(programInvoke);
        storage.setProgramListener(new CompositeProgramListener());

        /* Test. */
        storage.createAccount(addr.getData());
        storage.delete(addr.getData());
        assertEquals(false, storage.isExist(addr.getData()));
    }

    @Test
    public void setNonceTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        byte[] addr = { 0 };
        BigInteger nonce = new BigInteger("1");
        storage.setNonce(addr, nonce);
        assertEquals(0, storage.getNonce(addr).compareTo(nonce));
    }

    @Test
    public void increaseNonceTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        byte[] addr = { 0 };
        BigInteger oldNonce = storage.getNonce(addr);
        storage.increaseNonce(addr);
        assertEquals(1, storage.getNonce(addr).compareTo(oldNonce));
    }

    @Test
    public void hasContractDetailsTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        byte[] addr = { 0 };
        // Always true since, if it hasn't one it will be created:
        assertTrue(storage.hasContractDetails(addr));
    }

    @Test
    public void getContractDetailsTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        byte[] addr = { 0 };
        // Get (Create) contract details:
        storage.getContractDetails(addr);
        assertTrue(storage.hasContractDetails(addr));
    }

    @Test
    public void saveCodeTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        byte[] addr = { 0 };
        byte[] code = { 1 };
        storage.saveCode(addr, code);
        assertEquals(code, storage.getCode(addr));
    }

    @Test
    public void getCodeHashTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        byte[] addr = { 0 };
        byte[] code = { 1 };
        storage.saveCode(addr, code);
        AccountState as = storage.getAccountState(addr);
        assertEquals(as.getCodeHash(), storage.getCodeHash(addr));
    }

    @Test
    public void addStorageRowTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);
        byte[] addr = { 0 };

        /* Test. */
        DataWord key = new DataWord();
        DataWord value = new DataWord(1); // Checks if zero and sets to null.
        storage.addStorageRow(addr, key, value);
        assertEquals(value, storage.getStorageValue(addr, key));
    }

    @Test
    public void addStorageRowWithListenerWithSameAddressTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        DataWord addr = new DataWord(1);
        programInvoke.setOwnerAddress(addr.getData());
        Storage storage = new DefaultStorage(programInvoke);
        storage.setProgramListener(new CompositeProgramListener());

        /* Test. */
        DataWord key = new DataWord();
        DataWord value = new DataWord(1); // Checks if zero and sets to null.
        storage.addStorageRow(addr.getData(), key, value);
        assertEquals(value, storage.getStorageValue(addr.getData(), key));
    }

    @Test
    public void getBalanceTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        byte[] addr = { 0 };
        assertEquals(new BigInteger("0"), storage.getBalance(addr));
    }

    @Test
    public void addBalanceTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        BigInteger balance = new BigInteger("1");
        byte[] addr = { 0 };
        storage.addBalance(addr, balance);
        assertEquals(balance, storage.getBalance(addr));
    }

    @Test
    public void getAccountsKeysNotSupportedTest() throws RuntimeException {

        /* Setup exception handling. */
        exception.expect(RuntimeException.class);
        exception.expectMessage("Not supported");

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        storage.getAccountsKeys(); // Can never run (always throws exception).
    }

    @Test
    public void dumpStateNotSupportedTest() throws RuntimeException {

        /* Setup exception handling. */
        exception.expect(RuntimeException.class);
        exception.expectMessage("Not supported");

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        storage.dumpState(null, 0, 0, null); // Can never run (always throws exception).
    }

    @Test
    public void startTrackingTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        assertNotNull(storage.startTracking()); // Not null if started tracking.
    }

    @Test
    public void flushTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);
        RepositoryRoot repository = (RepositoryRoot) programInvoke.getRepository();

        /* Test. */
        assertEquals("<empty>", repository.dumpStateTrie()); // Starts as <empty>.
        storage.flush(); // Flush down values.
        assertNotNull(repository.dumpStateTrie()); // Not null.
        assertNotEquals("<empty>", repository.dumpStateTrie()); // Non <empty>.
    }

    @Test
    public void flushNoReconnectNotSupportedTest() throws UnsupportedOperationException {

        /* Setup exception handling. */
        exception.expect(UnsupportedOperationException.class);

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        storage.flushNoReconnect();
    }

    @Test
    public void commitTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);
        RepositoryRoot repository = (RepositoryRoot) programInvoke.getRepository();

        /* Test. */
        assertEquals("<empty>", repository.dumpStateTrie()); // Starts as <empty>.
        storage.commit(); // Flush down values.
        assertNotNull(repository.dumpStateTrie()); // Not null.
        assertNotEquals("<empty>", repository.dumpStateTrie()); // Non <empty>.
    }

    /* Method "rollback" does not do anything, unable to test. */

    @Test
    public void syncToRootTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);
        RepositoryRoot repository = (RepositoryRoot) programInvoke.getRepository();
        byte[] root = { 0 };

        /* Test. */
        assertEquals("<empty>", repository.dumpStateTrie());
        storage.syncToRoot(root); // Sets StateTrie to null.
        storage.flush(); // Sets StateTrie to something (not null).
        assertNotNull(repository.dumpStateTrie());
    }

    @Test
    public void isClosedNotSupportedTest() throws RuntimeException {

        /* Setup exception handling. */
        exception.expect(RuntimeException.class);
        exception.expectMessage("Not supported");

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        storage.isClosed();
    }

    /* Method "close" does not do anything, unable to test. */

    @Test
    public void resetNotSupportedTest() throws RuntimeException {

        /* Setup exception handling. */
        exception.expect(RuntimeException.class);
        exception.expectMessage("Not supported");

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        storage.reset();
    }

    @Test
    public void updateBatchTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);
        byte[] addr = { 0 };
        HashMap<ByteArrayWrapper, AccountState> stateBatch = new HashMap<>();
        HashMap<ByteArrayWrapper, ContractDetails> detailsBatch = new HashMap<>();
        AccountState state = storage.createAccount(addr);
        ContractDetails details = storage.getContractDetails(addr);

        /* Setup stateBatch. */
        stateBatch.put(wrap(parseData("0")), state);

        /* Setup detailsBatch through detailsCache. */
        ContractDetailsCacheImpl detailsCache = new ContractDetailsCacheImpl(details);
        DataWord key = new DataWord(0);
        DataWord value = new DataWord(1);
        detailsCache.put(key, value);
        detailsBatch.put(wrap(parseData("0")), detailsCache);

        /* Test. */
        storage.updateBatch(stateBatch, detailsBatch);
        byte[] c = detailsBatch.get(wrap(parseData("0"))).get(key).getData();
        assertEquals(1, c[c.length - 1]);
    }

    @Test
    public void updateBatchWithListenerWithSameAddressNotDeletedDirtyNotSupportedTest()
            throws RuntimeException {

        /* Setup exception handling. */
        exception.expect(RuntimeException.class);
        exception.expectMessage("Not supported");

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        byte[] addr = wrap(parseData("0")).getData();
        programInvoke.setOwnerAddress(addr);
        Storage storage = new DefaultStorage(programInvoke);
        storage.setProgramListener(new CompositeProgramListener());
        HashMap<ByteArrayWrapper, AccountState> stateBatch = new HashMap<>();
        HashMap<ByteArrayWrapper, ContractDetails> detailsBatch = new HashMap<>();
        AccountState state = storage.createAccount(addr);
        ContractDetails details = storage.getContractDetails(addr);

        /* Setup stateBatch. */
        stateBatch.put(wrap(parseData("0")), state);

        /* Setup detailsBatch through detailsCache. */
        ContractDetailsCacheImpl detailsCache = new ContractDetailsCacheImpl(details);
        DataWord key = new DataWord(0);
        DataWord value = new DataWord(1);
        detailsCache.put(key, value);
        detailsBatch.put(wrap(parseData("0")), detailsCache);

        /* Test. */
        storage.updateBatch(stateBatch, detailsBatch);
        // 1. RepositoryImpl.updateBatch ->
        // 2. ContractDetailsCacheImpl.getStorageKeys ->
        // 3. ContractDetailsImpl.getStorageKeys -> Not supported.
    }

    @Test
    public void updateBatchWithListenerWithSameAddressDeletedDirtyNotSupportedTest()
            throws RuntimeException {

        /* Setup exception handling. */
        exception.expect(RuntimeException.class);
        exception.expectMessage("Not supported");

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        byte[] addr = wrap(parseData("0")).getData();
        programInvoke.setOwnerAddress(addr);
        Storage storage = new DefaultStorage(programInvoke);
        storage.setProgramListener(new CompositeProgramListener());
        HashMap<ByteArrayWrapper, AccountState> stateBatch = new HashMap<>();
        HashMap<ByteArrayWrapper, ContractDetails> detailsBatch = new HashMap<>();
        AccountState state = storage.createAccount(addr);
        ContractDetails details = storage.getContractDetails(addr);

        /* Setup stateBatch. */
        stateBatch.put(wrap(parseData("0")), state);

        /* Setup detailsBatch through detailsCache. */
        ContractDetailsCacheImpl detailsCache = new ContractDetailsCacheImpl(details);
        DataWord key = new DataWord(0);
        DataWord value = new DataWord(1);
        detailsCache.put(key, value);

        /* Cover Deleted and Dirty branches. */
        detailsCache.setDeleted(true);
        detailsBatch.put(wrap(parseData("0")), detailsCache);

        /* Test. */
        storage.updateBatch(stateBatch, detailsBatch);
        // 1. RepositoryImpl.updateBatch ->
        // 2. ContractDetailsCacheImpl.getStorageKeys ->
        // 3. ContractDetailsImpl.getStorageKeys -> Not supported.
    }

    @Test
    public void updateBatchWithListenerWithSameAddressNotDeletedNotDirtyNotSupportedTest()
            throws RuntimeException {

        /* Setup exception handling. */
        exception.expect(RuntimeException.class);
        exception.expectMessage("Not supported");

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        byte[] addr = wrap(parseData("0")).getData();
        programInvoke.setOwnerAddress(addr);
        Storage storage = new DefaultStorage(programInvoke);
        storage.setProgramListener(new CompositeProgramListener());
        HashMap<ByteArrayWrapper, AccountState> stateBatch = new HashMap<>();
        HashMap<ByteArrayWrapper, ContractDetails> detailsBatch = new HashMap<>();
        AccountState state = storage.createAccount(addr);
        ContractDetails details = storage.getContractDetails(addr);

        /* Setup stateBatch. */
        stateBatch.put(wrap(parseData("0")), state);

        /* Setup detailsBatch through detailsCache. */
        ContractDetailsCacheImpl detailsCache = new ContractDetailsCacheImpl(details);
        DataWord key = new DataWord(0);
        DataWord value = new DataWord(1);
        detailsCache.put(key, value);

        /* Cover Not Deleted and Not Dirty branches. */
        detailsCache.setDirty(false);
        detailsBatch.put(wrap(parseData("0")), detailsCache);

        /* Test. */
        storage.updateBatch(stateBatch, detailsBatch);
        // 1. RepositoryImpl.updateBatch ->
        // 2. ContractDetailsCacheImpl.getStorageKeys ->
        // 3. ContractDetailsImpl.getStorageKeys -> Not supported.
    }

    @Test
    public void getRootTest() {

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);
        DataWord root = programInvoke.getOwnerAddress();

        /* Test. */
        storage.flush();
        storage.syncToRoot(root.getData());
        assertEquals(root.getData(), storage.getRoot());
    }

    @Test
    public void loadAccountTest() throws RuntimeException {

        /* Setup exception handling. */
        exception.expect(RuntimeException.class);
        exception.expectMessage("Not supported");

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        storage.loadAccount(null, null, null);
    }

    @Test
    public void getSnapshotToTest() throws UnsupportedOperationException {

        /* Setup exception handling. */
        exception.expect(UnsupportedOperationException.class);

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        storage.getSnapshotTo(null);
    }

    @Test
    public void getStorageSizeTest() throws RuntimeException {

        /* Setup exception handling. */
        exception.expect(RuntimeException.class);
        exception.expectMessage("Not supported");

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        storage.getStorageSize(null);
    }

    @Test
    public void getStorageKeysTest() throws RuntimeException {

        /* Setup exception handling. */
        exception.expect(RuntimeException.class);
        exception.expectMessage("Not supported");

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        storage.getStorageKeys(null);
    }

    @Test
    public void getStorageTest() throws RuntimeException {

        /* Setup exception handling. */
        exception.expect(RuntimeException.class);
        exception.expectMessage("Not supported");

        /* Setup storage. */
        ProgramInvokeMockImpl programInvoke = new ProgramInvokeMockImpl();
        Storage storage = new DefaultStorage(programInvoke);

        /* Test. */
        storage.getStorage(null, null);
    }
}
