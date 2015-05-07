package org.ethereum.vm;

import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;

public class MemoryBufferTest {

    @Test
    public void ensureAvailable_1() {
        MemoryBuffer memoryBuffer = new MemoryBuffer();
        memoryBuffer.ensureAvailable(0, 0);
        Assert.assertTrue(0 == memoryBuffer.chunks.size());
        Assert.assertTrue(memoryBuffer.memorySoftSize == 0);
    }

    @Test
    public void ensureAvailable_2() {
        MemoryBuffer memoryBuffer = new MemoryBuffer();
        memoryBuffer.ensureAvailable(0, 1);
        Assert.assertTrue(1 == memoryBuffer.chunks.size());
        Assert.assertTrue(memoryBuffer.memorySoftSize == 0);
    }


    @Test
    public void ensureAvailable_3() {
        MemoryBuffer memoryBuffer = new MemoryBuffer();
        memoryBuffer.ensureAvailable(0, 1023);
        Assert.assertTrue(1 == memoryBuffer.chunks.size());
        Assert.assertTrue(memoryBuffer.memorySoftSize == 0);
    }

    @Test
    public void ensureAvailable_4() {
        MemoryBuffer memoryBuffer = new MemoryBuffer();
        memoryBuffer.ensureAvailable(0, 1024);
        Assert.assertTrue(1 == memoryBuffer.chunks.size());
        Assert.assertTrue(memoryBuffer.memorySoftSize == 0);
    }

    @Test
    public void ensureAvailable_5() {
        MemoryBuffer memoryBuffer = new MemoryBuffer();
        memoryBuffer.ensureAvailable(0, 1025);
        Assert.assertTrue(2 == memoryBuffer.chunks.size());
        Assert.assertTrue(memoryBuffer.memorySoftSize == 0);
    }


    @Test
    public void ensureAvailable_6() {
        MemoryBuffer memoryBuffer = new MemoryBuffer();
        memoryBuffer.ensureAvailable(0, 2000);
        Assert.assertTrue(2 == memoryBuffer.chunks.size());
        Assert.assertTrue(memoryBuffer.memorySoftSize == 0);
    }

    @Test
    public void memorySave_1() {

        MemoryBuffer memoryBuffer = new MemoryBuffer();
        byte[] data = {1, 1, 1, 1};

        memoryBuffer.memorySave(0, data);

        Assert.assertTrue(1 == memoryBuffer.chunks.size());

        byte[] chunk = memoryBuffer.chunks.get(0);
        Assert.assertTrue(chunk[0] == 1);
        Assert.assertTrue(chunk[1] == 1);
        Assert.assertTrue(chunk[2] == 1);
        Assert.assertTrue(chunk[3] == 1);
        Assert.assertTrue(chunk[4] == 0);

        Assert.assertTrue(memoryBuffer.memorySoftSize == 32);
    }

    @Test
    public void memorySave_2() {

        MemoryBuffer memoryBuffer = new MemoryBuffer();
        byte[] data = Hex.decode("0101010101010101010101010101010101010101010101010101010101010101");

        memoryBuffer.memorySave(0, data);

        Assert.assertTrue(1 == memoryBuffer.chunks.size());

        byte[] chunk = memoryBuffer.chunks.get(0);
        Assert.assertTrue(chunk[0] == 1);
        Assert.assertTrue(chunk[1] == 1);

        Assert.assertTrue(chunk[30] == 1);
        Assert.assertTrue(chunk[31] == 1);
        Assert.assertTrue(chunk[32] == 0);

        Assert.assertTrue(memoryBuffer.memorySoftSize == 32);
    }

    @Test
    public void memorySave_3() {

        MemoryBuffer memoryBuffer = new MemoryBuffer();
        byte[] data = Hex.decode("010101010101010101010101010101010101010101010101010101010101010101");

        memoryBuffer.memorySave(0, data);

        Assert.assertTrue(1 == memoryBuffer.chunks.size());

        byte[] chunk = memoryBuffer.chunks.get(0);
        Assert.assertTrue(chunk[0] == 1);
        Assert.assertTrue(chunk[1] == 1);

        Assert.assertTrue(chunk[30] == 1);
        Assert.assertTrue(chunk[31] == 1);
        Assert.assertTrue(chunk[32] == 1);
        Assert.assertTrue(chunk[33] == 0);

        Assert.assertTrue(memoryBuffer.memorySoftSize == 64);
    }

