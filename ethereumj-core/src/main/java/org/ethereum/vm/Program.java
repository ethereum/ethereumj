package org.ethereum.vm;

import com.google.api.client.util.ByteStreams;
import io.netty.buffer.UnpooledDirectByteBuf;
import org.ethereum.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 01/06/2014 10:45
 */

public class Program {

    Logger logger = LoggerFactory.getLogger("VM");

    Stack<DataWord> stack = new Stack<DataWord>();
    Map<DataWord, DataWord> storage = new HashMap<DataWord, DataWord>();
    ByteBuffer memory = null;

    byte[] ops;
    int    pc = 0;

    public Program(byte[] ops) {

        if (ops == null) throw new RuntimeException("program can not run with ops: null");

        this.ops = ops;
    }

    public byte getCurrentOp(){
        return ops[pc];
    }


    public void stackPush(byte[] data){
        DataWord stackWord = new DataWord(data);
        stack.push(stackWord);
    }

    public void stackPush(DataWord stackWord){
        stack.push(stackWord);
    }

    public int getPC() {
        return pc;
    }

    public void setPC(DataWord pc) {
        this.pc = pc.value().intValue();

        if (this.pc > ops.length) throw new RuntimeException("pc overflow pc: " + pc);
    }

    public void setPC(int pc) {
        this.pc = pc;
    }

    public void step(){
        ++pc;
    }

    public byte[] sweep(int n){

        if (pc + n > ops.length) throw new RuntimeException("pc overflow sweep n: " + n + " pc: " + pc);

        byte[] data = Arrays.copyOfRange(ops, pc, pc + n);
        pc += n;
        return data;
    }

    public DataWord stackPull(){

        if (stack.size() == 0) throw new RuntimeException("attempted pull action for empty stack");
        return stack.pop();
    };

    public void memorySave(DataWord addrB, DataWord value){
        memorySave(addrB.data, value.data);
    }

    public void memorySave(byte[] addr, byte[] value){

        int address = new BigInteger(1, addr).intValue();
        allocateMemory(address, value);

        System.arraycopy(value, 0, memory.array(), address, value.length);
    }

    public DataWord memoryLoad(DataWord addr){

        int address = new BigInteger(1, addr.getData()).intValue();
        allocateMemory(address, DataWord.ZERO.data);

        byte[] data = new byte[32];
        System.arraycopy(memory.array(), address,  data , 0  ,32);

        return new DataWord(data);
    }

    private void allocateMemory(int address, byte[] value){

        int memSize = 0;
        if (memory != null) memSize = memory.limit();

        // check if you need to allocate
        if (memSize < (address + value.length)){

            int sizeToAllocate = 0;
            if (memSize > address){

                sizeToAllocate = memSize + value.length;
            } else {
                sizeToAllocate = memSize + (address - memSize) + value.length;
            }

            // complete to 32
            sizeToAllocate = (sizeToAllocate % 32)==0 ? sizeToAllocate :
                                                        sizeToAllocate + (32 - sizeToAllocate % 32);

            sizeToAllocate = (sizeToAllocate == 0)? 32: sizeToAllocate;

            ByteBuffer tmpMem = ByteBuffer.allocate(sizeToAllocate);
            if (memory != null)
                System.arraycopy(memory.array(), 0, tmpMem.array(), 0, memory.limit());

            memory = tmpMem;
        }
    }

    public void storageSave(DataWord word1, DataWord word2){
        storageSave(word1.getData(), word2.getData());
    }

    public void storageSave(byte[] key, byte[] val){
        DataWord keyWord = new DataWord(key);
        DataWord valWord = new DataWord(val);
        storage.put(keyWord, valWord);
    }


    public DataWord storageLoad(DataWord key){
        return storage.get(key);
    }


    public void fullTrace(){
        if (logger.isDebugEnabled()){

            StringBuilder stackData = new StringBuilder();
            for (int i = 0; i < stack.size(); ++i){

                stackData.append(" ").append(stack.get(i));
                if (i < stack.size() - 1) stackData.append("\n");
            }
            if (stackData.length() > 0) stackData.insert(0, "\n");

            StringBuilder storageData = new StringBuilder();
            for (DataWord key : storage.keySet()){

                storageData.append(" ").append(key).append(" -> ").append(storage.get(key)).append("\n");
            }
            if (storageData.length() > 0) storageData.insert(0, "\n");

            StringBuilder memoryData = new StringBuilder();
            StringBuilder oneLine = new StringBuilder();
            for (int i = 0; memory != null && i < memory.limit(); ++i){

                byte value = memory.get(i);
                oneLine.append(Utils.oneByteToHexString(value)).append(" ");

                if ((i + 1) % 16 == 0) {

                    String tmp = String.format("[%4s]-[%4s]", Integer.toString(i - 15, 16),
                            Integer.toString(i, 16)).replace(" ", "0");
                    memoryData.append("" ).append(tmp).append(" ");
                    memoryData.append(oneLine);
                    if (i < memory.limit()) memoryData.append("\n");
                    oneLine.setLength(0);
                }
            }
            if (memoryData.length() > 0) memoryData.insert(0, "\n");

            StringBuilder opsString = new StringBuilder();
            for (int i = 0; i < ops.length; ++i){

                String tmpString = Integer.toString(ops[i] & 0xFF, 16);
                tmpString = tmpString.length() == 1? "0" + tmpString : tmpString;

                if (i != pc)
                    opsString.append(tmpString);
                else
                    opsString.append(" >>").append(tmpString).append("");

            }
            if (pc >= ops.length) opsString.append(" >>");
            if (opsString.length() > 0) opsString.insert(0, "\n ");

            logger.debug(" -- OPS --     {}", opsString);
            logger.debug(" -- STACK --   {}", stackData);
            logger.debug(" -- MEMORY --  {}", memoryData);
            logger.debug(" -- STORAGE -- {}\n", storageData);


        };
    }

}
