package org.ethereum.jsontestsuite.builder;

import org.ethereum.core.Transaction;
import org.ethereum.jsontestsuite.model.TransactionTck;
import org.spongycastle.util.BigIntegers;

import java.math.BigInteger;

import static org.ethereum.jsontestsuite.Utils.*;
import static org.ethereum.util.BIUtil.exitLong;
import static org.ethereum.util.BIUtil.toBI;
import static org.ethereum.util.ByteUtil.byteArrayToLong;

public class TransactionBuilder {

    public static Transaction build(TransactionTck transactionTck) {

        // TODO: it can be removed in the future when block will be adapted to 32 bytes range gas limit
        BigInteger gasLimit = toBI(parseVarData(transactionTck.getGasLimit()));
        if (exitLong(gasLimit))
            gasLimit = new BigInteger(Long.MAX_VALUE + "");

        Transaction transaction;
        if (transactionTck.getSecretKey() != null){

            transaction = new Transaction(
                    parseVarData(transactionTck.getNonce()),
                    parseVarData(transactionTck.getGasPrice()),
                    BigIntegers.asUnsignedByteArray(gasLimit),
                    parseData(transactionTck.getTo()),
                    parseVarData(transactionTck.getValue()),
                    parseData(transactionTck.getData()));
            transaction.sign(parseData(transactionTck.getSecretKey()));

        } else {

            transaction = new Transaction(
                    parseNumericData(transactionTck.getNonce()),
                    parseNumericData(transactionTck.getGasPrice()),
                    BigIntegers.asUnsignedByteArray(gasLimit),
                    parseData(transactionTck.getTo()),
                    parseNumericData(transactionTck.getValue()),
                    parseData(transactionTck.getData()),
                    parseData(transactionTck.getR()),
                    parseData(transactionTck.getS()),
                    parseByte(transactionTck.getV())
            );
        }

        return transaction;
    }
}
