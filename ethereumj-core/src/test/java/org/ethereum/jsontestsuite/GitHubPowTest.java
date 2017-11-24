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

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;

import org.ethereum.core.BlockHeader;
import org.ethereum.jsontestsuite.suite.EthashTestCase;
import org.ethereum.jsontestsuite.suite.EthashTestSuite;
import org.ethereum.jsontestsuite.suite.JSONReader;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mikhail Kalinin
 * @since 03.09.2015
 */
public class GitHubPowTest {

    private static final Logger logger = LoggerFactory.getLogger("TCK-Test");
    public String shacommit = "b09975f0a37afe306f52d5b771f5e3836f53c8bc";

    @Test
    public void runEthashTest() throws IOException {

        String json = JSONReader.loadJSONFromCommit("PoWTests/ethash_tests.json", shacommit);

        EthashTestSuite testSuite = new EthashTestSuite(json);

        for (EthashTestCase testCase : testSuite.getTestCases()) {

            logger.info("Running {}\n", testCase.getName());

            BlockHeader header = testCase.getBlockHeader();

            assertArrayEquals(testCase.getResultBytes(), header.calcPowValue());
        }

    }
}
