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
package org.ethereum.config;

import java.math.BigInteger;

/**
 * Describes different constants specific for a blockchain
 *
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class Constants {
    private static final int MAXIMUM_EXTRA_DATA_SIZE = 32;
    private static final int MIN_GAS_LIMIT = 125000;
    private static final int GAS_LIMIT_BOUND_DIVISOR = 1024;
    private static final BigInteger MINIMUM_DIFFICULTY = BigInteger.valueOf(131072);
    private static final BigInteger DIFFICULTY_BOUND_DIVISOR = BigInteger.valueOf(2048);
    private static final int EXP_DIFFICULTY_PERIOD = 100000;

    private static final int UNCLE_GENERATION_LIMIT = 7;
    private static final int UNCLE_LIST_LIMIT = 2;

    private static final int BEST_NUMBER_DIFF_LIMIT = 100;

    private static final BigInteger BLOCK_REWARD = new BigInteger("1500000000000000000");

    private static final BigInteger SECP256K1N = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16);

    public int getDURATION_LIMIT() {
        return 8;
    }

    public BigInteger getInitialNonce() {
        return BigInteger.ZERO;
    }

    public int getMAXIMUM_EXTRA_DATA_SIZE() {
        return MAXIMUM_EXTRA_DATA_SIZE;
    }

    public int getMIN_GAS_LIMIT() {
        return MIN_GAS_LIMIT;
    }

    public int getGAS_LIMIT_BOUND_DIVISOR() {
        return GAS_LIMIT_BOUND_DIVISOR;
    }

    public BigInteger getMINIMUM_DIFFICULTY() {
        return MINIMUM_DIFFICULTY;
    }

    public BigInteger getDIFFICULTY_BOUND_DIVISOR() {
        return DIFFICULTY_BOUND_DIVISOR;
    }

    public int getEXP_DIFFICULTY_PERIOD() {
        return EXP_DIFFICULTY_PERIOD;
    }

    public int getUNCLE_GENERATION_LIMIT() {
        return UNCLE_GENERATION_LIMIT;
    }

    public int getUNCLE_LIST_LIMIT() {
        return UNCLE_LIST_LIMIT;
    }

    public int getBEST_NUMBER_DIFF_LIMIT() {
        return BEST_NUMBER_DIFF_LIMIT;
    }

    public BigInteger getBLOCK_REWARD() {
        return BLOCK_REWARD;
    }

    public int getMAX_CONTRACT_SZIE() { return Integer.MAX_VALUE; }

    /**
     * Introduced in the Homestead release
     */
    public boolean createEmptyContractOnOOG() {
        return true;
    }

    /**
     * New DELEGATECALL opcode introduced in the Homestead release. Before Homestead this opcode should generate
     * exception
     */
    public boolean hasDelegateCallOpcode() {return false; }

    /**
     * Introduced in the Homestead release
     */
    public static BigInteger getSECP256K1N() {
        return SECP256K1N;
    }
}
