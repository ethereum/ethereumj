package org.ethereum.config;

import java.math.BigInteger;

import static org.ethereum.config.SystemProperties.CONFIG;

public class Constants {

    public static int GENESIS_DIFFICULTY = 131072;
    public static int MAXIMUM_EXTRA_DATA_SIZE = 32;
    public static int EPOCH_DURATION = 30_000;
    public static int GENESIS_GAS_LIMIT = 3_141_592;
    public static int MIN_GAS_LIMIT = 125000;
    public static int GAS_LIMIT_BOUND_DIVISOR = 1024;
    public static BigInteger MINIMUM_DIFFICULTY = BigInteger.valueOf(131072);
    public static BigInteger DIFFICULTY_BOUND_DIVISOR = BigInteger.valueOf(2048);
    public static int EXP_DIFFICULTY_PERIOD = 100000;

    public static int UNCLE_GENERATION_LIMIT = 7;
    public static int UNCLE_LIST_LIMIT = 2;

    public static int BEST_NUMBER_DIFF_LIMIT = 1000;

    public static final BigInteger SECP256K1N = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16);
    public static final BigInteger SECP256K1N_HALF = SECP256K1N.divide(BigInteger.valueOf(2));
    public static long HOMESTEAD_FORK_BLKNUM = 900_000;

    public static int getDURATION_LIMIT() {
        return CONFIG.isFrontier() ? 13 : 8;
    }
}
