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
public class HomesteadConfig extends FrontierConfig {
    private static final BigInteger SECP256K1N = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16);
    private static final BigInteger SECP256K1N_HALF = SECP256K1N.divide(BigInteger.valueOf(2));

    public static class HomesteadConstants extends FrontierConstants {
        @Override
        public boolean createEmptyContractOnOOG() {
            return false;
        }

        @Override
        public boolean hasDelegateCallOpcode() {
            return true;
        }
    };

    public HomesteadConfig() {
        this(new HomesteadConstants());
    }

    public HomesteadConfig(Constants constants) {
        super(constants);
    }

    @Override
    protected BigInteger getCalcDifficultyMultiplier(BlockHeader curBlock, BlockHeader parent) {
        return BigInteger.valueOf(Math.max(1 - (curBlock.getTimestamp() - parent.getTimestamp()) / 10, -99));
    }

    @Override
    public long getTransactionCost(Transaction tx) {
        long nonZeroes = tx.nonZeroDataBytes();
        long zeroVals  = ArrayUtils.getLength(tx.getData()) - nonZeroes;

        return (tx.isContractCreation() ? GasCost.TRANSACTION_CREATE_CONTRACT : GasCost.TRANSACTION)
                + zeroVals * GasCost.TX_ZERO_DATA + nonZeroes * GasCost.TX_NO_ZERO_DATA;
    }

    @Override
    public boolean acceptTransactionSignature(Transaction tx) {
        return tx.getSignature().s.compareTo(SECP256K1N_HALF) < 0;
    }
}
