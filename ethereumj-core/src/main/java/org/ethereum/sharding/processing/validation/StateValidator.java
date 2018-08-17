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
package org.ethereum.sharding.processing.validation;

import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.processing.state.BeaconState;
import org.ethereum.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ethereum.sharding.processing.validation.ValidationResult.StateMismatch;
import static org.ethereum.sharding.processing.validation.ValidationResult.Success;

/**
 * @author Mikhail Kalinin
 * @since 16.08.2018
 */
public class StateValidator {

    private static final Logger logger = LoggerFactory.getLogger("beacon");

    ValidationRule<BeaconState> rule = (block, state) -> {
        if (!FastByteComparisons.equal(block.getStateHash(), state.getHash()))
            return StateMismatch;

        return Success;
    };

    public ValidationResult validateAndLog(Beacon block, BeaconState state) {
        ValidationResult res = rule.apply(block, state);
        if (res != Success) {
            logger.info("Process block {}, status: {}", block.toString(), res);
        }
        return res;
    }
}
