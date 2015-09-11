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
    public static int DURATION_LIMIT = CONFIG.isFrontier() ? 13 : 8;;
    public static int EXP_DIFFICULTY_PERIOD = 100000;

    public static int UNCLE_GENERATION_LIMIT = 7;
    public static int UNCLE_LIST_LIMIT = 2;

    public static int BEST_NUMBER_DIFF_LIMIT = 1000;
}
