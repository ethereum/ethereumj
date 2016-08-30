package org.ethereum.jsontestsuite.suite.builder;

import org.ethereum.core.AccountState;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.db.*;
import org.ethereum.core.Repository;
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

        RepositoryImpl repositoryDummy = new RepositoryImpl(new HashMapDB().setClearOnClose(false),
                new HashMapDB().setClearOnClose(false));
        Repository track = repositoryDummy.startTracking();
        track.updateBatch(stateBatch, detailsBatch);
        track.commit();

        return repositoryDummy;
    }
}
