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
package org.ethereum.core.casper;

import org.ethereum.casper.config.net.CasperTestConfig;
import org.ethereum.casper.core.CasperFacade;
import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.config.Constants;
import org.ethereum.config.ConstantsAdapter;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.net.BaseNetConfig;
import org.ethereum.core.Block;
import org.ethereum.core.BlockSummary;
import org.ethereum.crypto.ECKey;
import org.ethereum.casper.service.CasperValidatorService;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.sync.SyncManager;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.blockchain.EtherUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.spongycastle.util.encoders.Hex;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.math.BigDecimal;
import java.math.BigInteger;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static org.ethereum.casper.config.net.CasperTestConfig.DYNASTY_LOGOUT_DELAY;
import static org.ethereum.casper.config.net.CasperTestConfig.WITHDRAWAL_DELAY;
import static org.ethereum.crypto.HashUtil.sha3;

@Ignore  // Takes too long to run regularly
public class CasperValidatorTest extends CasperBase {

    private int totalVotes = 0;
    private int totalEpochs = 0;

    private Integer DEPOSIT_SIZE_ETH = 2000;

    protected CasperValidatorService service;

    final ECKey coinbase = ECKey.fromPrivate(sha3("cow".getBytes())); // Premined in light genesis

    protected static class CasperEasyNetConfig extends BaseNetConfig {
        private class CasperEasyConfig extends CasperTestConfig {
            private final Constants constants;
            CasperEasyConfig(BlockchainConfig parent) {

                super(parent);
                constants = new ConstantsAdapter(super.getConstants()) {
                    private final BigInteger BLOCK_REWARD = new BigInteger("1000000000000000000"); // 1 ETH

                    private final BigInteger MINIMUM_DIFFICULTY = BigInteger.ONE;

                    @Override
                    public BigInteger getBLOCK_REWARD() {
                        return BLOCK_REWARD;
                    }

                    @Override
                    public BigInteger getMINIMUM_DIFFICULTY() {
                        return MINIMUM_DIFFICULTY;
                    }
                };
            }

            @Override
            public Constants getConstants() {
                return constants;
            }
        }

        public CasperEasyNetConfig() {
            add(0, new CasperEasyConfig(new FrontierConfig()));
        }
    }

    protected final static BlockchainNetConfig CASPER_EASY_CONFIG = new CasperEasyNetConfig();

    @Override
    BlockchainNetConfig config() {
        return CASPER_EASY_CONFIG;
    }

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();
        // Init with light Genesis
        Resource casperGenesis = new ClassPathResource("/genesis/casper-test.json");
        systemProperties.useGenesis(casperGenesis.getInputStream());
        loadBlockchain();



        BigInteger zeroEpoch = (BigInteger) casper.constCall("current_epoch")[0];
        assertEquals(0, zeroEpoch.longValue());

        systemProperties.overrideParams(
                "casper.validator.enabled", "true",
                "casper.validator.privateKey", Hex.toHexString(coinbase.getPrivKeyBytes()),
                "casper.validator.deposit", DEPOSIT_SIZE_ETH.toString()
        );

        bc.createBlock();


        this.service = new CasperValidatorService(ethereum, systemProperties);
        service.setBlockchain(blockchain);
        SyncManager syncManager = Mockito.mock(SyncManager.class);
        Mockito.when(syncManager.isSyncDone()).thenReturn(true);
        service.setSyncManager(syncManager);
        service.setCasper(casper);
        service.setRepository(repository);
        service.start();
    }

    @Test
    public void complexValidatorTest() throws Exception {
        defaultListener.addListener(assertCasperReceipts);
        BigInteger initialBalance = ethereum.getRepository().getBalance(coinbase.getAddress());

        for (int i = 0; i < 10; ++i) {
            Block block = bc.createBlock();
        }

        BigDecimal curDeposit = calculateCurrentDepositSize(1);
        assertTrue(curDeposit.compareTo(new BigDecimal(EtherUtil.convert(DEPOSIT_SIZE_ETH, EtherUtil.Unit.ETHER))) == 0);
        for (int i = 0; i < 300; ++i) {
            Block block = bc.createBlock();
        }
        // We've earned some money on top of our deposit as premium for our votes, which finalized epochs!!
        BigDecimal increasedDeposit = calculateCurrentDepositSize(1);
        assertTrue(increasedDeposit.compareTo(new BigDecimal(EtherUtil.convert(DEPOSIT_SIZE_ETH, EtherUtil.Unit.ETHER))) > 0);

        // We've left less than (initial - 2000 ETH)
        assertTrue(ethereum.getRepository().getBalance(coinbase.getAddress())
                .compareTo(initialBalance.subtract(EtherUtil.convert(DEPOSIT_SIZE_ETH, EtherUtil.Unit.ETHER))) < 0);
        // Let's logout
        service.voteThenLogout();
        // Withdrawal delay is 5 logout epochs delay + 5 withdrawal  + 1 overhead epoch
        for (int i = 0; i < 50 * (DYNASTY_LOGOUT_DELAY + WITHDRAWAL_DELAY + 1); ++i) {
            Block block = bc.createBlock();
        }
        // We should have more than initialBalance in the end
        assertTrue(ethereum.getRepository().getBalance(coinbase.getAddress()).compareTo(initialBalance) > 0);

        // Check that assertCasperReceipts was called
        assertEquals(17, totalEpochs); // floor division (300 + 550 + 10) / 50
        assertEquals(9, totalVotes); // 5 votes for first 300 blocks (5 epochs as validator) + 4 votes for logout delay, last epoch is w/o vote

        // TODO: add more validators
    }

    protected BigDecimal calculateCurrentDepositSize(long validatorIndex) {
        return calculateCurrentDepositSize(validatorIndex, casper);
    }

    protected static BigDecimal calculateCurrentDepositSize(long validatorIndex, CasperFacade casperFacade) {
        BigDecimal scaleFactor = (BigDecimal) casperFacade.constCall("deposit_scale_factor",
                (BigInteger) casperFacade.constCall("current_epoch")[0])[0];
        BigDecimal curDeposit = (BigDecimal) casperFacade.constCall("validators__deposit", validatorIndex)[0];
        BigDecimal scaledDepositWei = curDeposit.multiply(scaleFactor);
        return scaledDepositWei;
    }

    private EthereumListener assertCasperReceipts = new EthereumListenerAdapter(){
        @Override
        public void onBlock(BlockSummary blockSummary) {
            blockSummary.getReceipts().stream().forEach(receipt -> {
                if (FastByteComparisons.equal(receipt.getTransaction().getReceiveAddress(), casper.getAddress())) {
                    // We don't expect any error receipts
                    assert receipt.isSuccessful();
                    // Following should be true for new epoch and votes, let's check it
                    if (casper.isServiceTx(receipt.getTransaction())) {
                        assert receipt.getGasUsed().length == 0;
                        assert blockSummary.getBlock().getGasUsed() == 0;
                        if (casper.isVote(receipt.getTransaction())) {
                            ++totalVotes;
                        } else {
                            ++totalEpochs;
                        }
                    }
                }
            });
        }
    };
}
