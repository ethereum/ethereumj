package org.ethereum.vm;

import java.util.HashMap;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 04/06/2014 23:58
 */

public class GasLedger {

/*  YP Appendix B.
    Gstep 1 Default amount of gas to pay for execution cycle.
    Gstop 0 Nothing paid for the STOP operation.
    Gsuicide 0 Nothing paid for the SUICIDE operation.
    Gsha3 20 Paid for a SHA3 operation.
    Gsload 20 Paid for a SLOAD operation.
    Gsstore 100 Paid for a normal SSTORE operation (doubled or waived sometimes).
    Gbalance 20 Paid for a BALANCE operation.
    Gcreate 100 Paid for a CREATE operation.
    Gcall 20 Paid for a CALL operation.
    Gmemory 1 Paid for every additional word when expanding memory.
    Gtxdata 5 Paid for every byte of data or code for a transaction.
    Gtransaction 500 Paid for every transaction.
 */


    public static int G_STEP    = 1;
    public static int G_STOP    = 0;
    public static int G_SUICIDE = 0;
    public static int G_SLOAD   = 20;
    public static int G_SHA3    = 20;
    public static int G_SSTORE  = 100;
    public static int G_BALANCE = 20;
    public static int G_CREATE  = 100;
    public static int G_CALL    = 20;
    public static int G_MEMORY  = 1;
    public static int G_TXDATA  = 5;
    public static int G_TRANSACTION = 500;
}
