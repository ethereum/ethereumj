package org.ethereum.vm.program.invoke;

import org.ethereum.core.Block;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.db.BlockStore;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.Program;

import java.math.BigInteger;

/**
 * @author Roman Mandeleil
 * @since 19.12.2014
 */
public interface ProgramInvokeFactory {

    ProgramInvoke createProgramInvoke(Transaction tx, Block block,
                                      Repository repository, BlockStore blockStore);

    ProgramInvoke createProgramInvoke(Program program, DataWord toAddress,
                                             DataWord inValue, DataWord inGas,
                                             BigInteger balanceInt, byte[] dataIn,
                                             Repository repository, BlockStore blockStore, boolean byTestingSuite);


}
