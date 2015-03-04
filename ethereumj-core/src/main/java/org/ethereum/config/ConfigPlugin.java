package org.ethereum.config;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.lang.reflect.Constructor;

/*
 * we support AtomicInteger and AtomicLong mostly just to
 * support all subclasses of java.lang.Number in the
 * standard libraries.
 */
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;

import static org.ethereum.config.KeysDefaults.*;


/**
 * To write a config plugin, override the {@link #getLocalOrNull},
 * and provide a public constructor that accepts a fallback ConfigPlugin
 * for its sole argument.
 *
 * The Object should be returned in the expected type.
 * (available in {@link org.ethereum.config.KeysDefaults#TYPES} for standard keys)
 *
 * @see KeysDefaults#expectedTypes() and {@link #attemptCoerce}
 */
public abstract class ConfigPlugin implements ConfigSource {

    // TODO: Factor coercion stuff out into a successor to com.mchange.v2.lang.Coerce
    //       Careful of conversion from slf4j to mlog.
    private final static List<Class<?>> ORDERED_INTEGRALS;
    private final static List<Class<?>> ORDERED_FPS;
    static {
	Class<?>[] tmp_i = { Byte.class, Short.class, Integer.class, Long.class, BigInteger.class };
	ORDERED_INTEGRALS = Collections.unmodifiableList( Arrays.asList( tmp_i ) );

	Class<?>[] tmp_fp = { Float.class, Double.class, BigDecimal.class };
	ORDERED_FPS = Collections.unmodifiableList( Arrays.asList( tmp_fp ) );
    }
    private static boolean isIntegral( Class<?> numClass ) {
	return ORDERED_INTEGRALS.indexOf( numClass ) >= 0 || numClass == AtomicInteger.class || numClass == AtomicLong.class;
    }
    private static boolean isFloatingPoint( Class<?> numClass ) {
	return ORDERED_FPS.indexOf( numClass ) >= 0;
    }
    private static int ordinalWidthIntegral( Class<?> numClass ) {
	// from a width perspective, AtomicInteger is the same as Integer,
	// AtomicLong is the same as Long
	if ( numClass == AtomicInteger.class ) {
	    numClass = Integer.class; 
	} else if ( numClass == AtomicLong.class ) {
	    numClass = Long.class;
	}
	int out = ORDERED_INTEGRALS.indexOf( numClass );
	if ( out == -1 )
	    throw new IllegalArgumentException("Not an integral Number: " + numClass.getSimpleName());
	return out;
    }
    private static int ordinalWidthFloatingPoint( Class<?> numClass ) {
	int out = ORDERED_FPS.indexOf( numClass );
	if ( out == -1 )
	    throw new IllegalArgumentException("Not a floating point Number: " + numClass.getSimpleName());
	return out;
    }

    protected final static Logger logger = KeysDefaults.getConfigPluginLogger();

    protected final static ConfigPlugin fromPluginPath( String pluginPath ) {
	String[] classNames = parsePluginPath( pluginPath );
	ConfigPlugin head = ConfigPlugin.NULL;
	for ( int i = classNames.length; --i >= 0; ) {
	    String className = classNames[i];
	    try { 
		head = instantiatePlugin( className, head ); 
		logger.info( "Loaded config plugin '{}' (priority {}).", className, i );
	    }
	    catch ( Exception e ) {
		if ( logger.isWarnEnabled() ) {
		    logger.warn("Could not instantiate a plugin of type '" + className + "'. Skipping...", e);
		}
	    }
	}
	return head;
    }
    protected static String vmPluginPath() {
	String replacePath = System.getProperty(SYSPROP_PLUGIN_PATH_REPLACE);
	String appendPath  = System.getProperty(SYSPROP_PLUGIN_PATH_APPEND);
	String prependPath = System.getProperty(SYSPROP_PLUGIN_PATH_PREPEND);
	String path = ( replacePath == null ? DEFAULT_PLUGIN_PATH : replacePath ); // base path
	if ( appendPath != null ) path = path + "," + appendPath;
	if ( prependPath != null ) path = prependPath + "," + path;
	return path;
    }

