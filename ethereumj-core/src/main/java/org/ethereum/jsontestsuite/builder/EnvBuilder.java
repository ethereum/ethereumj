package org.ethereum.jsontestsuite.builder;

import org.ethereum.jsontestsuite.Env;
import org.ethereum.jsontestsuite.model.EnvTck;

import static org.ethereum.jsontestsuite.Utils.parseData;
import static org.ethereum.jsontestsuite.Utils.parseNumericData;
import static org.ethereum.jsontestsuite.Utils.parseVarData;

public class EnvBuilder {

    public static Env build(EnvTck envTck){
        byte[] coinbase = parseData(envTck.getCurrentCoinbase());
        byte[] difficulty = parseVarData(envTck.getCurrentDifficulty());
        byte[] gasLimit = parseVarData(envTck.getCurrentGasLimit());
        byte[] number = parseNumericData(envTck.getCurrentNumber());
        byte[] timestamp = parseNumericData(envTck.getCurrentTimestamp());
        byte[] hash = parseData(envTck.getPreviousHash());

        return new Env(coinbase, difficulty, gasLimit, number, timestamp, hash);
    }

}
