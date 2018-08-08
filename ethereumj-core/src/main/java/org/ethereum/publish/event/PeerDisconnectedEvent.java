package org.ethereum.publish.event;

public class PeerDisconnectedEvent extends Event<PeerDisconnectedEvent.Data> {

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

    public PeerDisconnectedEvent(String host, long port) {
        super(new Data(host, port));
    }
}
