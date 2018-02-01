package org.ethereum.erp;

import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.db.RepositoryRoot;
import org.ethereum.erp.StateChangeObject.StateChangeAction;
import org.ethereum.util.ByteUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import static org.ethereum.erp.ErpExecutor.ErpExecutionException;
import static org.ethereum.erp.ErpExecutor.STORE_CODE;
import static org.ethereum.erp.ErpExecutor.WEI_TRANSFER;
import static org.junit.Assert.assertEquals;

public class ErpExecutorTest
{
    private static byte[] account1 = Hex.decode("11113D9F938E13CD947EC05ABC7FE734DF8DD826");
    private static byte[] account2 = Hex.decode("22228AEE95F38490E9769C39B2773ED763D9CD5F");
    private static byte[] account3 = Hex.decode("33338AEE95F38490E9769C39B2773ED763D9CD5E");
    private ErpExecutor executor;
    private RepositoryRoot repo;

    @Before
    public void setup() {
        this.executor = new ErpExecutor();
        this.repo = new RepositoryRoot(new HashMapDB<>());
    }

    @After
    public void cleanup() {
        this.repo.close();
    }

    @Test
    public void applyStateChanges() throws ErpExecutionException {
        byte[] existingCode = Hex.decode("deadbeef");
        repo.saveCode(account3, existingCode);
        repo.addBalance(account1, BigInteger.TEN);
        byte[] currentHash = repo.getCodeHash(account3);

        byte[] newCode = Hex.decode("deadbeef1234");

        executor.applyStateChanges(
                createStateChangeObject(
                        createTransferAction(account1, account2, BigInteger.ONE),
                        createStoreCodeAction(account3, currentHash, newCode),
                        createTransferAction(account1, account3, BigInteger.ONE)
                ),
                repo);

        assertEquals(repo.getBalance(account1), BigInteger.valueOf(8L));
        assertEquals(repo.getBalance(account2), BigInteger.valueOf(1L));
        assertEquals(repo.getBalance(account3), BigInteger.valueOf(1L));
        assertEquals(repo.getCode(account3), newCode);
    }

    @Test
    public void applyWeiTransfer() {
        repo.addBalance(account1, BigInteger.TEN);

        executor.applyWeiTransfer(
                createTransferAction(account1, account2, BigInteger.valueOf(3L)),
                repo);

        assertEquals(repo.getBalance(account1), BigInteger.valueOf(7L));
        assertEquals(repo.getBalance(account2), BigInteger.valueOf(3L));
    }

    @Test
    public void applyStoreCode(){
        byte[] existingCode = Hex.decode("deadbeef");
        repo.saveCode(account1, existingCode);
        byte[] currentHash = repo.getCodeHash(account1);

        byte[] newCode = Hex.decode("deadbeef1234");

        // calling storeCode directly
        executor.applyStoreCode(
                createStoreCodeAction(account1, currentHash, newCode),
                repo);

        assertEquals(repo.getCode(account1), newCode);
    }




    @Test(expected = ErpExecutor.ErpExecutionException.class)
    public void applyStateChanges_failsWithUnkOp() throws ErpExecutionException {
        byte[] existingCode = Hex.decode("deadbeef");
        repo.saveCode(account3, existingCode);
        repo.addBalance(account1, BigInteger.TEN);
        byte[] currentHash = repo.getCodeHash(account3);

        byte[] newCode = Hex.decode("deadbeef1234");

        executor.applyStateChanges(
                createStateChangeObject(
                        createTransferAction(account1, account2, BigInteger.ONE),
                        createStoreCodeAction(account3, currentHash, newCode),
                        createUnsupportedAction(account2),
                        createTransferAction(account1, account3, BigInteger.ONE)
                ),
                repo);
    }

    @Test
    public void applyStateChangeAction_transfer() {
        repo.addBalance(account1, BigInteger.TEN);

        // calling weiTransfer via applyStateChangeAction
        executor.applyStateChangeAction(
                createTransferAction(account1, account2, BigInteger.valueOf(3L)),
                repo);

        assertEquals(repo.getBalance(account1), BigInteger.valueOf(7L));
        assertEquals(repo.getBalance(account2), BigInteger.valueOf(3L));
    }

    @Test
    public void applyStateChangeAction_store() {
        byte[] existingCode = Hex.decode("deadbeef");
        repo.saveCode(account1, existingCode);
        byte[] currentHash = repo.getCodeHash(account1);

        byte[] newCode = Hex.decode("deadbeef1234");

        // calling storeCode via applyStateChangeAction
        executor.applyStateChangeAction(
                createStoreCodeAction(account1, currentHash, newCode),
                repo);

        assertEquals(repo.getCode(account1), newCode);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void applyStateChangeAction_Unk() {
        executor.applyStateChangeAction(
                createUnsupportedAction(account1),
                repo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void applyStoreCode_BadAddress() {
        byte[] existingCode = Hex.decode("deadbeef");
        repo.saveCode(account1, existingCode);
        byte[] currentHash = repo.getCodeHash(account1);

        byte[] newCode = Hex.decode("deadbeef1234");

        executor.applyStoreCode(
                createStoreCodeAction(ByteUtil.hexStringToBytes(null), currentHash, newCode),
                repo);
    }

    @Test(expected = IllegalStateException.class)
    public void applyStoreCode_WrongHash(){
        byte[] existingCode = Hex.decode("deadbeef");
        repo.saveCode(account1, existingCode);


        byte[] newCode = Hex.decode("deadbeef1234");
        byte[] wrongHash = HashUtil.sha3(Hex.decode("deadbeefdeadbeef"));

        executor.applyStoreCode(
                createStoreCodeAction(account1, wrongHash, newCode),
                repo);
    }

    @Test(expected = IllegalStateException.class)
    public void applyWeiTransfer_InsufficientFunds() {
        repo.addBalance(account1, BigInteger.TEN);

        executor.applyWeiTransfer(
                createTransferAction(account1, account2, BigInteger.valueOf(20L)),
                repo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void applyWeiTransfer_BadAddress() {
        repo.addBalance(account1, BigInteger.TEN);

        executor.applyWeiTransfer(
                createTransferAction(account1, ByteUtil.hexStringToBytes(null), BigInteger.valueOf(1L)),
                repo);
    }

    private static StateChangeAction createTransferAction(byte[] fromAddress, byte[] toAddress, BigInteger value) {
        final StateChangeAction action = new StateChangeAction();
        action.toAddress = toAddress;
        action.fromAddress = fromAddress;
        action.type = WEI_TRANSFER;
        action.valueInWei = value;
        return action;
    }

    private static StateChangeAction createStoreCodeAction(byte[] toAddress, byte[] expectedHash, byte[] code) {
        final StateChangeAction action = new StateChangeAction();
        action.toAddress = toAddress;
        action.type = STORE_CODE;
        action.code = code;
        action.expectedCodeHash = expectedHash;
        return action;
    }

    private static StateChangeAction createUnsupportedAction(byte[] toAddress) {
        final StateChangeAction action = new StateChangeAction();
        action.toAddress = toAddress;
        action.type = "doSomethingNaughty";
        return action;
    }

    private static StateChangeObject createStateChangeObject(StateChangeAction ... actions) {
        final StateChangeObject sco = new StateChangeObject();
        sco.targetBlock = 1234;
        sco.erpId = "eip-999";
        sco.actions = actions;
        return sco;
    }
}