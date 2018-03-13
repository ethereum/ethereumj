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

import java.util.List;

/**
 * Composite {@link BlockHeader} validator
 * aggregating list of simple validation rules depending on parent's block header
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class ParentBlockHeaderValidator extends DependentBlockHeaderRule {

    private List<DependentBlockHeaderRule> rules;

    public ParentBlockHeaderValidator(List<DependentBlockHeaderRule> rules) {
        this.rules = rules;
    }

    @Override
    public boolean validate(BlockHeader header, BlockHeader parent) {
        errors.clear();

        for (DependentBlockHeaderRule rule : rules) {
            if (!rule.validate(header, parent)) {
                errors.addAll(rule.getErrors());
                return false;
            }
        }

        return true;
    }
}