    @Test
    public void memorySave_4() {

        MemoryBuffer memoryBuffer = new MemoryBuffer();
        byte[] data = new byte[1024];
        Arrays.fill(data, (byte) 1);

        memoryBuffer.memorySave(0, data);

        Assert.assertTrue(1 == memoryBuffer.chunks.size());

        byte[] chunk = memoryBuffer.chunks.get(0);
        Assert.assertTrue(chunk[0] == 1);
        Assert.assertTrue(chunk[1] == 1);

        Assert.assertTrue(chunk[1022] == 1);
        Assert.assertTrue(chunk[1023] == 1);

        Assert.assertTrue(memoryBuffer.memorySoftSize == 1024);
    }

    @Test
    public void memorySave_5() {

        MemoryBuffer memoryBuffer = new MemoryBuffer();

        byte[] data = new byte[1025];
        Arrays.fill(data, (byte) 1);

        memoryBuffer.memorySave(0, data);

        Assert.assertTrue(2 == memoryBuffer.chunks.size());

        byte[] chunk1 = memoryBuffer.chunks.get(0);
        Assert.assertTrue(chunk1[0] == 1);
        Assert.assertTrue(chunk1[1] == 1);

        Assert.assertTrue(chunk1[1022] == 1);
        Assert.assertTrue(chunk1[1023] == 1);

        byte[] chunk2 = memoryBuffer.chunks.get(1);
        Assert.assertTrue(chunk2[0] == 1);
        Assert.assertTrue(chunk2[1] == 0);

        Assert.assertTrue(memoryBuffer.memorySoftSize == 1056);
    }

    @Test
    public void memorySave_6() {

        MemoryBuffer memoryBuffer = new MemoryBuffer();

        byte[] data1 = new byte[1024];
        Arrays.fill(data1, (byte) 1);

        byte[] data2 = new byte[1024];
        Arrays.fill(data2, (byte) 2);

        memoryBuffer.memorySave(0, data1);
        memoryBuffer.memorySave(1024, data2);

        Assert.assertTrue(2 == memoryBuffer.chunks.size());

        byte[] chunk1 = memoryBuffer.chunks.get(0);
        Assert.assertTrue(chunk1[0] == 1);
        Assert.assertTrue(chunk1[1] == 1);

        Assert.assertTrue(chunk1[1022] == 1);
        Assert.assertTrue(chunk1[1023] == 1);

        byte[] chunk2 = memoryBuffer.chunks.get(1);
        Assert.assertTrue(chunk2[0] == 2);
        Assert.assertTrue(chunk2[1] == 2);

        Assert.assertTrue(chunk2[1022] == 2);
        Assert.assertTrue(chunk2[1023] == 2);

        Assert.assertTrue(memoryBuffer.memorySoftSize == 2048);
    }

    @Test
    public void memorySave_7() {

        MemoryBuffer memoryBuffer = new MemoryBuffer();

        byte[] data1 = new byte[1024];
        Arrays.fill(data1, (byte) 1);

        byte[] data2 = new byte[1024];
        Arrays.fill(data2, (byte) 2);

        byte[] data3 = new byte[1];
        Arrays.fill(data3, (byte) 3);

        memoryBuffer.memorySave(0, data1);
        memoryBuffer.memorySave(1024, data2);
        memoryBuffer.memorySave(2048, data3);

        Assert.assertTrue(3 == memoryBuffer.chunks.size());

        byte[] chunk1 = memoryBuffer.chunks.get(0);
        Assert.assertTrue(chunk1[0] == 1);
        Assert.assertTrue(chunk1[1] == 1);

        Assert.assertTrue(chunk1[1022] == 1);
        Assert.assertTrue(chunk1[1023] == 1);

        byte[] chunk2 = memoryBuffer.chunks.get(1);
        Assert.assertTrue(chunk2[0] == 2);
        Assert.assertTrue(chunk2[1] == 2);

        Assert.assertTrue(chunk2[1022] == 2);
        Assert.assertTrue(chunk2[1023] == 2);

        byte[] chunk3 = memoryBuffer.chunks.get(2);
        Assert.assertTrue(chunk3[0] == 3);

        Assert.assertTrue(memoryBuffer.memorySoftSize == 2080);
    }

