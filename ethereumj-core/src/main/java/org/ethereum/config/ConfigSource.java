package org.ethereum.config;

interface ConfigSource {

    /** 
     *  The Object should be in the type expected by the accessor method in SystemProperties.
     *
     *  @see KeysDefaults#expectedTypes() and {@link org.ethereum.config.ConfigPlugin#attemptCoerceValueForKey}
     */
    public Object getOrNull( String key );

}
