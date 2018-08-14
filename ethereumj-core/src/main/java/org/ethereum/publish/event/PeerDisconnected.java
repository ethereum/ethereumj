package org.ethereum.publish.event;

public class PeerDisconnected extends Event<PeerDisconnected.Data> {

    public static class Data {
        private final String host;
        private final long port;

        public Data(String host, long port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public long getPort() {
            return port;
        }
    }

    public PeerDisconnected(String host, long port) {
        super(new Data(host, port));
    }
}