    final static String[] parsePluginPath( String pluginPath ) {
	return pluginPath.split("\\s*,\\s*");
    }
    final static ConfigPlugin instantiatePlugin( String fqcn, ConfigPlugin fallback ) throws Exception {
	Class clz = Class.forName( fqcn );
	return instantiatePlugin( clz, fallback );
    }
    final static ConfigPlugin instantiatePlugin( Class clz, ConfigPlugin fallback ) throws Exception {
	Constructor<ConfigPlugin> ctor = clz.getConstructor( ConfigPlugin.class );
	return ctor.newInstance( fallback );
    }

    private final static ConfigPlugin NULL = new ConfigPlugin(null) {
	    protected Object getLocalOrNull( String key, Class<?> expectedType ) { return null; }

	    @Override 
	    boolean matchesTail( String[] classNames, int index ) { return classNames.length == index; }
	    
	    @Override
	    StringBuilder appendPathTail( StringBuilder in, boolean first ) { return in; }
    };

    private ConfigPlugin fallback;

    protected ConfigPlugin( ConfigPlugin fallback ) {
	this.fallback = fallback;
    }

    private final void warnCannotCoerce( Object value, Class<?> desiredType, String key ) {
	logger.warn("Cannot coerce {} to {} for key '{}'. Left as {}. [{}]", value, desiredType, key, value.getClass().getName(), this.getClass().getSimpleName());
    }

    protected Object attemptCoerce( Object value, Class<?> type, String key ) {
	Object out;
	if ( value == null ) {
	    out = value;
	} else {
	    Class<?> vClass = value.getClass();
	    if ( type.isAssignableFrom( vClass ) ) {
		out = value;
	    } else {
		Class<?> normalizedType = normalizeType( type );
		if ( normalizedType.isAssignableFrom( vClass ) ) {
		    out = value;
		} else if ( value instanceof String ) {
		    out = attemptCoerceStringValueForType( (String) value, normalizedType, key );
		} else if ( value instanceof Number ) {
		    out = attemptCoerceNumberForType( (Number) value, normalizedType, key );
		} else {
		    warnCannotCoerce( value, type, key );
		    out = value;
		}
	    }
	}
	return out;
    }

    private Object attemptCoerceNumberForType( Number value, Class<?> normalizedType, String key ) {
	Class<?> valueClass = value.getClass();
	Object out;
	if ( Number.class.isAssignableFrom( normalizedType ) ) {
	    boolean integralValue = isIntegral( value.getClass() );
	    boolean integralType = isIntegral( normalizedType );
	    if ( integralValue && integralType ) {
		int valueWidth = ordinalWidthIntegral( valueClass );
		int typeWidth = ordinalWidthIntegral( valueClass );
		if ( valueWidth > typeWidth ) {
		    logger.warn("Conversion from {} (value {}) to {} desired for key '{}' may require truncation.", 
			     valueClass.getSimpleName(), value, normalizedType.getSimpleName(), key);
		}
	    } else if ( !integralValue && !integralType ) {
		int valueWidth = ordinalWidthFloatingPoint( valueClass );
		int typeWidth = ordinalWidthFloatingPoint( valueClass );
		if ( valueWidth > typeWidth ) {
		    logger.warn("Conversion from {} (value {}) to {} desired for key '{}' may lose precsion.", 
			     valueClass.getSimpleName(), value, normalizedType.getSimpleName(), key);
		}
	    } else {
		logger.warn("Conversion between integral and nonintegral values (from {}, value {} to {}) for key '{}' is unsafe.", 
			 valueClass.getSimpleName(), value, normalizedType.getSimpleName(), key);
	    }
	    out = forceConvertNumberToNumeric( value, normalizedType );
	} else {
	    warnCannotCoerce( value, normalizedType, key );
	    out = value;
	}
	return out;
    }

