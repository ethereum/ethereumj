package org.ethereum.vm;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.db.BlockStore;
import org.ethereum.facade.Repository;

import java.math.BigInteger;

/**
 * @author Roman Mandeleil
 * @since 19.12.2014
 */
public interface ProgramInvokeFactory {

    public ProgramInvoke createProgramInvoke(Transaction tx, Block block,
                                             Repository repository, BlockStore blockStore);

    public ProgramInvoke createProgramInvoke(Program program, DataWord toAddress,
                                             DataWord inValue, DataWord inGas,
                                             BigInteger balanceInt, byte[] dataIn,
                                             Repository repository, BlockStore blockStore, boolean byTestingSuite);


}
