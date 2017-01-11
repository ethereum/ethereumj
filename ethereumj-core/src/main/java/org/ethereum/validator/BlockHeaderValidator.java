package org.ethereum.validator;

import org.ethereum.core.BlockHeader;

import java.util.List;

/**
 * Composite {@link BlockHeader} validator
 * aggregating list of simple validation rules
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class BlockHeaderValidator extends BlockHeaderRule {

    private List<BlockHeaderRule> rules;

    public BlockHeaderValidator(List<BlockHeaderRule> rules) {
        this.rules = rules;
    }

    @Override
    public boolean validate(BlockHeader header) {
        errors.clear();

        for (BlockHeaderRule rule : rules) {
            if (!rule.validate(header)) {
                errors.addAll(rule.getErrors());
                return false;
            }
        }

        return true;
    }
}
