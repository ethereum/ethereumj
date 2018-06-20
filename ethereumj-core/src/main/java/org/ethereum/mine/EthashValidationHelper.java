package org.ethereum.mine;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.BlockHeader;
import org.ethereum.crypto.HashUtil;

/**
 * @author Mikhail Kalinin
 * @since 20.06.2018
 */
public class EthashValidationHelper {

    SystemProperties config;
    Cache cache = Cache.INVALID;

    final static EthashAlgo EthashAlgo = new EthashAlgo(Ethash.ethashParams);

    public EthashValidationHelper(SystemProperties config) {
        this.config = config;
    }
    
    public Pair<byte[], byte[]> ethashWorkFor(BlockHeader header, byte[] nonce) {
        if (!cache.isValidFor(header.getNumber()))
            cache = Cache.forBlock(header.getNumber());

        long fullSize = EthashAlgo.getParams().getFullSize(header.getNumber());
        byte[] hashWithoutNonce = HashUtil.sha3(header.getEncodedWithoutNonce());

        return EthashAlgo.hashimotoLight(fullSize, cache.dataSet, hashWithoutNonce, nonce);
    }

    static class Cache {
        int[] dataSet;
        long epoch;

        public static final Cache INVALID = new Cache(null, Long.MAX_VALUE);

        private Cache(int[] dataSet, long epoch) {
            this.dataSet = dataSet;
            this.epoch = epoch;
        }

        boolean isValidFor(long blockNumber) {
            return epoch == blockNumber / EthashAlgo.getParams().getEPOCH_LENGTH();
        }

        static Cache forBlock(long blockNumber) {
            byte[] seed = EthashAlgo.getSeedHash(blockNumber);
            long size = EthashAlgo.getParams().getCacheSize(blockNumber);

            return new Cache(
                    EthashAlgo.makeCache(size, seed),
                    blockNumber / EthashAlgo.getParams().getEPOCH_LENGTH());
        }
    }
}
