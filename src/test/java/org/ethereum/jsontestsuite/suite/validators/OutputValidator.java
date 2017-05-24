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

import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

import static org.ethereum.jsontestsuite.suite.Utils.parseData;

public class OutputValidator {

    public static List<String> valid(String origOutput, String postOutput){

        List<String> results = new ArrayList<>();

        if (postOutput.startsWith("#")) {
            int postLen = Integer.parseInt(postOutput.substring(1));
            if (postLen != origOutput.length() / 2) {
                results.add("Expected output length: " + postLen + ", actual: " + origOutput.length() / 2);
            }
        } else {
            String postOutputFormated = Hex.toHexString(parseData(postOutput));

            if (!origOutput.equals(postOutputFormated)) {
                String formattedString = String.format("HReturn: wrong expected: %s, current: %s",
                        postOutputFormated, origOutput);
                results.add(formattedString);
            }
        }

        return results;
    }

}