    @Test
    public void memorySave_8() {

        MemoryBuffer memoryBuffer = new MemoryBuffer();

        byte[] data1 = new byte[128];
        Arrays.fill(data1, (byte) 1);

        memoryBuffer.memorySave(0, 256, data1);

        int ones = 0; int zeroes = 0;
        for (int i = 0; i < memoryBuffer.getSize(); ++i){
            if (memoryBuffer.getByte(i) == 1) ++ones;
            if (memoryBuffer.getByte(i) == 0) ++zeroes;
        }

        Assert.assertTrue(ones == zeroes);
        Assert.assertTrue(256 == memoryBuffer.memorySoftSize);
    }



    @Test
    public void memoryLoad_1() {

        MemoryBuffer memoryBuffer = new MemoryBuffer();
        DataWord value = memoryBuffer.memoryLoad(100);
        Assert.assertTrue(value.intValue() == 0);
        Assert.assertTrue(memoryBuffer.chunks.size() == 1);
        Assert.assertTrue(memoryBuffer.memorySoftSize == 32 * 5);
    }

    @Test
    public void memoryLoad_2() {

        MemoryBuffer memoryBuffer = new MemoryBuffer();
        DataWord value = memoryBuffer.memoryLoad(2015);
        Assert.assertTrue(value.intValue() == 0);
        Assert.assertTrue(memoryBuffer.chunks.size() == 2);
        Assert.assertTrue(memoryBuffer.memorySoftSize == 2048);
    }

    @Test
    public void memoryLoad_3() {

        MemoryBuffer memoryBuffer = new MemoryBuffer();
        DataWord value = memoryBuffer.memoryLoad(2016);
        Assert.assertTrue(value.intValue() == 0);
        Assert.assertTrue(memoryBuffer.chunks.size() == 2);
        Assert.assertTrue(memoryBuffer.memorySoftSize == 2048);
    }

    @Test
    public void memoryLoad_4() {

        MemoryBuffer memoryBuffer = new MemoryBuffer();
        DataWord value = memoryBuffer.memoryLoad(2017);
        Assert.assertTrue(value.intValue() == 0);
        Assert.assertTrue(memoryBuffer.chunks.size() == 3);
        Assert.assertTrue(memoryBuffer.memorySoftSize == 2080);
    }

    @Test
    public void memoryLoad_5() {

        MemoryBuffer memoryBuffer = new MemoryBuffer();

        byte[] data1 = new byte[1024];
        Arrays.fill(data1, (byte) 1);

        byte[] data2 = new byte[1024];
        Arrays.fill(data2, (byte) 2);

        memoryBuffer.memorySave(0, data1);
        memoryBuffer.memorySave(1024, data2);

        Assert.assertTrue(memoryBuffer.chunks.size() == 2);
        Assert.assertTrue(memoryBuffer.memorySoftSize == 2048);

        DataWord val1 = memoryBuffer.memoryLoad(0x3df);
        DataWord val2 = memoryBuffer.memoryLoad(0x3e0);
        DataWord val3 = memoryBuffer.memoryLoad(0x3e1);

        Assert.assertArrayEquals(
                Hex.decode("0101010101010101010101010101010101010101010101010101010101010101"),
                val1.getData());

        Assert.assertArrayEquals(
                Hex.decode("0101010101010101010101010101010101010101010101010101010101010101"),
                val2.getData());

        Assert.assertArrayEquals(
                Hex.decode("0101010101010101010101010101010101010101010101010101010101010102"),
                val3.getData());
        Assert.assertTrue(memoryBuffer.memorySoftSize == 2048);
    }


