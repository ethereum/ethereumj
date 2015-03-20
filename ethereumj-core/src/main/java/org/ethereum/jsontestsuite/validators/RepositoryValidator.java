package org.ethereum.jsontestsuite.validators;

import com.google.common.collect.Sets;
import org.ethereum.core.AccountState;
import org.ethereum.db.ContractDetails;
import org.ethereum.facade.Repository;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.ethereum.util.ByteUtil.difference;

public class RepositoryValidator {

    public static List<String> valid(Repository currentRepository, Repository postRepository) {

        List<String> results = new ArrayList<>();

        Set<byte[]> currentKeys = currentRepository.getAccountsKeys();
        Set<byte[]> expectedKeys = postRepository.getAccountsKeys();

        if (expectedKeys.size() != currentKeys.size()) {

            String out =
                    String.format("The size of the repository is invalid \n expected: %d, \n current: %d",
                            expectedKeys.size(), currentKeys.size());
            results.add(out);
        }

        for (byte[] address : currentKeys) {

            AccountState state = currentRepository.getAccountState(address);
            ContractDetails details = currentRepository.getContractDetails(address);

            AccountState postState = postRepository.getAccountState(address);
            ContractDetails postDetails = postRepository.getContractDetails(address);

            List<String> accountResult =
                AccountValidator.valid(Hex.toHexString(address), postState, postDetails, state, details);

            results.addAll(accountResult);
        }

        Set<byte[]> expectedButAbsent = difference(expectedKeys, currentKeys);
        for (byte[] address : expectedButAbsent){
            String formattedString = String.format("Account: %s: expected but doesn't exist",
                    Hex.toHexString(address));
            results.add(formattedString);
        }

        return results;
    }

}
