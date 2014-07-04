package org.ethereum.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.UnpooledUnsafeDirectByteBuf;
import io.netty.channel.FileRegion;
import io.netty.channel.MessageSizeEstimator;
import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 04/07/2014 13:16
 */

public class EthereumMessageSizeEstimator implements MessageSizeEstimator {

    private final Handle handle = new HandleImpl();

    private static final class HandleImpl implements Handle {


        private HandleImpl() {

        }

        @Override
        public int size(Object msg) {

            ByteBuf buffer = ((ByteBuf)msg);

            if (buffer.readableBytes() < 8) throw new RuntimeException("Not Ethereum packet");
            int msgSize = ((buffer.getByte(4) & 0xFF) << 24) +
                          ((buffer.getByte(5) & 0xFF) << 16) +
                          ((buffer.getByte(6) & 0xFF) << 8) +
                          ((buffer.getByte(7) & 0xFF));

            return msgSize;
        }
    }

    @Override
    public Handle newHandle() {
        return handle;
    }
}
