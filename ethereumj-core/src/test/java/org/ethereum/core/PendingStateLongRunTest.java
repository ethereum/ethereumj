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
package org.ethereum.core;

import org.ethereum.config.CommonConfig;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.db.RepositoryRoot;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.validator.DependentBlockHeaderRuleAdapter;
import org.ethereum.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.ethereum.util.BIUtil.toBI;
import static org.junit.Assert.*;

/**
 * @author Mikhail Kalinin
 * @since 24.09.2015
 */
@Ignore
public class PendingStateLongRunTest {

    private Blockchain blockchain;

    private PendingState pendingState;

    private List<String> strData;

    @Before
    public void setup() throws URISyntaxException, IOException, InterruptedException {

        blockchain = createBlockchain((Genesis) Genesis.getInstance());
        pendingState = ((BlockchainImpl) blockchain).getPendingState();

        URL blocks = ClassLoader.getSystemResource("state/47250.dmp");
        File file = new File(blocks.toURI());
        strData = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        for (int i = 0; i < 46000; i++) {
            Block b = new Block(Hex.decode(strData.get(i)));
            blockchain.tryToConnect(b);
        }
    }

    @Test // test with real data from the frontier net
    public void test_1() {

        Block b46169 = new Block(Hex.decode(strData.get(46169)));
        Block b46170 = new Block(Hex.decode(strData.get(46170)));

        Transaction tx46169 = b46169.getTransactionsList().get(0);
        Transaction tx46170 = b46170.getTransactionsList().get(0);

        Repository pending = pendingState.getRepository();

        BigInteger balanceBefore46169 = pending.getAccountState(tx46169.getReceiveAddress()).getBalance();
        BigInteger balanceBefore46170 = pending.getAccountState(tx46170.getReceiveAddress()).getBalance();

        pendingState.addPendingTransaction(tx46169);
        pendingState.addPendingTransaction(tx46170);

        for (int i = 46000; i < 46169; i++) {
            Block b = new Block(Hex.decode(strData.get(i)));
            blockchain.tryToConnect(b);
        }

        pending = pendingState.getRepository();

        BigInteger balanceAfter46169 = balanceBefore46169.add(toBI(tx46169.getValue()));

        assertEquals(pendingState.getPendingTransactions().size(), 2);
        assertEquals(balanceAfter46169, pending.getAccountState(tx46169.getReceiveAddress()).getBalance());

        blockchain.tryToConnect(b46169);
        pending = pendingState.getRepository();

        assertEquals(balanceAfter46169, pending.getAccountState(tx46169.getReceiveAddress()).getBalance());
        assertEquals(pendingState.getPendingTransactions().size(), 1);

        BigInteger balanceAfter46170 = balanceBefore46170.add(toBI(tx46170.getValue()));

        assertEquals(balanceAfter46170, pending.getAccountState(tx46170.getReceiveAddress()).getBalance());

        blockchain.tryToConnect(b46170);
        pending = pendingState.getRepository();

        assertEquals(balanceAfter46170, pending.getAccountState(tx46170.getReceiveAddress()).getBalance());
        assertEquals(pendingState.getPendingTransactions().size(), 0);
    }

    private Blockchain createBlockchain(Genesis genesis) {
        IndexedBlockStore blockStore = new IndexedBlockStore();
        blockStore.init(new HashMapDB<byte[]>(), new HashMapDB<byte[]>());

        Repository repository = new RepositoryRoot(new HashMapDB());

        ProgramInvokeFactoryImpl programInvokeFactory = new ProgramInvokeFactoryImpl();

        BlockchainImpl blockchain = new BlockchainImpl(blockStore, repository)
                .withParentBlockHeaderValidator(new CommonConfig().parentHeaderValidator());
        blockchain.setParentHeaderValidator(new DependentBlockHeaderRuleAdapter());
        blockchain.setProgramInvokeFactory(programInvokeFactory);

        blockchain.byTest = true;

        PendingStateImpl pendingState = new PendingStateImpl(new EthereumListenerAdapter(), blockchain);

        pendingState.setBlockchain(blockchain);
        blockchain.setPendingState(pendingState);

        Repository track = repository.startTracking();
        Genesis.populateRepository(track, genesis);

        track.commit();

        blockStore.saveBlock(Genesis.getInstance(), Genesis.getInstance().getCumulativeDifficulty(), true);

        blockchain.setBestBlock(Genesis.getInstance());
        blockchain.setTotalDifficulty(Genesis.getInstance().getCumulativeDifficulty());

        return blockchain;
    }
}
