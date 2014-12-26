package org.ethereum.util;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import com.cedarsoftware.util.DeepEquals;
import org.spongycastle.util.encoders.Hex;

/**
 * Class to encapsulate an object and provide utilities for conversion
 */
public class Value {

    private Object value;

    public static Value fromRlpEncoded(byte[] data) {
        if (data != null && data.length != 0) {
            return new Value(RLP.decode(data, 0).getDecoded());
        } return null;
    }

    public Value(Object obj) {

        if (obj == null) return;

        if (obj instanceof Value) {
            this.value = ((Value) obj).asObj();
        } else {
            this.value = obj;
        }
    }

    /* *****************
     *      Convert
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
            return new BigInteger(1, asBytes()).intValue();
        }
        return 0;
    }

    public long asLong() {
        if (isLong()) {
            return (Long) value;
        } else if (isBytes()) {
            return new BigInteger(1, asBytes()).longValue();
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
        return ByteUtil.EMPTY_BYTE_ARRAY;
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
     *      Utility
     * *****************/

    public byte[] encode() {
        return RLP.encode(value);
    }

    public boolean cmp(Value o) {
        return DeepEquals.deepEquals(this, o);
    }

    /* *****************
     *      Checks
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

    // it's only if the isBytes() = true;
    public boolean isReadbleString(){

        int readableChars = 0;
        byte[] data = (byte[])value;

        if (data.length == 1 && data[0] > 31 && data[0] < 126){
            return true;
        }

        for (int i = 0; i < data.length; ++i){
            if (data[i] > 32 && data[i] < 126) ++readableChars;
        }

        if ((double)readableChars / (double)data.length > 0.55)
            return true;
        else
            return false;
    }

    // it's only if the isBytes() = true;
    public boolean isHexString(){

        int hexChars = 0;
        byte[] data = (byte[])value;

        for (int i = 0; i < data.length; ++i) {

            if ((data[i] >= 48 && data[i] <= 57)
                    || (data[i] >= 97 && data[i] <= 102))
                ++hexChars;
        }

        if ((double) hexChars / (double) data.length > 0.9)
            return true;
        else
            return false;
    }

    public boolean isHashCode(){
        return this.asBytes().length == 32;
    }

    public boolean isNull() {
        return value == null;
    }

    public boolean isEmpty() {
        if (isNull()) return true;
        if (isBytes() && asBytes().length == 0) return true;
        if (isList() && asList().isEmpty()) return true;
        if (isString() && asString().equals("")) return true;

        return false;
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

    public String toString() {

        StringBuffer buffer = new StringBuffer();

        if (isList()) {

            Object[] list = (Object[]) value;

            // special case - key/value node
            if (list.length == 2) {

                buffer.append("[ ");

                Value key = new Value(list[0]);

                byte[] keyNibbles = CompactEncoder.binToNibblesNoTerminator(key.asBytes());
                String keyString = ByteUtil.nibblesToPrettyString(keyNibbles);
                buffer.append(keyString);

                buffer.append(",");

                Value val = new Value(list[1]);
                buffer.append(val.toString());

                buffer.append(" ]");
                return buffer.toString();
            }
            buffer.append(" [");

            for (int i = 0; i < list.length; ++i){
                Value val = new Value(list[i]);
                if (val.isString() || val.isEmpty()){
                    buffer.append("'").append(val.toString()).append("'");
                } else {
                    buffer.append(val.toString());
                }
                if (i < list.length - 1)
                    buffer.append(", ");
            }
            buffer.append("] ");

            return buffer.toString();
        } else if (isEmpty()) {
            return "";
        } else if (isBytes()) {

            StringBuffer output = new StringBuffer();
            if (isHashCode()) {
                output.append(Hex.toHexString(asBytes()));
            } else if (isReadbleString()) {
                output.append("'");
                for (byte oneByte : asBytes()) {
                    if (oneByte < 16) {
                        output.append("\\x").append(ByteUtil.oneByteToHexString(oneByte));
                    } else {
                        output.append(Character.valueOf((char)oneByte));
                    }
                }
                output.append("'");
                return output.toString();
            }
            return Hex.toHexString(this.asBytes());
        } else if (isString()){
            return asString();
        }
        return "Unexpected type";
    }

    public int countBranchNodes() {

        if (this.isList()) {
            List<Object> objList = this.asList();
            int i = 0;
            for (Object obj : objList) {
                i += (new Value(obj)).countBranchNodes();
            }
            return i;
        } else if (this.isBytes()) {
            this.asBytes();
        }
        return 0;
    }
}
