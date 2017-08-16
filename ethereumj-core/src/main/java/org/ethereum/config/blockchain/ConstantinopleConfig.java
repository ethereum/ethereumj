package org.ethereum.config.blockchain;

import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.Constants;
import org.ethereum.core.Block;
import org.ethereum.core.Repository;
import org.ethereum.vm.GasCost;
import org.spongycastle.util.encoders.Hex;

/**
 * EIPs included in the Hard Fork:
 * <ul>
 *     <li>86 - Abstraction of transaction origin and signature</li>
 *     <li>96 - Blockhash refactoring</li>
 * </ul>
 *
 * @author Mikhail Kalinin
 * @since 15.08.2017
 */
public class ConstantinopleConfig extends Eip160HFConfig {

    private long forkBlockNumber;

    static class GasCostConstantinople extends GasCostEip160HF {
        public int getBLOCKHASH() { return 800; }
    }

    private static final GasCost NEW_GAS_COST = new GasCostConstantinople();

    public ConstantinopleConfig(BlockchainConfig parent, long forkBlockNumber) {
        super(parent);
        this.forkBlockNumber = forkBlockNumber;
    }

    @Override
    public GasCost getGasCost() {
        return NEW_GAS_COST;
    }

    @Override
    public void hardForkTransfers(Block block, Repository repo) {

        // set blockhash contract
        if (block.getNumber() == forkBlockNumber) {
            repo.saveCode(
                    Constants.getBLOCKHASH_CONTRACT_ADDR(),
                    Hex.decode("0x600073fffffffffffffffffffffffffffffffffffffffe33141561005957600143035b60011561005357600035610100820683015561010081061561004057005b6101008104905061010082019150610022565b506100e0565b4360003512156100d4576000356001814303035b61010081121515610085576000610100830614610088565b60005b156100a75761010083019250610100820491506101008104905061006d565b610100811215156100bd57600060a052602060a0f35b610100820683015460c052602060c0f350506100df565b600060e052602060e0f35b5b50")
            );
        }
    }

    @Override
    public boolean eip96() {
        return true;
    }
}
