package org.ethereum.jsontestsuite.builder;

import org.ethereum.core.AccountState;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.ContractDetails;
import org.ethereum.db.RepositoryDummy;
import org.ethereum.db.RepositoryImpl;
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
            detailsBatch.put(wrap(parseData(address)), details);
        }

        RepositoryImpl repositoryDummy = new RepositoryImpl(new HashMapDB(), new HashMapDB());
        repositoryDummy.updateBatch(stateBatch, detailsBatch);

        return repositoryDummy;
    }
}
