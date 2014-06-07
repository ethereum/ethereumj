package org.ethereum.vm;

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
    ProgramListener listener;

    Stack<DataWord> stack = new Stack<DataWord>();
    Map<DataWord, DataWord> storage = new HashMap<DataWord, DataWord>();
    ByteBuffer memory = null;

    ProgramResult result = new ProgramResult();

    byte[]   ops;
    int      pc = 0;
    boolean  stopped = false;

    ProgramInvoke invokeData;

    Map<byte[], DataWord> addressChange;
    int      spendGas = 0;


    public Program(byte[] ops, ProgramInvoke invokeData) {

        if (ops == null)        throw new RuntimeException("program can not run with ops: null");

        this.invokeData = invokeData;
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

        if (this.pc > ops.length) {
            stop();
            throw new RuntimeException("pc overflow pc: " + pc);
        }
    }

    public void setPC(int pc) {
        this.pc = pc;
    }

    public boolean isStopped(){
        return stopped;
    }

    public void stop(){
        stopped = true;
    }

    public void setHReturn(ByteBuffer buff){
        result.setHReturn(buff.array());
    }

    public void step(){
        ++pc;
        if (pc >= ops.length) stop();
    }

    public byte[] sweep(int n){

        if (pc + n > ops.length) {
            stop();
            throw new RuntimeException("pc overflow sweep n: " + n + " pc: " + pc);
        }

        byte[] data = Arrays.copyOfRange(ops, pc, pc + n);
        pc += n;
        if (pc >= ops.length) stop();

        return data;
    }

    public DataWord stackPop(){

        if (stack.size() == 0){
            stop();
            throw new RuntimeException("attempted pull action for empty stack");
        }
        return stack.pop();
    };

    public int getMemSize(){

        int memSize = 0;
        if (memory != null) memSize = memory.limit();
        return memSize;
    }

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

    public ByteBuffer memoryChunk(DataWord offsetData, DataWord sizeData){

        int offset = offsetData.value().intValue();
        int size   = sizeData.value().intValue();

        byte[] chunk = new byte[size];

        if (memory.limit() < offset + size) size = memory.limit() - offset;

        System.arraycopy(memory.array(), offset, chunk, 0, size);

        return ByteBuffer.wrap(chunk);
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

    public void sendToAddress(byte[] addr, DataWord bChange ){

        DataWord currentBChange = addressChange.get(addr);
        if (currentBChange == null){
            addressChange.put(addr, bChange);
        } else {
            currentBChange.add(bChange);
        }
    }


    public void spendGas(int gasValue){
        // todo: check it against avail gas
        // todo: out of gas will revert the changes [YP 5, 6 ]
        spendGas += gasValue;
    }

    public void storageSave(DataWord word1, DataWord word2){
        storageSave(word1.getData(), word2.getData());
    }

    public void storageSave(byte[] key, byte[] val){
        DataWord keyWord = new DataWord(key);
        DataWord valWord = new DataWord(val);
        storage.put(keyWord, valWord);
    }

    public DataWord getOwnerAddress(){
        if (invokeData == null) return new DataWord( new byte[0]);
        return invokeData.getOwnerAddress();
    }

    public DataWord getBalance(){
        if (invokeData == null) return new DataWord( new byte[0]);
        return invokeData.getBalance();
    }

    public DataWord getOriginAddress(){
        if (invokeData == null) return new DataWord( new byte[0]);
        return invokeData.getOriginAddress();
    }

    public DataWord getCallerAddress(){
        if (invokeData == null) return new DataWord( new byte[0]);
        return invokeData.getCallerAddress();
    }

    public DataWord getMinGasPrice(){
        if (invokeData == null) return new DataWord( new byte[0]);
        return invokeData.getMinGasPrice();
    }

    public DataWord getCallValue(){
        if (invokeData == null) return new DataWord( new byte[0]);
        return invokeData.getCallValue();
    }

    public DataWord getDataSize(){
        if (invokeData == null) return new DataWord( new byte[0]);
        return invokeData.getDataSize();
    }

    public DataWord getDataValue(DataWord index){
        if (invokeData == null) return new DataWord( new byte[0]);
        return invokeData.getDataValue(index);
    }

    public byte[] getDataCopy(DataWord offset, DataWord length){
        if (invokeData == null) return new byte[0];
        return invokeData.getDataCopy(offset, length);
    }

    public DataWord storageLoad(DataWord key){
        return storage.get(key);
    }


    public ProgramResult getResult() {
        return result;
    }

    public void setRuntimeFailure(RuntimeException e){
        result.setException(e);
    }

    public void fullTrace(){

        // todo: add gas to full trace calc

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


            StringBuilder global = new StringBuilder("\n");
            if (stackData.length() > 0) stackData.append("\n");

            global.append(" -- OPS --     ").append(opsString).append("\n");
            global.append(" -- STACK --   ").append(stackData).append("\n");
            global.append(" -- MEMORY --  ").append(memoryData).append("\n");
            global.append(" -- STORAGE -- ").append(storageData).append("\n");

            if (result.gethReturn() != null){
                global.append("\n  HReturn: ").append(Hex.toHexString(result.gethReturn().array()));
            }

            if (listener != null){
                listener.output(global.toString());
            }

        };
    }



    public void addListener(ProgramListener listener){
        this.listener = listener;
    }

    public interface ProgramListener{
        public void output(String out);
    }
}
