package org.ethereum.jsontestsuite.suite.validators;

import org.ethereum.core.AccountState;
import org.ethereum.db.ContractDetails;
import org.ethereum.vm.DataWord;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.*;

public class AccountValidator {


    public static List<String> valid(String address, AccountState expectedState, ContractDetails expectedDetails,
                             AccountState currentState, ContractDetails currentDetails){

        List<String> results = new ArrayList<>();

        if (currentState == null || currentDetails == null){
            String formattedString = String.format("Account: %s: expected but doesn't exist",
                    address);
            results.add(formattedString);
            return results;
        }

        if (expectedState == null || expectedDetails == null){
            String formattedString = String.format("Account: %s: unexpected account in the repository",
                    address);
            results.add(formattedString);
            return results;
        }


        BigInteger expectedBalance = expectedState.getBalance();
        if (currentState.getBalance().compareTo(expectedBalance) != 0) {
            String formattedString = String.format("Account: %s: has unexpected balance, expected balance: %s found balance: %s",
                    address, expectedBalance.toString(), currentState.getBalance().toString());
            results.add(formattedString);
        }

        BigInteger expectedNonce = expectedState.getNonce();
        if (currentState.getNonce().compareTo(expectedNonce) != 0) {
            String formattedString = String.format("Account: %s: has unexpected nonce, expected nonce: %s found nonce: %s",
                    address, expectedNonce.toString(), currentState.getNonce().toString());
            results.add(formattedString);
        }

        byte[] code = currentDetails.getCode(currentState.getCodeHash());
        if (!Arrays.equals(expectedDetails.getCode(), code)) {
            String formattedString = String.format("Account: %s: has unexpected code, expected code: %s found code: %s",
                    address, Hex.toHexString(expectedDetails.getCode()), Hex.toHexString(currentDetails.getCode()));
            results.add(formattedString);
        }


        // compare storage
        Set<DataWord> currentKeys = currentDetails.getStorage().keySet();
        Set<DataWord> expectedKeys = expectedDetails.getStorage().keySet();
        Set<DataWord> checked = new HashSet<>();

        for (DataWord key : currentKeys) {

            DataWord currentValue = currentDetails.getStorage().get(key);
            DataWord expectedValue = expectedDetails.getStorage().get(key);
            if (expectedValue == null) {

                String formattedString = String.format("Account: %s: has unexpected storage data: %s = %s",
                        address,
                        key,
                        currentValue);

                results.add(formattedString);
                continue;
            }

            if (!expectedValue.equals(currentValue)) {

                String formattedString = String.format("Account: %s: has unexpected value, for key: %s , expectedValue: %s real value: %s",
                        address,
                        key.toString(),
                        expectedValue.toString(), currentValue.toString());
                results.add(formattedString);
                continue;
            }

            checked.add(key);
        }

        for (DataWord key : expectedKeys) {
            if (!checked.contains(key)) {
                String formattedString = String.format("Account: %s: doesn't exist expected storage key: %s",
                        address, key.toString());
                results.add(formattedString);
            }
        }

        return results;
    }
}
