package org.ethereum.config.blockchain;

import org.ethereum.config.Constants;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Transaction;
import org.ethereum.db.BlockStore;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.RepositoryTrack;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Set;

/**
 * Created by Anton Nashatyrev on 20.06.2016.
 */
public class HomesteadDAOConfig extends HomesteadConfig {

    public static final long DAO_RESCUE_BLOCK = 1_800_000;
    public static final long DAO_RESCUE_GAS_LIMIT_TRIGGER = 4_000_000;
    public static final byte[] DAO_CODE_HASH = Hex.decode("6a5d24750f78441e56fec050dc52fe8e911976485b7472faac7464a176a67caa");
    public static final byte[][] WHITELISTED_RECIPIENTS = new byte[][] {
            Hex.decode("Da4a4626d3E16e094De3225A751aAb7128e96526"),
            Hex.decode("2ba9D006C1D72E67A70b5526Fc6b4b0C0fd6D334")
    };


    private final long daoRescueBlock;
    private final long daoRescueGasLimitTrigger;
    private final byte[] daoCodeHash;
    private final byte[][] whitelist;
    private Boolean rescue = null;

    public HomesteadDAOConfig() {
        this(DAO_RESCUE_BLOCK, DAO_RESCUE_GAS_LIMIT_TRIGGER, DAO_CODE_HASH, WHITELISTED_RECIPIENTS);
    }

    public HomesteadDAOConfig(Constants constants) {
        this(constants, DAO_RESCUE_BLOCK, DAO_RESCUE_GAS_LIMIT_TRIGGER, DAO_CODE_HASH, WHITELISTED_RECIPIENTS);
    }

    public HomesteadDAOConfig(long daoRescueBlock, long daoRescueGasLimitTrigger, byte[] daoCodeHash, byte[][] whitelist) {
        this.daoRescueBlock = daoRescueBlock;
        this.daoRescueGasLimitTrigger = daoRescueGasLimitTrigger;
        this.daoCodeHash = daoCodeHash;
        this.whitelist = whitelist;
    }

    public HomesteadDAOConfig(Constants constants, long daoRescueBlock, long daoRescueGasLimitTrigger,
                              byte[] daoCodeHash, byte[][] whitelist) {
        super(constants);
        this.daoRescueBlock = daoRescueBlock;
        this.daoRescueGasLimitTrigger = daoRescueGasLimitTrigger;
        this.daoCodeHash = daoCodeHash;
        this.whitelist = whitelist;
    }

    private boolean shouldRescueDAO(BlockStore bs, Block curBlock) {
        if (rescue == null) {
            BlockHeader forkBlock;
            if (curBlock.getNumber() == daoRescueBlock) {
                forkBlock = curBlock.getHeader();
            } else {
                byte[] blockHash =  bs.getBlockHashByNumber(daoRescueBlock, curBlock.getParentHash());
                if (blockHash == null) return false;
                forkBlock = bs.getBlockByHash(blockHash).getHeader();
            }
            long gasLimit = ByteUtil.byteArrayToLong(forkBlock.getGasLimit());
            boolean ret = gasLimit < daoRescueGasLimitTrigger;
            if (curBlock.getNumber() - daoRescueBlock > 192) {
                // no more need to check the branch
                rescue = ret;
            }
            return ret;
        } else {
            return rescue;
        }
    }

    @Override
    public String validateTransactionChanges(BlockStore blockStore, Block curBlock, Transaction tx,
                                             RepositoryTrack repositoryTrack) {
        if (shouldRescueDAO(blockStore, curBlock)) {
            Set<ByteArrayWrapper> changedAddresses = repositoryTrack.getFullAddressSet();
            for (ByteArrayWrapper address : changedAddresses) {
                AccountState accountState = repositoryTrack.getOriginRepository().getAccountState(address.getData());
                byte[] codeHash = accountState.getCodeHash();
                if (codeHash != null && FastByteComparisons.compareTo(daoCodeHash, 0, 32, codeHash, 0, 32) == 0) {
                    BigInteger newBalance = repositoryTrack.getBalance(address.getData());
                    BigInteger oldBalance = repositoryTrack.getOriginRepository().getBalance(address.getData());
                    if (newBalance.compareTo(oldBalance) < 0) {
                        for (byte[] whiteRecipient : whitelist) {
                            if (FastByteComparisons.compareTo(tx.getReceiveAddress(), 0, 20, whiteRecipient, 0, 20) == 0) {
                                return null;
                            }
                        }
                        return String.format("RescueDAO: DAO balance decrease %s > %s, recipient is not whitelisted: %s",
                                oldBalance, newBalance, Hex.toHexString(tx.getReceiveAddress()));
                    }
                }
            }
        }
        return null;
    }
}
