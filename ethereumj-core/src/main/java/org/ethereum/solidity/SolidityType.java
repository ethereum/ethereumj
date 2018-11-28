/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.solidity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.DataWord;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ethereum.util.ByteUtil.toHexString;

public abstract class SolidityType {
    private final static int Int32Size = 32;
    protected String name;

    public SolidityType(String name) {
        this.name = name;
    }

    /**
     * The type name as it was specified in the interface description
     */
    public String getName() {
        return name;
    }

    /**
     * The canonical type name (used for the method signature creation)
     * E.g. 'int' - canonical 'int256'
     */
    @JsonValue
    public String getCanonicalName() {
        return getName();
    }

    @JsonCreator
    public static SolidityType getType(String typeName) {
        if (typeName.endsWith("]")) return ArrayType.getType(typeName);
        if ("bool".equals(typeName)) return new BoolType();
        if (typeName.startsWith("int")) return new IntType(typeName);
        if (typeName.startsWith("uint")) return new UnsignedIntType(typeName);
        if ("address".equals(typeName)) return new AddressType();
        if ("string".equals(typeName)) return new StringType();
        if ("bytes".equals(typeName)) return new BytesType();
        if ("function".equals(typeName)) return new FunctionType();
        if (typeName.startsWith("bytes")) return new Bytes32Type(typeName);
        throw new RuntimeException("Unknown type: " + typeName);
    }

    /**
     * Encodes the value according to specific type rules
     *
     * @param value
     */
    public abstract byte[] encode(Object value);

    public abstract Object decode(byte[] encoded, int offset);

    public Object decode(byte[] encoded) {
        return decode(encoded, 0);
    }

    /**
     * @return fixed size in bytes. For the dynamic types returns IntType.getFixedSize()
     * which is effectively the int offset to dynamic data
     */
    public int getFixedSize() {
        return Int32Size;
    }

    public boolean isDynamicType() {
        return false;
    }

    @Override
    public String toString() {
        return getName();
    }


    public static abstract class ArrayType extends SolidityType {
        public static ArrayType getType(String typeName) {
            int idx1 = typeName.lastIndexOf("[");
            int idx2 = typeName.lastIndexOf("]");
            if (idx1 + 1 == idx2) {
                return new DynamicArrayType(typeName);
            } else {
                return new StaticArrayType(typeName);
            }
        }

        SolidityType elementType;

        public ArrayType(String name) {
            super(name);
            elementType = SolidityType.getType(name.substring(0, name.lastIndexOf("[")));
        }

        @Override
        public byte[] encode(Object value) {
            if (value.getClass().isArray()) {
                List<Object> elems = new ArrayList<>();
                for (int i = 0; i < Array.getLength(value); i++) {
                    elems.add(Array.get(value, i));
                }
                return encodeList(elems);
            } else if (value instanceof List) {
                return encodeList((List) value);
            } else {
                throw new RuntimeException("List value expected for type " + getName());
            }
        }

        protected byte[] encodeTuple(List l) {
            byte[][] elems;
            if (elementType.isDynamicType()) {
                elems = new byte[l.size() * 2][];
                int offset = l.size() * Int32Size;
                for (int i = 0; i < l.size(); i++) {
                    elems[i] = IntType.encodeInt(offset);
                    byte[] encoded = elementType.encode(l.get(i));
                    elems[l.size() + i] = encoded;
                    offset += Int32Size * ((encoded.length - 1) / Int32Size + 1);
                }
            } else {
                elems = new byte[l.size()][];
                for (int i = 0; i < l.size(); i++) {
                    elems[i] = elementType.encode(l.get(i));
                }
            }
            return ByteUtil.merge(elems);
        }

        public Object[] decodeTuple(byte[] encoded, int origOffset, int len) {
            int offset = origOffset;
            Object[] ret = new Object[len];

            for (int i = 0; i < len; i++) {
                if (elementType.isDynamicType()) {
                    ret[i] = elementType.decode(encoded, origOffset + IntType.decodeInt(encoded, offset).intValue());
                } else {
                    ret[i] = elementType.decode(encoded, offset);
                }
                offset += elementType.getFixedSize();
            }
            return ret;
        }


        public SolidityType getElementType() {
            return elementType;
        }

        public abstract byte[] encodeList(List l);
    }

    public static class StaticArrayType extends ArrayType {
        int size;

        public StaticArrayType(String name) {
            super(name);
            int idx1 = name.lastIndexOf("[");
            int idx2 = name.lastIndexOf("]");
            String dim = name.substring(idx1 + 1, idx2);
            size = Integer.parseInt(dim);
        }

