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

import java.math.BigInteger;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class MordenConfig {
    private static final BigInteger NONSE = BigInteger.valueOf(0x100000);

    public static class Frontier extends FrontierConfig {
        public Frontier() {
            super(new FrontierConstants() {
                @Override
                public BigInteger getInitialNonce() {
                    return NONSE;
                }
            });
        }
    }

    public static class Homestead extends HomesteadConfig {
        public Homestead() {
            super(new HomesteadConstants() {
                @Override
                public BigInteger getInitialNonce() {
                    return NONSE;
                }
            });
        }
    }
}
