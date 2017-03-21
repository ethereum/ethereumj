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

import org.ethereum.core.BlockIdentifier;
import org.ethereum.net.eth.message.EthMessageCodes;
import org.ethereum.net.eth.message.NewBlockHashesMessage;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.spongycastle.util.encoders.Hex.decode;
import static org.spongycastle.util.encoders.Hex.toHexString;

/**
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
public class NewBlockHashesMessageTest {

    @Test /* NewBlockHashesMessage 1 from new */
    public void test_1() {

        List<BlockIdentifier> identifiers = Arrays.asList(
                new BlockIdentifier(decode("4ee6424d776b3f59affc20bc2de59e67f36e22cc07897ff8df152242c921716b"), 1),
                new BlockIdentifier(decode("7d2fe4df0dbbc9011da2b3bf177f0c6b7e71a11c509035c5d751efa5cf9b4817"), 2)
        );

        NewBlockHashesMessage newBlockHashesMessage = new NewBlockHashesMessage(identifiers);
        System.out.println(newBlockHashesMessage);

        String expected = "f846e2a04ee6424d776b3f59affc20bc2de59e67f36e22cc07897ff8df152242c921716b01e2a07d2fe4df0dbbc9011da2b3bf177f0c6b7e71a11c509035c5d751efa5cf9b481702";
        assertEquals(expected, toHexString(newBlockHashesMessage.getEncoded()));

        assertEquals(EthMessageCodes.NEW_BLOCK_HASHES, newBlockHashesMessage.getCommand());
        assertEquals(2, newBlockHashesMessage.getBlockIdentifiers().size());

        assertEquals(null, newBlockHashesMessage.getAnswerMessage());
    }
}
