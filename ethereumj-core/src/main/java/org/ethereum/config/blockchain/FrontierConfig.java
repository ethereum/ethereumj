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

import org.ethereum.config.Constants;
import org.ethereum.core.Transaction;

import java.math.BigInteger;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class FrontierConfig extends OlympicConfig {

    public static class FrontierConstants extends Constants {
        private static final BigInteger BLOCK_REWARD = new BigInteger("5000000000000000000");

        @Override
        public int getDURATION_LIMIT() {
            return 13;
        }

        @Override
        public BigInteger getBLOCK_REWARD() {
            return BLOCK_REWARD;
        }

        @Override
        public int getMIN_GAS_LIMIT() {
            return 5000;
        }
    };

    public FrontierConfig() {
        this(new FrontierConstants());
    }

    public FrontierConfig(Constants constants) {
        super(constants);
    }


    @Override
    public boolean acceptTransactionSignature(Transaction tx) {
        if (!super.acceptTransactionSignature(tx)) return false;
        if (tx.getSignature() == null) return false;
        return tx.getSignature().validateComponents();
    }

}
