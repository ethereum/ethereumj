package org.ethereum.net.swarm.kademlia.stub;

import java.util.Date;

/**
 * Created by Admin on 24.06.2015.
 */
public interface Node {
    Address addr();

    String url();

    Date lastActive();

    void Drop();

}
