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
    int   pc = 0;

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

    public void memorySave(byte[] addrB, byte[] value){

        BigInteger address = new BigInteger(1, addrB);

        int memSize = 0;
        if (memory != null) memSize = memory.limit();

        if (memSize < address.intValue()){

            int sizeToAllocate = address.intValue() + (32 - address.intValue() % 32);
            ByteBuffer tmpMem = ByteBuffer.allocate(sizeToAllocate);
            if (memory != null) tmpMem.put(memory);
            memory = tmpMem;
        }
        System.arraycopy(value, 0, memory.array(), address.intValue(), value.length);
    }


    public void memoryLoad(){};

    public void storageSave(byte[] key, byte[] val){

        DataWord keyWord = new DataWord(key);
        DataWord valWord = new DataWord(val);
        storage.put(keyWord, valWord);
    }


    public void storageLoad(){}


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

                    String tmp = String.format(" [%4s]-[%4s]", Integer.toString(i, 16),
                            Integer.toString(i - 15, 16)).replace(" ", "0");
                    memoryData.append(tmp).append(" ");
                    memoryData.append(oneLine.reverse());
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
