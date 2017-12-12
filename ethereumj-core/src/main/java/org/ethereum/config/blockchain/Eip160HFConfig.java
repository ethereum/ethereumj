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
package org.ethereum.config.blockchain;

import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.Constants;
import org.ethereum.config.ConstantsAdapter;
import org.ethereum.core.Transaction;
import org.ethereum.vm.GasCost;

import java.util.Objects;

import static org.ethereum.config.blockchain.HomesteadConfig.SECP256K1N_HALF;

/**
 * Hard fork includes following EIPs:
 * EIP 155 - Simple replay attack protection
 * EIP 160 - EXP cost increase
 * EIP 161 - State trie clearing (invariant-preserving alternative)
 */
public class Eip160HFConfig extends Eip150HFConfig {

    static class GasCostEip160HF extends GasCostEip150HF {
        public int getEXP_BYTE_GAS()        {     return 50;     }
    }

    private static final GasCost NEW_GAS_COST = new GasCostEip160HF();

    private final Constants constants;

    public Eip160HFConfig(BlockchainConfig parent) {
        super(parent);
        constants = new ConstantsAdapter(parent.getConstants()) {
            @Override
            public int getMAX_CONTRACT_SZIE() {
                return 0x6000;
            }
        };
    }

    @Override
    public GasCost getGasCost() {
        return NEW_GAS_COST;
    }

    @Override
    public boolean eip161() {
        return true;
    }

    @Override
    public Integer getChainId() {
        return 1;
    }

    @Override
    public Constants getConstants() {
        return constants;
    }

    @Override
    public boolean acceptTransactionSignature(Transaction tx) {

        if (tx.getSignature() == null) return false;

        // Restoring old logic. Making this through inheritance stinks too much
        if (!tx.getSignature().validateComponents() ||
                tx.getSignature().s.compareTo(SECP256K1N_HALF) > 0) return false;
        return  tx.getChainId() == null || Objects.equals(getChainId(), tx.getChainId());
    }
}
