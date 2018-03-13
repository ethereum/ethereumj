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

/**
 * Checks if {@link BlockHeader#number} == {@link BlockHeader#number} + 1 of parent's block
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class ParentNumberRule extends DependentBlockHeaderRule {

    @Override
    public boolean validate(BlockHeader header, BlockHeader parent) {

        errors.clear();

        if (header.getNumber() != (parent.getNumber() + 1)) {
            errors.add(String.format("#%d: block number is not parentBlock number + 1", header.getNumber()));
            return false;
        }

        return true;
    }
}
