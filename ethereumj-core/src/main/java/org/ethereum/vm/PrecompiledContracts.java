package org.ethereum.vm;

import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

/**
 * @author Roman Mandeleil
 * @since 09.01.2015
 */
public class PrecompiledContracts {

    private static ECRecover ecRecover = new ECRecover();
    private static Sha256 sha256 = new Sha256();
    private static Ripempd160 ripempd160 = new Ripempd160();
    private static Identity identity = new Identity();


    public static PrecompiledContract getContractForAddress(DataWord address) {

        if (address == null) return identity;
        if (address.isHex("0000000000000000000000000000000000000000000000000000000000000001")) return ecRecover;
        if (address.isHex("0000000000000000000000000000000000000000000000000000000000000002")) return sha256;
        if (address.isHex("0000000000000000000000000000000000000000000000000000000000000003")) return ripempd160;
        if (address.isHex("0000000000000000000000000000000000000000000000000000000000000004")) return identity;

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

            if (data == null) return HashUtil.sha256(ByteUtil.EMPTY_BYTE_ARRAY);
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
            if (data == null) result = HashUtil.ripemd160(ByteUtil.EMPTY_BYTE_ARRAY);
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

                ECKey key = ECKey.signatureToKey(h, signature.toBase64());
                out = new DataWord(key.getAddress());
            } catch (Throwable any) {
            }

            if (out == null) out = new DataWord(0);

            return out.getData();
        }
    }


}
