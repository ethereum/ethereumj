package org.ethereum.mine;

/**
 * Created by Anton Nashatyrev on 27.11.2015.
 */
public class EthashParams {
    // bytes in word
    public final int WORD_BYTES = 4;

    // bytes in dataset at genesis
    public final long DATASET_BYTES_INIT = 1L << 30;

    // dataset growth per epoch
    public final long DATASET_BYTES_GROWTH = 1L << 23;

    //  bytes in dataset at genesis
    public final long CACHE_BYTES_INIT = 1L << 24;

    // cache growth per epoch
    public final long CACHE_BYTES_GROWTH = 1L << 17;

    //  Size of the DAG relative to the cache
    public final long CACHE_MULTIPLIER = 1024;

    //  blocks per epoch
    public final long EPOCH_LENGTH = 30000;

    // width of mix
    public final int MIX_BYTES = 128;

    //  hash length in bytes
    public final int HASH_BYTES = 64;

    // number of parents of each dataset element
    public final long DATASET_PARENTS = 256;

    // number of rounds in cache production
    public final long CACHE_ROUNDS = 3;

    //  number of accesses in hashimoto loop
    public final long ACCESSES = 64;

    /**
     * The parameters for Ethash's cache and dataset depend on the block number.
     * The cache size and dataset size both grow linearly; however, we always take the highest
     * prime below the linearly growing threshold in order to reduce the risk of accidental
     * regularities leading to cyclic behavior.
     */
    public long getCacheSize(long blockNumber) {
        long sz = CACHE_BYTES_INIT + CACHE_BYTES_GROWTH * (blockNumber / EPOCH_LENGTH);
        sz -= HASH_BYTES;
        while (!isPrime(sz / HASH_BYTES)) {
            sz -= 2 * HASH_BYTES;
        }
        return sz;
    }

    public long getFullSize(long blockNumber) {
        long sz = DATASET_BYTES_INIT + DATASET_BYTES_GROWTH * (blockNumber / EPOCH_LENGTH);
        sz -= MIX_BYTES;
        while (!isPrime(sz / MIX_BYTES)) {
            sz -= 2 * MIX_BYTES;
        }
        return sz;
    }

    private static boolean isPrime(long num) {
        if (num == 2) return true;
        if (num % 2 == 0) return false;
        for (int i = 3; i * i < num; i += 2)
            if (num % i == 0) return false;
        return true;
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 1_000_000; i += 30000) {
            System.out.println(new EthashParams().getCacheSize(i));
            System.out.println(new EthashParams().getFullSize(i));
            System.out.println();
        }
    }
}

