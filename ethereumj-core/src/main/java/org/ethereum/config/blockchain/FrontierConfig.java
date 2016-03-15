package org.ethereum.config.blockchain;

import org.ethereum.config.Constants;
import org.ethereum.core.Transaction;

import java.math.BigInteger;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class FrontierConfig extends OlympicConfig {

    public static class FrontierConstants extends Constants {
        private static final BigInteger BLOCK_REWARD = new BigInteger("5000000000000000000");

        @Override
        public int getDURATION_LIMIT() {
            return 13;
        }

        @Override
        public BigInteger getBLOCK_REWARD() {
            return BLOCK_REWARD;
        }

        @Override
        public int getMIN_GAS_LIMIT() {
            return 5000;
        }
    };

    public FrontierConfig() {
        this(new FrontierConstants());
    }

    public FrontierConfig(Constants constants) {
        super(constants);
    }


    @Override
    public boolean acceptTransactionSignature(Transaction tx) {
        return tx.getSignature().validateComponents();
    }

}
