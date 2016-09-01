package org.ethereum.validator;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.BlockHeader;
import org.ethereum.util.FastByteComparisons;
import org.spongycastle.util.encoders.Hex;

import java.util.List;

/**
 *  Checks if the block is from the right fork  
 */
public class BlockHashRule extends BlockHeaderRule {

    private final BlockchainNetConfig blockchainConfig;

    public BlockHashRule(SystemProperties config) {
        blockchainConfig = config.getBlockchainConfig();
    }

    @Override
    public boolean validate(BlockHeader header) {
        errors.clear();

        List<Pair<Long, byte[]>> hashes = blockchainConfig.getConfigForBlock(header.getNumber()).blockHashConstraints();

        for (Pair<Long, byte[]> hash : hashes) {
            if (header.getNumber() == hash.getLeft() &&
                    !FastByteComparisons.equal(header.getHash(), hash.getRight())) {
                errors.add("Block " + header.getNumber() + " hash constraint violated. Expected:" +
                        Hex.toHexString(hash.getRight()) + ", got: " + Hex.toHexString(header.getHash()));
                return false;
            }
        }

        return true;
    }
}
