package org.ethereum.validator;

import org.ethereum.config.Constants;
import org.ethereum.core.BlockHeader;

import static org.ethereum.config.Constants.BEST_NUMBER_DIFF_LIMIT;

/**
 * Checks diff between number of some block and number of our best block. <br>
 * The diff must be more than -1 * {@link Constants#BEST_NUMBER_DIFF_LIMIT}
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class BestNumberRule extends DependentBlockHeaderRule {

    @Override
    public boolean validate(BlockHeader header, BlockHeader bestHeader) {

        errors.clear();

        long diff = header.getNumber() - bestHeader.getNumber();

        if (diff <= 0) {
            errors.add(String.format(
                    "#%d: (header.getNumber() - bestHeader.getNumber()) <= BEST_NUMBER_DIFF_LIMIT",
                    header.getNumber()
            ));
            return false;
        }

        return true;
    }
}
