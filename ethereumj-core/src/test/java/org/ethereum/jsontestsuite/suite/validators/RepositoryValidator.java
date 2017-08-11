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
package org.ethereum.jsontestsuite.suite.validators;

import org.ethereum.core.AccountState;
import org.ethereum.db.ContractDetails;
import org.ethereum.core.Repository;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.ethereum.util.ByteUtil.difference;

public class RepositoryValidator {

    public static List<String> valid(Repository currentRepository, Repository postRepository) {

        List<String> results = new ArrayList<>();

        Set<byte[]> expectedKeys = postRepository.getAccountsKeys();

        for (byte[] key : expectedKeys) {
            // force to load known accounts to cache to enumerate them
            currentRepository.getAccountState(key);
        }

        Set<byte[]> currentKeys = currentRepository.getAccountsKeys();

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

        // Compare roots
        results.addAll(validRoot(
                Hex.toHexString(currentRepository.getRoot()),
                Hex.toHexString(postRepository.getRoot()))
        );

        return results;
    }

    public static List<String> validRoot(String currRoot, String postRoot) {

        List<String> results = new ArrayList<>();
        if (!postRoot.equals(currRoot)){

            String formattedString = String.format("Root hash don't much: expected: %s current: %s",
                    postRoot, currRoot);
            results.add(formattedString);
        }

        return results;
    }

}
