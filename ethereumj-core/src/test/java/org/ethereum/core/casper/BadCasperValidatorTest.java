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

import org.ethereum.casper.service.CasperValidatorService;
import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.core.Block;
import org.ethereum.core.BlockSummary;
import org.ethereum.crypto.ECKey;
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
import static org.ethereum.crypto.HashUtil.sha3;

@Ignore  // Takes too long to run usually
public class BadCasperValidatorTest extends CasperBase {

    private int totalVotes = 0;
    private int totalEpochs = 0;

    private Integer DEPOSIT_SIZE_ETH = 2000;

    protected CasperValidatorService service;

    final ECKey coinbase = ECKey.fromPrivate(sha3("cow".getBytes())); // Premined in light genesis

    @Override
    BlockchainNetConfig config() {
        return CasperValidatorTest.CASPER_EASY_CONFIG;
    }

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();
        // Init with light Genesis
        Resource casperGenesis = new ClassPathResource("/genesis/casper-test.json");
        systemProperties.useGenesis(casperGenesis.getInputStream());
        loadBlockchain();



        BigInteger zeroEpoch = (BigInteger) casper.constCall("get_current_epoch")[0];
        assertEquals(0, zeroEpoch.longValue());

        systemProperties.overrideParams(
                "casper.validator.enabled", "true",
                "casper.validator.privateKey", Hex.toHexString(coinbase.getPrivKeyBytes()),
                "casper.validator.deposit", DEPOSIT_SIZE_ETH.toString()
        );

        bc.createBlock();

        this.service = new CasperValidatorService(ethereum, systemProperties) {
            @Override
            protected byte[] makeVote(long validatorIndex, byte[] targetHash, long targetEpoch, long sourceEpoch, ECKey sender) {
                // Make it send incorrect votes
                return super.makeVote(validatorIndex, targetHash, targetEpoch + 1, sourceEpoch, sender);
            }
        };
        service.setBlockchain(blockchain);
        SyncManager syncManager = Mockito.mock(SyncManager.class);
        Mockito.when(syncManager.isSyncDone()).thenReturn(true);
        service.setSyncManager(syncManager);
        service.setCasper(casper);
        service.setRepository(repository);
        service.start();
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


    /**
     * Let's send bad votes
     * these votes should not be included in blocks
     * plus deposit size should decrease
     */
    @Test
    public void badVotesValidatorTest() throws Exception {
        defaultListener.addListener(assertCasperReceipts);

        for (int i = 0; i < 10; ++i) {
            Block block = bc.createBlock();
        }
        BigDecimal curDeposit = calculateCurrentDepositSize(1);
        assertTrue(curDeposit.compareTo(new BigDecimal(EtherUtil.convert(DEPOSIT_SIZE_ETH, EtherUtil.Unit.ETHER))) == 0);

        for (int i = 0; i < 300; ++i) {
            Block block = bc.createBlock();
        }
        assertEquals(6, totalEpochs); // floor division (300 + 10) / 50
        assertEquals(0, totalVotes); // No vote was included in any block

        // We've lost some money from our deposit because we are sleeping
        BigDecimal decreasedDeposit = calculateCurrentDepositSize(1);
        assertTrue(decreasedDeposit.compareTo(new BigDecimal(EtherUtil.convert(DEPOSIT_SIZE_ETH, EtherUtil.Unit.ETHER))) < 0);
    }

    protected BigDecimal calculateCurrentDepositSize(long validatorIndex) {
        return CasperValidatorTest.calculateCurrentDepositSize(validatorIndex, casper);
    }
}
