package org.ethereum.config;

interface ConfigSource {

    /** 
     *  The Object should be returned in the expected type .
     *  (available in {@link org.ethereum.config.KeysDefaults#TYPES} for standard keys)
     *
     *  @see KeysDefaults#expectedTypes() and {@link org.ethereum.config.ConfigPlugin#attemptCoerce}
     */
    public Object getOrNull( String key, Class<?> expectedType );
}
