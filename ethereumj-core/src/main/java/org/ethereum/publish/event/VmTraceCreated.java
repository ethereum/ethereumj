package org.ethereum.publish.event;

public class VmTraceCreated extends Event<VmTraceCreated.Data> {

    public static class Data {
        private final String txHash;
        private final String trace;

        public Data(String txHash, String trace) {
            this.txHash = txHash;
            this.trace = trace;
        }

        public String getTxHash() {
            return txHash;
        }

        public String getTrace() {
            return trace;
        }
    }

    public VmTraceCreated(String txHash, String trace) {
        super(new Data(txHash, trace));
    }
}
