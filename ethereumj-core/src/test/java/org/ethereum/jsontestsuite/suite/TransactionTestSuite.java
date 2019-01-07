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
package org.ethereum.jsontestsuite.suite;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ethereum.jsontestsuite.GitHubJSONTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TransactionTestSuite {

    private Logger logger = LoggerFactory.getLogger("TCK-Test");

    Map<String, TransactionTestCase> testCases = new HashMap<>();

    public TransactionTestSuite(String json) throws IOException {
        Map<String, TransactionTestCase> testCases = new HashMap<>();

        Map<String, Object> jsonData = (Map<String, Object>) new ObjectMapper().readValue(json, Object.class);
        for (Map.Entry<String, Object> currentCase : jsonData.entrySet()) {
            String namePrefix = currentCase.getKey() + "_";
            Map<String, TransactionTestCase> currentCases = new HashMap<>();
            Map<String, Object> networksEtc = (Map<String, Object>) currentCase.getValue();
            String rlp = null;

            for (Map.Entry<String, Object> currentField : networksEtc.entrySet()) {
                String currentKey = currentField.getKey();
                GitHubJSONTestSuite.Network maybeNetwork = null;
                try {
                    maybeNetwork = GitHubJSONTestSuite.Network.valueOf(currentKey);
                } catch (IllegalArgumentException ex) {
                    // just not filling maybeNetwork
                }

                if (maybeNetwork != null) {
                    TransactionTestCase txCase = new TransactionTestCase();
                    txCase.setNetwork(maybeNetwork);
                    Map<String, Object> txCaseMap = (Map<String, Object>) currentField.getValue();
                    for (String key : txCaseMap.keySet()) {
                        if (key.equals("hash")) {
                            txCase.setExpectedHash((String) txCaseMap.get(key));
                        } else if (key.equals("sender")) {
                            txCase.setExpectedRlp((String) txCaseMap.get(key));
                        } else {
                            String error = String.format("Expected transaction is not fully parsed, key \"%s\" missed!", key);
                            throw new RuntimeException(error);
                        }
                    }
                    currentCases.put(namePrefix + currentKey, txCase);
                } else if (currentKey.equals("rlp")) {
                    rlp = (String) currentField.getValue();
                } else if (currentKey.equals("_info")) {
                    // legal key, just skip
                } else {
                    String error = String.format("Transaction test case is not fully parsed, key \"%s\" missed!", currentKey);
                    throw new RuntimeException(error);
                }
            }

            for (TransactionTestCase testCase : currentCases.values()) {
                testCase.setRlp(rlp);
            }
            testCases.putAll(currentCases);
        }

        this.testCases = testCases;
    }

    public Map<String, TransactionTestCase> getTestCases() {
        return testCases;
    }

    @Override
    public String toString() {
        return "TransactionTestSuite{" +
                "testCases=" + testCases +
                '}';
    }
}
