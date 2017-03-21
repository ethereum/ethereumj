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
package org.ethereum.datasource;

import org.ethereum.core.AccountState;
import org.ethereum.core.Repository;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.db.RepositoryRoot;
import org.ethereum.vm.DataWord;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.valueOf;
import static org.spongycastle.util.encoders.Hex.decode;
import static org.spongycastle.util.encoders.Hex.toHexString;

/**
 * Created by Anton Nashatyrev on 12.10.2016.
 */
public class RepoNewTest {

    @Test
    public void test1() throws Exception {

        Source<byte[], byte[]> stateDb = new NoDeleteSource<>(new HashMapDB<byte[]>());
        RepositoryRoot repo = new RepositoryRoot(stateDb, null);
        byte[] addr1 = decode("aaaa");
        byte[] addr2 = decode("bbbb");
        repo.createAccount(addr1);
        repo.addBalance(addr1, BigInteger.ONE);
        repo.createAccount(addr2);
        repo.addBalance(addr2, valueOf(10));
        repo.commit();
        byte[] root1 = repo.getRoot();
        System.out.println(repo.dumpStateTrie());

        System.out.println("Root: " + toHexString(root1));
//        System.out.println("Storage size: " + stateDb.getStorage().size());

        RepositoryRoot repo1 = new RepositoryRoot(stateDb, root1);
        Assert.assertEquals(repo1.getBalance(addr1), valueOf(1));
        Assert.assertEquals(repo1.getBalance(addr2), valueOf(10));

        repo.addBalance(addr2, valueOf(20));
        repo.commit();
        byte[] root2 = repo.getRoot();

        System.out.println("Root: " + toHexString(root2));
//        System.out.println("Storage size: " + stateDb.getStorage().size());

        RepositoryRoot repo2 = new RepositoryRoot(stateDb, root1);
        System.out.println(repo2.dumpStateTrie());
        Assert.assertEquals(repo2.getBalance(addr1), valueOf(1));
        Assert.assertEquals(repo2.getBalance(addr2), valueOf(10));

        repo2 = new RepositoryRoot(stateDb, root2);
        Assert.assertEquals(repo2.getBalance(addr1), valueOf(1));
        Assert.assertEquals(repo2.getBalance(addr2), valueOf(30));

        Repository repo2_1 = repo2.startTracking();
        repo2_1.addBalance(addr2, valueOf(3));
        byte[] addr3 = decode("cccc");
        repo2_1.createAccount(addr3);
        repo2_1.addBalance(addr3, valueOf(333));
        repo2_1.delete(addr1);
        Assert.assertEquals(repo2_1.getBalance(addr1), valueOf(0));
        Assert.assertEquals(repo2_1.getBalance(addr2), valueOf(33));
        Assert.assertEquals(repo2_1.getBalance(addr3), valueOf(333));
        Assert.assertEquals(repo2.getBalance(addr1), valueOf(1));
        Assert.assertEquals(repo2.getBalance(addr2), valueOf(30));
        Assert.assertEquals(repo2.getBalance(addr3), valueOf(0));
        repo2_1.commit();
        Assert.assertEquals(repo2.getBalance(addr1), valueOf(0));
        Assert.assertEquals(repo2.getBalance(addr2), valueOf(33));
        Assert.assertEquals(repo2.getBalance(addr3), valueOf(333));
//        byte[] root21 = repo2.getRoot();
        repo2.commit();
        byte[] root22 = repo2.getRoot();
        Assert.assertEquals(repo2.getBalance(addr1), valueOf(0));
        Assert.assertEquals(repo2.getBalance(addr2), valueOf(33));
        Assert.assertEquals(repo2.getBalance(addr3), valueOf(333));

        RepositoryRoot repo3 = new RepositoryRoot(stateDb, root22);
        System.out.println(repo3.getBalance(addr1));
        System.out.println(repo3.getBalance(addr2));
        System.out.println(repo3.getBalance(addr3));
        Assert.assertEquals(repo3.getBalance(addr1), valueOf(0));
        Assert.assertEquals(repo3.getBalance(addr2), valueOf(33));
        Assert.assertEquals(repo3.getBalance(addr3), valueOf(333));

        Repository repo3_1 = repo3.startTracking();
        repo3_1.addBalance(addr1, valueOf(10));

        Repository repo3_1_1 = repo3_1.startTracking();
        repo3_1_1.addBalance(addr1, valueOf(20));
        repo3_1_1.addBalance(addr2, valueOf(10));
        Assert.assertEquals(repo3.getBalance(addr1), valueOf(0));
        Assert.assertEquals(repo3.getBalance(addr2), valueOf(33));
        Assert.assertEquals(repo3.getBalance(addr3), valueOf(333));
        Assert.assertEquals(repo3_1.getBalance(addr1), valueOf(10));
        Assert.assertEquals(repo3_1.getBalance(addr2), valueOf(33));
        Assert.assertEquals(repo3_1.getBalance(addr3), valueOf(333));
        Assert.assertEquals(repo3_1_1.getBalance(addr1), valueOf(30));
        Assert.assertEquals(repo3_1_1.getBalance(addr2), valueOf(43));
        Assert.assertEquals(repo3_1_1.getBalance(addr3), valueOf(333));

        repo3_1_1.commit();
        Assert.assertEquals(repo3.getBalance(addr1), valueOf(0));
        Assert.assertEquals(repo3.getBalance(addr2), valueOf(33));
        Assert.assertEquals(repo3.getBalance(addr3), valueOf(333));
        Assert.assertEquals(repo3_1.getBalance(addr1), valueOf(30));
        Assert.assertEquals(repo3_1.getBalance(addr2), valueOf(43));
        Assert.assertEquals(repo3_1.getBalance(addr3), valueOf(333));

        repo3_1.commit();
        Assert.assertEquals(repo3.getBalance(addr1), valueOf(30));
        Assert.assertEquals(repo3.getBalance(addr2), valueOf(43));
        Assert.assertEquals(repo3.getBalance(addr3), valueOf(333));

        byte[] addr4 = decode("dddd");
        Repository repo3_2 = repo3.startTracking();
        repo3_2.addBalance(addr4, ONE);
        repo3_2.rollback();
        AccountState state = repo3.getAccountState(addr4);
        Assert.assertNull(state);

        Repository repo3_3 = repo3.startTracking();
        repo3_3.addBalance(addr4, ONE);
        repo3_3.commit();
        state = repo3.getAccountState(addr4);
        Assert.assertNotNull(state);
    }

