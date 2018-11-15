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
import org.ethereum.sharding.processing.db.BeaconStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.ethereum.sharding.processing.validation.ValidationResult.Exist;
import static org.ethereum.sharding.processing.validation.ValidationResult.NoParent;
import static org.ethereum.sharding.processing.validation.ValidationResult.Success;

/**
 * Runs a set of basic validations that is triggered before block processing.
 *
 * @author Mikhail Kalinin
 * @since 16.08.2018
 */
public class BasicBeaconValidator implements BeaconValidator {

    private static final Logger logger = LoggerFactory.getLogger("beacon");

    BeaconStore store;
    List<ValidationRule<BeaconStore>> rules;

    public BasicBeaconValidator(BeaconStore store) {
        this.store = store;

        rules = new ArrayList<>();
        rules.add((block, st) -> st.exist(block.getHash()) ? Exist : Success);
        rules.add((block, st) -> st.exist(block.getParentHash()) ? Success : NoParent);
    }

    @Override
    public ValidationResult validateAndLog(Beacon block) {
        for (ValidationRule<BeaconStore> rule : rules) {
            ValidationResult res = rule.apply(block, store);
            if (res != Success) {
                logger.info("Process block {}, status: {}", block.toString(), res);
                return res;
            }
        }

        return Success;
    }
}
