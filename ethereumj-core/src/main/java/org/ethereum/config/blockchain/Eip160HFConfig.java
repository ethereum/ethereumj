package org.ethereum.config.blockchain;

import org.ethereum.config.BlockchainConfig;
import org.ethereum.core.Transaction;
import org.ethereum.vm.GasCost;

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

    public Eip160HFConfig(BlockchainConfig parent) {
        super(parent);
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
    public boolean acceptTransactionSignature(Transaction tx) {
        if (!super.acceptTransactionSignature(tx)) return false;
        return tx.getChainId() == null || getChainId().equals(tx.getChainId());
    }
}
