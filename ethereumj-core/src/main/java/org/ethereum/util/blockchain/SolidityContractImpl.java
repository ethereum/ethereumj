package org.ethereum.util.blockchain;

import org.ethereum.core.*;
import org.ethereum.crypto.ECKey;
import org.ethereum.solidity.compiler.CompilationResult;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arsalan on 2017-04-20.
 */
public class SolidityContractImpl implements SolidityContract {
    byte[] address;
    public CompilationResult.ContractMetadata compiled;
    public CallTransaction.Contract contract;
    public List<CallTransaction.Contract> relatedContracts = new ArrayList<>();

    public SolidityContractImpl(String abi) {
        contract = new CallTransaction.Contract(abi);
    }
    public SolidityContractImpl(CompilationResult.ContractMetadata result) {
        this(result.abi);
        compiled = result;
    }

    public void addRelatedContract(String abi) {
        CallTransaction.Contract c = new CallTransaction.Contract(abi);
        relatedContracts.add(c);
    }
    StandaloneBlockchain standaloneBlockchain = new StandaloneBlockchain();
    ECKey txSender;
    SolidityStorageImpl solidityStorageImpl;

    void setAddress(byte[] address) {
        this.address = address;
    }

    @Override
    public byte[] getAddress() {
        if (address == null) {
            throw new RuntimeException("Contract address will be assigned only after block inclusion. Call createBlock() first.");
        }
        return address;
    }

    @Override
    public SolidityCallResult callFunction(String functionName, Object... args) {
        return callFunction(0, functionName, args);
    }

    @Override
    public SolidityCallResult callFunction(long value, String functionName, Object... args) {
        CallTransaction.Function function = contract.getByName(functionName);
        byte[] data = function.encode(args);
        SolidityCallResult res = new SolidityCallResultImpl(this, function);
        standaloneBlockchain.submitNewTx(new PendingTx(this.txSender, null, BigInteger.valueOf(value), data, null, this, res));
        return res;
    }

    @Override
    public Object[] callConstFunction(String functionName, Object... args) {
        return callConstFunction(standaloneBlockchain.getBlockchain().getBestBlock(), functionName, args);
    }

    @Override
    public Object[] callConstFunction(Block callBlock, String functionName, Object... args) {

        CallTransaction.Function func = contract.getByName(functionName);
        if (func == null) throw new RuntimeException("No function with name '" + functionName + "'");
        Transaction tx = CallTransaction.createCallTransaction(0, 0, 100000000000000L,
                Hex.toHexString(getAddress()), 0, func, args);
        tx.sign(new byte[32]);

        Repository repository = standaloneBlockchain.getBlockchain().getRepository().getSnapshotTo(callBlock.getStateRoot()).startTracking();

        try {
            org.ethereum.core.TransactionExecutor executor = new org.ethereum.core.TransactionExecutor
                    (tx, callBlock.getCoinbase(), repository, standaloneBlockchain.getBlockchain().getBlockStore(),
                            standaloneBlockchain.getBlockchain().getProgramInvokeFactory(), callBlock)
                    .setLocalCall(true);

            executor.init();
            executor.execute();
            executor.go();
            executor.finalization();

            return func.decodeResult(executor.getResult().getHReturn());
        } finally {
            repository.rollback();
        }
    }

    @Override
    public SolidityStorage getStorage() {
        return new SolidityStorageImpl(getAddress());
    }

    @Override
    public String getABI() {
        return compiled.abi;
    }

    @Override
    public String getBinary() {
        return compiled.bin;
    }

    @Override
    public void call(byte[] callData) {
        // for this we need cleaner separation of EasyBlockchain to
        // Abstract and Solidity specific
        throw new UnsupportedOperationException();
    }
}