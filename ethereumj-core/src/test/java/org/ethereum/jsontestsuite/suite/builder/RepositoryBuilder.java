package org.ethereum.jsontestsuite.suite.builder;

import org.ethereum.core.AccountState;
import org.ethereum.core.Repository;
import org.ethereum.datasource.test.MapDB;
import org.ethereum.datasource.test.RepositoryRoot;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.ContractDetails;
import org.ethereum.jsontestsuite.suite.ContractDetailsCacheImpl;
import org.ethereum.jsontestsuite.suite.model.AccountTck;

import java.util.HashMap;
import java.util.Map;

import static org.ethereum.jsontestsuite.suite.Utils.parseData;
import static org.ethereum.util.ByteUtil.wrap;

public class RepositoryBuilder {

    public static Repository build(Map<String, AccountTck> accounts){
        HashMap<ByteArrayWrapper, AccountState> stateBatch = new HashMap<>();
        HashMap<ByteArrayWrapper, ContractDetails> detailsBatch = new HashMap<>();

        for (String address : accounts.keySet()) {

            AccountTck accountTCK = accounts.get(address);
            AccountBuilder.StateWrap stateWrap = AccountBuilder.build(accountTCK);

            AccountState state = stateWrap.getAccountState();
            ContractDetails details = stateWrap.getContractDetails();

            stateBatch.put(wrap(parseData(address)), state);

            ContractDetailsCacheImpl detailsCache = new ContractDetailsCacheImpl(details);
            detailsCache.setDirty(true);

            detailsBatch.put(wrap(parseData(address)), detailsCache);
        }

        RepositoryRoot repositoryDummy = new RepositoryRoot(new MapDB<byte[]>());
        Repository track = repositoryDummy.startTracking();

        track.updateBatch(stateBatch, detailsBatch);
        track.commit();
        repositoryDummy.commit();

        return repositoryDummy;
    }
}
