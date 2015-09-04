package org.ethereum.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ethereum.crypto.SHA3Helper;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.ethereum.util.ByteUtil.longToBytesNoLeadZeroes;

/**
 * Creates a contract function call transaction.
 * Serializes arguments according to the function ABI .
 *
 * Created by Anton Nashatyrev on 25.08.2015.
 */
public class CallTransaction {

    public static Transaction createRawTransaction(long nonce, long gasPrice, long gasLimit, String toAddress,
                                                    long value, byte[] data) {
        Transaction tx = new Transaction(longToBytesNoLeadZeroes(nonce),
                longToBytesNoLeadZeroes(gasPrice),
                longToBytesNoLeadZeroes(gasLimit),
                toAddress == null ? null : Hex.decode(toAddress),
                longToBytesNoLeadZeroes(value),
                data);
        return tx;
    }

    public static Transaction createCallTransaction(long nonce, long gasPrice, long gasLimit, String toAddress,
                        long value, Function callFunc, Object ... funcArgs) {

        byte[] callData = callFunc.encode(funcArgs);
        return createRawTransaction(nonce, gasPrice, gasLimit, toAddress, value, callData);
    }

    /**
     * Generic ABI type
     */
    public static abstract class Type {
        protected String name;

        public Type(String name) {
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
        public String getCanonicalName() {return getName();}

        @JsonCreator
        public static Type getType(String typeName) {
            if (typeName.contains("[")) return ArrayType.getType(typeName);
            if ("bool".equals(typeName)) return new BoolType();
            if (typeName.startsWith("int") || typeName.startsWith("uint")) return new IntType(typeName);
            if ("address".equals(typeName)) return new AddressType();
            if ("string".equals(typeName)) return new StringType();
            if ("bytes".equals(typeName)) return new BytesType();
            if (typeName.startsWith("bytes")) return new Bytes32Type(typeName);
            throw new RuntimeException("Unknown type: " + typeName);
        }

        /**
         * Encodes the value according to specific type rules
         * @param value
         */
        public abstract byte[] encode(Object value);

        public abstract Object decode(byte[] encoded);

        /**
         * @return fixed size in bytes or negative value if the type is dynamic
         */
        public int getFixedSize() {return 32;}

        @Override
        public String toString() {
            return getName();
        }
    }

    public static abstract class ArrayType extends Type {
        public static ArrayType getType(String typeName) {
            int idx1 = typeName.indexOf("[");
            int idx2 = typeName.indexOf("]", idx1);
            if (idx1 + 1 == idx2) {
                return new DynamicArrayType(typeName);
            } else {
                return new StaticArrayType(typeName);
            }
        }

        Type elementType;

        public ArrayType(String name) {
            super(name);
            int idx = name.indexOf("[");
            String st = name.substring(0, idx);
            int idx2 = name.indexOf("]", idx);
            String subDim = idx2 + 1 == name.length() ? "" : name.substring(idx2 + 1);
            elementType = Type.getType(st + subDim);
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

        public abstract byte[] encodeList(List l);
    }

    public static class StaticArrayType extends ArrayType {
        int size;

        public StaticArrayType(String name) {
            super(name);
            int idx1 = name.indexOf("[");
            int idx2 = name.indexOf("]", idx1);
            String dim = name.substring(idx1 + 1, idx2);
            size = Integer.parseInt(dim);
        }

        @Override
        public String getCanonicalName() {
            return elementType.getCanonicalName() + "[" + size + "]";
        }

        @Override
        public byte[] encodeList(List l) {
            if (l.size() != size) throw new RuntimeException("List size (" + l.size() + ") != " + size + " for type " + getName());
            byte[][] elems = new byte[size][];
            for (int i = 0; i < l.size(); i++) {
                elems[i] = elementType.encode(l.get(i));
            }
            return ByteUtil.merge(elems);
        }

        @Override
        public Object decode(byte[] encoded) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getFixedSize() {
            // return negative if elementType is dynamic
            return elementType.getFixedSize() * size;
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
            byte[][] elems = new byte[l.size() + 1][];
            elems[0] = IntType.encodeInt(l.size());
            for (int i = 0; i < l.size(); i++) {
                elems[i + 1] = elementType.encode(l.get(i));
            }
            return ByteUtil.merge(elems);
        }

        @Override
        public Object decode(byte[] encoded) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getFixedSize() {
            return -1;
        }
    }

    public static class BytesType extends Type {
        protected BytesType(String name) {
            super(name);
        }

        public BytesType() {
            super("bytes");
        }

        @Override
        public byte[] encode(Object value) {
            if (!(value instanceof byte[])) throw new RuntimeException("byte[] value expected for type 'bytes'");
            byte[] bb = (byte[]) value;
            byte[] ret = new byte[((bb.length - 1) / 32 + 1) * 32]; // padding 32 bytes
            System.arraycopy(bb, 0, ret, 0, bb.length);

            return ByteUtil.merge(IntType.encodeInt(bb.length), ret);
        }

        @Override
        public Object decode(byte[] encoded) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getFixedSize() {
            return -1;
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
        public Object decode(byte[] encoded) {
            throw new UnsupportedOperationException();
//            return new String(encoded, StandardCharsets.UTF_8);
        }
    }

