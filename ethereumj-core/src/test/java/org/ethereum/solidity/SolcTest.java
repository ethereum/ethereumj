package org.ethereum.solidity;

import org.ethereum.solidity.compiler.Solc;
import org.junit.Test;

public class SolcTest {

    @Test
    public void initBundle() {
        assert Solc.INSTANCE.getExecutable() != null;
        assert Solc.INSTANCE.getExecutable().canExecute();
    }
}
