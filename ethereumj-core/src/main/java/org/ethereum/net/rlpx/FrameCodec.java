package org.ethereum.net.rlpx;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.ethereum.util.RLP;
import org.spongycastle.crypto.StreamCipher;
import org.spongycastle.crypto.digests.SHA3Digest;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.modes.SICBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

import java.io.*;

/**
 * Created by devrandom on 2015-04-11.
 */
public class FrameCodec {
    private final StreamCipher enc;
    private final StreamCipher dec;
    private final SHA3Digest egressMac;
    private final SHA3Digest ingressMac;
    private final byte[] mac;

    public FrameCodec(EncryptionHandshake.Secrets secrets) {
        this.mac = secrets.mac;
        int blockSize = secrets.aes.length * 8;
        enc = new SICBlockCipher(new AESFastEngine());
        enc.init(true, new ParametersWithIV(new KeyParameter(secrets.aes), new byte[blockSize / 8]));
        dec = new SICBlockCipher(new AESFastEngine());
        dec.init(false, new ParametersWithIV(new KeyParameter(secrets.aes), new byte[blockSize / 8]));
        egressMac = secrets.egressMac;
        ingressMac = secrets.ingressMac;
    }

    private AESFastEngine makeMacCipher() {
        // Stateless AES encryption
        AESFastEngine macc = new AESFastEngine();
        macc.init(true, new KeyParameter(mac));
        return macc;
    }

    public static class Frame {
        long type;
        int size;
        InputStream payload;

        public Frame(long type, int size, InputStream payload) {
            this.type = type;
            this.size = size;
            this.payload = payload;
        }

        public Frame(int type, byte[] payload) {
            this.type = type;
            this.size = payload.length;
            this.payload = new ByteArrayInputStream(payload);
        }

        public int getSize() {
            return size;
        }

        public long getType() {return  type;}

        public InputStream getStream() {
            return payload;
        }
    }

    public void writeFrame(Frame frame, ByteBuf buf) throws IOException {
        writeFrame(frame, new ByteBufOutputStream(buf));
    }

    public void writeFrame(Frame frame, OutputStream out) throws IOException {
        dumpEgress();
        byte[] headBuffer = new byte[32];
        byte[] ptype = RLP.encodeInt((int) frame.type); // FIXME encodeLong
        int totalSize = frame.size + ptype.length;
        headBuffer[0] = (byte)(totalSize >> 16);
        headBuffer[1] = (byte)(totalSize >> 8);
        headBuffer[2] = (byte)(totalSize);
        enc.processBytes(headBuffer, 0, 16, headBuffer, 0);
        updateMac(egressMac, headBuffer, 0, headBuffer, 16);
        dumpEgress();

        byte[] buff = new byte[256];
        out.write(headBuffer);
        enc.processBytes(ptype, 0, ptype.length, buff, 0);
        out.write(buff, 0, ptype.length);
        egressMac.update(buff, 0, ptype.length);
        while (true) {
            int n = frame.payload.read(buff);
            if (n <= 0) break;
            enc.processBytes(buff, 0, n, buff, 0);
            egressMac.update(buff, 0, n);
            out.write(buff, 0, n);
        }
        int padding = 16 - (totalSize % 16);
        byte[] pad = new byte[16];
        if (padding < 16) {
            enc.processBytes(pad, 0, padding, buff, 0);
            egressMac.update(buff, 0, padding);
            out.write(buff, 0, padding);
        }
        byte[] macBuffer = new byte[egressMac.getDigestSize()];
        doSum(egressMac, macBuffer); // fmacseed
        updateMac(egressMac, macBuffer, 0, macBuffer, 0);
        out.write(macBuffer, 0, 16);
    }

    private void dumpEgress() {
        byte[] buf = new byte[32];
        new SHA3Digest(egressMac).doFinal(buf, 0);
//        System.out.println("egress MAC " + Hex.toHexString(buf));
    }

    public Frame readFrame(ByteBuf buf) throws IOException {
        return readFrame(new ByteBufInputStream(buf));
    }

    public Frame readFrame(DataInput inp) throws IOException {
        byte[] headBuffer = new byte[32];
        inp.readFully(headBuffer);
        dec.processBytes(headBuffer, 0, 16, headBuffer, 0);
        int totalSize;
        totalSize = headBuffer[0];
        totalSize = (totalSize << 8) + headBuffer[1];
        totalSize = (totalSize << 8) + headBuffer[2];
        int padding = 16 - (totalSize % 16);
        if (padding == 16) padding = 0;
        byte[] buffer = new byte[totalSize + padding];
        inp.readFully(buffer);
        dec.processBytes(buffer, 0, buffer.length, buffer, 0);
        int pos = 0;
        byte[] macBuffer = new byte[ingressMac.getDigestSize()];
        inp.readFully(macBuffer, 0, 16);
        long type = RLP.decodeInt(buffer, pos); // FIXME long
        pos = RLP.getNextElementIndex(buffer, pos);
        InputStream payload = new ByteArrayInputStream(buffer, pos, totalSize - pos);
        int size = totalSize - pos;
        return new Frame(type, size, payload);
    }

    private byte[] updateMac(SHA3Digest mac, byte[] seed, int offset, byte[] buf, int outOffset) {
        byte[] aesBlock = new byte[mac.getDigestSize()];
        doSum(mac, aesBlock);
        makeMacCipher().processBlock(aesBlock, 0, aesBlock, 0);
        // Note that although the mac digest size is 32 bytes, we only use 16 bytes in the computation
        int length = 16;
        for (int i = 0; i < length; i++) {
            aesBlock[i] ^= seed[i + offset];
        }
//        System.out.println("update seed " + Hex.toHexString(seed, offset, length));
//        System.out.println("update aesbuf ^ seed " + Hex.toHexString(aesBlock));
        mac.update(aesBlock, 0, length);
        byte[] result = new byte[mac.getDigestSize()];
        doSum(mac, result);
        for (int i = 0; i < length ; i++) {
            buf[i + outOffset] = result[i];
        }
        return result;
    }

    private void doSum(SHA3Digest mac, byte[] out) {
        // doFinal without resetting the MAC by using clone of digest state
        new SHA3Digest(mac).doFinal(out, 0);
    }
}
