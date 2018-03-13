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

import org.ethereum.config.Constants;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.BlockHeader;

/**
 * Checks diff between number of some block and number of our best block. <br>
 * The diff must be more than -1 * {@link Constants#getBEST_NUMBER_DIFF_LIMIT}
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class BestNumberRule extends DependentBlockHeaderRule {

    private final int BEST_NUMBER_DIFF_LIMIT;

    public BestNumberRule(SystemProperties config) {
        BEST_NUMBER_DIFF_LIMIT = config.getBlockchainConfig().
                getCommonConstants().getBEST_NUMBER_DIFF_LIMIT();
    }

    @Override
    public boolean validate(BlockHeader header, BlockHeader bestHeader) {

        errors.clear();

        long diff = header.getNumber() - bestHeader.getNumber();

        if (diff > -1 * BEST_NUMBER_DIFF_LIMIT) {
            errors.add(String.format(
                    "#%d: (header.getNumber() - bestHeader.getNumber()) <= BEST_NUMBER_DIFF_LIMIT",
                    header.getNumber()
            ));
            return false;
        }

        return true;
    }
}
