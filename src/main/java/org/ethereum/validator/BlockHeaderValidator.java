/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.validator;

import org.ethereum.core.BlockHeader;

import java.util.Arrays;
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

    public BlockHeaderValidator(BlockHeaderRule ...rules) {
        this.rules = Arrays.asList(rules);
    }

    @Override
    public ValidationResult validate(BlockHeader header) {
        for (BlockHeaderRule rule : rules) {
            ValidationResult result = rule.validate(header);
            if (!result.success) {
                return result;
            }
        }
        return Success;
    }
}
