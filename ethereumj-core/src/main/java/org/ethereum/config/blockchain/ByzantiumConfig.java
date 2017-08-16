package org.ethereum.config.blockchain;

import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.Constants;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Repository;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

/**
 * EIPs included in the Hard Fork:
 * <ul>
 *     <li>100 - Change difficulty adjustment to target mean block time including uncles</li>
 *     <li>140 - REVERT instruction in the Ethereum Virtual Machine</li>
 *     <li>196 - Precompiled contracts for addition and scalar multiplication on the elliptic curve alt_bn128</li>
 *     <li>197 - Precompiled contracts for optimal Ate pairing check on the elliptic curve alt_bn128</li>
 *     <li>198 - Precompiled contract for bigint modular exponentiation</li>
 *     <li>211 - New opcodes: RETURNDATASIZE and RETURNDATACOPY</li>
 *     <li>214 - New opcode STATICCALL</li>
 *     <li>658 - Embedding transaction return data in receipts</li>
 * </ul>
 *
 * @author Mikhail Kalinin
 * @since 14.08.2017
 */
public class ByzantiumConfig extends Eip160HFConfig {

    public ByzantiumConfig(BlockchainConfig parent) {
        super(parent);
    }

    @Override
    public BigInteger getCalcDifficultyMultiplier(BlockHeader curBlock, BlockHeader parent) {
        long unclesAdj = parent.hasUncles() ? 2 : 1;
        return BigInteger.valueOf(Math.max(unclesAdj - (curBlock.getTimestamp() - parent.getTimestamp()) / 9, -99));
    }
}
