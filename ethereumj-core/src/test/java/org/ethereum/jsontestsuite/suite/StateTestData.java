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

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.ethereum.jsontestsuite.GitHubJSONTestSuite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mikhail Kalinin
 * @since 09.08.2017
 */
public class StateTestData {

    Map<String, StateTestDataEntry> testData;

    public StateTestData(String json) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().
                constructMapType(HashMap.class, String.class, StateTestDataEntry.class);

        testData = new ObjectMapper().readValue(json, type);
    }

    public List<StateTestCase> getTestCases(GitHubJSONTestSuite.Network network) {

        List<StateTestCase> cases = new ArrayList<>();

        for (Map.Entry<String, StateTestDataEntry> e : testData.entrySet()) {
            List<StateTestCase> testCases = e.getValue().getTestCases(network);
            for (int i = 0; i < testCases.size(); i++) {
                StateTestCase testCase = testCases.get(i);
                testCase.setName(String.format("%s_%s%s", e.getKey(), network.name(),
                        testCases.size() > 1 ? "_" + String.valueOf(i + 1) : ""));
            }
            cases.addAll(testCases);
        }

        return cases;
    }

}
