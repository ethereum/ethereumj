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
package org.ethereum.crypto;

import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.DerivationFunction;
import org.spongycastle.crypto.DerivationParameters;
import org.spongycastle.crypto.Digest;
import org.spongycastle.crypto.params.MGFParameters;

/**
 * This class is borrowed from spongycastle project
 * The only change made is addition of 'counterStart' parameter to
 * conform to Crypto++ capabilities
 */
public class MGF1BytesGeneratorExt implements DerivationFunction {
    private Digest digest;
    private byte[] seed;
    private int hLen;
    private int counterStart;

    public MGF1BytesGeneratorExt(Digest digest, int counterStart) {
        this.digest = digest;
        this.hLen = digest.getDigestSize();
        this.counterStart = counterStart;
    }

    public void init(DerivationParameters param) {
        if(!(param instanceof MGFParameters)) {
            throw new IllegalArgumentException("MGF parameters required for MGF1Generator");
        } else {
            MGFParameters p = (MGFParameters)param;
            this.seed = p.getSeed();
        }
    }

    public Digest getDigest() {
        return this.digest;
    }

    private void ItoOSP(int i, byte[] sp) {
        sp[0] = (byte)(i >>> 24);
        sp[1] = (byte)(i >>> 16);
        sp[2] = (byte)(i >>> 8);
        sp[3] = (byte)(i >>> 0);
    }

    public int generateBytes(byte[] out, int outOff, int len) throws DataLengthException, IllegalArgumentException {
        if(out.length - len < outOff) {
            throw new DataLengthException("output buffer too small");
        } else {
            byte[] hashBuf = new byte[this.hLen];
            byte[] C = new byte[4];
            int counter = 0;
            int hashCounter = counterStart;
            this.digest.reset();
            if(len > this.hLen) {
                do {
                    this.ItoOSP(hashCounter++, C);
                    this.digest.update(this.seed, 0, this.seed.length);
                    this.digest.update(C, 0, C.length);
                    this.digest.doFinal(hashBuf, 0);
                    System.arraycopy(hashBuf, 0, out, outOff + counter * this.hLen, this.hLen);
                    ++counter;
                } while(counter < len / this.hLen);
            }

            if(counter * this.hLen < len) {
                this.ItoOSP(hashCounter, C);
                this.digest.update(this.seed, 0, this.seed.length);
                this.digest.update(C, 0, C.length);
                this.digest.doFinal(hashBuf, 0);
                System.arraycopy(hashBuf, 0, out, outOff + counter * this.hLen, len - counter * this.hLen);
            }

            return len;
        }
    }
}
