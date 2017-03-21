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
