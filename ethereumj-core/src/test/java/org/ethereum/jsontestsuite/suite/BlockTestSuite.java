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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class BlockTestSuite {

    private Logger logger = LoggerFactory.getLogger("TCK-Test");

    Map<String, BlockTestCase> testCases = new HashMap<>();

    public BlockTestSuite(String json) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().
                constructMapType(HashMap.class, String.class, BlockTestCase.class);

        testCases = new ObjectMapper().readValue(json, type);
    }

    public Map<String, BlockTestCase> getTestCases() {
        return testCases;
    }

    public Map<String, BlockTestCase> getTestCases(GitHubJSONTestSuite.Network[] networks) {

        Set<GitHubJSONTestSuite.Network> nets = new HashSet<>(Arrays.asList(networks));
        Map<String, BlockTestCase> filtered = new HashMap<>();

        for (Map.Entry<String, BlockTestCase> testCase : testCases.entrySet()) {
            if (nets.contains(testCase.getValue().getNetwork())) {
                filtered.put(testCase.getKey(), testCase.getValue());
            }
        }

        return filtered;
    }

    @Override
    public String toString() {
        return "BlockTestSuite{" +
                "testCases=" + testCases +
                '}';
    }
}
