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
package org.ethereum.vm;

import org.ethereum.config.BlockchainConfig;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;

import java.math.BigInteger;

import static org.ethereum.util.BIUtil.addSafely;
import static org.ethereum.util.BIUtil.isLessThan;
import static org.ethereum.util.BIUtil.isZero;
import static org.ethereum.util.ByteUtil.*;

/**
 * @author Roman Mandeleil
 * @since 09.01.2015
 */
public class PrecompiledContracts {

    private static final ECRecover ecRecover = new ECRecover();
    private static final Sha256 sha256 = new Sha256();
    private static final Ripempd160 ripempd160 = new Ripempd160();
    private static final Identity identity = new Identity();
    private static final ModExp modExp = new ModExp();

    private static final DataWord ecRecoverAddr =   new DataWord("0000000000000000000000000000000000000000000000000000000000000001");
    private static final DataWord sha256Addr =      new DataWord("0000000000000000000000000000000000000000000000000000000000000002");
    private static final DataWord ripempd160Addr =  new DataWord("0000000000000000000000000000000000000000000000000000000000000003");
    private static final DataWord identityAddr =    new DataWord("0000000000000000000000000000000000000000000000000000000000000004");
    private static final DataWord modExpAddr =      new DataWord("0000000000000000000000000000000000000000000000000000000000000005");


    public static PrecompiledContract getContractForAddress(DataWord address, BlockchainConfig config) {

        if (address == null) return identity;
        if (address.equals(ecRecoverAddr)) return ecRecover;
        if (address.equals(sha256Addr)) return sha256;
        if (address.equals(ripempd160Addr)) return ripempd160;
        if (address.equals(identityAddr)) return identity;

        // Byzantium precompiles
        if (address.equals(modExpAddr) && config.eip198()) return modExp;

        return null;
    }


    public static abstract class PrecompiledContract {
        public abstract long getGasForData(byte[] data);

        public abstract byte[] execute(byte[] data);
    }

    public static class Identity extends PrecompiledContract {

        public Identity() {
        }

        @Override
        public long getGasForData(byte[] data) {

            // gas charge for the execution:
            // minimum 1 and additional 1 for each 32 bytes word (round  up)
            if (data == null) return 15;
            return 15 + (data.length + 31) / 32 * 3;
        }

        @Override
        public byte[] execute(byte[] data) {
            return data;
        }
    }

    public static class Sha256 extends PrecompiledContract {


        @Override
        public long getGasForData(byte[] data) {

            // gas charge for the execution:
            // minimum 50 and additional 50 for each 32 bytes word (round  up)
            if (data == null) return 60;
            return 60 + (data.length + 31) / 32 * 12;
        }

        @Override
        public byte[] execute(byte[] data) {

            if (data == null) return HashUtil.sha256(EMPTY_BYTE_ARRAY);
            return HashUtil.sha256(data);
        }
    }


    public static class Ripempd160 extends PrecompiledContract {


        @Override
        public long getGasForData(byte[] data) {

            // TODO #POC9 Replace magic numbers with constants
            // gas charge for the execution:
            // minimum 50 and additional 50 for each 32 bytes word (round  up)
            if (data == null) return 600;
            return 600 + (data.length + 31) / 32 * 120;
        }

        @Override
        public byte[] execute(byte[] data) {

            byte[] result = null;
            if (data == null) result = HashUtil.ripemd160(EMPTY_BYTE_ARRAY);
            else result = HashUtil.ripemd160(data);

            return new DataWord(result).getData();
        }
    }


    public static class ECRecover extends PrecompiledContract {

        @Override
        public long getGasForData(byte[] data) {
            return 3000;
        }

        @Override
        public byte[] execute(byte[] data) {

            byte[] h = new byte[32];
            byte[] v = new byte[32];
            byte[] r = new byte[32];
            byte[] s = new byte[32];

            DataWord out = null;

            try {
                System.arraycopy(data, 0, h, 0, 32);
                System.arraycopy(data, 32, v, 0, 32);
                System.arraycopy(data, 64, r, 0, 32);

                int sLength = data.length < 128 ? data.length - 96 : 32;
                System.arraycopy(data, 96, s, 0, sLength);

                ECKey.ECDSASignature signature = ECKey.ECDSASignature.fromComponents(r, s, v[31]);
                if (validateV(v) && signature.validateComponents()) {
                    out = new DataWord(ECKey.signatureToAddress(h, signature));
                }
            } catch (Throwable any) {
            }

            if (out == null) {
                return new byte[0];
            } else {
                return out.getData();
            }
        }

