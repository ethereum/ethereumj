package org.ethereum.validator;

import org.ethereum.config.Constants;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.BlockHeader;

/**
 * Checks {@link BlockHeader#extraData} size against {@link Constants#getMAXIMUM_EXTRA_DATA_SIZE}
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class ExtraDataRule extends BlockHeaderRule {

    private final int MAXIMUM_EXTRA_DATA_SIZE;

    public ExtraDataRule(SystemProperties config) {
        this.MAXIMUM_EXTRA_DATA_SIZE = config.getBlockchainConfig().
                getCommonConstants().getMAXIMUM_EXTRA_DATA_SIZE();
    }

    @Override
    public boolean validate(BlockHeader header) {

        errors.clear();

        if (header.getExtraData() != null && header.getExtraData().length > MAXIMUM_EXTRA_DATA_SIZE) {
            errors.add(String.format(
                    "#%d: header.getExtraData().length > MAXIMUM_EXTRA_DATA_SIZE",
                    header.getNumber()
            ));
            return false;
        }

        return true;
    }
}
