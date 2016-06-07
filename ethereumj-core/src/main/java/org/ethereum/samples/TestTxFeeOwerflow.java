package org.ethereum.samples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.ethereum.core.Block;
import org.ethereum.core.TransactionExecutionSummary;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.core.TransactionTouchedStorage;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.listener.EthereumListenerAdapter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.ethereum.util.ByteUtil.toHexString;

/**
 * Created by eshevchenko on 03.06.16.
 */
public class TestTxFeeOwerflow extends EthereumListenerAdapter {

    private final Ethereum ethereum;

    public TestTxFeeOwerflow(Ethereum ethereum) {
        this.ethereum = ethereum;
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {
        System.out.println(block.getNumber() + " handled...");
    }

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public void onTransactionExecuted(TransactionExecutionSummary summary) {
        Collection<TransactionTouchedStorage.Entry> entries = summary.getTouchedStorage().getEntries();
        if (entries.isEmpty()) return;

        File file = new File("/home/eshevchenko/projects/git/ethereumj/database/storage-diff/" + toHexString(summary.getTransactionHash()));
        File dir = file.getParentFile();
        if (!dir.exists()) dir.mkdirs();

        try {
            OBJECT_MAPPER.writeValue(file, entries);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        Ethereum ethereum = EthereumFactory.createEthereum();
        ethereum.addListener(new TestTxFeeOwerflow(ethereum));
    }
}
