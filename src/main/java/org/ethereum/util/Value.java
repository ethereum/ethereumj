package org.ethereum.util;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import com.cedarsoftware.util.DeepEquals;

/**
 * Class to encapsulate an object and provide utilities for conversion
 */
public class Value {
	
	private Object value;

	public void fromRlpEncoded(byte[] data) {
		if (data.length != 0) {
			this.value = RlpEncoder.decode(data, 0).getDecoded();
		}
	}

	public Value(Object obj) {
		if (obj instanceof Value) {
			this.value = ((Value) obj).asObj();
		} else {
			this.value = obj;
		}
	}
	
	/* *****************
	 * 		Convert
	 * *****************/

	public Object asObj() {
		return value;
	}
		
	public List<Object> asList() {
		Object[] valueArray = (Object[]) value;
		return Arrays.asList(valueArray);
	}

	public int asInt() {
		if (isInt()) {
			return (Integer) value;			
		} else if (isBytes()) {
			return new BigInteger(asBytes()).intValue();
		}
		return 0;
	}
	
	public long asLong() {
		if (isLong()) {
			return (Long) value;			
		} else if (isBytes()) {
			return new BigInteger(asBytes()).longValue();
		}
		return 0;
	}
	
	public BigInteger asBigInt() {
		return (BigInteger) value;
	}
	
	public String asString() {
		if (isBytes()) {
			return new String((byte[]) value);
		} else if (isString()) {
			return (String) value;
		}
		return "";
	}
	
	public byte[] asBytes() {
		if(isBytes()) {
			return (byte[]) value;
		} else if(isString()) {
			return asString().getBytes();
		}
		return new byte[0];
	}
	
	public int[] asSlice() {
		return (int[]) value;
	}
	
	public Value get(int index) {
		if(isList()) {
			// Guard for OutOfBounds
			if (asList().size() <= index) {
				return new Value(null);
			}
			if (index < 0) {
				throw new RuntimeException("Negative index not allowed");
			}
			return new Value(asList().get(index));
		}
		// If this wasn't a slice you probably shouldn't be using this function
		return new Value(null);
	}
	
	/* *****************
	 * 		Utility
	 * *****************/
	
	public byte[] encode() {
		return RlpEncoder.encode(value);
	}

	public boolean cmp(Value o) {
		return DeepEquals.deepEquals(this, o);
	}
	
	/* *****************
	 * 		Checks
	 * *****************/
	
	public boolean isList() {
		return value != null && value.getClass().isArray() && !value.getClass().getComponentType().isPrimitive();
	}
	
	public boolean isString() {
		return value instanceof String;
	}
	
	public boolean isInt() {
		return value instanceof Integer;
	}
	
	public boolean isLong() {
		return value instanceof Long;
	}
	
	public boolean isBigInt() {
		return value instanceof BigInteger;
	}
	
	public boolean isBytes() {
		return value instanceof byte[];
	}
	
	public boolean isNull() {
		return value == null;
	}
	
	public boolean isEmpty() {
		return !isNull() && isList() && asList().size() == 0;
	}
	
	public int length() {
		if (isList()) { 
			return asList().size(); 
		} else if (isBytes()) {
			return asBytes().length;
		} else if (isString()) {
			return asString().length();
		}
		return 0;
	}
}
