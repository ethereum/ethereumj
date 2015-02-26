package org.ethereum.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.ethereum.cli.CLIInterface;

import static org.ethereum.config.KeysDefaults.*;

public final class PropertiesSystemProperties extends StringSourceSystemProperties {
    private final Properties prop = new Properties();

    public PropertiesSystemProperties() {
        InputStream input = null;
        try {
            File file = new File( TRADITIONAL_PROPS_FILENAME );
	    
            if (file.exists()) {
                logger.debug("config loaded from {}", TRADITIONAL_PROPS_FILENAME );
                input = new FileInputStream(file);
            } else {
                input = SystemProperties.class.getClassLoader().getResourceAsStream( TRADITIONAL_PROPS_RESOURCE );
                if (input == null) {
                    logger.error("Sorry, unable to find file {} or resource {}", TRADITIONAL_PROPS_FILENAME, TRADITIONAL_PROPS_RESOURCE);
                    return;
                } else {
		    logger.debug("config loaded from resource {}", TRADITIONAL_PROPS_FILENAME );
		}
            }

            // load a properties file from class path, inside static method
            prop.load(input);

	    applyCommandLineOverrides();
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    /*
     *
     *  Private utilities
     *
     */
    private void applyCommandLineOverrides() {
	Map<String,Object> overrides = CLIInterface.getConfigOverrides();
	if ( overrides != null ) {
	    for ( Map.Entry<String,Object> entry : overrides.entrySet() ) {
		String k = entry.getKey();
		Object v = entry.getValue();
		prop.setProperty( k, String.valueOf( v ) );
		logger.debug("Applied command line config override: {} -> {}", k, v);
	    }
	} else {
	    logger.debug("Command-line overrides have not been set, not even to an empty Map. Presumably this application was not run as a command-line application");
	}
    }

    /*
     *
     *  Abstract method implementations
     *
     */
    protected String getStringOrNull( String key ) {
	String value = System.getProperty( key );  // accept System.property overrides
	if ( value == null ) value = prop.getProperty( key );
	return value;
    }
}
