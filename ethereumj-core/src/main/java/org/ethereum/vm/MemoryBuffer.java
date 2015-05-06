package org.ethereum.vm;

import org.ethereum.util.ByteUtil;

import java.util.LinkedList;

import static java.lang.String.format;

public class MemoryBuffer {

    int CHUNK_SIZE = 1024;

    LinkedList<byte[]> chunks = new LinkedList<byte[]>();
    int memorySoftSize = 0;

    public MemoryBuffer() {
    }

    public void memorySave(int address, byte[] data) {

        ensureAvailable(address, data.length);

        int chunkIndex = address / CHUNK_SIZE;
        int chunkOffset = address % CHUNK_SIZE;

        int toCapture = data.length;
        int start = 0;

        while(toCapture > 0){
            int captured = captureMax(chunkIndex, chunkOffset, toCapture, data, start);

            // capture next chunk
            ++chunkIndex;
            chunkOffset = 0;

            // mark remind
            toCapture -= captured;
            start += captured;
        }

        memorySoftSize = Math.max(memorySoftSize, (int) Math.ceil((double) (address + data.length) / 32) * 32);
    }

    public DataWord memoryLoad(int address){

        ensureAvailable(address, 32);
        byte[] retData = new byte[32];

        int chunkIndex = address / CHUNK_SIZE;
        int chunkOffset = address % CHUNK_SIZE;

        int toGrab = retData.length;
        int start = 0;

        while(toGrab > 0){
            int copied = grabMax(chunkIndex, chunkOffset, toGrab, retData, start);

            // read next chunk from the start
            ++chunkIndex;
            chunkOffset = 0;

            // mark remind
            toGrab -= copied;
            start += copied;
        }

        memorySoftSize = Math.max(memorySoftSize, (int) Math.ceil((double) (address + retData.length) / 32) * 32);
        return new DataWord(retData);
    }


    public void memoryExpand(DataWord offsetDW, DataWord sizeDW){

        int offset = offsetDW.intValue();
        int size = sizeDW.intValue();

        ensureAvailable(offset, size);
        memorySoftSize = Math.max(memorySoftSize,offset + size);
    }

    public byte[] memoryChunk(DataWord offsetDW, DataWord sizeDW) {

        int offset = offsetDW.intValue();
        int size = sizeDW.intValue();

        byte[] data = new byte[size];
        ensureAvailable(offset, size);

        int chunkIndex = offset / CHUNK_SIZE;
        int chunkOffset = offset % CHUNK_SIZE;

        int toGrab = data.length;
        int start = 0;

        while(toGrab > 0){
            int copied = grabMax(chunkIndex, chunkOffset, toGrab, data, start);

            // read next chunk from the start
            ++chunkIndex;
            chunkOffset = 0;

            // mark remind
            toGrab -= copied;
            start += copied;
        }

        memorySoftSize = Math.max(memorySoftSize, (int) Math.ceil((double) (offset + data.length) / 32) * 32);

        return data;
    }

    public String memoryToString() {

        StringBuilder memoryData = new StringBuilder();
        StringBuilder firstLine = new StringBuilder();
        StringBuilder secondLine = new StringBuilder();

        for (int i = 0; i < memorySoftSize; ++i){

            byte value =  getByte(i);

            // Check if value is ASCII
            String character = ((byte) 0x20 <= value && value <= (byte) 0x7e) ? new String(new byte[]{value}) : "?";
            firstLine.append(character).append("");
            secondLine.append(ByteUtil.oneByteToHexString(value)).append(" ");

            if ((i + 1) % 8 == 0) {
                String tmp = format("%4s", Integer.toString(i - 7, 16)).replace(" ", "0");
                memoryData.append("").append(tmp).append(" ");
                memoryData.append(firstLine).append(" ");
                memoryData.append(secondLine);
                if (i + 1 < memorySoftSize) memoryData.append("\n");
                firstLine.setLength(0);
                secondLine.setLength(0);
            }
        }

        return memoryData.toString();
    }

/*****************************/
/*****************************/
/*****************************/

    // just access expecting all data valid
    byte getByte(int address){

        int chunkIndex = address / CHUNK_SIZE;
        int chunkOffset = address % CHUNK_SIZE;

        byte[] chunk = chunks.get(chunkIndex);

        return chunk[chunkOffset];
    }


    void ensureAvailable(int address, int offset){

        int memHardSize = getMemoryHardSize();
        int endNewMem = Math.max(memHardSize, address + offset);

        // there is enough mem allocated
        if (endNewMem <= memHardSize) return;

        int toAllocate = endNewMem - memHardSize ;
        int chunks = (toAllocate % (CHUNK_SIZE) == 0) ?
                toAllocate / (CHUNK_SIZE)  :
                (toAllocate / (CHUNK_SIZE)) + 1;
        addChunks(chunks);
    }

    int getMemoryHardSize(){
        return chunks.size() * CHUNK_SIZE;
    }

    int getMemorySoftSize() {
        return memorySoftSize;
    }

    int captureMax(int chunkIndex, int chunkOffset, int size, byte[] src, int srcPos){

        byte[] chunk = chunks.get(chunkIndex);
        int toCapture = Math.min(size, chunk.length - chunkOffset);

        System.arraycopy(src, srcPos, chunk, chunkOffset, toCapture);
        return  toCapture;
    }

    int grabMax(int chunkIndex, int chunkOffset, int size, byte[] dest, int destPos) {

        byte[] chunk = chunks.get(chunkIndex);

        int toGrab = Math.min(size, chunk.length - chunkOffset);
        System.arraycopy(chunk, chunkOffset, dest, destPos, toGrab);

        return toGrab;
    }

    void addChunks(int num){
        for (int i = 0; i < num; ++i)
            addChunk();
    }

    void addChunk(){
        chunks.add(new byte[CHUNK_SIZE]);
    }
}
