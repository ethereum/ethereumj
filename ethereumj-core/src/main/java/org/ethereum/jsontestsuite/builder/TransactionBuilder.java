package org.ethereum.jsontestsuite.builder;

import org.ethereum.core.Transaction;
import org.ethereum.jsontestsuite.model.TransactionTck;

import static org.ethereum.jsontestsuite.Utils.parseByte;
import static org.ethereum.jsontestsuite.Utils.parseData;
import static org.ethereum.jsontestsuite.Utils.parseNumericData;

public class TransactionBuilder {

    public static Transaction build(TransactionTck transactionTck) {

        Transaction tx = new Transaction(
                parseNumericData(transactionTck.getNonce()),
                parseNumericData(transactionTck.getGasPrice()),
                parseNumericData(transactionTck.getGasLimit()),
                parseData(transactionTck.getTo()),
                parseNumericData(transactionTck.getValue()),
                parseData(transactionTck.getData()),
                parseData(transactionTck.getR()),
                parseData(transactionTck.getS()),
                parseByte(transactionTck.getV())
        );

        return tx;
    }
}
