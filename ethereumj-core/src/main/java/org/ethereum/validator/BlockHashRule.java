package org.ethereum.validator;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.BlockHeader;

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
    public ValidationResult validate(BlockHeader header) {
        List<Pair<Long, BlockHeaderValidator>> validators = blockchainConfig.getConfigForBlock(header.getNumber()).headerValidators();
        for (Pair<Long, BlockHeaderValidator> pair : validators) {
            if (header.getNumber() == pair.getLeft()) {
                ValidationResult result = pair.getRight().validate(header);
                if (!result.success) {
                    return fault("Block " + header.getNumber() + " header constraint violated. " + result.error);
                }
            }
        }

        return Success;
    }
}
