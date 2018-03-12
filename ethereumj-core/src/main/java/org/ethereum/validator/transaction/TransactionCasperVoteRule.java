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
import org.ethereum.core.Transaction;

import java.util.Arrays;
import java.util.List;

/**
 * Checks {@link Transaction} is Casper contract vote
 * if it is, the checks are over
 * otherwise all other checks are applied
 */
public class TransactionCasperVoteRule extends TransactionRule {

    private final byte[] CASPER_ADDRESS;
    private List<TransactionRule> nextRules;

    public TransactionCasperVoteRule(CasperProperties config, TransactionRule ...rules) {
        this.CASPER_ADDRESS = config.getCasperAddress();
        this.nextRules = Arrays.asList(rules);
    }


    @Override
    public ValidationResult validate(Transaction transaction) {
        boolean isCasperVote = CasperFacade.isVote(transaction, CASPER_ADDRESS);
        if (isCasperVote) {
            return Success;
        } else {
            return new TransactionValidator(nextRules).validate(transaction);
        }
    }
}
