package org.ethereum.tools.bc;

import org.ethereum.core.Block;

/**
 * Created by Anton Nashatyrev on 23.03.2016.
 */
public interface SolidityContract {

    byte[] getAddress();

    Object[] callFunction(String functionName, Object ... args);

    Object[] callFunction(long value, String functionName, Object ... args);

    Object[] callConstFunction(String functionName, Object ... args);

    Object[] callConstFunction(Block callBlock, String functionName, Object... args);

    SolidityStorage getStorage();

    String getABI();

    String getBinary();
}
