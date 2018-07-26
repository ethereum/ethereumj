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

import org.ethereum.core.Block;
import org.ethereum.core.TransactionInfo;
import org.ethereum.db.BlockStore;
import org.ethereum.db.TransactionStore;
import org.ethereum.sharding.contract.DepositContract;
import org.ethereum.sharding.domain.Validator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mikhail Kalinin
 * @since 23.07.2018
 */
public class ValidatorRepository {

    @Autowired
    BlockStore blockStore;

    @Autowired
    TransactionStore txStore;

    @Autowired
    DepositContract depositContract;

    public List<Validator> query(byte[] fromBlock, byte[] toBlock) {

        List<Block> blocks = blockStore.listBlocks(fromBlock, toBlock);

        if (blocks.isEmpty())
            return Collections.emptyList();

        List<Validator> validators = new ArrayList<>();
        // the order of returned blocks is inverted
        for (int i = blocks.size() - 1; i >= 0; i--) {
            Block block = blocks.get(i);

            block.getTransactionsList().forEach(tx -> {
                TransactionInfo txInfo = txStore.get(tx.getHash(), block.getHash());
                if (txInfo != null) {
                    txInfo.getReceipt().getLogInfoList().forEach(log -> {
                        if (depositContract.isDepositLog(log)) {
                            Validator validator = Validator.fromLogData(log.getData());
                            if (validator != null)
                                validators.add(validator);
                        }
                    });
                }
            });
        }

        return validators;
    }
}
