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

        List<Pair<Long, BlockHeaderValidator>> validators = blockchainConfig.getConfigForBlock(header.getNumber()).headerValidators();
        for (Pair<Long, BlockHeaderValidator> pair : validators) {
            if (header.getNumber() == pair.getLeft()) {
                List<String> validationErrors = pair.getRight().getValidationErrors(header);
                if (!validationErrors.isEmpty()) {
                    errors.add("Block " + header.getNumber() + " header constraint violated. " + validationErrors.get(0));
                    return false;
                }
            }
        }

        return true;
    }
}
