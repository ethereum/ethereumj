package org.ethereum.config.blockchain;

import org.ethereum.config.BlockchainConfig;

/**
 * A version of Constantinople Hard Fork after removing eip-1283.
 * <p>
 *   Unofficial name 'Petersburg', includes:
 * <ul>
 *     <li>1234 - Constantinople Difficulty Bomb Delay and Block Reward Adjustment (2 ETH)</li>
 *     <li>145  - Bitwise shifting instructions in EVM</li>
 *     <li>1014 - Skinny CREATE2</li>
 *     <li>1052 - EXTCODEHASH opcode</li>
 * </ul>
 */
public class PetersburgConfig extends ConstantinopleConfig {

  public PetersburgConfig(BlockchainConfig parent) {
    super(parent);
  }

  @Override
  public boolean eip1283() {
    return false;
  }
}
