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
                // network has no discovery, peers are connected directly
                "peer.discovery.enabled = false \n" +
                // set port to 0 to disable accident inbound connections
                "peer.listen.port = 0 \n" +
                "peer.networkId = 161 \n" +
                // a number of public peers for this network (not all of then may be functioning)
                "peer.active = [" +
                "    { url = 'enode://9bcff30ea776ebd28a9424d0ac7aa500d372f918445788f45a807d83186bd52c4c0afaf504d77e2077e5a99f1f264f75f8738646c1ac3673ccc652b65565c3bb@peer-1.ether.camp:30303' }," +
                "    { url = 'enode://c2b35ed63f5d79c7f160d05c54dd60b3ba32d455dbb10a5fe6fde44854073db02f9a538423a63a480126c74c7f650d77066ae446258e3d00388401d419b99f88@peer-2.ether.camp:30303' }," +
                "    { url = 'enode://8246787f8d57662b850b354f0b526251eafee1f077fc709460dc8788fa640a597e49ffc727580f3ebbbc5eacb34436a66ea40415fab9d73563481666090a6cf0@peer-3.ether.camp:30303' }" +
                "] \n" +
                "sync.enabled = true \n" +
                // special genesis for this test network
                "genesis = frontier-test.json \n" +
                "blockchain.config.name = 'testnet' \n" +
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
