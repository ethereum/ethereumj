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
package org.ethereum.net;

import org.ethereum.net.eth.message.StatusMessage;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertEquals;

public class StatusMessageTest {

    /* STATUS_MESSAGE */
    private static final Logger logger = LoggerFactory.getLogger("test");

    @Test // Eth 60
    public void test1() {

        byte[] payload = Hex.decode("f84927808425c60144a0832056d3c93ff2739ace7199952e5365aa29f18805be05634c4db125c5340216a0955f36d073ccb026b78ab3424c15cf966a7563aa270413859f78702b9e8e22cb");
        StatusMessage statusMessage = new StatusMessage(payload);

        logger.info(statusMessage.toString());

        assertEquals(39, statusMessage.getProtocolVersion());
        assertEquals("25c60144",
                Hex.toHexString(statusMessage.getTotalDifficulty()));
        assertEquals("832056d3c93ff2739ace7199952e5365aa29f18805be05634c4db125c5340216",
                Hex.toHexString(statusMessage.getBestHash()));
    }

}

