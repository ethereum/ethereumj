package org.ethereum.jsontestsuite.builder;

import org.ethereum.core.AccountState;
import org.ethereum.db.ContractDetailsImpl;
import org.ethereum.jsontestsuite.model.AccountTck;
import org.ethereum.util.Utils;
import org.ethereum.vm.DataWord;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.jsontestsuite.Utils.parseData;
import static org.ethereum.util.Utils.unifiedNumericToBigInteger;

public class AccountBuilder {

    public static StateWrap build(AccountTck account) {

        ContractDetailsImpl details = new ContractDetailsImpl();
        details.setCode(parseData(account.getCode()));
        details.setStorage(convertStorage(account.getStorage()));

        AccountState state = new AccountState();

        state.addToBalance(unifiedNumericToBigInteger(account.getBalance()));
        state.setNonce(unifiedNumericToBigInteger(account.getNonce()));
        state.setStateRoot(details.getStorageHash());
        state.setCodeHash(sha3(details.getCode()));

        return new StateWrap(state, details);
    }


    private static Map<DataWord, DataWord> convertStorage(Map<String, String> storageTck) {

        Map<DataWord, DataWord> storage = new HashMap<>();

        for (String keyTck : storageTck.keySet()) {
            String valueTck = storageTck.get(keyTck);

            DataWord key = new DataWord(parseData(keyTck));
            DataWord value = new DataWord(parseData(valueTck));

            storage.put(key, value);
        }

        return storage;
    }


    public static class StateWrap {

        AccountState accountState;
        ContractDetailsImpl contractDetails;

        public StateWrap(AccountState accountState, ContractDetailsImpl contractDetails) {
            this.accountState = accountState;
            this.contractDetails = contractDetails;
        }

        public AccountState getAccountState() {
            return accountState;
        }

        public ContractDetailsImpl getContractDetails() {
            return contractDetails;
        }
    }
}
