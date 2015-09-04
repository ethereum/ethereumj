package org.ethereum.validator;

import org.ethereum.core.BlockHeader;

import java.math.BigInteger;

import static org.ethereum.util.BIUtil.isEqual;

/**
 * Checks block's difficulty against calculated difficulty value
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class DifficultyRule extends DependentBlockHeaderRule {

    @Override
    public boolean validate(BlockHeader header, BlockHeader parent) {

        errors.clear();

        BigInteger calcDifficulty = header.calcDifficulty(parent);
        BigInteger difficulty = header.getDifficultyBI();

        if (!isEqual(difficulty, calcDifficulty)) {

            errors.add("difficulty != calcDifficulty");
            return false;
        }

        return true;
    }
}
