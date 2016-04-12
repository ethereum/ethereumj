package org.ethereum.validator;

import org.ethereum.config.SystemProperties;
import org.ethereum.config.Constants;
import org.ethereum.core.BlockHeader;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.math.BigInteger;

/**
 * Checks {@link BlockHeader#gasLimit} against {@link Constants#getMIN_GAS_LIMIT}. <br>
 *
 * This check is NOT run in Frontier
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class GasLimitRule extends BlockHeaderRule {

    @Autowired
    SystemProperties config;

    private int MIN_GAS_LIMIT;
    @PostConstruct
    private void populateFromConfig() {
        MIN_GAS_LIMIT = config.getBlockchainConfig().
                getCommonConstants().getMIN_GAS_LIMIT();
    }

    @Override
    public boolean validate(BlockHeader header) {

        errors.clear();

        if (new BigInteger(1, header.getGasLimit()).compareTo(BigInteger.valueOf(MIN_GAS_LIMIT)) < 0) {
            errors.add("header.getGasLimit() < MIN_GAS_LIMIT");
            return false;
        }

        return true;
    }
}
