package org.ethereum.validator;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.BlockHeader;
import org.ethereum.mine.EthashValidationHelper;
import org.ethereum.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mikhail Kalinin
 * @since 19.06.2018
 */
public class EthashRule extends BlockHeaderRule {

    EthashValidationHelper ethash;

    public EthashRule(SystemProperties config) {
        this.ethash = new EthashValidationHelper(config);
    }

    int hits = 0;
    long nanos = 0;
    int checkpoint = 10000;

    @Override
    public ValidationResult validate(BlockHeader header) {

        if (header.isGenesis())
            return Success;

        long t = System.nanoTime();

        Pair<byte[], byte[]> res = ethash.ethashWorkFor(header, header.getNonce());

        if (!FastByteComparisons.equal(res.getLeft(), header.getMixHash())) {
            return fault(String.format("#%d: mixHash doesn't match", header.getNumber()));
        }

        if (FastByteComparisons.compareTo(res.getRight(), 0, 32, header.getPowBoundary(), 0, 32) > 0) {
            return fault(String.format("#%d: proofValue > header.getPowBoundary()", header.getNumber()));
        }

        nanos += System.nanoTime() - t;
        logStats();

        return Success;
    }

    private static final Logger logger = LoggerFactory.getLogger("ethash");
    void logStats() {
        if (++hits % checkpoint == 0) {
            logger.info("time per {} blocks: {}s", checkpoint, String.format("%.4f", nanos / 1_000_000_000d));
            nanos = 0;
        }
    }
}
