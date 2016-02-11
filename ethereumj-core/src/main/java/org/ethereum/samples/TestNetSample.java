package org.ethereum.samples;

import com.typesafe.config.ConfigFactory;
import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.SHA3Helper;
import org.ethereum.facade.EthereumFactory;
import org.springframework.context.annotation.Bean;

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
    protected final byte[] senderPrivateKey = SHA3Helper.sha3("cow".getBytes());
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
                "    { url = 'enode://3281d70897cbc1aac429cc2f61582a8982000ddde7edd62c1a6dbd3d9bc80f306a2443071248cba5ec23aab8134eb1c0512c076617f7da5d2aec432822c8cad5@peer-1.ether.camp:10101' }," +
                "    { url = 'enode://9bcff30ea776ebd28a9424d0ac7aa500d372f918445788f45a807d83186bd52c4c0afaf504d77e2077e5a99f1f264f75f8738646c1ac3673ccc652b65565c3bb@peer-1.ether.camp:30303' }," +
                "    { url = 'enode://ba8248e425bb8128982de4f05dd93a496f45843013ba04fc024a48589044b8c8529f2b24e046b723edcdfdf947d17951fe4de667c2ab73265a12c9b4b952ef75@peer-2.ether.camp:30303' }," +
                "    { url = 'enode://c45b6d519ea1f50363baf9fda501bafd9187a04badea6c3708b63ae3ccdb679ffa1575a97726e10c4ca33e69ef13c4f8f5c4e66597180f4b1322a40b2d174e7b@peer-3.ether.camp:30303' }" +
                "] \n" +
                "sync.enabled = true \n" +
                // special genesis for this test network
                "genesis = frontier-test.json \n" +
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
    public void onSyncDone() {
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
