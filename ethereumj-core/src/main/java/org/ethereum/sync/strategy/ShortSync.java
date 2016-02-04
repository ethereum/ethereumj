package org.ethereum.sync.strategy;

import org.ethereum.net.eth.message.NewBlockHashesMessage;
import org.ethereum.net.eth.message.NewBlockMessage;

/**
 * Implements short sync algorithm <br/>
 *
 * Keeps peer's sync state by processing {@link NewBlockMessage} and {@link NewBlockHashesMessage}.
 * It's turned on when {@link LongSync} finishes its work
 *
 * @author Mikhail Kalinin
 * @since 02.02.2016
 */
public class ShortSync extends AbstractSyncStrategy {

    @Override
    protected void doWork() {

    }
}
