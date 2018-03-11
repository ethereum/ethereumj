/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.casper.core.genesis;

import javafx.util.Pair;
import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.casper.config.CasperProperties;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Blockchain;
import org.ethereum.core.CallTransaction;
import org.ethereum.core.Genesis;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.core.genesis.StateInit;
import org.ethereum.crypto.ECKey;
import org.ethereum.db.BlockStore;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.ethereum.casper.config.net.CasperTestNetConfig.BASE_INTEREST_FACTOR;
import static org.ethereum.casper.config.net.CasperTestNetConfig.BASE_PENALTY_FACTOR;
import static org.ethereum.casper.config.net.CasperTestNetConfig.MIN_DEPOSIT_ETH;
import static org.ethereum.casper.config.net.CasperTestNetConfig.NULL_SIGN_SENDER;
import static org.ethereum.casper.config.net.CasperTestNetConfig.WITHDRAWAL_DELAY;
import static org.ethereum.crypto.HashUtil.sha3;

public class CasperStateInit implements StateInit {

    private static final Logger logger = LoggerFactory.getLogger("general");

    private Genesis genesis;

    private Repository repository;

    private Blockchain blockchain;

    private CasperProperties systemProperties;

    private Genesis initGenesis;

    public CasperStateInit(Genesis genesis, Repository repository, Blockchain blockchain,
                           SystemProperties systemProperties) {
        this.genesis = genesis;
        this.repository = repository;
        this.blockchain = blockchain;
        this.systemProperties = (CasperProperties) systemProperties;
        init();
    }

    @Override
    public void initDB() {
        if (blockchain.getBlockByNumber(0) != null) {
            return;  // Already initialized
        }
        logger.info("DB is empty - adding Genesis");
        Genesis.populateRepository(repository, genesis);
        repository.commit();
        ((org.ethereum.facade.Blockchain)blockchain).getBlockStore().saveBlock(genesis, genesis.getCumulativeDifficulty(), true);
        blockchain.setBestBlock(genesis);
        blockchain.setTotalDifficulty(genesis.getCumulativeDifficulty());

        logger.info("Genesis block loaded");
    }

