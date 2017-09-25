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
package org.ethereum.jsontestsuite;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.ethereum.jsontestsuite.suite.JSONReader;
import org.ethereum.jsontestsuite.suite.RLPTestCase;
import org.json.simple.parser.ParseException;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubRLPTest {

    private static final Logger logger = LoggerFactory.getLogger("TCK-Test");
    private static Map<String , RLPTestCase> TEST_SUITE = new HashMap<>();
    private static String commitSHA = "develop";

    @BeforeClass
    public static void init() throws ParseException, IOException {
        logger.info("    Initializing RLP tests...");

        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().
                constructMapType(HashMap.class, String.class, RLPTestCase.class);

        List<String> files = Arrays.asList(
                "RLPTests/rlptest.json",
                "RLPTests/invalidRLPTest.json",
                "RLPTests/RandomRLPTests/example.json"
        );

        List<String> jsons = JSONReader.loadJSONsFromCommit(files, commitSHA);

        for (String json : jsons) {
            Map<String, RLPTestCase> cases = mapper.readValue(json, type);
            TEST_SUITE.putAll(cases);
        }
    }

    @Test
    public void rlpEncodeTest() throws Exception {
        logger.info("    Testing RLP encoding...");

        for (String key : TEST_SUITE.keySet()) {
            logger.info("    " + key);
            RLPTestCase testCase = TEST_SUITE.get(key);
            testCase.doEncode();
            Assert.assertEquals(testCase.getExpected(), testCase.getComputed());
        }
    }

    @Test
    public void rlpDecodeTest() throws Exception {
        logger.info("    Testing RLP decoding...");

        Set<String> excluded = new HashSet<>();

        for (String key : TEST_SUITE.keySet()) {
            if ( excluded.contains(key)) {
                logger.info("[X] " + key);
                continue;
            }
            else {
                logger.info("    " + key);
            }

            RLPTestCase testCase = TEST_SUITE.get(key);
            testCase.doDecode();
            Assert.assertEquals(testCase.getExpected(), testCase.getComputed());
        }
    }
}
