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
package org.ethereum.sharding.service;

import org.ethereum.sharding.domain.Validator;

import java.util.List;

/**
 * Helper interface to look for deposited validators in the receipts.
 *
 * @author Mikhail Kalinin
 * @since 30.07.2018
 */
public interface ValidatorRepository {

    /**
     * Returns a list of validators deployed in an inclusive range {@code [fromBlock, toBlock]}.
     * An order of deposits is preserved, hence first deposited validator has the lowest index in returned list.
     */
    List<Validator> query(byte[] fromBlock, byte[] toBlock);
}
