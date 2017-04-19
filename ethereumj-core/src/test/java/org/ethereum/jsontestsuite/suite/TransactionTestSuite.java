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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TransactionTestSuite {

    private Logger logger = LoggerFactory.getLogger("TCK-Test");

    Map<String, TransactionTestCase> testCases = new HashMap<>();

    public TransactionTestSuite(String json) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().
                constructMapType(HashMap.class, String.class, TransactionTestCase.class);

        testCases = new ObjectMapper().readValue(json, type);
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
