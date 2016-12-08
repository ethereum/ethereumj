package org.ethereum.config.net;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.blockchain.*;
import org.spongycastle.util.encoders.Hex;

import java.util.Collections;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class RopstenNetConfig extends AbstractNetConfig {

    public RopstenNetConfig() {
        add(0, new HomesteadConfig());
        add(10, new RopstenConfig(new HomesteadConfig()));
    }
}
