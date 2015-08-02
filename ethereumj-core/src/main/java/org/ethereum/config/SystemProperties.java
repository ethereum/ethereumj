package org.ethereum.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigRenderOptions;
import org.ethereum.crypto.ECKey;
import org.ethereum.net.rlpx.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.*;

import static org.ethereum.crypto.SHA3Helper.sha3;

/**
 * Utility class to retrieve property values from the ethereumj.conf files
 *
 * The properties are taken from different sources and merged in the following order
 * (the config option from the next source overrides option from previous):
 * - resource ethereumj.conf : normally used as a reference config with default values
 *          and shouldn't be changed
 * - system property : each config entry might be altered via -D VM option
 * - [user dir]/config/ethereumj.conf
 * - config specified with the -Dethereumj.conf.file=[file.conf] VM option
 * - CLI options
 *
 * @author Roman Mandeleil
 * @since 22.05.2014
 */
public class SystemProperties {
    private static Logger logger = LoggerFactory.getLogger("general");

    public final static String PROPERTY_DB_DIR = "database.dir";
    public final static String PROPERTY_LISTEN_PORT = "peer.listen.port";
    public final static String PROPERTY_PEER_ACTIVE = "peer.active";
    public final static String PROPERTY_DB_RESET = "database.reset";

    /* Testing */
    private final static Boolean DEFAULT_VMTEST_LOAD_LOCAL = false;
    private final static String DEFAULT_BLOCKS_LOADER = "";

    public final static SystemProperties CONFIG = new SystemProperties();

    /**
     * Marks config accessor methods which need to be called (for value validation)
     * upon config creation or modification
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface ValidateMe {};


    private Config config;

    // mutable options for tests
    private String databaseDir = null;
    private Boolean databaseReset = null;
    private String projectVersion = null;

    private String genesisInfo = null;

    private SystemProperties() {
        try {
            Config testConfig = ConfigFactory.load("test-ethereumj.conf");

            Config referenceConfig = ConfigFactory.load("ethereumj.conf");
            Config userConfig = ConfigFactory.parseResources("user.conf");
            Config userDirConfig = ConfigFactory.parseFile(
                    new File(System.getProperty("user.dir"), "/config/ethereumj.conf"));
            String file = System.getProperty("ethereumj.conf.file");
            Config sysPropConfig = file != null ? ConfigFactory.parseFile(new File(file)) :
                    ConfigFactory.empty();
            config = sysPropConfig
                    .withFallback(testConfig)
                    .withFallback(userConfig)
                    .withFallback(userDirConfig)
                    .withFallback(referenceConfig);
            validateConfig();

            Properties props = new Properties();
            InputStream is = ClassLoader.getSystemResourceAsStream("version.properties");
            props.load(is);
            this.projectVersion = props.getProperty("versionNumber");
            this.projectVersion = this.projectVersion.replaceAll("'", "");

            if (this.projectVersion == null) this.projectVersion = "-.-.-";

        } catch (Exception e) {
            logger.error("Can't read config.", e);
            throw new RuntimeException(e);
        }
    }

    public Config getConfig() {
        return config;
    }

    /**
     * Puts a new config atop of existing stack making the options
     * in the supplied config overriding existing options
     * Once put this config can't be removed
     *
     * @param overrideOptions - atop config
     */
    public void overrideParams(Config overrideOptions) {
        config = overrideOptions.withFallback(config);
        validateConfig();
    }

