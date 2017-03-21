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
package org.ethereum.config.blockchain;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.Constants;
import org.ethereum.core.BlockHeader;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.validator.BlockHeaderRule;
import org.ethereum.validator.BlockHeaderValidator;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 18.07.2016.
 */
public class DaoNoHFConfig extends HomesteadConfig {
    private long forkBlockNumber = 1_920_000;

    public static final byte[] EtcForkBlockHash = Hex.decode("94365e3a8c0b35089c1d1195081fe7489b528a84b22199c916180db8b28ade7f");

    public DaoNoHFConfig() {
    }

    public DaoNoHFConfig(Constants constants) {
        super(constants);
    }

    public DaoNoHFConfig withForkBlock(long blockNumber) {
        forkBlockNumber = blockNumber;
        return this;
    }

    @Override
    public List<Pair<Long, byte[]>> blockHashConstraints() {
        return Collections.singletonList(Pair.of(forkBlockNumber, EtcForkBlockHash));
    }
}
