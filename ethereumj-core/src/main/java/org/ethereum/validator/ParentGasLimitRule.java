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

import org.ethereum.config.SystemProperties;
import org.ethereum.core.BlockHeader;

import java.math.BigInteger;

/**
 * Checks if {@link BlockHeader#gasLimit} matches gas limit bounds. <br>
 *
 * This check is NOT run in Frontier
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class ParentGasLimitRule extends DependentBlockHeaderRule {

    private final int GAS_LIMIT_BOUND_DIVISOR;

    public ParentGasLimitRule(SystemProperties config) {
        GAS_LIMIT_BOUND_DIVISOR = config.getBlockchainConfig().
                getCommonConstants().getGAS_LIMIT_BOUND_DIVISOR();
    }

    @Override
    public boolean validate(BlockHeader header, BlockHeader parent) {

        errors.clear();
        BigInteger headerGasLimit = new BigInteger(1, header.getGasLimit());
        BigInteger parentGasLimit = new BigInteger(1, parent.getGasLimit());

        if (headerGasLimit.compareTo(parentGasLimit.multiply(BigInteger.valueOf(GAS_LIMIT_BOUND_DIVISOR - 1)).divide(BigInteger.valueOf(GAS_LIMIT_BOUND_DIVISOR))) < 0 ||
            headerGasLimit.compareTo(parentGasLimit.multiply(BigInteger.valueOf(GAS_LIMIT_BOUND_DIVISOR + 1)).divide(BigInteger.valueOf(GAS_LIMIT_BOUND_DIVISOR))) > 0) {

            errors.add(String.format(
                    "#%d: gas limit exceeds parentBlock.getGasLimit() (+-) GAS_LIMIT_BOUND_DIVISOR",
                    header.getNumber()
            ));
            return false;
        }

        return true;
    }
}