    private Object forceConvertNumberToNumeric( Number value, Class<?> normalizedNumType ) {
	Object out;
	if ( normalizedNumType == BigInteger.class ) {
	    out = BigInteger.valueOf( value.longValue() );
	} else if ( normalizedNumType == Long.class ) {
	    out = value.longValue();
	} else if ( normalizedNumType == AtomicLong.class ) {
	    out = new AtomicLong( value.longValue() );
	} else if ( normalizedNumType == Integer.class ) {
	    out = value.intValue();
	} else if ( normalizedNumType == AtomicInteger.class ) {
	    out = new AtomicInteger( value.intValue() );
	} else if ( normalizedNumType == Short.class ) {
	    out = value.shortValue();
	} else if ( normalizedNumType == Byte.class ) {
	    out = value.byteValue();
	} else if ( normalizedNumType == BigDecimal.class ) {
	    if ( isIntegral( value.getClass() ) ) {
		out = BigDecimal.valueOf( value.longValue() );
	    } else {
		out = BigDecimal.valueOf( value.doubleValue() );
	    }
	} else if ( normalizedNumType == Double.class ) {
	    out = value.doubleValue();
	} else if ( normalizedNumType == Float.class ) {
	    out = value.floatValue();
	} else {
	    throw new IllegalArgumentException( "Unknown Number type: " + normalizedNumType.getName() );
	}
	return out;
    }

    private Object attemptCoerceStringValueForType( String value, Class<?> normalizedType, String key ) {
	Object out;
	if ( normalizedType == Boolean.class ) {
	    out = Boolean.valueOf( value );
	} else if ( normalizedType == Byte.class ) {
	    out = Byte.decode( value );
	} else if ( normalizedType == Character.class ) {
	    if ( value.length() != 1 ) {
		warnCannotCoerce( value, Character.class, key );
		out = value;
	    } else {
		out = value.charAt(0);
	    }
	} else if ( normalizedType == Short.class ) {
	    out = Short.valueOf( value );
	} else if ( normalizedType == Integer.class ) {
	    out = Integer.valueOf( value );
	} else if ( normalizedType == Long.class ) { 
	    out = Long.valueOf( value );
	} else if ( normalizedType == Float.class ) { 
	    out = Float.valueOf( value );
	} else if ( normalizedType == Double.class ) { 
	    out = Double.valueOf( value );
	} else if ( normalizedType == BigInteger.class ) { 
	    out = new BigInteger( value );
	} else if ( normalizedType == BigDecimal.class ) { 
	    out = new BigDecimal( value );
	} else if ( normalizedType == AtomicInteger.class ) { 
	    out = new AtomicInteger( Integer.parseInt( value ) );
	} else if ( normalizedType == AtomicLong.class ) { 
	    out = new AtomicLong( Long.parseLong( value ) );
	}else {
	    warnCannotCoerce( value, normalizedType, key );
	    out = value;
	}
	return out;
    }

    private Class<?> normalizeType( Class<?> type ) { // primitives should be normalized to their wrapper types
	if ( type == boolean.class )     return Boolean.class;
	else if ( type == byte.class )   return Byte.class;
	else if ( type == char.class )   return Character.class;
	else if ( type == short.class )  return Short.class;
	else if ( type == int.class )    return Integer.class;
	else if ( type == long.class )   return Long.class;
	else if ( type == float.class )  return Float.class;
	else if ( type == double.class ) return Double.class;
	else if ( type == void.class )   return Void.class; // for a kind of ridiculous completeness
	else return type;
    }

    boolean matchesTail( String[] classNames, int index ) {
	if ( index < classNames.length )
	    return this.getClass().getName().equals( classNames[index] ) && fallback.matchesTail( classNames, index + 1 );
	else
	    return false;
    }

    boolean matches( String[] classNames ) {
	return matchesTail( classNames, 0 );
    }

    StringBuilder appendPathTail( StringBuilder in, boolean first ) {
	if (! first) in.append(", ");
	in.append( this.getClass().getName() );
	return fallback.appendPathTail( in, false );
    }

    String path() {
	StringBuilder sb = new StringBuilder();
	return appendPathTail( sb, true ).toString();
    }

    /*
     *
     *  Abstract, protected methods
     *
     */
    protected abstract Object getLocalOrNull( String key, Class<?> expectedType );
    
    public final Object getOrNull( String key, Class<?> expectedType ) {
	Object out = getLocalOrNull( key, expectedType );
	return ( out == null ? fallback.getLocalOrNull( key, expectedType ) : out );
    }
}
