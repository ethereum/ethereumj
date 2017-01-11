package org.ethereum.validator;

import org.ethereum.core.BlockHeader;
import org.ethereum.util.FastByteComparisons;

/**
 * Checks proof value against its boundary for the block header
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class ProofOfWorkRule extends BlockHeaderRule {

    @Override
    public boolean validate(BlockHeader header) {

        errors.clear();

        byte[] proof = header.calcPowValue();
        byte[] boundary = header.getPowBoundary();

        if (!header.isGenesis() && FastByteComparisons.compareTo(proof, 0, 32, boundary, 0, 32) > 0) {
            errors.add(String.format("#%d: proofValue > header.getPowBoundary()", header.getNumber()));
            return false;
        }

        return true;
    }
}
