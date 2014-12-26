package org.ethereum.vm;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Repository;

import java.math.BigInteger;

/**
 * www.etherj.com
 *
 * @author Roman Mandeleil
 * Created on: 19/12/2014 12:14
 */

public interface ProgramInvokeFactory {

    public ProgramInvoke createProgramInvoke(Transaction tx, Block block, Repository repository);

    public ProgramInvoke createProgramInvoke(Program program, DataWord toAddress,
                                             DataWord inValue, DataWord inGas,
                                             BigInteger balanceInt, byte[] dataIn,
                                             Repository repository);


}
