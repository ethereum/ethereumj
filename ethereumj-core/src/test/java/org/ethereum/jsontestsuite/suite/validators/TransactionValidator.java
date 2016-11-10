package org.ethereum.jsontestsuite.suite.validators;

import org.ethereum.core.Transaction;

import java.util.ArrayList;

import static org.ethereum.util.ByteUtil.toHexString;

public class TransactionValidator {


    public static ArrayList<String> valid(Transaction orig, Transaction valid) {

        ArrayList<String> outputSummary = new ArrayList<>();

        if (orig == null && valid == null) {
            return outputSummary;
        }

        if (orig != null && valid == null) {

            String output ="Transaction expected to be not valid";

            outputSummary.add(output);
            return outputSummary;
        }

        if (orig == null && valid != null) {

            String output ="Transaction expected to be valid";

            outputSummary.add(output);
            return outputSummary;
        }

        if (!toHexString(orig.getEncoded())
                .equals(toHexString(valid.getEncoded()))) {

            String output =
                    String.format("Wrong transaction.encoded: \n expected: %s \n got: %s",
                            toHexString(valid.getEncoded()),
                            toHexString(orig.getEncoded())
                    );

            outputSummary.add(output);
        }

        return outputSummary;
    }
}
