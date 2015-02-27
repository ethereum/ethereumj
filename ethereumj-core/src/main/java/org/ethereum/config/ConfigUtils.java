package org.ethereum.config;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static org.ethereum.config.KeysDefaults.*;

class ConfigUtils {

    static Properties ensurePrefixedProperties( Properties in ) {
	Properties out = new Properties();
	for ( String oldKey : in.stringPropertyNames() ) {
	    String newKey = oldKey.startsWith( ETHEREUMJ_PREFIX ) ? oldKey : ETHEREUMJ_PREFIX + oldKey;
	    out.setProperty( newKey, in.getProperty( oldKey ) );
	}
	return out;
    }

    static Properties loadPropertiesFile( File file ) throws IOException {
	try ( InputStream input = new BufferedInputStream( new FileInputStream( file ) ) ) {
	   Properties out = new Properties();
	   out.load( input );
	   return out;
	}
    }

    static Properties loadPropertiesURL( URL url ) throws IOException {
	try ( InputStream input = new BufferedInputStream( url.openStream() ) ) {
	   Properties out = new Properties();
	   out.load( input );
	   return out;
	}
    }

    private ConfigUtils()
    {}
}
