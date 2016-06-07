package org.ethereum.samples;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.core.TransactionExecutionSummary;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.ethereum.util.ByteUtil.toHexString;

public class TxSummarySaver extends EthereumListenerAdapter {

    private SummaryUpdates summaryUpdates = new SummaryUpdates();

    public static void main(String[] args) {
        Ethereum ethereum = EthereumFactory.createEthereum();
        ethereum.addListener(new TxSummarySaver());
    }

    @Override
    public void onTransactionExecuted(TransactionExecutionSummary summary) {
        summaryUpdates.add(summary);
    }

    private static class SummaryUpdates extends ArrayList<String> {

        private static class Log {

            public List<String> topics;
            public String data;

            Log(LogInfo logInfo) {
                this.data = toHexString(logInfo.getData());
                this.topics = new ArrayList<>();
                for (DataWord topic : logInfo.getTopics()) {
                    this.topics.add(topic.toString());
                }
            }

            public static String listToJson(List<LogInfo> logsInfo) {
                String result = "[]";

                try {
                    List<Log> logs = new ArrayList<>();
                    for (LogInfo logInfo : logsInfo) {
                        logs.add(new Log(logInfo));
                    }
                    result = new ObjectMapper().writeValueAsString(logs);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return result;
            }
        }

        public static final File SCRIPT_DIR = new File("/home/eshevchenko/Desktop/summary_updates");
        public static final int BATCH_SIZE = 25_000;

        private int flushCount;

        public SummaryUpdates() {
            SCRIPT_DIR.mkdirs();
        }

        public void add(TransactionExecutionSummary summary) {
            if (CollectionUtils.isEmpty(summary.getLogs()) && ArrayUtils.isEmpty(summary.getResult())) return;

            String txHash = toHexString(summary.getTransactionHash());
            String logs = Log.listToJson(summary.getLogs());
            String result = toHexString(summary.getResult());
            add(format("update transaction_summary set logs = '%s', result = '%s' where transaction_hash = '%s';\n", logs, result, txHash));

            if (size() >= BATCH_SIZE) {
                flushToDisk();
                clear();
                System.out.println(flushCount * BATCH_SIZE + " flushed on disk.");
            }
        }

        private void flushToDisk() {
            long start = System.currentTimeMillis();
            File file = new File(SCRIPT_DIR, format("summary_update_%d.sql", ++flushCount));
            System.out.println("=========== " + file.getName() + " writing ...");
            try (FileWriter writer = new FileWriter(file)) {
                for (String record : this) {
                    writer.write(record);
                }
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                long end = System.currentTimeMillis();
                System.out.println("=========== " + (end - start) / 1000f + " seconds");
            }
        }

    }
}