    @Test
    public void testStorage1() throws Exception {
        HashMapDB<byte[]> stateDb = new HashMapDB<>();
        RepositoryRoot repo = new RepositoryRoot(stateDb, null);
        byte[] addr1 = decode("aaaa");
        repo.createAccount(addr1);
        repo.addStorageRow(addr1, new DataWord(1), new DataWord(111));
        repo.commit();

        byte[] root1 = repo.getRoot();
        System.out.println(repo.dumpStateTrie());

        RepositoryRoot repo2 = new RepositoryRoot(stateDb, root1);
        DataWord val1 = repo.getStorageValue(addr1, new DataWord(1));
        assert new DataWord(111).equals(val1);

        Repository repo3 = repo2.startTracking();
        repo3.addStorageRow(addr1, new DataWord(2), new DataWord(222));
        repo3.addStorageRow(addr1, new DataWord(1), new DataWord(333));
        assert new DataWord(333).equals(repo3.getStorageValue(addr1, new DataWord(1)));
        assert new DataWord(222).equals(repo3.getStorageValue(addr1, new DataWord(2)));
        assert new DataWord(111).equals(repo2.getStorageValue(addr1, new DataWord(1)));
        Assert.assertNull(repo2.getStorageValue(addr1, new DataWord(2)));
        repo3.commit();
        assert new DataWord(333).equals(repo2.getStorageValue(addr1, new DataWord(1)));
        assert new DataWord(222).equals(repo2.getStorageValue(addr1, new DataWord(2)));
        repo2.commit();

        RepositoryRoot repo4 = new RepositoryRoot(stateDb, repo2.getRoot());
        assert new DataWord(333).equals(repo4.getStorageValue(addr1, new DataWord(1)));
        assert new DataWord(222).equals(repo4.getStorageValue(addr1, new DataWord(2)));
    }

    @Test
    public void testStorage2() throws Exception {
        RepositoryRoot repo = new RepositoryRoot(new HashMapDB<byte[]>());

        Repository repo1 = repo.startTracking();
        byte[] addr2 = decode("bbbb");
        repo1.addStorageRow(addr2, new DataWord(1), new DataWord(111));
        repo1.commit();

        Assert.assertEquals(new DataWord(111), repo.getStorageValue(addr2, new DataWord(1)));
    }
}