        @Override
        public String getCanonicalName() {
            return getElementType().getCanonicalName() + "[" + size + "]";
        }

        @Override
        public byte[] encodeList(List l) {
            if (l.size() != size) throw new RuntimeException("List size (" + l.size() + ") != " + size + " for type " + getName());
            return encodeTuple(l);
        }

        @Override
        public Object[] decode(byte[] encoded, int offset) {
            return decodeTuple(encoded, offset, size);
        }

        @Override
        public int getFixedSize() {
            if (isDynamicType()) {
                return Int32Size;
            } else {
                return elementType.getFixedSize() * size;
            }
        }

        @Override
        public boolean isDynamicType() {
            return getElementType().isDynamicType() && size > 0;
        }
    }

    public static class DynamicArrayType extends ArrayType {
        public DynamicArrayType(String name) {
            super(name);
        }

        @Override
        public String getCanonicalName() {
            return elementType.getCanonicalName() + "[]";
        }

        @Override
        public byte[] encodeList(List l) {
            return ByteUtil.merge(IntType.encodeInt(l.size()), encodeTuple(l));
        }

        @Override
        public Object decode(byte[] encoded, int origOffset) {
            int len = IntType.decodeInt(encoded, origOffset).intValue();
            return decodeTuple(encoded, origOffset + Int32Size, len);
        }

        @Override
        public boolean isDynamicType() {
            return true;
        }
    }

    public static class BytesType extends SolidityType {
        protected BytesType(String name) {
            super(name);
        }

        public BytesType() {
            super("bytes");
        }

        @Override
        public byte[] encode(Object value) {
            byte[] bb;
            if (value instanceof byte[]) {
                bb = (byte[]) value;
            } else if (value instanceof String) {
                bb = ((String) value).getBytes();
            } else {
                throw new RuntimeException("byte[] or String value is expected for type 'bytes'");
            }
            byte[] ret = new byte[((bb.length - 1) / Int32Size + 1) * Int32Size]; // padding 32 bytes
            System.arraycopy(bb, 0, ret, 0, bb.length);

            return ByteUtil.merge(IntType.encodeInt(bb.length), ret);
        }

        @Override
        public Object decode(byte[] encoded, int offset) {
            int len = IntType.decodeInt(encoded, offset).intValue();
            if (len == 0) return new byte[0];
            offset += Int32Size;
            return Arrays.copyOfRange(encoded, offset, offset + len);
        }

        @Override
        public boolean isDynamicType() {
            return true;
        }
    }

    public static class StringType extends BytesType {
        public StringType() {
            super("string");
        }

