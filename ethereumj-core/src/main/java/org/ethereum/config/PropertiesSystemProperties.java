package org.ethereum.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

final class PropertiesSystemProperties extends SystemProperties {
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
    private String getNonDefaultAsString( String key ) {
	String value = System.getProperty( key );
	if ( value == null ) value = prop.getProperty( key );
	return value;
    }

    /*
     *
     *  Abstract method implementations
     *
     */
    boolean getBoolean( String key ) {
	String value = getNonDefaultAsString( key );
	return ( value != null ? Boolean.parseBoolean( value ) : ((Boolean) DEFAULTS.get( key )).booleanValue() );
    }
    int getInt( String key ) {
	String value = getNonDefaultAsString( key );
	return ( value != null ? Integer.parseInt( value ) : ((Integer) DEFAULTS.get( key )).intValue() );
    }
    String getString( String key ) {
	String value = getNonDefaultAsString( key );
	return ( value != null ? value : (String) DEFAULTS.get( key ) );
    }
    String getCoerceToString( String key ) {
	Object value = getNonDefaultAsString( key );
	if ( value == null ) value = DEFAULTS.get( key );
	return String.valueOf( value );
    }


    public void setListenPort(Integer port)        { prop.setProperty(K_PEER_LISTEN_PORT, port.toString()); }
    public void setDatabaseReset(Boolean reset)    { prop.setProperty(K_DATABASE_RESET, reset.toString()); }
    public void setActivePeerIP(String host)       { prop.setProperty(K_PEER_ACTIVE_IP, host); }
    public void setActivePeerPort(Integer port)    { prop.setProperty(K_PEER_ACTIVE_PORT, port.toString()); }
    public void setDataBaseDir(String dataBaseDir) { prop.setProperty(K_DATABASE_DIR, dataBaseDir); }
}
