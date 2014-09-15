package org.ethereum.db;

import org.ethereum.core.AccountState;
import org.ethereum.facade.Repository;
import org.ethereum.vm.DataWord;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 23/06/2014 23:52
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RepositoryTest {

    @Test // create account, get account
	public void test1() {

		String addr = "cd2a3d9f938e13cd947ec05abc7fe734df8dd826";
		Repository repository = new RepositoryImpl();

		try {
			AccountState createdState = repository.createAccount(Hex.decode(addr));
			AccountState fetchedState = repository.getAccountState(Hex.decode(addr));
			assertEquals(createdState.getEncoded(), fetchedState.getEncoded());
		} finally {
			repository.close();
		}
	}

    @Test  // increase nonce
    public void test2() {

        String addr = "cd2a3d9f938e13cd947ec05abc7fe734df8dd826";
        Repository repository = new RepositoryImpl();

        try {
            BigInteger nonce0 = repository.getNonce(Hex.decode(addr));

            repository.createAccount(Hex.decode(addr));
            BigInteger nonce1 = repository.getNonce(Hex.decode(addr));

            repository.increaseNonce(Hex.decode(addr));
            BigInteger nonce2 = repository.getNonce(Hex.decode(addr));

            assertEquals(0, nonce0.intValue());
            assertEquals(0, nonce1.intValue());
            assertEquals(1, nonce2.intValue());
        } finally {
            repository.close();
        }
    }

    @Test  // increase nonce
    public void test3() {

        String addr = "cd2a3d9f938e13cd947ec05abc7fe734df8dd826";
        Repository repository = new RepositoryImpl();
        try {
            BigInteger nonce0 = repository.getNonce(Hex.decode(addr));

            repository.createAccount(Hex.decode(addr));
            BigInteger nonce1 = repository.getNonce(Hex.decode(addr));

            repository.increaseNonce(Hex.decode(addr));
            repository.increaseNonce(Hex.decode(addr));
            repository.increaseNonce(Hex.decode(addr));
            BigInteger nonce2 = repository.getNonce(Hex.decode(addr));

            assertEquals(0, nonce0.intValue());
            assertEquals(0, nonce1.intValue());
            assertEquals(3, nonce2.intValue());
        } finally {
            repository.close();
        }
    }

    @Test  // change balance
    public void test4() {

        String addr = "cd2a3d9f938e13cd947ec05abc7fe734df8dd826";
        Repository repository = new RepositoryImpl();
        try {
            BigInteger balance0 = repository.getBalance(Hex.decode(addr));

            repository.createAccount(Hex.decode(addr));
            BigInteger balance1 = repository.getBalance(Hex.decode(addr));

            repository.addBalance(Hex.decode(addr), BigInteger.valueOf(300));
            BigInteger balance2 = repository.getBalance(Hex.decode(addr));

            assertEquals(0, balance0.intValue());
            assertEquals(0,   balance1.intValue());
            assertEquals(300, balance2.intValue());
        } finally {
            repository.close();
        }
    }

    @Test  // change balance
    public void test5() {

        String addr = "cd2a3d9f938e13cd947ec05abc7fe734df8dd826";
        Repository repository = new RepositoryImpl();
        try {
            BigInteger balance0 = repository.getBalance(Hex.decode(addr));

            repository.createAccount(Hex.decode(addr));
            BigInteger balance1 = repository.getBalance(Hex.decode(addr));

            repository.addBalance(Hex.decode(addr), BigInteger.valueOf(300));
            BigInteger balance2 = repository.getBalance(Hex.decode(addr));

            repository.addBalance(Hex.decode(addr), BigInteger.valueOf(-150));
            BigInteger balance3 = repository.getBalance(Hex.decode(addr));

            assertEquals(0, balance0.intValue());
            assertEquals(0,   balance1.intValue());
            assertEquals(300, balance2.intValue());
            assertEquals(150, balance3.intValue());
        } finally {
            repository.close();
        }
    }

    @Test  // get/set code
    public void test6() {

        String addr = "cd2a3d9f938e13cd947ec05abc7fe734df8dd826";
        Repository repository = new RepositoryImpl();

        byte[] code;
        try {
            code = repository.getCode(Hex.decode(addr));
            assertTrue(code == null);
        } finally {
            repository.close();
        }
    }

    @Test  // get/set code
    public void test7() {

        String addr = "cd2a3d9f938e13cd947ec05abc7fe734df8dd826";
        String codeString = "7f60c860005461012c602054000000000000000000000000000000000000000000600060206000f200";
        String codeHash = "8f0d7fc8cc6fdd688fa58ae9256310069f5659ed2a8a3af994d80350fbf1e798";

        Repository repository = new RepositoryImpl();

        try {
            byte[] code0 = repository.getCode(Hex.decode(addr));
            repository.createAccount(Hex.decode(addr));
            repository.saveCode(Hex.decode(addr), Hex.decode(codeString));
            byte[] code1 = repository.getCode(Hex.decode(addr));
            AccountState accountState = repository.getAccountState(Hex.decode(addr));

            assertTrue(code0 == null);
            assertEquals(codeString, Hex.toHexString(code1));
            assertEquals(codeHash, Hex.toHexString(accountState.getCodeHash()));
        } finally {
            repository.close();
        }
    }

    @Test  // get/set code
    public void test8() {

        String addr = "cd2a3d9f938e13cd947ec05abc7fe734df8dd826";
        String codeHash = "c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470";

        Repository repository = new RepositoryImpl();

        try {
            byte[] code0 = repository.getCode(Hex.decode(addr));
            repository.createAccount(Hex.decode(addr));
            repository.saveCode(Hex.decode(addr), null);
            byte[] code1 = repository.getCode(Hex.decode(addr));
            AccountState accountState = repository.getAccountState(Hex.decode(addr));
            assertTrue(code0 == null);
            assertNull(code1);
            assertEquals(codeHash, Hex.toHexString(accountState.getCodeHash()));
        } finally {
            repository.close();
        }
    }

    @Test // storage set/get
    public void test9() {

        String addr = "cd2a3d9f938e13cd947ec05abc7fe734df8dd826";
        byte[] keyBytes = Hex.decode("c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470");
        DataWord key = new DataWord(keyBytes);

        Repository repository = new RepositoryImpl();

        try {
            DataWord value = repository.getStorageValue(Hex.decode(addr), key);
            assertNull(value);
        } finally {
            repository.close();
        }
    }

    @Test // storage set/get
    public void test10() {

        String addr = "cd2a3d9f938e13cd947ec05abc7fe734df8dd826";
        Repository repository = new RepositoryImpl();

        try {
            repository.createAccount(Hex.decode(addr));
            byte[] keyBytes = Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826");
            DataWord key = new DataWord(keyBytes);
            byte[] valueBytes = Hex.decode("0F4240");
            DataWord value = new DataWord(valueBytes);
            repository.addStorageRow(Hex.decode(addr), key, value);
            DataWord fetchedValue = repository.getStorageValue(Hex.decode(addr), key);
            assertEquals(value, fetchedValue);
        } finally {
            repository.close();
        }
    }

    @Test // storage set/get
    public void test11() {

        String addr = "cd2a3d9f938e13cd947ec05abc7fe734df8dd826";
        String expectedStorageHash = "a737c40a4aa895fb9eb464536c376ee7c2c08eb733c8fd2353fcc62dc734f075";

        Repository repository = new RepositoryImpl();

        try {
            repository.createAccount(Hex.decode(addr));

            byte[] keyBytes = Hex.decode("03E8");
            DataWord key1 = new DataWord(keyBytes);

            keyBytes = Hex.decode("03E9");
            DataWord key2 = new DataWord(keyBytes);

            keyBytes = Hex.decode("03F0");
            DataWord key3 = new DataWord(keyBytes);

            byte[] valueBytes = Hex.decode("0F4240");
            DataWord value1 = new DataWord(valueBytes);

            valueBytes = Hex.decode("0F4241");
            DataWord value2 = new DataWord(valueBytes);

            valueBytes = Hex.decode("0F4242");
            DataWord value3 = new DataWord(valueBytes);

            repository.addStorageRow(Hex.decode(addr), key1, value1);
            repository.addStorageRow(Hex.decode(addr), key2, value2);
            repository.addStorageRow(Hex.decode(addr), key3, value3);

            DataWord fetchedValue1 = repository.getStorageValue(Hex.decode(addr), key1);
            DataWord fetchedValue2 = repository.getStorageValue(Hex.decode(addr), key2);
            DataWord fetchedValue3 = repository.getStorageValue(Hex.decode(addr), key3);

            AccountState accountState = repository.getAccountState(Hex.decode(addr));
            String stateRoot = Hex.toHexString(accountState.getStateRoot());

            assertEquals(value1, fetchedValue1);
            assertEquals(value2, fetchedValue2);
            assertEquals(value3, fetchedValue3);
            assertEquals(expectedStorageHash, stateRoot);
        } finally {
            repository.close();
        }
    }

    @Test // commit/rollback
    public void test12() {

        String addr = "cd2a3d9f938e13cd947ec05abc7fe734df8dd826";
        String expectedStorageHash = "365ed874ad42c2b4af335212465291e03dcd1f0c5b600f40f048ed238ad61fd3";
        long expectedBalance = 333;

        Repository origRepository = new RepositoryImpl();

        try {
            Repository repository = origRepository.getTrack();
            repository.startTracking();

            repository.createAccount(Hex.decode(addr));
            repository.addBalance(Hex.decode(addr), BigInteger.valueOf(expectedBalance));

            repository.commit();

            BigInteger balance =  repository.getBalance(Hex.decode(addr));

            assertEquals(expectedBalance, balance.longValue());
        } finally {
            origRepository.close();
        }
    }

    @Test // commit/rollback
    public void test13() {

        String addr = "cd2a3d9f938e13cd947ec05abc7fe734df8dd826";
        String expectedStorageHash = "365ed874ad42c2b4af335212465291e03dcd1f0c5b600f40f048ed238ad61fd3";
        long expectedBalance_1 = 55500;
        long expectedBalance_2 = 0;

        Repository origRepository = new RepositoryImpl();
        Repository repository = origRepository.getTrack();
        repository.startTracking();

        repository.createAccount(Hex.decode(addr));
        repository.addBalance(Hex.decode(addr), BigInteger.valueOf(55500));

        BigInteger balance =  repository.getBalance(Hex.decode(addr));
        assertEquals(expectedBalance_1, balance.longValue());
        repository.rollback();

        balance =  repository.getBalance(Hex.decode(addr));
        assertEquals(expectedBalance_2, balance.longValue());

        origRepository.close();
    }

    @Test // commit/rollback
    public void test14() {

        String addr_1 = "cd2a3d9f938e13cd947ec05abc7fe734df8dd826";
        String addr_2 = "77045e71a7a2c50903d88e564cd72fab11e82051";
        String codeString = "7f60c860005461012c602054000000000000000000000000000000000000000000600060206000f200";

        long expectedBalance = 55500;

        Repository origRepository = new RepositoryImpl();
        Repository repository = origRepository.getTrack();

        try {
            repository.createAccount(Hex.decode(addr_1));
            repository.addBalance(Hex.decode(addr_1), BigInteger.valueOf(expectedBalance));
            repository.startTracking();

            repository.createAccount(Hex.decode(addr_2));
            repository.saveCode(Hex.decode(addr_2), Hex.decode(codeString));
            repository.addStorageRow(Hex.decode(addr_2), new DataWord(101), new DataWord(1000001));
            repository.addStorageRow(Hex.decode(addr_2), new DataWord(102), new DataWord(1000002));
            repository.addStorageRow(Hex.decode(addr_2), new DataWord(103), new DataWord(1000003));
            repository.rollback();

            BigInteger balance =  repository.getBalance(Hex.decode(addr_1));
            assertEquals(expectedBalance, balance.longValue());

            DataWord value = repository.getStorageValue(Hex.decode(addr_2), new DataWord(101));
            assertNull(value);
        } finally {
            origRepository.close();
        }
    }
}
