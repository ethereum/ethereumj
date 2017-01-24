package org.ethereum.jsontestsuite.suite;

import org.ethereum.core.*;
import org.ethereum.db.RepositoryImpl;

import java.math.BigInteger;

/**
 * Repository for running GitHubVMTest.
 * The slightly modified behavior from original impl:
 * it creates empty account whenever it 'touched': getCode() or getBalance()
 * Created by Anton Nashatyrev on 03.11.2016.
 */
public class EnvTestRepository extends IterableTestRepository {

    public EnvTestRepository(Repository src) {
        super(src);
    }

    public BigInteger setNonce(byte[] addr, BigInteger nonce) {
        if (!(src instanceof RepositoryImpl)) throw new RuntimeException("Not supported");
        return ((RepositoryImpl) src).setNonce(addr, nonce);
    }


    @Override
    public byte[] getCode(byte[] addr) {
        addAccount(addr);
        return src.getCode(addr);
    }

    @Override
    public BigInteger getBalance(byte[] addr) {
        addAccount(addr);
        return src.getBalance(addr);
    }
}
