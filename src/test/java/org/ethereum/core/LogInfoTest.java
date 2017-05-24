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
package org.ethereum.core;

import org.ethereum.vm.LogInfo;

import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.util.encoders.Hex;

import org.junit.Ignore;
import static org.junit.Assert.assertEquals;

/**
 * @author Roman Mandeleil
 * @since 05.12.2014
 */
public class LogInfoTest {

    private static final Logger logger = LoggerFactory.getLogger("test");

    @Test // rlp decode
    public void test_1() {

        //   LogInfo{address=d5ccd26ba09ce1d85148b5081fa3ed77949417be, topics=[000000000000000000000000459d3a7595df9eba241365f4676803586d7d199c 436f696e73000000000000000000000000000000000000000000000000000000 ], data=}
        byte[] rlp = Hex.decode("f85a94d5ccd26ba09ce1d85148b5081fa3ed77949417bef842a0000000000000000000000000459d3a7595df9eba241365f4676803586d7d199ca0436f696e7300000000000000000000000000000000000000000000000000000080");
        LogInfo logInfo = new LogInfo(rlp);

        assertEquals("d5ccd26ba09ce1d85148b5081fa3ed77949417be",
                Hex.toHexString(logInfo.getAddress()));
        assertEquals("", Hex.toHexString(logInfo.getData()));

        assertEquals("000000000000000000000000459d3a7595df9eba241365f4676803586d7d199c",
                logInfo.getTopics().get(0).toString());
        assertEquals("436f696e73000000000000000000000000000000000000000000000000000000",
                logInfo.getTopics().get(1).toString());

        logger.info("{}", logInfo);
    }

    @Test // rlp decode
    public void test_2() {

        LogInfo log = new LogInfo(Hex.decode("d5ccd26ba09ce1d85148b5081fa3ed77949417be"), null, null);
        assertEquals("d794d5ccd26ba09ce1d85148b5081fa3ed77949417bec080", Hex.toHexString(log.getEncoded()));

        logger.info("{}", log);
    }

    @Ignore //TODO #POC9
    @Test // rlp decode
    public void test_3() {

        //   LogInfo{address=f2b1a404bcb6112a0ff2c4197cb02f3de40018b3, topics=[5a360139cff27713da0fe18a2100048a7ce1b7700c953a82bc3ff011437c8c2a 588d7ddcc06c14843ea68e690dfd4ec91ba09a8ada15c5b7fa6fead9c8befe4b ], data=}
        byte[] rlp = Hex.decode("f85a94f2b1a404bcb6112a0ff2c4197cb02f3de40018b3f842a05a360139cff27713da0fe18a2100048a7ce1b7700c953a82bc3ff011437c8c2aa0588d7ddcc06c14843ea68e690dfd4ec91ba09a8ada15c5b7fa6fead9c8befe4b80");
        LogInfo logInfo = new LogInfo(rlp);

        assertEquals("f2b1a404bcb6112a0ff2c4197cb02f3de40018b3",
                Hex.toHexString(logInfo.getAddress()));

        assertEquals("00800000000000000010000000000000000000000000002000000000000000000012000000100000000050000020000000000000000000000000000000000000",
                logInfo.getBloom().toString());

        assertEquals("f85a94f2b1a404bcb6112a0ff2c4197cb02f3de40018b3f842a05a360139cff27713da0fe18a2100048a7ce1b7700c953a82bc3ff011437c8c2aa0588d7ddcc06c14843ea68e690dfd4ec91ba09a8ada15c5b7fa6fead9c8befe4b80",
                Hex.toHexString(logInfo.getEncoded()));

        logger.info("{}", logInfo);
    }
}
