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

import org.ethereum.casper.config.CasperProperties;
import org.ethereum.casper.core.CasperFacade;
import org.ethereum.core.TransactionReceipt;

import java.util.Arrays;
import java.util.List;

/**
 * Checks {@link org.ethereum.core.TransactionReceipt} is from Casper contract vote
 * if it is, the checks are over
 * otherwise all other checks are applied
 */
public class ReceiptCasperVoteRule extends TransactionReceiptRule {

    private final byte[] CASPER_ADDRESS;
    private List<TransactionReceiptRule> nextRules;

    public ReceiptCasperVoteRule(CasperProperties config, TransactionReceiptRule ...rules) {
        this.CASPER_ADDRESS = config.getCasperAddress();
        this.nextRules = Arrays.asList(rules);
    }

    @Override
    public ValidationResult validate(TransactionReceipt receipt) {
        boolean isCasperVote = CasperFacade.isVote(receipt.getTransaction(), CASPER_ADDRESS);
        if (isCasperVote) {
            if (receipt.isSuccessful()) {
                return Success;
            } else {
                return fault("Receipt is not correct: Unsuccessful casper vote");
            }
        } else {
            return new TransactionReceiptValidator(nextRules).validate(receipt);
        }
    }
}
