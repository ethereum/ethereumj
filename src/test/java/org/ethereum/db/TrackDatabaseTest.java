package org.ethereum.db;

import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 11/06/2014 14:54
 */

public class TrackDatabaseTest {


    @Test
    public void test1(){

        Database db1 = new Database("temp");
        TrackDatabase trackDatabase1 = new TrackDatabase(db1);

        trackDatabase1.put(Hex.decode("abcdef"), Hex.decode("abcdef"));
        byte[] value  = trackDatabase1.get(Hex.decode("abcdef"));
        Assert.assertEquals("abcdef", Hex.toHexString(value));

        trackDatabase1.startTrack();
        trackDatabase1.put(Hex.decode("abcdef"), Hex.decode("ffffff"));
        value  = trackDatabase1.get(Hex.decode("abcdef"));
        Assert.assertEquals("ffffff", Hex.toHexString(value));

        trackDatabase1.rollbackTrack();
        value  = trackDatabase1.get(Hex.decode("abcdef"));
        Assert.assertEquals("abcdef", Hex.toHexString(value));

        trackDatabase1.startTrack();
        trackDatabase1.put(Hex.decode("abcdef"), Hex.decode("ffffff"));
        trackDatabase1.commitTrack();
        value  = trackDatabase1.get(Hex.decode("abcdef"));
        Assert.assertEquals("ffffff", Hex.toHexString(value));
    }
}
