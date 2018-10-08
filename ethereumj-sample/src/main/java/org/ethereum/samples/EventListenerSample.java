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
package org.ethereum.samples;

import org.ethereum.core.Block;
import org.ethereum.core.CallTransaction;
import org.ethereum.core.PendingStateImpl;
import org.ethereum.core.PendingTransaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.listener.EventListener;
import org.ethereum.listener.TxStatus;
import org.ethereum.samples.util.Account;
import org.ethereum.samples.util.Contract;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.math.BigInteger;

import static org.ethereum.publish.event.Events.Type.BLOCK_ADED;
import static org.ethereum.publish.event.Events.Type.PENDING_TRANSACTION_UPDATED;

/**
 * Sample usage of events listener API.
 * {@link EventListener}        Contract events listener
 * <p>
 * - getting free Ether assuming we are running in test network
 * - deploying contract with event, which we are going to track
 * - calling contract and catching corresponding events
 */
public class EventListenerSample extends SingleMinerNetSample {

    class IncEvent {
        IncEvent(String address, Long inc, Long total) {
            this.address = address;
            this.inc = inc;
            this.total = total;
        }

        String address;
        Long inc;
        Long total;

        @Override
        public String toString() {
            return String.format("IncEvent{address='%s', inc=%d, total=%d}", address, inc, total);
        }
    }

    class IncEventListener extends EventListener<IncEvent> {
        /**
         * Minimum required Tx block confirmations for the events
         * from this Tx to be confirmed
         * After this number of confirmations, event will fire {@link #processConfirmed(PendingEvent, IncEvent)}
         * on each confirmation
         */
        protected int blocksToConfirm = 10;
        /**
         * Minimum required Tx block confirmations for this Tx to be purged
         * from the tracking list
         * After this number of confirmations, event will not fire {@link #processConfirmed(PendingEvent, IncEvent)}
         */
        protected int purgeFromPendingsConfirmations = 12;

        public IncEventListener(PendingStateImpl pendingState) {
            super(pendingState);
        }

        public IncEventListener(PendingStateImpl pendingState, String contractABI, byte[] contractAddress) {
            super(pendingState);
            initContractAddress(contractABI, contractAddress);
            // Instead you can init with topic search,
            // so you could get events from all contracts with the same code
            // You could init listener only once
//            initContractTopic(contractABI, sha3("Inc(address,int256,int256)".getBytes()));
        }

        @Override
        protected IncEvent onEvent(CallTransaction.Invocation event, Block block, TransactionReceipt receipt, int txCount, PendingTransaction.State state) {
            // Processing raw event data to fill our model IncEvent
            if ("Inc".equals(event.function.name)) {
                String address = Hex.toHexString((byte[]) event.args[0]);
                Long inc = ((BigInteger) event.args[1]).longValue();
                Long total = ((BigInteger) event.args[2]).longValue();

                IncEvent incEvent = new IncEvent(address, inc, total);
                logger.info("Pending event: {}", incEvent);
                return incEvent;
            } else {
                logger.error("Unknown event: " + event);
            }
            return null;
        }

        @Override
        protected void pendingTransactionsUpdated() {
        }

        /**
         * Events are fired here on every block since blocksToConfirm to purgeFromPendingsConfirmations
         */
        void processConfirmed(PendingEvent evt, IncEvent event) {
            // +1 because on included block we have 1 confirmation
            long numberOfConfirmations = evt.bestConfirmingBlock.getNumber() - evt.includedTo.getNumber() + 1;
            logger.info("Confirmed event: {}, confirmations: {}", event, numberOfConfirmations);
        }

        @Override
        protected boolean pendingTransactionUpdated(PendingEvent evt) {
            if (evt.txStatus == TxStatus.REJECTED || evt.txStatus.confirmed >= blocksToConfirm) {
                evt.eventData.forEach(d -> processConfirmed(evt, d));
            }
            return evt.txStatus == TxStatus.REJECTED || evt.txStatus.confirmed >= purgeFromPendingsConfirmations;
        }
    }

    @Autowired
    private PendingStateImpl pendingState;

    @Override
    protected void onSampleReady() {
        Contract contract = contract("sample");
        IncEventListener eventListener = new IncEventListener(pendingState, contract.getAbi(), contract.getAddress());

        this.ethereum
                .subscribe(BLOCK_ADED, eventListener::onBlock)
                .subscribe(PENDING_TRANSACTION_UPDATED, eventListener::onPendingTransactionUpdated);

        Contract.Caller cow = contractCaller("cow", "sample");
        Contract.Caller cat = contractCaller("cat", "sample");

        cow.call("inc", 777);
        cat.call("inc", 555);
    }

    public static void main(String[] args) {
        class Config extends SingleMinerNetSample.Config {


            @Bean
            @Override
            public SingleMinerNetSample sample() {
                return new EventListenerSample();
            }

            @Override
            protected void registerAccounts(Account.Register register) {
                register
                        .addSameNameAndPass("cat")
                        .addSameNameAndPass("cow");
            }

            @Override
            protected void registerContracts(Contract.Register register) {
                register
                        .add("sample", loadContractSource("sample.sol") );
            }
        }

        // Based on Config class the BasicSample would be created by Spring
        // and its springInit() method would be called as an entry point
        EthereumFactory.createEthereum(Config.class);
    }
}
