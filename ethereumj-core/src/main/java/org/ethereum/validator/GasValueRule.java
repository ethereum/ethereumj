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

import java.math.BigInteger;

/**
 * Checks {@link BlockHeader#gasUsed} against {@link BlockHeader#gasLimit}
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class GasValueRule extends BlockHeaderRule {

    @Override
    public ValidationResult validate(BlockHeader header) {
        if (new BigInteger(1, header.getGasLimit()).compareTo(BigInteger.valueOf(header.getGasUsed())) < 0) {
            return fault("header.getGasLimit() < header.getGasUsed()");
        }

        return Success;
    }
}