        private static boolean validateV(byte[] v) {
            for (int i = 0; i < v.length - 1; i++) {
                if (v[i] != 0) return false;
            }
            return true;
        }
    }

    /**
     * Computes modular exponentiation on big numbers
     *
     * format of data[] array:
     * [length_of_BASE] [length_of_EXPONENT] [length_of_MODULUS] [BASE] [EXPONENT] [MODULUS]
     * where every length is a 32-byte left-padded integer representing the number of bytes.
     * Call data is assumed to be infinitely right-padded with zero bytes.
     *
     * Returns an output as a byte array with the same length as the modulus
     */
    public static class ModExp extends PrecompiledContract {

        private static final BigInteger GQUAD_DIVISOR = BigInteger.valueOf(100);

        private static final int ARGS_OFFSET = 32 * 3; // addresses length part

        @Override
        public long getGasForData(byte[] data) {

            if (data == null) return 0;

            int baseLen = parseLen(data, 0);
            int expLen  = parseLen(data, 1);
            int modLen  = parseLen(data, 2);

            byte[] expHighBytes = parseBytes(data, addSafely(ARGS_OFFSET, baseLen), Math.min(expLen, 32));

            long multComplexity = getMultComplexity(Math.max(baseLen, modLen));
            long adjExpLen = getAdjustedExponentLength(expHighBytes, expLen);

            // use big numbers to stay safe in case of overflow
            BigInteger gas = BigInteger.valueOf(multComplexity)
                    .multiply(BigInteger.valueOf(Math.max(adjExpLen, 1)))
                    .divide(GQUAD_DIVISOR);

            return isLessThan(gas, BigInteger.valueOf(Long.MAX_VALUE)) ? gas.longValue() : Long.MAX_VALUE;
        }

        @Override
        public byte[] execute(byte[] data) {

            if (data == null)
                return EMPTY_BYTE_ARRAY;

            int baseLen = parseLen(data, 0);
            int expLen  = parseLen(data, 1);
            int modLen  = parseLen(data, 2);

            BigInteger base = parseArg(data, ARGS_OFFSET, baseLen);
            BigInteger exp  = parseArg(data, addSafely(ARGS_OFFSET, baseLen), expLen);
            BigInteger mod  = parseArg(data, addSafely(addSafely(ARGS_OFFSET, baseLen), expLen), modLen);

            // check if modulus is zero
            if (isZero(mod))
                return EMPTY_BYTE_ARRAY;

            byte[] res = stripLeadingZeroes(base.modPow(exp, mod).toByteArray());

            // adjust result to the same length as the modulus has
            if (res.length < modLen) {

                byte[] adjRes = new byte[modLen];
                System.arraycopy(res, 0, adjRes, modLen - res.length, res.length);

                return adjRes;

            } else {
                return res;
            }
        }

        private long getMultComplexity(long x) {

            long x2 = x * x;

            if (x <= 64)    return x2;
            if (x <= 1024)  return x2 / 4 + 96 * x - 3072;

            return x2 / 16 + 480 * x - 199680;
        }

        private long getAdjustedExponentLength(byte[] expHighBytes, long expLen) {

            int leadingZeros = numberOfLeadingZeros(expHighBytes);
            int highestBit = 8 * expHighBytes.length - leadingZeros;

            // set index basement to zero
            if (highestBit > 0) highestBit--;

            if (expLen <= 32) {
                return highestBit;
            } else {
                return 8 * (expLen - 32) + highestBit;
            }
        }

        private int parseLen(byte[] data, int idx) {
            byte[] bytes = parseBytes(data, 32 * idx, 32);
            return new DataWord(bytes).intValueSafe();
        }

        private BigInteger parseArg(byte[] data, int offset, int len) {
            byte[] bytes = parseBytes(data, offset, len);
            return bytesToBigInteger(bytes);
        }

        private byte[] parseBytes(byte[] data, int offset, int len) {

            if (offset >= data.length || len == 0)
                return EMPTY_BYTE_ARRAY;

            byte[] bytes = new byte[len];
            System.arraycopy(data, offset, bytes, 0, Math.min(data.length - offset, len));
            return bytes;
        }

    }
}
