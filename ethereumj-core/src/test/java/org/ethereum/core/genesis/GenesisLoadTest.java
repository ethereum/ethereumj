package org.ethereum.core.genesis;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.ethereum.config.SystemProperties;
import org.junit.Ignore;
import static org.junit.Assert.*;
import org.junit.Test;

import java.net.URL;


/**
 * Testing system exit
 * http://stackoverflow.com/questions/309396/java-how-to-test-methods-that-call-system-exit
 *
 * Created by Stan Reshetnyk on 17.09.16.
 */
public class GenesisLoadTest {

    @Test
    public void shouldLoadGenesis_whenShortWay() {
        loadGenesis(null, "frontier-test.json");
        assertTrue(true);
    }

    @Test
    public void shouldLoadGenesis_whenFullPathSpecified() {
        URL url = GenesisLoadTest.class.getClassLoader().getResource("genesis/frontier-test.json");

        // full path
        System.out.println("url.getPath() " + url.getPath());
        loadGenesis(url.getPath(), null);

        loadGenesis("src/main/resources/genesis/frontier-test.json", null);
        assertTrue(true);
    }

    @Test
    public void shouldLoadGenesisFromFile_whenBothSpecified() {
        URL url = GenesisLoadTest.class.getClassLoader().getResource("genesis/frontier-test.json");

        // full path
        System.out.println("url.getPath() " + url.getPath());
        loadGenesis(url.getPath(), "NOT_EXIST");
        assertTrue(true);
    }

    @Test(expected = RuntimeException.class)
    public void shouldError_whenWrongPath() {
        loadGenesis("NON_EXISTED_PATH", null);
        assertTrue(false);
    }

    private void loadGenesis(String genesisFile, String genesisResource) {
        Config config = ConfigFactory.empty();

        if (genesisResource != null) {
            config = config.withValue("genesis",
                    ConfigValueFactory.fromAnyRef(genesisResource));
        }
        if (genesisFile != null) {
            config = config.withValue("genesisFile",
                    ConfigValueFactory.fromAnyRef(genesisFile));
        }

        new SystemProperties(config).getGenesis();
    }
}