        @Override
        public byte[] encode(Object value) {
            if (!(value instanceof String)) throw new RuntimeException("String value expected for type 'string'");
            return super.encode(((String)value).getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public Object decode(byte[] encoded, int offset) {
            return new String((byte[]) super.decode(encoded, offset), StandardCharsets.UTF_8);
        }
    }

    public static class Bytes32Type extends SolidityType {
        public Bytes32Type(String s) {
            super(s);
        }

        @Override
        public byte[] encode(Object value) {
            if (value instanceof Number) {
                BigInteger bigInt = new BigInteger(value.toString());
                return IntType.encodeInt(bigInt);
            } else if (value instanceof String) {
                byte[] ret = new byte[Int32Size];
                byte[] bytes = ((String) value).getBytes(StandardCharsets.UTF_8);
                System.arraycopy(bytes, 0, ret, 0, bytes.length);
                return ret;
            } else if (value instanceof byte[]) {
                byte[] bytes = (byte[]) value;
                byte[] ret = new byte[Int32Size];
                System.arraycopy(bytes, 0, ret, Int32Size - bytes.length, bytes.length);
                return ret;
            }

            throw new RuntimeException("Can't encode java type " + value.getClass() + " to bytes32");
        }

        @Override
        public Object decode(byte[] encoded, int offset) {
            return decodeBytes32(encoded, offset);
        }

        public static byte[] decodeBytes32(byte[] encoded, int offset) {
            return Arrays.copyOfRange(encoded, offset, offset + Int32Size);
        }
    }

    public static class AddressType extends IntType {
        public AddressType() {
            super("address");
        }

        @Override
        public byte[] encode(Object value) {
            if (value instanceof String && !((String)value).startsWith("0x")) {
                // address is supposed to be always in hex
                value = "0x" + value;
            }
            byte[] addr = super.encode(value);
            for (int i = 0; i < 12; i++) {
                if (addr[i] != 0) {
                    throw new RuntimeException("Invalid address (should be 20 bytes length): " + toHexString(addr));
                }
            }
            return addr;
        }

        @Override
        public Object decode(byte[] encoded, int offset) {
            BigInteger bi = (BigInteger) super.decode(encoded, offset);
            return ByteUtil.bigIntegerToBytes(bi, 20);
        }
    }
    
    public static abstract class NumericType extends SolidityType {
        public NumericType(String name) {
            super(name);
        }

        BigInteger encodeInternal(Object value) {
            BigInteger bigInt;
            if (value instanceof String) {
                String s = ((String)value).toLowerCase().trim();
                int radix = 10;
                if (s.startsWith("0x")) {
                    s = s.substring(2);
                    radix = 16;
                } else if (s.contains("a") || s.contains("b") || s.contains("c") ||
                        s.contains("d") || s.contains("e") || s.contains("f")) {
                    radix = 16;
                }
                bigInt = new BigInteger(s, radix);
            } else  if (value instanceof BigInteger) {
                bigInt = (BigInteger) value;
            } else  if (value instanceof Number) {
                bigInt = new BigInteger(value.toString());
            } else  if (value instanceof byte[]) {
                bigInt = ByteUtil.bytesToBigInteger((byte[]) value);
            } else {
                throw new RuntimeException("Invalid value for type '" + this + "': " + value + " (" + value.getClass() + ")");
            }
            return bigInt;
        }
    }

    public static class IntType extends NumericType {
        public IntType(String name) {
            super(name);
        }

        @Override
        public String getCanonicalName() {
            if (getName().equals("int")) return "int256";
            return super.getCanonicalName();
        }
        public static BigInteger decodeInt(byte[] encoded, int offset) {
            return new BigInteger(Arrays.copyOfRange(encoded, offset, offset + Int32Size));
        }
        public static byte[] encodeInt(int i) {
            return encodeInt(new BigInteger("" + i));
        }
        public static byte[] encodeInt(BigInteger bigInt) {
            return ByteUtil.bigIntegerToBytesSigned(bigInt, Int32Size);
        }
        @Override
        public Object decode(byte[] encoded, int offset) {
            return decodeInt(encoded, offset);
        }
        @Override
        public byte[] encode(Object value) {
            BigInteger bigInt = encodeInternal(value);
            return encodeInt(bigInt);
        }
    }
    
    public static class UnsignedIntType extends NumericType {
        public UnsignedIntType(String name) {
            super(name);
        }
        
        @Override
        public String getCanonicalName() {
            if (getName().equals("uint")) return "uint256";
            return super.getCanonicalName();
        }
        public static BigInteger decodeInt(byte[] encoded, int offset) {
            return new BigInteger(1, Arrays.copyOfRange(encoded, offset, offset + Int32Size));
        }
        public static byte[] encodeInt(int i) {
            return encodeInt(new BigInteger("" + i));
        }
        public static byte[] encodeInt(BigInteger bigInt) {
            if (bigInt.signum() == -1) {
                throw new RuntimeException("Wrong value for uint type: " + bigInt);
            }
            return ByteUtil.bigIntegerToBytes(bigInt, Int32Size);
        }
        @Override
        public byte[] encode(Object value) {
            BigInteger bigInt = encodeInternal(value);
            return encodeInt(bigInt);
        }
        @Override
        public Object decode(byte[] encoded, int offset) {
            return decodeInt(encoded, offset);
        }
    }

    public static class BoolType extends IntType {
        public BoolType() {
            super("bool");
        }

        @Override
        public byte[] encode(Object value) {
            if (!(value instanceof Boolean)) throw new RuntimeException("Wrong value for bool type: " + value);
            return super.encode(value == Boolean.TRUE ? 1 : 0);
        }

        @Override
        public Object decode(byte[] encoded, int offset) {
            return Boolean.valueOf(((Number) super.decode(encoded, offset)).intValue() != 0);
        }
    }

    public static class FunctionType extends Bytes32Type {
        public FunctionType() {
            super("function");
        }

        @Override
        public byte[] encode(Object value) {
            if (!(value instanceof byte[])) throw new RuntimeException("Expected byte[] value for FunctionType");
            if (((byte[]) value).length != 24) throw new RuntimeException("Expected byte[24] for FunctionType");
            return super.encode(ByteUtil.merge((byte[]) value, new byte[8]));
        }
    }
}
