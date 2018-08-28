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
package org.ethereum.sharding.processing;

import org.ethereum.sharding.processing.validation.ValidationResult;

/**
 * The result of processing a certain block.
 *
 * @author Mikhail Kalinin
 * @since 16.08.2018
 */
public enum ProcessingResult {
    Exist,
    NoParent,
    Invalid,
    ConsensusBreak,
    Best,
    NotBest;

    public static ProcessingResult fromValidation(ValidationResult res) {
        switch (res) {
            case Exist:
                return Exist;
            case NoParent:
                return NoParent;
            case StateMismatch:
                return ConsensusBreak;
            default:
                throw new RuntimeException("Can't convert " + res + " to processing result");
        }
    }
}
