package org.ethereum.core;

import org.spongycastle.util.encoders.Hex;

/**
 * www.etherj.com
 *
 * @author: Roman Mandeleil
 * Created on: 20/11/2014 11:10
 *
 * http://www.herongyang.com/Java/Bit-String-Set-Bit-to-Byte-Array.html
 */

public class  Bloom {

    byte[] data = new byte[512];


    public Bloom(byte[] data){
    }

    public static Bloom create(byte[] toBloom){

        int mov1 = (toBloom[0] & 1) * 256 + (toBloom[1] & 255);
        int mov2 = (toBloom[2] & 1) * 256 + (toBloom[3] & 255);
        int mov3 = (toBloom[4] & 1) * 256 + (toBloom[5] & 255);

        byte[] data = new byte[512];
        Bloom bloom = new Bloom(data);

        bloom.setBit(mov1, 1);
        bloom.setBit(mov2, 1);
        bloom.setBit(mov3, 1);

        return bloom;
    }

    public void or(Bloom bloom){

        for (int i = 0; i < data.length; ++i){
            data[i] |= bloom.data[i];
        }
    }

    public void setBit(int pos, int val) {

        if (data.length - 1 < pos )
            throw new Error("outside bloom limit, pos: " + pos);

        int posByte  = (pos - 1) / 8;
        int posBit   = (pos - 1) % 8;
        byte oldByte = data[posByte];
             oldByte = (byte) (((0xFF7F >>  (( posBit + 1)) & oldByte) & 0x00FF));
        byte newByte = (byte) ((val << ( 8 - ( posBit + 1))) | oldByte);
        data[posByte] = newByte;
    }

    public int getBit(int pos) {

        if (data.length - 1 < pos )
            throw new Error("outside bloom limit, pos: " + pos);

        int  posByte = pos / 8;
        int  posBit = pos % 8;
        byte valByte = data[posByte];
        int  valInt = valByte >> (8 - (posBit + 1)) & 0x0001;
        return valInt;
    }


    @Override
    public String toString() {
        return Hex.toHexString(data);
    }
}
