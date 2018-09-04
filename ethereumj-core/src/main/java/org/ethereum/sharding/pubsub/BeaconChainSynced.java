package org.ethereum.sharding.pubsub;

import org.ethereum.sharding.domain.Beacon;

/**
 * @author Mikhail Kalinin
 * @since 03.09.2018
 */
public class BeaconChainSynced extends Event<BeaconChainSynced.Data> {

    public static class Data {
        private final Beacon head;

        public Data(Beacon head) {
            this.head = head;
        }

        public Beacon getHead() {
            return head;
        }
    }

    public BeaconChainSynced(Beacon head) {
        super(new Data(head));
    }
}
