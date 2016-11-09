package org.ethereum.config.blockchain;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.config.Constants;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.db.BlockStore;
import org.ethereum.mine.MinerIfc;
import org.ethereum.util.Utils;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.GasCost;
import org.ethereum.vm.OpCode;
import org.ethereum.vm.program.Program;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 14.10.2016.
 */
public class Eip150HFConfig implements BlockchainConfig, BlockchainNetConfig {
    protected BlockchainConfig parent;


    private static final GasCost NEW_GAS_COST = new GasCost() {
        public int getBALANCE()             {     return 400;     }
        public int getEXT_CODE_SIZE()       {     return 700;     }
        public int getEXT_CODE_COPY()       {     return 700;     }
        public int getSLOAD()               {     return 200;     }
        public int getCALL()                {     return 700;     }
        public int getSUICIDE()             {     return 5000;    }
        public int getNEW_ACCT_SUICIDE()    {     return 25000;   }
    };

    public Eip150HFConfig(BlockchainConfig parent) {
        this.parent = parent;
    }

    @Override
    public DataWord getCallGas(OpCode op, DataWord requestedGas, DataWord availableGas) throws Program.OutOfGasException {
        DataWord maxAllowed = Utils.allButOne64th(availableGas);
        return requestedGas.compareTo(maxAllowed) > 0 ? maxAllowed : requestedGas;
    }

    @Override
    public DataWord getCreateGas(DataWord availableGas) {
        return Utils.allButOne64th(availableGas);
    }

    @Override
    public Constants getConstants() {
        return parent.getConstants();
    }

    @Override
    public MinerIfc getMineAlgorithm(SystemProperties config) {
        return parent.getMineAlgorithm(config);
    }

    @Override
    public BigInteger calcDifficulty(BlockHeader curBlock, BlockHeader parent) {
        return this.parent.calcDifficulty(curBlock, parent);
    }

    @Override
    public long getTransactionCost(Transaction tx) {
        return parent.getTransactionCost(tx);
    }

    @Override
    public boolean acceptTransactionSignature(Transaction tx) {
        return parent.acceptTransactionSignature(tx);
    }

    @Override
    public String validateTransactionChanges(BlockStore blockStore, Block curBlock, Transaction tx, Repository repository) {
        return parent.validateTransactionChanges(blockStore, curBlock, tx, repository);
    }

    @Override
    public void hardForkTransfers(Block block, Repository repo) {
        parent.hardForkTransfers(block, repo);
    }

    @Override
    public List<Pair<Long, byte[]>> blockHashConstraints() {
        return parent.blockHashConstraints();
    }

    @Override
    public boolean noEmptyAccounts() {
        return parent.noEmptyAccounts();
    }

    @Override
    public GasCost getGasCost() {
        return NEW_GAS_COST;
    }

    @Override
    public BlockchainConfig getConfigForBlock(long blockNumber) {
        return this;
    }

    @Override
    public Constants getCommonConstants() {
        return getConstants();
    }

}
