/*
 * Copyright 2015, 2016 Ether.Camp Inc. (US)
 * This file is part of Ethereum Harmony.
 *
 * Ethereum Harmony is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ethereum Harmony is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ethereum Harmony.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ethereum.config.blockchain;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.validator.BlockHeaderRule;
import org.ethereum.validator.BlockHeaderValidator;
import org.ethereum.validator.ExtraDataPresenceRule;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Stan Reshetnyk on 26.12.16.
 */
public abstract class AbstractDaoConfig extends HomesteadConfig {

    /**
     * Hardcoded values from live network
     */
    public static final long ETH_FORK_BLOCK_NUMBER = 1_920_000;

    protected long forkBlockNumber;
    protected byte[] DAO_EXTRA_DATA = Hex.decode("64616f2d686172642d666f726b");

    protected List<Pair<Long, BlockHeaderValidator>> VALIDATOR;

    protected void initDaoConfig(long forkBlockNumber, boolean supportFork) {
        this.forkBlockNumber = forkBlockNumber;
        BlockHeaderRule rule = new ExtraDataPresenceRule(DAO_EXTRA_DATA, supportFork);
        VALIDATOR = Arrays.asList(Pair.of(forkBlockNumber, new BlockHeaderValidator(Arrays.asList(rule))));
    }
}
