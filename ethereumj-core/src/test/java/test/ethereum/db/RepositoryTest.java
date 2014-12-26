package test.ethereum.db;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Genesis;
import org.ethereum.crypto.HashUtil;
import org.ethereum.facade.Repository;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.vm.DataWord;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * www.etherj.com
 *
 * @author Roman Mandeleil
 * Created on: 17/11/2014 23:08
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RepositoryTest {


    @Test
    public void test1() {

        SystemProperties.CONFIG.setDataBaseDir("test_db/" + RepositoryTest.class);
        Repository repository = new RepositoryImpl();

        byte[] cow = Hex.decode("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826");
        byte[] horse = Hex.decode("13978AEE95F38490E9769C39B2773ED763D9CD5F");

        repository.increaseNonce(cow);
        repository.increaseNonce(horse);

        assertEquals(BigInteger.ONE, repository.getNonce(cow));
        assertEquals(BigInteger.ONE, repository.getNonce(horse));

        repository.close();
    }


    @Test
    public void test2() {

        SystemProperties.CONFIG.setDataBaseDir("test_db/" + RepositoryTest.class);
        Repository repository = new RepositoryImpl();

        byte[] cow = Hex.decode("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826");
        byte[] horse = Hex.decode("13978AEE95F38490E9769C39B2773ED763D9CD5F");

        repository.addBalance(cow, BigInteger.TEN);
        repository.addBalance(horse, BigInteger.ONE);

        assertEquals(BigInteger.TEN, repository.getBalance(cow));
        assertEquals(BigInteger.ONE, repository.getBalance(horse));

        repository.close();
    }


    @Test
    public void test3() {

        SystemProperties.CONFIG.setDataBaseDir("test_db/" + RepositoryTest.class);
        Repository repository = new RepositoryImpl();

        byte[] cow = Hex.decode("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826");
        byte[] horse = Hex.decode("13978AEE95F38490E9769C39B2773ED763D9CD5F");

        byte[] cowCode = Hex.decode("A1A2A3");
        byte[] horseCode = Hex.decode("B1B2B3");

        repository.saveCode(cow, cowCode);
        repository.saveCode(horse, horseCode);

        assertArrayEquals(cowCode, repository.getCode(cow));
        assertArrayEquals(horseCode, repository.getCode(horse));

        repository.close();
    }

    @Test
    public void test4() {

        SystemProperties.CONFIG.setDataBaseDir("test_db/" + RepositoryTest.class);
        Repository repository = new RepositoryImpl();

        byte[] cow = Hex.decode("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826");
        byte[] horse = Hex.decode("13978AEE95F38490E9769C39B2773ED763D9CD5F");

        byte[] cowKey = Hex.decode("A1A2A3");
        byte[] cowValue = Hex.decode("A4A5A6");

        byte[] horseKey = Hex.decode("B1B2B3");
        byte[] horseValue = Hex.decode("B4B5B6");

        repository.addStorageRow(cow, new DataWord(cowKey), new DataWord(cowValue));
        repository.addStorageRow(horse, new DataWord(horseKey), new DataWord(horseValue));

        assertEquals(new DataWord(cowValue), repository.getStorageValue(cow, new DataWord(cowKey)));
        assertEquals(new DataWord(horseValue), repository.getStorageValue(horse, new DataWord(horseKey)));

        repository.close();
    }


    @Test
    public void test5() {

        SystemProperties.CONFIG.setDataBaseDir("test_db/" + RepositoryTest.class);
        Repository repository = new RepositoryImpl();
        Repository track = repository.startTracking();

        byte[] cow = Hex.decode("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826");
        byte[] horse = Hex.decode("13978AEE95F38490E9769C39B2773ED763D9CD5F");

        track.increaseNonce(cow);
        track.increaseNonce(cow);
        track.increaseNonce(cow);
        track.increaseNonce(cow);
        track.increaseNonce(cow);
        track.increaseNonce(cow);
        track.increaseNonce(cow);
        track.increaseNonce(cow);
        track.increaseNonce(cow);
        track.increaseNonce(cow);

        track.increaseNonce(horse);

        track.commit();

        assertEquals(BigInteger.TEN, repository.getNonce(cow));
        assertEquals(BigInteger.ONE, repository.getNonce(horse));

        repository.close();
    }

    @Test
    public void test6() {

        SystemProperties.CONFIG.setDataBaseDir("test_db/" + RepositoryTest.class);
        Repository repository = new RepositoryImpl();
        Repository track = repository.startTracking();

        byte[] cow = Hex.decode("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826");
        byte[] horse = Hex.decode("13978AEE95F38490E9769C39B2773ED763D9CD5F");

        track.increaseNonce(cow);
        track.increaseNonce(cow);
        track.increaseNonce(cow);
        track.increaseNonce(cow);
        track.increaseNonce(cow);
        track.increaseNonce(cow);
        track.increaseNonce(cow);
        track.increaseNonce(cow);
        track.increaseNonce(cow);
        track.increaseNonce(cow);

        track.increaseNonce(horse);

        assertEquals(BigInteger.TEN, track.getNonce(cow));
        assertEquals(BigInteger.ONE, track.getNonce(horse));

        track.rollback();

        assertEquals(BigInteger.ZERO, repository.getNonce(cow));
        assertEquals(BigInteger.ZERO, repository.getNonce(horse));

        repository.close();
    }

    @Test
    public void test7() {

        SystemProperties.CONFIG.setDataBaseDir("test_db/" + RepositoryTest.class);
        Repository repository = new RepositoryImpl();
        Repository track = repository.startTracking();

        byte[] cow = Hex.decode("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826");
        byte[] horse = Hex.decode("13978AEE95F38490E9769C39B2773ED763D9CD5F");

        track.addBalance(cow, BigInteger.TEN);
        track.addBalance(horse, BigInteger.ONE);

        assertEquals(BigInteger.TEN, track.getBalance(cow));
        assertEquals(BigInteger.ONE, track.getBalance(horse));

        track.commit();

        assertEquals(BigInteger.TEN, repository.getBalance(cow));
        assertEquals(BigInteger.ONE, repository.getBalance(horse));

        repository.close();
    }


    @Test
    public void test8() {

        SystemProperties.CONFIG.setDataBaseDir("test_db/" + RepositoryTest.class);
        Repository repository = new RepositoryImpl();
        Repository track = repository.startTracking();

        byte[] cow = Hex.decode("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826");
        byte[] horse = Hex.decode("13978AEE95F38490E9769C39B2773ED763D9CD5F");

        track.addBalance(cow, BigInteger.TEN);
        track.addBalance(horse, BigInteger.ONE);

        assertEquals(BigInteger.TEN, track.getBalance(cow));
        assertEquals(BigInteger.ONE, track.getBalance(horse));

        track.rollback();

        assertEquals(BigInteger.ZERO, repository.getBalance(cow));
        assertEquals(BigInteger.ZERO, repository.getBalance(horse));

        repository.close();
    }

    @Test
    public void test9() {

        SystemProperties.CONFIG.setDataBaseDir("test_db/" + RepositoryTest.class);
        Repository repository = new RepositoryImpl();
        Repository track = repository.startTracking();

        byte[] cow = Hex.decode("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826");
        byte[] horse = Hex.decode("13978AEE95F38490E9769C39B2773ED763D9CD5F");

        DataWord cowKey = new DataWord(Hex.decode("A1A2A3"));
        DataWord cowValue = new DataWord(Hex.decode("A4A5A6"));

        DataWord horseKey = new DataWord(Hex.decode("B1B2B3"));
        DataWord horseValue = new DataWord(Hex.decode("B4B5B6"));

        track.addStorageRow(cow, cowKey, cowValue);
        track.addStorageRow(horse, horseKey, horseValue);

        assertEquals(cowValue, track.getStorageValue(cow, cowKey));
        assertEquals(horseValue, track.getStorageValue(horse, horseKey));

        track.commit();

        assertEquals(cowValue, repository.getStorageValue(cow, cowKey));
        assertEquals(horseValue, repository.getStorageValue(horse, horseKey));

        repository.close();
    }

    @Test
    public void test10() {

        SystemProperties.CONFIG.setDataBaseDir("test_db/" + RepositoryTest.class);
        Repository repository = new RepositoryImpl();
        Repository track = repository.startTracking();

        byte[] cow = Hex.decode("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826");
        byte[] horse = Hex.decode("13978AEE95F38490E9769C39B2773ED763D9CD5F");

        DataWord cowKey = new DataWord(Hex.decode("A1A2A3"));
        DataWord cowValue = new DataWord(Hex.decode("A4A5A6"));

        DataWord horseKey = new DataWord(Hex.decode("B1B2B3"));
        DataWord horseValue = new DataWord(Hex.decode("B4B5B6"));

        track.addStorageRow(cow, cowKey, cowValue);
        track.addStorageRow(horse, horseKey, horseValue);

        assertEquals(cowValue, track.getStorageValue(cow, cowKey));
        assertEquals(horseValue, track.getStorageValue(horse, horseKey));

        track.rollback();

        assertEquals(null, repository.getStorageValue(cow, cowKey));
        assertEquals(null, repository.getStorageValue(horse, horseKey));

        repository.close();
    }


    @Test
    public void test11() {

        SystemProperties.CONFIG.setDataBaseDir("test_db/" + RepositoryTest.class);
        Repository repository = new RepositoryImpl();
        Repository track = repository.startTracking();

        byte[] cow = Hex.decode("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826");
        byte[] horse = Hex.decode("13978AEE95F38490E9769C39B2773ED763D9CD5F");

        byte[] cowCode = Hex.decode("A1A2A3");
        byte[] horseCode = Hex.decode("B1B2B3");

        track.saveCode(cow, cowCode);
        track.saveCode(horse, horseCode);

        assertArrayEquals(cowCode, track.getCode(cow));
        assertArrayEquals(horseCode, track.getCode(horse));

        track.commit();

        assertArrayEquals(cowCode, repository.getCode(cow));
        assertArrayEquals(horseCode, repository.getCode(horse));

        repository.close();
    }


    @Test
    public void test12() {

        SystemProperties.CONFIG.setDataBaseDir("test_db/" + RepositoryTest.class);
        Repository repository = new RepositoryImpl();
        Repository track = repository.startTracking();

        byte[] cow = Hex.decode("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826");
        byte[] horse = Hex.decode("13978AEE95F38490E9769C39B2773ED763D9CD5F");

        byte[] cowCode = Hex.decode("A1A2A3");
        byte[] horseCode = Hex.decode("B1B2B3");

        track.saveCode(cow, cowCode);
        track.saveCode(horse, horseCode);

        assertArrayEquals(cowCode, track.getCode(cow));
        assertArrayEquals(horseCode, track.getCode(horse));

        track.rollback();

        assertArrayEquals(null, repository.getCode(cow));
        assertArrayEquals(null, repository.getCode(horse));

        repository.close();
    }

    @Test  // Let's upload genesis pre-mine just like in the real world
    public void test13() {

        SystemProperties.CONFIG.setDataBaseDir("test_db/" + RepositoryTest.class);
        Repository repository = new RepositoryImpl();
        Repository track = repository.startTracking();

        for (String address : Genesis.getPremine()) {
            track.addBalance(Hex.decode(address), Genesis.PREMINE_AMOUNT);
        }

        track.commit();

        assertArrayEquals(Genesis.getInstance().getStateRoot(), repository.getRoot());

        repository.close();
    }


    @Test
    public void test14() {

        SystemProperties.CONFIG.setDataBaseDir("test_db/" + RepositoryTest.class);
        Repository repository = new RepositoryImpl();

        byte[] cow = Hex.decode("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826");
        byte[] horse = Hex.decode("13978AEE95F38490E9769C39B2773ED763D9CD5F");
        final BigInteger ELEVEN = BigInteger.TEN.add(BigInteger.ONE);


        // changes level_1
        Repository track1 = repository.startTracking();
        track1.addBalance(cow, BigInteger.TEN);
        track1.addBalance(horse, BigInteger.ONE);

        assertEquals(BigInteger.TEN, track1.getBalance(cow));
        assertEquals(BigInteger.ONE, track1.getBalance(horse));


        // changes level_2
        Repository track2 = track1.startTracking();
        track2.addBalance(cow, BigInteger.ONE);
        track2.addBalance(horse, BigInteger.TEN);

        assertEquals(ELEVEN, track2.getBalance(cow));
        assertEquals(ELEVEN, track2.getBalance(horse));

        track2.commit();
        track1.commit();

        assertEquals(ELEVEN, repository.getBalance(cow));
        assertEquals(ELEVEN, repository.getBalance(horse));

        repository.close();
    }


    @Test
    public void test15() {

        SystemProperties.CONFIG.setDataBaseDir("test_db/" + RepositoryTest.class);
        Repository repository = new RepositoryImpl();

        byte[] cow = Hex.decode("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826");
        byte[] horse = Hex.decode("13978AEE95F38490E9769C39B2773ED763D9CD5F");
        final BigInteger ELEVEN = BigInteger.TEN.add(BigInteger.ONE);


        // changes level_1
        Repository track1 = repository.startTracking();
        track1.addBalance(cow, BigInteger.TEN);
        track1.addBalance(horse, BigInteger.ONE);

        assertEquals(BigInteger.TEN, track1.getBalance(cow));
        assertEquals(BigInteger.ONE, track1.getBalance(horse));

        // changes level_2
        Repository track2 = track1.startTracking();
        track2.addBalance(cow, BigInteger.ONE);
        track2.addBalance(horse, BigInteger.TEN);

        assertEquals(ELEVEN, track2.getBalance(cow));
        assertEquals(ELEVEN, track2.getBalance(horse));

        track2.rollback();
        track1.commit();

        assertEquals(BigInteger.TEN, repository.getBalance(cow));
        assertEquals(BigInteger.ONE, repository.getBalance(horse));

        repository.close();
    }


    @Test
    public void test16() {

        SystemProperties.CONFIG.setDataBaseDir("test_db/" + RepositoryTest.class);
        Repository repository = new RepositoryImpl();

        byte[] cow = Hex.decode("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826");
        byte[] horse = Hex.decode("13978AEE95F38490E9769C39B2773ED763D9CD5F");

        byte[] cowKey1 = "key-c-1".getBytes();
        byte[] cowValue1 = "val-c-1".getBytes();

        byte[] horseKey1 = "key-h-1".getBytes();
        byte[] horseValue1 = "val-h-1".getBytes();

        byte[] cowKey2 = "key-c-2".getBytes();
        byte[] cowValue2 = "val-c-2".getBytes();

        byte[] horseKey2 = "key-h-2".getBytes();
        byte[] horseValue2 = "val-h-2".getBytes();

        // changes level_1
        Repository track1 = repository.startTracking();
        track1.addStorageRow(cow, new DataWord(cowKey1), new DataWord(cowValue1));
        track1.addStorageRow(horse, new DataWord(horseKey1), new DataWord(horseValue1));

        assertEquals(new DataWord(cowValue1), track1.getStorageValue(cow, new DataWord(cowKey1)));
        assertEquals(new DataWord(horseValue1), track1.getStorageValue(horse, new DataWord(horseKey1)));

        // changes level_2
        Repository track2 = track1.startTracking();
        track2.addStorageRow(cow, new DataWord(cowKey2), new DataWord(cowValue2));
        track2.addStorageRow(horse, new DataWord(horseKey2), new DataWord(horseValue2));

        assertEquals(new DataWord(cowValue1), track2.getStorageValue(cow, new DataWord(cowKey1)));
        assertEquals(new DataWord(horseValue1), track2.getStorageValue(horse, new DataWord(horseKey1)));

        assertEquals(new DataWord(cowValue2), track2.getStorageValue(cow, new DataWord(cowKey2)));
        assertEquals(new DataWord(horseValue2), track2.getStorageValue(horse, new DataWord(horseKey2)));

        track2.commit();
        // leaving level_2

        assertEquals(new DataWord(cowValue1), track1.getStorageValue(cow, new DataWord(cowKey1)));
        assertEquals(new DataWord(horseValue1), track1.getStorageValue(horse, new DataWord(horseKey1)));

        assertEquals(new DataWord(cowValue2), track1.getStorageValue(cow, new DataWord(cowKey2)));
        assertEquals(new DataWord(horseValue2), track1.getStorageValue(horse, new DataWord(horseKey2)));


        track1.commit();
        // leaving level_1

        assertEquals(new DataWord(cowValue1), repository.getStorageValue(cow, new DataWord(cowKey1)));
        assertEquals(new DataWord(horseValue1), repository.getStorageValue(horse, new DataWord(horseKey1)));

        assertEquals(new DataWord(cowValue2), repository.getStorageValue(cow, new DataWord(cowKey2)));
        assertEquals(new DataWord(horseValue2), repository.getStorageValue(horse, new DataWord(horseKey2)));

        repository.close();
    }


    @Test
    public void test17() {

        SystemProperties.CONFIG.setDataBaseDir("test_db/" + RepositoryTest.class);
        Repository repository = new RepositoryImpl();

        byte[] cow = Hex.decode("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826");

        byte[] cowKey1 = "key-c-1".getBytes();
        byte[] cowValue1 = "val-c-1".getBytes();

        // changes level_1
        Repository track1 = repository.startTracking();

        // changes level_2
        Repository track2 = track1.startTracking();
        track2.addStorageRow(cow, new DataWord(cowKey1), new DataWord(cowValue1));
        assertEquals(new DataWord(cowValue1), track2.getStorageValue(cow, new DataWord(cowKey1)));
        track2.rollback();
        // leaving level_2

        track1.commit();
        // leaving level_1

        Assert.assertEquals(Hex.toHexString(HashUtil.EMPTY_TRIE_HASH), Hex.toHexString(repository.getRoot()));
        repository.close();
    }


}
