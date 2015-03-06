package org.ethereum.vm;

/**
 * The fundamental network cost unit. Paid for exclusively by Ether, which is converted
 * freely to and from Gas as required. Gas does not exist outside of the internal Ethereum
 * computation engine; its price is set by the Transaction and miners are free to
 * ignore Transactions whose Gas price is too low.
 */
public class GasCost {

    /* backwards compatibility, remove eventually */
    public final static int STEP = 1;
    public final static int SSTORE = 300;
    /* backwards compatibility, remove eventually */

    public final static int ZEROSTEP = 0;
    public final static int QUICKSTEP = 2;
    public final static int FASTESTSTEP = 3;
    public final static int FASTSTEP = 5;
    public final static int MIDSTEP = 8;
    public final static int SLOWSTEP = 10;
    public final static int EXTSTEP = 20;

    public final static int GENESISGASLIMIT = 1000000;
    public final static int MINGASLIMIT = 125000;

    public final static int BALANCE = 20;
    public final static int SHA3 = 30;
    public final static int SHA3_WORD = 6;
    public final static int SLOAD = 50;
    public final static int STOP = 0;
    public final static int SUICIDE = 0;
    public final static int CLEAR_SSTORE = 5000;
    public final static int SET_SSTORE = 20000;
    public final static int RESET_SSTORE = 5000;
    public final static int REFUND_SSTORE = 15000;
    public final static int CREATE = 32000;

    public final static int JUMPDEST = 1;
    public final static int CREATE_DATA_BYTE = 5;
    public final static int CALL = 40;
    public final static int STIPEND_CALL = 2300;
    public final static int VT_CALL = 9000;  //value transfer call
    public final static int NEW_ACCT_CALL = 25000;  //new account call
    public final static int MEMORY = 3;
    public final static int SUICIDE_REFUND = 24000;
    public final static int QUAD_COEFF_DIV = 512;
    public final static int CREATE_DATA = 200;
    public final static int TX_NO_ZERO_DATA = 68;
    public final static int TX_ZERO_DATA = 4;
    public final static int TRANSACTION = 21000;
    public final static int LOG_GAS = 375;
    public final static int LOG_DATA_GAS = 8;
    public final static int LOG_TOPIC_GAS = 375;
    public final static int COPY_GAS = 3;
    public final static int EXP_GAS = 10;
    public final static int EXP_BYTE_GAS = 10;
    public final static int IDENTITY = 15;
    public final static int IDENTITY_WORD = 3;
    public final static int RIPEMD160 = 600;
    public final static int RIPEMD160_WORD = 120;
    public final static int SHA256 = 60;
    public final static int SHA256_WORD = 12;
    public final static int EC_RECOVER = 3000;
}
