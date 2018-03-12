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

import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.config.Constants;
import org.ethereum.config.ConstantsAdapter;
import org.ethereum.config.blockchain.ByzantiumConfig;
import org.ethereum.config.blockchain.Eip150HFConfig;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.net.BaseNetConfig;
import org.ethereum.core.Block;
import org.ethereum.crypto.ECKey;
import org.ethereum.casper.service.CasperValidatorService;
import org.ethereum.sync.SyncManager;
import org.ethereum.util.blockchain.EtherUtil;
import org.ethereum.vm.GasCost;
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
public class CasperValidatorTest extends CasperBase {

    class CasperEasyConfig extends BaseNetConfig {
        class CasperGasCost extends Eip150HFConfig.GasCostEip150HF {
            public int getEXP_BYTE_GAS()        {     return 10;     }      // before spurious dragon hard fork
        }

        private final GasCost NEW_GAS_COST = new CasperEasyConfig.CasperGasCost();

        private class CasperConfig extends ByzantiumConfig {
            private final Constants constants;
            CasperConfig(BlockchainConfig parent) {

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
            public GasCost getGasCost() {
                return NEW_GAS_COST;
            }

            @Override
            public boolean eip161() {
                return false;
            }

            @Override
            public Constants getConstants() {
                return constants;
            }
        }

        public CasperEasyConfig() {
            add(0, new CasperConfig(new FrontierConfig()));
        }
    }

    @Override
    BlockchainNetConfig config() {
        return new CasperEasyConfig();
    }

    @Test
    public void validatorTest() throws Exception {
        // Init with light Genesis
        Resource casperGenesis = new ClassPathResource("/genesis/casper-test.json");
        systemProperties.useGenesis(casperGenesis.getInputStream());
        loadBlockchain();

        final ECKey coinbase = ECKey.fromPrivate(sha3("cow".getBytes())); // Premined in light genesis

        BigInteger zeroEpoch = (BigInteger) casper.constCall("get_current_epoch")[0];
        assertEquals(0, zeroEpoch.longValue());

        systemProperties.overrideParams(
                "casper.validator.enabled", "true",
                "casper.validator.privateKey", Hex.toHexString(coinbase.getPrivKeyBytes()),
                "casper.validator.deposit", "2000"
        );


        bc.createBlock();

        CasperValidatorService service = new CasperValidatorService(ethereum, systemProperties);
        service.setBlockchain(blockchain);
        SyncManager syncManager = Mockito.mock(SyncManager.class);
        Mockito.when(syncManager.isSyncDone()).thenReturn(true);
        service.setSyncManager(syncManager);
        service.setCasper(casper);
        service.setRepository(repository);
        service.start();

        BigInteger initialBalance = ethereum.getRepository().getBalance(coinbase.getAddress());

        for (int i = 0; i < 10; ++i) {
            Block block = bc.createBlock();
        }
        // Deposit is scaled, so it's neither in wei nor in ETH. Should be 2000 ETH
        // TODO: Convert to ETH or wei
        BigDecimal curDeposit = (BigDecimal) casper.constCall("get_validators__deposit", 1)[0];
        assertTrue(curDeposit.compareTo(new BigDecimal("200000000000")) == 0);
        for (int i = 0; i < 300; ++i) {
            Block block = bc.createBlock();
        }
        // We've earned some money on top of our deposit as premium for our votes, which finalized epochs!!
        BigDecimal increasedDeposit = (BigDecimal) casper.constCall("get_validators__deposit", 1)[0];
        assertTrue(increasedDeposit.compareTo(new BigDecimal("200000000000")) > 0);

        // We've left less than (initial - 2000 ETH)
        assertTrue(ethereum.getRepository().getBalance(coinbase.getAddress()).compareTo(initialBalance.subtract(EtherUtil.convert(2000, EtherUtil.Unit.ETHER))) < 0);
        // Let's logout
        service.voteThenLogout();
        // Withdrawal delay is 5 epochs + 1 vote epoch + overhead
        for (int i = 0; i < 400; ++i) {
            Block block = bc.createBlock();
        }
        // We should have more than initialBalance in the end
        assertTrue(ethereum.getRepository().getBalance(coinbase.getAddress()).compareTo(initialBalance) > 0);

        // TODO: add more checking with listeners etc.
        // TODO: add more validators
    }
}
