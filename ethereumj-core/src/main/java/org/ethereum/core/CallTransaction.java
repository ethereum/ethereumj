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
package org.ethereum.core;

import static java.lang.String.format;
import static org.apache.commons.lang3.ArrayUtils.subarray;
import static org.apache.commons.lang3.StringUtils.stripEnd;
import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.solidity.SolidityType.IntType;
import static org.ethereum.util.ByteUtil.longToBytesNoLeadZeroes;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ethereum.solidity.SolidityType;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.vm.LogInfo;
import org.spongycastle.util.encoders.Hex;

/**
 * Creates a contract function call transaction.
 * Serializes arguments according to the function ABI .
 *
 * Created by Anton Nashatyrev on 25.08.2015.
 */
public class CallTransaction {

    private final static ObjectMapper DEFAULT_MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);

    public static Transaction createRawTransaction(long nonce, long gasPrice, long gasLimit, String toAddress,
                                                    long value, byte[] data) {
        Transaction tx = new Transaction(longToBytesNoLeadZeroes(nonce),
                longToBytesNoLeadZeroes(gasPrice),
                longToBytesNoLeadZeroes(gasLimit),
                toAddress == null ? null : Hex.decode(toAddress),
                longToBytesNoLeadZeroes(value),
                data,
                null);
        return tx;
    }



    public static Transaction createCallTransaction(long nonce, long gasPrice, long gasLimit, String toAddress,
                        long value, Function callFunc, Object ... funcArgs) {

        byte[] callData = callFunc.encode(funcArgs);
        return createRawTransaction(nonce, gasPrice, gasLimit, toAddress, value, callData);
    }


    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Param {
        public Boolean indexed;
        public String name;
        public SolidityType type;

        @JsonGetter("type")
        public String getType() {
            return type.getName();
        }
    }

    public enum StateMutabilityType {
        pure,
        view,
        nonpayable,
        payable
    }

    public enum FunctionType {
        constructor,
        function,
        event,
        fallback
    }

    public static class Function {
        public boolean anonymous;
        public boolean constant;
        public boolean payable;
        public String name = "";
        public Param[] inputs = new Param[0];
        public Param[] outputs = new Param[0];
        public FunctionType type;
        public StateMutabilityType stateMutability;
        public Long gas;

        private Function() {}

        public byte[] encode(Object ... args) {
            return ByteUtil.merge(encodeSignature(), encodeArguments(args));
        }
        public byte[] encodeArguments(Object ... args) {
            if (args.length > inputs.length) throw new RuntimeException("Too many arguments: " + args.length + " > " + inputs.length);

            int staticSize = 0;
            int dynamicCnt = 0;
            // calculating static size and number of dynamic params
            for (int i = 0; i < args.length; i++) {
                Param param = inputs[i];
                if (param.type.isDynamicType()) {
                    dynamicCnt++;
                }
                staticSize += param.type.getFixedSize();
            }

            byte[][] bb = new byte[args.length + dynamicCnt][];

            int curDynamicPtr = staticSize;
            int curDynamicCnt = 0;
            for (int i = 0; i < args.length; i++) {
                if (inputs[i].type.isDynamicType()) {
                    byte[] dynBB = inputs[i].type.encode(args[i]);
                    bb[i] = SolidityType.IntType.encodeInt(curDynamicPtr);
                    bb[args.length + curDynamicCnt] = dynBB;
                    curDynamicCnt++;
                    curDynamicPtr += dynBB.length;
                } else {
                    bb[i] = inputs[i].type.encode(args[i]);
                }
            }
            return ByteUtil.merge(bb);
        }

        private Object[] decode(byte[] encoded, Param[] params) {
            Object[] ret = new Object[params.length];

            int off = 0;
            for (int i = 0; i < params.length; i++) {
                if (params[i].type.isDynamicType()) {
                    ret[i] = params[i].type.decode(encoded, IntType.decodeInt(encoded, off).intValue());
                } else {
                    ret[i] = params[i].type.decode(encoded, off);
                }
                off += params[i].type.getFixedSize();
            }
            return ret;
        }

        public Object[] decode(byte[] encoded) {
            return decode(subarray(encoded, 4, encoded.length), inputs);
        }

        public Object[] decodeResult(byte[] encodedRet) {
            return decode(encodedRet, outputs);
        }

        public String formatSignature() {
            StringBuilder paramsTypes = new StringBuilder();
            for (Param param : inputs) {
                paramsTypes.append(param.type.getCanonicalName()).append(",");
            }

            return format("%s(%s)", name, stripEnd(paramsTypes.toString(), ","));
        }

        public byte[] encodeSignatureLong() {
            String signature = formatSignature();
            byte[] sha3Fingerprint = sha3(signature.getBytes());
            return sha3Fingerprint;
        }

        public byte[] encodeSignature() {
            return Arrays.copyOfRange(encodeSignatureLong(), 0, 4);
        }

        @Override
        public String toString() {
            return formatSignature();
        }

        public static Function fromJsonInterface(String json) {
            try {
                return DEFAULT_MAPPER.readValue(json, Function.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static Function fromSignature(String funcName, String ... paramTypes) {
            return fromSignature(funcName, paramTypes, new String[0]);
        }

        public static Function fromSignature(String funcName, String[] paramTypes, String[] resultTypes) {
            Function ret = new Function();
            ret.name = funcName;
            ret.constant = false;
            ret.type = FunctionType.function;
            ret.inputs = new Param[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                ret.inputs[i] = new Param();
                ret.inputs[i].name = "param" + i;
                ret.inputs[i].type = SolidityType.getType(paramTypes[i]);
            }
            ret.outputs = new Param[resultTypes.length];
            for (int i = 0; i < resultTypes.length; i++) {
                ret.outputs[i] = new Param();
                ret.outputs[i].name = "res" + i;
                ret.outputs[i].type = SolidityType.getType(resultTypes[i]);
            }
            return ret;
        }


    }

    public static class Contract {
        public Contract(String jsonInterface) {
            try {
                functions = DEFAULT_MAPPER.readValue(jsonInterface, Function[].class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public Function getByName(String name) {
            for (Function function : functions) {
                if (name.equals(function.name)) {
                    return function;
                }
            }
            return null;
        }

        public Function getConstructor() {
            for (Function function : functions) {
                if (function.type == FunctionType.constructor) {
                    return function;
                }
            }
            return null;
        }

        private Function getBySignatureHash(byte[] hash) {
            if (hash.length == 4 ) {
                for (Function function : functions) {
                    if (FastByteComparisons.equal(function.encodeSignature(), hash)) {
                        return function;
                    }
                }
            } else if (hash.length == 32 ) {
                for (Function function : functions) {
                    if (FastByteComparisons.equal(function.encodeSignatureLong(), hash)) {
                        return function;
                    }
                }
            } else {
                throw new RuntimeException("Function signature hash should be 4 or 32 bytes length");
            }
            return null;
        }

        /**
         * Parses function and its arguments from transaction invocation binary data
         */
        public Invocation parseInvocation(byte[] data) {
            if (data.length < 4) throw new RuntimeException("Invalid data length: " + data.length);
            Function function = getBySignatureHash(Arrays.copyOfRange(data, 0, 4));
            if (function == null) throw new RuntimeException("Can't find function/event by it signature");
            Object[] args = function.decode(data);
            return new Invocation(this, function, args);
        }

        /**
         * Parses Solidity Event and its data members from transaction receipt LogInfo
         * Finds corresponding event by its signature hash and thus is not applicable to
         * anonymous events
         */
        public Invocation parseEvent(LogInfo eventLog) {
            CallTransaction.Function event = getBySignatureHash(eventLog.getTopics().get(0).getData());
            int indexedArg = 1;
            if (event == null) return null;
            List<Object> indexedArgs = new ArrayList<>();
            List<Param> unindexed = new ArrayList<>();
            for (Param input : event.inputs) {
                if (input.indexed) {
                    byte[] topicBytes = eventLog.getTopics().get(indexedArg++).getData();
                    Object decodedTopic;
                    if (input.type.isDynamicType()) {
                        // If arrays (including string and bytes) are used as indexed arguments,
                        // the Keccak-256 hash of it is stored as topic instead.
                        decodedTopic = SolidityType.Bytes32Type.decodeBytes32(topicBytes, 0);
                    } else {
                        decodedTopic = input.type.decode(topicBytes);
                    }
                    indexedArgs.add(decodedTopic);
                } else {
                    unindexed.add(input);
                }
            }

            Object[] unindexedArgs = event.decode(eventLog.getData(), unindexed.toArray(new Param[unindexed.size()]));
            Object[] args = new Object[event.inputs.length];
            int unindexedIndex = 0;
            int indexedIndex = 0;
            for (int i = 0; i < args.length; i++) {
                if (event.inputs[i].indexed) {
                    args[i] = indexedArgs.get(indexedIndex++);
                    continue;
                }
                args[i] = unindexedArgs[unindexedIndex++];
            }
            return new Invocation(this, event, args);
        }

        public Function[] functions;
    }

    /**
     * Represents either function invocation with its arguments
     * or Event instance with its data members
     */
    public static class Invocation {
        public final Contract contract;
        public final Function function;
        public final Object[] args;

        public Invocation(Contract contract, Function function, Object[] args) {
            this.contract = contract;
            this.function = function;
            this.args = args;
        }

        @Override
        public String toString() {
            return "[" + "contract=" + contract +
                    (function.type == FunctionType.event ? ", event=" : ", function=")
                    + function + ", args=" + Arrays.toString(args) + ']';
        }
    }
}
