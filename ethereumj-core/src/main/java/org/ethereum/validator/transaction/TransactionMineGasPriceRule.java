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
package org.ethereum.validator.transaction;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Transaction;
import org.ethereum.util.ByteUtil;

import java.math.BigInteger;

/**
 * Checks {@link Transaction} fields are with correct length and in allowable range
 */
public class TransactionMineGasPriceRule extends TransactionRule {

    private final BigInteger MIN_GAS_PRICE;

    public TransactionMineGasPriceRule(SystemProperties config) {
        this.MIN_GAS_PRICE = config.getMineMinGasPrice();
    }

    @Override
    public ValidationResult validate(Transaction transaction) {
        if (MIN_GAS_PRICE.compareTo(ByteUtil.bytesToBigInteger(transaction.getGasPrice())) >= 0) {
            return fault("Too low gas price for transaction: " + ByteUtil.bytesToBigInteger(transaction.getGasPrice()));
        }

        return Success;
    }
}