    /**
     * @return key: Casper contract address
     *       value: Casper state initialization transactions
     */
    public Pair<byte[], List<Transaction>> makeInitTxes() {
        final byte[] casperAddress = new byte[20];

        // All contracts except Casper itself
        final String VIPER_RLP_DECODER_TX = "0xf9035b808506fc23ac0083045f788080b903486103305660006109ac5260006109cc527f0100000000000000000000000000000000000000000000000000000000000000600035046109ec526000610a0c5260006109005260c06109ec51101515585760f86109ec51101561006e5760bf6109ec510336141558576001610a0c52610098565b60013560f76109ec51036020035260005160f66109ec510301361415585760f66109ec5103610a0c525b61022060016064818352015b36610a0c511015156100b557610291565b7f0100000000000000000000000000000000000000000000000000000000000000610a0c5135046109ec526109cc5160206109ac51026040015260016109ac51016109ac5260806109ec51101561013b5760016109cc5161044001526001610a0c516109cc5161046001376001610a0c5101610a0c5260216109cc51016109cc52610281565b60b86109ec5110156101d15760806109ec51036109cc51610440015260806109ec51036001610a0c51016109cc51610460013760816109ec5114156101ac5760807f01000000000000000000000000000000000000000000000000000000000000006001610a0c5101350410151558575b607f6109ec5103610a0c5101610a0c5260606109ec51036109cc51016109cc52610280565b60c06109ec51101561027d576001610a0c51013560b76109ec510360200352600051610a2c526038610a2c5110157f01000000000000000000000000000000000000000000000000000000000000006001610a0c5101350402155857610a2c516109cc516104400152610a2c5160b66109ec5103610a0c51016109cc516104600137610a2c5160b66109ec5103610a0c510101610a0c526020610a2c51016109cc51016109cc5261027f565bfe5b5b5b81516001018083528114156100a4575b5050601f6109ac511115155857602060206109ac5102016109005260206109005103610a0c5261022060016064818352015b6000610a0c5112156102d45761030a565b61090051610a0c516040015101610a0c51610900516104400301526020610a0c5103610a0c5281516001018083528114156102c3575b50506109cc516109005101610420526109cc5161090051016109005161044003f35b61000461033003610004600039610004610330036000f31b2d4f";
        final String SIG_HASHER_TX = "0xf9016d808506fc23ac0083026a508080b9015a6101488061000e6000396101565660007f01000000000000000000000000000000000000000000000000000000000000006000350460f8811215610038576001915061003f565b60f6810391505b508060005b368312156100c8577f01000000000000000000000000000000000000000000000000000000000000008335048391506080811215610087576001840193506100c2565b60b881121561009d57607f8103840193506100c1565b60c08112156100c05760b68103600185013560b783036020035260005101840193505b5b5b50610044565b81810360388112156100f4578060c00160005380836001378060010160002060e052602060e0f3610143565b61010081121561010557600161011b565b6201000081121561011757600261011a565b60035b5b8160005280601f038160f701815382856020378282600101018120610140526020610140f350505b505050505b6000f31b2d4f";
        final String PURITY_CHECKER_TX = "0xf90467808506fc23ac00830583c88080b904546104428061000e60003961045056600061033f537c0100000000000000000000000000000000000000000000000000000000600035047f80010000000000000000000000000000000000000030ffff1c0e00000000000060205263a1903eab8114156103f7573659905901600090523660048237600435608052506080513b806020015990590160009052818152602081019050905060a0526080513b600060a0516080513c6080513b8060200260200159905901600090528181526020810190509050610100526080513b806020026020015990590160009052818152602081019050905061016052600060005b602060a05103518212156103c957610100601f8360a051010351066020518160020a161561010a57fe5b80606013151561011e57607f811315610121565b60005b1561014f5780607f036101000a60018460a0510101510482602002610160510152605e8103830192506103b2565b60f18114801561015f5780610164565b60f282145b905080156101725780610177565b60f482145b9050156103aa5760028212151561019e5760606001830360200261010051015112156101a1565b60005b156101bc57607f6001830360200261010051015113156101bf565b60005b156101d157600282036102605261031e565b6004821215156101f057600360018303602002610100510151146101f3565b60005b1561020d57605a6002830360200261010051015114610210565b60005b1561022b57606060038303602002610100510151121561022e565b60005b1561024957607f60038303602002610100510151131561024c565b60005b1561025e57600482036102605261031d565b60028212151561027d57605a6001830360200261010051015114610280565b60005b1561029257600282036102605261031c565b6002821215156102b157609060018303602002610100510151146102b4565b60005b156102c657600282036102605261031b565b6002821215156102e65760806001830360200261010051015112156102e9565b60005b156103035760906001830360200261010051015112610306565b60005b1561031857600282036102605261031a565bfe5b5b5b5b5b604060405990590160009052600081526102605160200261016051015181602001528090502054156103555760016102a052610393565b60306102605160200261010051015114156103755760016102a052610392565b60606102605160200261010051015114156103915760016102a0525b5b5b6102a051151561039f57fe5b6001830192506103b1565b6001830192505b5b8082602002610100510152600182019150506100e0565b50506001604060405990590160009052600081526080518160200152809050205560016102e05260206102e0f35b63c23697a8811415610440573659905901600090523660048237600435608052506040604059905901600090526000815260805181602001528090502054610300526020610300f35b505b6000f31b2d4f";

        List<String> txStrs = new ArrayList<>();
        txStrs.add(VIPER_RLP_DECODER_TX);
        txStrs.add(SIG_HASHER_TX);
        txStrs.add(PURITY_CHECKER_TX);
        BigInteger nonce = repository.getNonce(NULL_SIGN_SENDER.getAddress());
        List<Transaction> txs = new ArrayList<>();
        final long gasPriceFund = 25_000_000_000L;
        for (int i = 0; i < txStrs.size(); ++i) {
            Transaction deployTx = new Transaction(ByteUtil.hexStringToBytes(txStrs.get(i)));
            BigInteger value = BigInteger.ZERO;
            value = value.add(ByteUtil.bytesToBigInteger(deployTx.getValue()));
            value = value.add(
                    ByteUtil.bytesToBigInteger(deployTx.getGasPrice())
                            .multiply(ByteUtil.bytesToBigInteger(deployTx.getGasLimit()))
            );
            Transaction fundTx = new Transaction(
                    ByteUtil.bigIntegerToBytes(nonce),
                    ByteUtil.longToBytesNoLeadZeroes(gasPriceFund),
                    ByteUtil.longToBytesNoLeadZeroes(90_000),
                    deployTx.getSender(),
                    ByteUtil.bigIntegerToBytes(value),
                    new byte[0],
                    null
            );
            fundTx.sign(NULL_SIGN_SENDER);
            txs.add(fundTx);
            txs.add(deployTx);
            nonce = nonce.add(BigInteger.ONE);
        }

        // 0 - fund, 1 - rlp, 2 - fund, 3 - sig hasher, 4 - fund, 5 - purity checker
        byte[] sigHasherContract = txs.get(3).getContractAddress();
        byte[] purityCheckerContract = txs.get(5).getContractAddress();

        // Casper!
        try {
            // Sources:
            // https://github.com/ethereum/casper/blob/9106ad647857e6a545f55d7f6193bdc03bb9f5cd/casper/contracts/simple_casper.v.py
            String casperBinStr = systemProperties.getCasperBin();
            byte[] casperBin = ByteUtil.hexStringToBytes(casperBinStr);

            CallTransaction.Contract contract = new CallTransaction.Contract(systemProperties.getCasperAbi());

            byte[] casperInit = contract.getConstructor().encodeArguments(
                    systemProperties.getCasperEpochLength(),  // Epoch length
                    WITHDRAWAL_DELAY, // Withdrawal delay
                    ECKey.fromPrivate(sha3("0".getBytes())).getAddress(),  // Owner
                    sigHasherContract,  // Signature hasher contract
                    purityCheckerContract,  // Purity checker contract
                    BASE_INTEREST_FACTOR,  // Base interest factor
                    BASE_PENALTY_FACTOR,  // Base penalty factor
                    BigInteger.valueOf(MIN_DEPOSIT_ETH).multiply(BigInteger.TEN.pow(18)) // Minimum validator deposit in wei
            );

            Transaction tx = new Transaction(
                    ByteUtil.bigIntegerToBytes(nonce),
                    ByteUtil.longToBytesNoLeadZeroes(gasPriceFund),
                    ByteUtil.longToBytesNoLeadZeroes(5_000_000),
                    new byte[0],
                    ByteUtil.longToBytesNoLeadZeroes(0),
                    ArrayUtils.addAll(casperBin, casperInit),  // Merge contract and constructor args
                    null);
            tx.sign(NULL_SIGN_SENDER);

            // set casperAddress
            System.arraycopy(tx.getContractAddress(), 0, casperAddress, 0, 20);
            txs.add(tx);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate Casper init transactions", ex);
        }

        return new Pair<>(casperAddress, txs);
    }

    private void init() {
        Repository repo = repository.getSnapshotTo(null);
        Genesis.populateRepository(repo, genesis);

        // Metropolis dummy
        repo.saveCode(Hex.decode("0000000000000000000000000000000000000010"), Hex.decode("6000355460205260206020f3"));
        repo.saveCode(Hex.decode("0000000000000000000000000000000000000020"), Hex.decode("6000355460205260206020f3"));

        this.initGenesis = genesis;
        this.initGenesis.setStateRoot(repo.getRoot());

        Pair<byte[], List<Transaction>> res = makeInitTxes();
        systemProperties.setCasperAddress(res.getKey());
    }

    @Override
    public Genesis getInitGenesis() {
        return initGenesis;
    }
}
