package org.ethereum.config.blockchain;

import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.config.Constants;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Transaction;
import org.ethereum.vm.GasCost;

import java.math.BigInteger;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class OlympicConfig extends AbstractConfig {

    public OlympicConfig() {
    }

    public OlympicConfig(Constants constants) {
        super(constants);
    }

    @Override
    protected BigInteger getCalcDifficultyMultiplier(BlockHeader curBlock, BlockHeader parent) {
        return BigInteger.valueOf(curBlock.getTimestamp() >= parent.getTimestamp() +
                getConstants().getDURATION_LIMIT() ? -1 : 1);
    }

    @Override
    public long getTransactionCost(Transaction tx) {
        long nonZeroes = tx.nonZeroDataBytes();
        long zeroVals  = ArrayUtils.getLength(tx.getData()) - nonZeroes;

        return GasCost.TRANSACTION + zeroVals * GasCost.TX_ZERO_DATA + nonZeroes * GasCost.TX_NO_ZERO_DATA;
    }
}
