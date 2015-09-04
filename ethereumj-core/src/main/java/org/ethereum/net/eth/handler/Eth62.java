package org.ethereum.net.eth.handler;

import io.netty.channel.ChannelHandlerContext;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Eth 62
 *
 * @author Mikhail Kalinin
 * @since 04.09.2015
 */
@Scope("prototype")
@Component
public class Eth62 extends EthHandler {

    private final static Logger logger = LoggerFactory.getLogger("sync");

    public Eth62(EthVersion version) {
        super(version);
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, EthMessage msg) throws InterruptedException {

        super.channelRead0(ctx, msg);

        switch (msg.getCommand()) {
            case GET_BLOCK_HEADERS:
                processGetBlockHeaders((GetBlockHeadersMessage) msg);
                break;
            case BLOCK_HEADERS:
                processBlockHeaders((BlockHeadersMessage) msg);
                break;
            case GET_BLOCK_BODIES:
                processGetBlockBodies((GetBlockBodiesMessage) msg);
                break;
            case BLOCK_BODIES:
                processBlockBodies((BlockBodiesMessage) msg);
                break;
            default:
                break;
        }
    }

    @Override
    protected void processNewBlockHashes(NewBlockHashesMessage msg) {

    }

    @Override
    protected void startHashRetrieving() {

    }

    @Override
    protected boolean startBlockRetrieving() {
        return false;
    }

    private void processGetBlockHeaders(GetBlockHeadersMessage msg) {

    }

    private void processBlockHeaders(BlockHeadersMessage msg) {

    }

    private void processGetBlockBodies(GetBlockBodiesMessage msg) {

    }

    private void processBlockBodies(BlockBodiesMessage msg) {

    }
}
