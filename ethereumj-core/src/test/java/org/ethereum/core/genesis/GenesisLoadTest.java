package org.ethereum.core.genesis;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.ethereum.config.SystemProperties;
import org.junit.Ignore;
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
        loadGenesis("frontier-test.json");
    }

    @Test
    public void shouldLoadGenesis_whenFullPathSpecified() {
        URL url = GenesisLoadTest.class.getClassLoader().getResource("genesis/frontier-test.json");

        // full path
        System.out.println("url.getPath() " + url.getPath());
        loadGenesis(url.getPath());

        loadGenesis("src/main/resources/genesis/frontier-test.json");
    }

    @Test
    @Ignore("Ignored as System.exit is used")
    public void shouldExitJava_whenWrongPath() {
        loadGenesis("NON_EXISTEN_PATH");
    }

    private void loadGenesis(String path) {
        Config config = ConfigFactory.empty()
                .withValue("genesis",
                        ConfigValueFactory.fromAnyRef(path));

        new SystemProperties(config).getGenesis();
    }
}
