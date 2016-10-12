package org.ethereum.datasource;

import org.ethereum.core.AccountState;
import org.ethereum.core.Repository;
import org.ethereum.datasource.test.MapDB;
import org.ethereum.datasource.test.RepositoryNew;
import org.ethereum.vm.DataWord;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import static java.math.BigInteger.valueOf;
import static org.spongycastle.util.encoders.Hex.decode;
import static org.spongycastle.util.encoders.Hex.toHexString;

/**
 * Created by Anton Nashatyrev on 12.10.2016.
 */
public class RepoNewTest {

    @Test
    public void test1() throws Exception {

        MapDB<byte[]> stateDb = new MapDB<>();
        RepositoryNew repo = RepositoryNew.createFromStateDS(stateDb, null);
        byte[] addr1 = decode("aaaa");
        byte[] addr2 = decode("bbbb");
        repo.createAccount(addr1);
        repo.addBalance(addr1, BigInteger.ONE);
        repo.createAccount(addr2);
        repo.addBalance(addr2, valueOf(10));
        repo.commit();
        byte[] root1 = repo.getRoot();
        System.out.println(repo.dumpStateTrie());

        System.out.println(toHexString(root1));
        System.out.println(stateDb.getStorage().size());

        RepositoryNew repo1 = RepositoryNew.createFromStateDS(stateDb, root1);
        System.out.println(repo1.getBalance(addr1));
        System.out.println(repo1.getBalance(addr2));
        repo.addBalance(addr2, valueOf(20));
        repo.commit();
        byte[] root2 = repo.getRoot();

        System.out.println(toHexString(root2));
        System.out.println(stateDb.getStorage().size());

        RepositoryNew repo2 = RepositoryNew.createFromStateDS(stateDb, root1);
        System.out.println(repo2.getBalance(addr1));
        System.out.println(repo2.getBalance(addr2));

        RepositoryNew repo2_1 = repo2.startTracking();
        repo2_1.addBalance(addr1, valueOf(3));
        byte[] addr3 = decode("cccc");
        repo2_1.createAccount(addr3);
        repo2_1.addBalance(addr3, valueOf(333));
        repo2_1.delete(addr1);
        repo2_1.commit();
        repo2.commit();

        RepositoryNew repo3 = RepositoryNew.createFromStateDS(stateDb, repo2.getRoot());
        System.out.println(repo3.getBalance(addr1));
        System.out.println(repo3.getBalance(addr2));
        System.out.println(repo3.getBalance(addr3));
    }

    @Test
    public void testStorage1() throws Exception {
        MapDB<byte[]> stateDb = new MapDB<>();
        RepositoryNew repo = RepositoryNew.createFromStateDS(stateDb, null);
        byte[] addr1 = decode("aaaa");
        byte[] addr2 = decode("bbbb");
        repo.createAccount(addr1);
        repo.addStorageRow(addr1, new DataWord(1), new DataWord(111));
        repo.commit();

        byte[] root1 = repo.getRoot();
        System.out.println(repo.dumpStateTrie());
    }
}
