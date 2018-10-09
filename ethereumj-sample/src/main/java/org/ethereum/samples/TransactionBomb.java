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

import org.ethereum.core.Transaction;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.publish.event.BlockAdded;
import org.ethereum.sync.SyncManager;
import org.spongycastle.util.encoders.Hex;

import java.util.Collections;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.publish.event.Events.Type.BLOCK_ADDED;
import static org.ethereum.publish.event.Events.Type.SYNC_DONE;
import static org.ethereum.util.ByteUtil.longToBytesNoLeadZeroes;
import static org.ethereum.util.ByteUtil.toHexString;

public class TransactionBomb {


    Ethereum ethereum = null;
    boolean startedTxBomb = false;

    public TransactionBomb(Ethereum ethereum) {
        this.ethereum = ethereum
                .subscribe(SYNC_DONE, this::onSyncDone)
                .subscribe(BLOCK_ADDED, this::onBlock);
    }

    public static void main(String[] args) {
        new TransactionBomb(EthereumFactory.createEthereum());
    }


    public void onSyncDone(SyncManager.State state) {
        // We will send transactions only
        // after we have the full chain syncs
        // - in order to prevent old nonce usage
        startedTxBomb = true;
        System.err.println(" ~~~ SYNC DONE ~~~ ");
    }

    public void onBlock(BlockAdded.Data data) {
        if (startedTxBomb){
            byte[] sender = Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826");
            long nonce = ethereum.getRepository().getNonce(sender).longValue();

            for (int i=0; i < 20; ++i){
                sendTx(nonce);
                ++nonce;
                sleep(10);
            }
        }
    }

    private void sendTx(long nonce){

        byte[] gasPrice = longToBytesNoLeadZeroes(1_000_000_000_000L);
        byte[] gasLimit = longToBytesNoLeadZeroes(21000);

        byte[] toAddress = Hex.decode("9f598824ffa7068c1f2543f04efb58b6993db933");
        byte[] value = longToBytesNoLeadZeroes(10_000);

        Transaction tx = new Transaction(longToBytesNoLeadZeroes(nonce),
                gasPrice,
                gasLimit,
                toAddress,
                value,
                null,
                ethereum.getChainIdForNextBlock());

        byte[] privKey = sha3("cow".getBytes());
        tx.sign(privKey);

        ethereum.getChannelManager().sendTransaction(Collections.singletonList(tx), null);
        System.err.println("Sending tx: " + toHexString(tx.getHash()));
    }

    private void sleep(int millis){
        try {Thread.sleep(millis);}
        catch (InterruptedException e) {e.printStackTrace();}
    }
}
