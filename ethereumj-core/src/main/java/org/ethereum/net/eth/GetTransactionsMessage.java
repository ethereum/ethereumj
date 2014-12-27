package org.ethereum.net.eth;

import org.spongycastle.util.encoders.Hex;

/**
 * Wrapper around an Ethereum GetTransactions message on the network
 *
 * @see org.ethereum.net.eth.EthMessageCodes#GET_TRANSACTIONS
 */
public class GetTransactionsMessage extends EthMessage {

    /**
     * GetTransactions message is always a the same single command payload
     */
    private final static byte[] FIXED_PAYLOAD = Hex.decode("C116");

    public byte[] getEncoded() {
        return FIXED_PAYLOAD;
    }

    @Override
    public Class<TransactionsMessage> getAnswerMessage() {
        return TransactionsMessage.class;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.GET_TRANSACTIONS;
    }

    @Override
    public String toString() {
        return "[" + this.getCommand().name() + "]";
    }
}

