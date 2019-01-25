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
package org.ethereum.core.genesis;

import java.util.List;

/**
 * Created by Anton on 03.03.2017.
 */
public class GenesisConfig {
    public Integer homesteadBlock;
    public Integer daoForkBlock;
    public Integer eip150Block;
    public Integer eip155Block;
    public boolean daoForkSupport;
    public Integer eip158Block;
    public Integer byzantiumBlock;
    public Integer constantinopleBlock;
    public Integer petersburgBlock;
    public Integer chainId;

    // EthereumJ private options

    public static class HashValidator {
        public long number;
        public String hash;
    }

    public List<HashValidator> headerValidators;

    public boolean isCustomConfig() {
        return homesteadBlock != null || daoForkBlock != null || eip150Block != null ||
                eip155Block != null || eip158Block != null || byzantiumBlock != null ||
                constantinopleBlock != null || petersburgBlock != null;
    }
}
