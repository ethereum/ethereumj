/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.samples;

import com.typesafe.config.ConfigFactory;
import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.ECKey;
import org.ethereum.facade.EthereumFactory;
import org.springframework.context.annotation.Bean;

import static org.ethereum.crypto.HashUtil.sha3;

/**
 * This class just extends the BasicSample with the config which connect the peer to the test network
 * This class can be used as a base for free transactions testing
 * (everyone may use that 'cow' sender which has pretty enough fake coins)
 *
 * Created by Anton Nashatyrev on 10.02.2016.
 */
public class TestNetSample extends BasicSample {
    /**
     * Use that sender key to sign transactions
     */
    protected final byte[] senderPrivateKey = sha3("cow".getBytes());
    // sender address is derived from the private key
    protected final byte[] senderAddress = ECKey.fromPrivate(senderPrivateKey).getAddress();

    protected abstract static class TestNetConfig {
        private final String config =
                // Ropsten revive network configuration
                "peer.discovery.enabled = true \n" +
                "peer.listen.port = 30303 \n" +
                "peer.networkId = 3 \n" +
                // a number of public peers for this network (not all of then may be functioning)
                "peer.active = [" +
                "    {url = 'enode://6ce05930c72abc632c58e2e4324f7c7ea478cec0ed4fa2528982cf34483094e9cbc9216e7aa349691242576d552a2a56aaeae426c5303ded677ce455ba1acd9d@13.84.180.240:30303'}," +
                "    {url = 'enode://20c9ad97c081d63397d7b685a412227a40e23c8bdc6688c6f37e97cfbc22d2b4d1db1510d8f61e6a8866ad7f0e17c02b14182d37ea7c3c8b9c2683aeb6b733a1@52.169.14.227:30303'}" +
                "] \n" +
                "sync.enabled = true \n" +
                // special genesis for this test network
                "genesis = ropsten.json \n" +
                "blockchain.config.name = 'ropsten' \n" +
                "database.dir = testnetSampleDb \n" +
                "cache.flush.memory = 0";

        public abstract TestNetSample sampleBean();

        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties();
            props.overrideParams(ConfigFactory.parseString(config.replaceAll("'", "\"")));
            return props;
        }
    }

    @Override
    public void onSyncDone() throws Exception {
        super.onSyncDone();
    }

    public static void main(String[] args) throws Exception {
        sLogger.info("Starting EthereumJ!");

        class SampleConfig extends TestNetConfig {
            @Bean
            public TestNetSample sampleBean() {
                return new TestNetSample();
            }
        }

        // Based on Config class the BasicSample would be created by Spring
        // and its springInit() method would be called as an entry point
        EthereumFactory.createEthereum(SampleConfig.class);
    }
}
