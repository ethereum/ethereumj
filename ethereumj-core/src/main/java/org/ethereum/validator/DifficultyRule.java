package org.ethereum.validator;

import org.ethereum.core.BlockHeader;

import java.math.BigInteger;

import static org.ethereum.util.BIUtil.isLessThan;

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

        BigInteger minDifficulty = header.calcDifficulty(parent);
        BigInteger difficulty = header.getDifficultyBI();

        if (isLessThan(difficulty, minDifficulty)) {

            errors.add("difficulty < minDifficulty");
            return false;
        }

        return true;
    }
}