    @Test
    public void memoryExpand_1(){

        MemoryBuffer memoryBuffer = new MemoryBuffer();
        memoryBuffer.memoryExpand(0, 32);
        Assert.assertTrue(memoryBuffer.memorySoftSize == 32);
        Assert.assertTrue(1 == memoryBuffer.chunks.size());
    }

    @Test
    public void memoryExpand_2(){

        MemoryBuffer memoryBuffer = new MemoryBuffer();
        memoryBuffer.memoryExpand(0, 64);
        Assert.assertTrue(memoryBuffer.memorySoftSize == 64);
        Assert.assertTrue(1 == memoryBuffer.chunks.size());
    }



    @Test
    public void memoryChunk_1(){
        MemoryBuffer memoryBuffer = new MemoryBuffer();

        byte[] data1 = new byte[32];
        Arrays.fill(data1, (byte) 1);

        byte[] data2 = new byte[32];
        Arrays.fill(data2, (byte) 2);

        memoryBuffer.memorySave(0, data1);
        memoryBuffer.memorySave(32, data2);

        byte[] data = memoryBuffer.memoryChunk(0, 64);

        Assert.assertArrayEquals(
                Hex.decode("0101010101010101010101010101010101010101010101010101010101010101" +
                           "0202020202020202020202020202020202020202020202020202020202020202"),
                data
        );

        Assert.assertTrue(64 == memoryBuffer.memorySoftSize);
    }


    @Test
    public void memoryChunk_2(){
        MemoryBuffer memoryBuffer = new MemoryBuffer();

        byte[] data1 = new byte[32];
        Arrays.fill(data1, (byte) 1);

        memoryBuffer.memorySave(0, data1);
        Assert.assertTrue(32 == memoryBuffer.memorySoftSize);

        byte[] data = memoryBuffer.memoryChunk(0, 64);

        Assert.assertArrayEquals(
                Hex.decode("0101010101010101010101010101010101010101010101010101010101010101" +
                           "0000000000000000000000000000000000000000000000000000000000000000"),
                data
        );

        Assert.assertTrue(64 == memoryBuffer.memorySoftSize);
    }

    @Test
    public void memoryChunk_3(){

        MemoryBuffer memoryBuffer = new MemoryBuffer();

        byte[] data1 = new byte[1024];
        Arrays.fill(data1, (byte) 1);

        byte[] data2 = new byte[1024];
        Arrays.fill(data2, (byte) 2);

        memoryBuffer.memorySave(0, data1);
        memoryBuffer.memorySave(1024, data2);

        byte[] data = memoryBuffer.memoryChunk(0, 2048);

        int ones = 0; int twos = 0;
        for (int i = 0; i < data.length; ++i){
            if (data[i] == 1) ++ones;
            if (data[i] == 2) ++twos;
        }

        Assert.assertTrue(ones == twos);
        Assert.assertTrue(2048 == memoryBuffer.memorySoftSize);
    }

    @Test
    public void memoryChunk_4(){

        MemoryBuffer memoryBuffer = new MemoryBuffer();

        byte[] data1 = new byte[1024];
        Arrays.fill(data1, (byte) 1);

        byte[] data2 = new byte[1024];
        Arrays.fill(data2, (byte) 2);

        memoryBuffer.memorySave(0, data1);
        memoryBuffer.memorySave(1024, data2);

        byte[] data = memoryBuffer.memoryChunk(0, 2049);

        int ones = 0; int twos = 0; int zero = 0;
        for (int i = 0; i < data.length; ++i){
            if (data[i] == 1) ++ones;
            if (data[i] == 2) ++twos;
            if (data[i] == 0) ++zero;
        }

        Assert.assertTrue(zero == 1);
        Assert.assertTrue(ones == twos);
        Assert.assertTrue(2080 == memoryBuffer.memorySoftSize);
    }



}