    public static class Bytes32Type extends Type {
        public Bytes32Type(String s) {
            super(s);
        }

        @Override
        public byte[] encode(Object value) {
            if (value instanceof Number) {
                BigInteger bigInt = new BigInteger(value.toString());
                return IntType.encodeInt(bigInt);
            } else if (value instanceof String) {
                byte[] ret = new byte[32];
                byte[] bytes = ((String) value).getBytes(StandardCharsets.UTF_8);
                System.arraycopy(bytes, 0, ret, 0, bytes.length);
                return ret;
            }

            return new byte[0];
        }

        @Override
        public Object decode(byte[] encoded) {
            return encoded;
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
                    throw new RuntimeException("Invalid address (should be 20 bytes length): " + Hex.toHexString(addr));
                }
            }
            return addr;
        }
    }

    public static class IntType extends Type {
        public IntType(String name) {
            super(name);
        }

        @Override
        public String getCanonicalName() {
            if (getName().equals("int")) return "int256";
            if (getName().equals("uint")) return "uint256";
            return super.getCanonicalName();
        }

        @Override
        public byte[] encode(Object value) {
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
            } else {
                throw new RuntimeException("Invalid value for type '" + this + "': " + value + " (" + value.getClass() + ")");
            }
            return encodeInt(bigInt);
        }

        @Override
        public Object decode(byte[] encoded) {
            return new BigInteger(encoded);
        }

        public static byte[] encodeInt(int i) {
            return encodeInt(new BigInteger("" + i));
        }
        public static byte[] encodeInt(BigInteger bigInt) {
            byte[] ret = new byte[32];
            Arrays.fill(ret, bigInt.signum() < 0 ? (byte) 0xFF : 0);
            byte[] bytes = bigInt.toByteArray();
            System.arraycopy(bytes, 0, ret, 32 - bytes.length, bytes.length);
            return ret;
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
        public Object decode(byte[] encoded) {
            return Boolean.valueOf(((Number) super.decode(encoded)).intValue() != 0);
        }
    }

    public static class Param {
        public String name;
        public Type type;
    }

    enum FunctionType {
        constructor,
        function
    }

    public static class Function {
        public boolean constant;
        public String name;
        public Param[] inputs;
        public Param[] outputs;
        public FunctionType type;

        private Function() {}

        public byte[] encode(Object ... args) {
            if (args.length > inputs.length) throw new RuntimeException("Too many arguments: " + args.length + " > " + inputs.length);

            int staticSize = 0;
            int dynamicCnt = 0;
            // calculating static size and number of dynamic params
            for (int i = 0; i < args.length; i++) {
                Param param = inputs[i];
                int sz = param.type.getFixedSize();
                if (sz < 0) {
                    dynamicCnt++;
                    staticSize += 32;
                } else {
                    staticSize += sz;
                }
            }

            byte[][] bb = new byte[args.length + 1 + dynamicCnt][];
            bb[0] = encodeSignature();

            int curDynamicPtr = staticSize;
            int curDynamicCnt = 0;
            for (int i = 0; i < args.length; i++) {
                if (inputs[i].type.getFixedSize() < 0) {
                    byte[] dynBB = inputs[i].type.encode(args[i]);
                    bb[i + 1] = IntType.encodeInt(curDynamicPtr);
                    bb[args.length + 1 + curDynamicCnt] = dynBB;
                    curDynamicCnt++;
                    curDynamicPtr += dynBB.length;
                } else {
                    bb[i + 1] = inputs[i].type.encode(args[i]);
                }
            }
            return ByteUtil.merge(bb);
        }

        public Object[] decodeResult(byte[] encodedRet) {
            if (outputs.length > 1) {
                throw new UnsupportedOperationException("Multiple return values not supported yet");
            }
            if (outputs.length == 0) {
                return new Object[0];
            }
            Type retType = outputs[0].type;
            return new Object[] {retType.decode(encodedRet)};
        }

        public byte[] encodeSignature() {
            String sig = name + "(";
            for (Param input : inputs) {
                sig += input.type.getCanonicalName() + ",";
            }
            sig = sig.endsWith(",") ? sig.substring(0, sig.length() - 1) : sig;
            sig = sig + ")";
            return Arrays.copyOfRange(SHA3Helper.sha3(sig.getBytes()), 0, 4);
        }

        public static Function fromJsonInterface(String json) {
            try {
                return new ObjectMapper().readValue(json, Function.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static Function fromSignature(String funcName, String ... paramTypes) {
            Function ret = new Function();
            ret.name = funcName;
            ret.constant = false;
            ret.type = FunctionType.function;
            ret.outputs = new Param[0];
            ret.inputs = new Param[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                ret.inputs[i] = new Param();
                ret.inputs[i].name = "param" + i;
                ret.inputs[i].type = Type.getType(paramTypes[i]);
            }
            return ret;
        }
    }

    public static class Contract {
        public Contract(String jsonInterface) {
            try {
                functions = new ObjectMapper().readValue(jsonInterface, Function[].class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public Function[] functions;
    }
}
