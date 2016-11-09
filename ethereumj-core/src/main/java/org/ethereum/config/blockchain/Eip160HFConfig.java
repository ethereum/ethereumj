package org.ethereum.config.blockchain;

import org.ethereum.config.BlockchainConfig;
import org.ethereum.vm.GasCost;

/**
 * Created by Anton Nashatyrev on 14.10.2016.
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
}
