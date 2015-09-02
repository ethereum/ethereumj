package org.ethereum.validator;

import org.ethereum.config.Constants;
import org.ethereum.core.BlockHeader;

import java.math.BigInteger;

import static org.ethereum.config.Constants.DURATION_LIMIT;

/**
 * Checks block's difficulty against min difficulty value
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class DifficultyRule extends DependentBlockHeaderRule {

    @Override
    public boolean validate(BlockHeader header, BlockHeader parent) {

        errors.clear();

        if (!validate(header.getTimestamp(), parent.getTimestamp(),
                      header.getDifficultyBI(), parent.getDifficultyBI())) {

            errors.add("difficulty < minDifficulty");
            return false;
        }

        return true;
    }

    public boolean validate(long timestamp, long parentTimestamp, BigInteger difficulty, BigInteger parentDifficulty) {
        BigInteger minDifficulty = calculateMinDifficulty(timestamp, parentTimestamp, parentDifficulty);
        return minDifficulty.compareTo(difficulty) <= 0;
    }

    private BigInteger calculateMinDifficulty(long timestamp, long parentTimestamp, BigInteger parentDifficulty) {

        BigInteger quotient = parentDifficulty.divide(BigInteger.valueOf(Constants.DIFFICULTY_BOUND_DIVISOR));

        if (timestamp >= parentTimestamp + DURATION_LIMIT) {
            return parentDifficulty.subtract(quotient);
        } else {
            return parentDifficulty.add(quotient);
        }
    }
}
