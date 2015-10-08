package org.ethereum.validator;

import org.ethereum.core.BlockHeader;

/**
 * @author Mikhail Kalinin
 * @since 25.09.2015
 */
public class DependentBlockHeaderRuleAdapter extends DependentBlockHeaderRule {

    @Override
    public boolean validate(BlockHeader header, BlockHeader dependency) {
        return true;
    }
}
