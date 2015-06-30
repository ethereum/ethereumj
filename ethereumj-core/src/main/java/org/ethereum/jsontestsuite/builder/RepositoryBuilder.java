package org.ethereum.jsontestsuite.builder;

import org.ethereum.core.AccountState;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.db.*;
import org.ethereum.facade.Repository;
import org.ethereum.jsontestsuite.model.AccountTck;

import java.util.HashMap;
import java.util.Map;

import static org.ethereum.jsontestsuite.Utils.parseData;
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

        RepositoryImpl repositoryDummy = new RepositoryImpl(new HashMapDB(), new HashMapDB());
        repositoryDummy.updateBatch(stateBatch, detailsBatch);
        repositoryDummy.flush();

        return repositoryDummy;
    }
}