    /**
     * Puts a new config atop of existing stack making the options
     * in the supplied config overriding existing options
     * Once put this config can't be removed
     *
     * @param keyValuePairs [name] [value] [name] [value] ...
     */
    public void overrideParams(String ... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) throw new RuntimeException("Odd argument number");
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            map.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        overrideParams(map);
    }

    /**
     * Puts a new config atop of existing stack making the options
     * in the supplied config overriding existing options
     * Once put this config can't be removed
     *
     * @param cliOptions -  command line options to take presidency
     */
    public void overrideParams(Map<String, String> cliOptions) {
        Config cliConf = ConfigFactory.parseMap(cliOptions);
        overrideParams(cliConf);
    }

    private void validateConfig() {
        for (Method method : getClass().getMethods()) {
            try {
                if (method.isAnnotationPresent(ValidateMe.class)) {
                    method.invoke(this);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error validating config method: " + method, e);
            }
        }
    }

    @ValidateMe
    public boolean peerDiscovery() {
        return config.getBoolean("peer.discovery.enabled");
    }

    @ValidateMe
    public boolean peerDiscoveryPersist() {
        return config.getBoolean("peer.discovery.persist");
    }

    @ValidateMe
    public int peerDiscoveryWorkers() {
        return config.getInt("peer.discovery.workers");
    }

    @ValidateMe
    public int peerConnectionTimeout() {
        return config.getInt("peer.connection.timeout") * 1000;
    }

    @ValidateMe
    public int transactionApproveTimeout() {
        return config.getInt("transaction.approve.timeout") * 1000;
    }

    @ValidateMe
    public List<String> peerDiscoveryIPList() {
        return config.getStringList("peer.discovery.ip.list");
    }

    @ValidateMe
    public boolean databaseReset() {
        return databaseReset == null ? config.getBoolean("database.reset") : databaseReset;
    }

    public void setDatabaseReset(Boolean reset) {
        databaseReset = reset;
    }

    @ValidateMe
    public List<Node> peerActive() {
        if (!config.hasPath("peer.active")) {
            return Collections.EMPTY_LIST;
        }
        List<Node> ret = new ArrayList<>();
        List<? extends ConfigObject> list = config.getObjectList("peer.active");
        for (ConfigObject configObject : list) {
            Node n;
            if (configObject.get("url") != null) {
                String url = configObject.toConfig().getString("url");
                n = new Node(url.startsWith("enode://") ? url : "enode://" + url);
            } else if (configObject.get("ip") != null) {
                String ip = configObject.toConfig().getString("ip");
                int port = configObject.toConfig().getInt("port");
                byte[] nodeId;
                if (configObject.toConfig().hasPath("nodeId")) {
                    nodeId = Hex.decode(configObject.toConfig().getString("nodeId").trim());
                    if (nodeId.length != 64) {
                        throw new RuntimeException("Invalid config nodeId '" + nodeId + "' at " + configObject);
                    }
                } else {
                    if (configObject.toConfig().hasPath("nodeName")) {
                        String nodeName = configObject.toConfig().getString("nodeName").trim();
                        // FIXME should be sha3-512 here
                        byte[] ecPublic = ECKey.fromPrivate(sha3(nodeName.getBytes())).getPubKeyPoint().getEncoded(false);
                        nodeId = new byte[ecPublic.length - 1];
                        System.arraycopy(ecPublic, 1, nodeId, 0, nodeId.length);
                    } else {
                        throw new RuntimeException("Either nodeId or nodeName should be specified: " + configObject);
                    }
                }
                n = new Node(nodeId, ip, port);
            } else {
                throw new RuntimeException("Unexpected element within 'peer.active' config list: " + configObject);
            }
            ret.add(n);
        }
        return ret;
    }

    public String samplesDir() {
        return config.getString("samples.dir");
    }

    @ValidateMe
    public String coinbaseSecret() {
        return config.getString("coinbase.secret");
    }

    @ValidateMe
    public Integer peerChannelReadTimeout() {
        return config.getInt("peer.channel.read.timeout");
    }

    @ValidateMe
    public Integer traceStartBlock() {
        return config.getInt("trace.startblock");
    }

    @ValidateMe
    public boolean recordBlocks() {
        return config.getBoolean("record.blocks");
    }

    @ValidateMe
    public boolean dumpFull() {
        return config.getBoolean("dump.full");
    }

    @ValidateMe
    public String dumpDir() {
        return config.getString("dump.dir");
    }

    @ValidateMe
    public String dumpStyle() {
        return config.getString("dump.style");
    }

    @ValidateMe
    public int dumpBlock() {
        return config.getInt("dump.block");
    }

    @ValidateMe
    public String databaseDir() {
        return databaseDir == null ? config.getString("database.dir") : databaseDir;
    }

    public void setDataBaseDir(String dataBaseDir) {
        this.databaseDir = dataBaseDir;
    }

    @ValidateMe
    public boolean dumpCleanOnRestart() {
        return config.getBoolean("dump.clean.on.restart");
    }

    @ValidateMe
    public boolean playVM() {
        return config.getBoolean("play.vm");
    }

    @ValidateMe
    public boolean blockChainOnly() {
        return config.getBoolean("blockchain.only");
    }

    @ValidateMe
    public int maxHashesAsk() {
        return config.getInt("sync.max.hashes.ask");
    }

    @ValidateMe
    public int maxBlocksAsk() {
        return config.getInt("sync.max.blocks.ask");
    }

    @ValidateMe
    public int syncPeerCount() {
        return config.getInt("sync.peer.count");
    }

    @ValidateMe
    public String projectVersion() {
        return projectVersion;
    }

    @ValidateMe
    public String helloPhrase() {
        return config.getString("hello.phrase");
    }

    @ValidateMe
    public String rootHashStart() {
        return config.hasPath("root.hash.start") ? config.getString("root.hash.start") : null;
    }

    @ValidateMe
    public List<String> peerCapabilities() {
        return config.getStringList("peer.capabilities");
    }

    @ValidateMe
    public boolean vmTrace() {
        return config.getBoolean("vm.structured.trace");
    }

    @ValidateMe
    public boolean vmTraceCompressed() {
        return config.getBoolean("vm.structured.compressed");
    }

    @ValidateMe
    public int vmTraceInitStorageLimit() {
        return config.getInt("vm.structured.initStorageLimit");
    }

    @ValidateMe
    public int detailsInMemoryStorageLimit() {
        return config.getInt("details.inmemory.storage.limit");
    }

    @ValidateMe
    public double cacheFlushMemory() {
        return config.getDouble("cache.flush.memory");
    }

    @ValidateMe
    public int cacheFlushBlocks() {
        return config.getInt("cache.flush.blocks");
    }

    @ValidateMe
    public String vmTraceDir() {
        return config.getString("vm.structured.dir");
    }

    @ValidateMe
    public String privateKey() {
        return config.getString("peer.privateKey");
    }

    @ValidateMe
    public int networkId() {
        return config.getInt("peer.networkId");
    }

    @ValidateMe
    public int listenPort() {
        return config.getInt("peer.listen.port");
    }

    @ValidateMe
    public String getKeyValueDataSource() {
        return config.getString("keyvalue.datasource");
    }

    @ValidateMe
    public boolean isRedisEnabled() {
        return config.getBoolean("redis.enabled");
    }

    @ValidateMe
    public boolean isSyncEnabled() { return config.getBoolean("sync.enabled");}

    @ValidateMe
    public String genesisInfo() {

        if (genesisInfo == null)
            return config.getString("genesis");
        else
            return genesisInfo;
    }

    public void setGenesisInfo(String genesisInfo){
        this.genesisInfo = genesisInfo;
    }

    public String dump() {
        return config.root().render(ConfigRenderOptions.defaults().setComments(false));
    }

    /*
     *
     * Testing
     *
     */
    public boolean vmTestLoadLocal() {
        return config.hasPath("GitHubTests.VMTest.loadLocal") ?
                config.getBoolean("GitHubTests.VMTest.loadLocal") : DEFAULT_VMTEST_LOAD_LOCAL;
    }

    public String blocksLoader() {
        return config.hasPath("blocks.loader") ?
                config.getString("blocks.loader") : DEFAULT_BLOCKS_LOADER;
    }
}