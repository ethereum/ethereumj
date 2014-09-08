package org.ethereum.vm;

import org.ethereum.crypto.HashUtil;
import org.ethereum.db.ContractDetails;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 01/06/2014 10:45
 */
public class Program {
	
    private Logger logger = LoggerFactory.getLogger("VM");
    private Logger gasLogger = LoggerFactory.getLogger("gas");
    private int invokeHash;
    private ProgramListener listener;

    Stack<DataWord> stack = new Stack<>();
    ByteBuffer memory = null;
    DataWord programAddress;

    ProgramResult result = new ProgramResult();

    byte[]   ops;
    int      pc = 0;
    byte     lastOp = 0;
    boolean  stopped = false;

    ProgramInvoke invokeData;

    public Program(byte[] ops, ProgramInvoke invokeData) {
    	
        if (ops == null) ops = ByteUtil.EMPTY_BYTE_ARRAY;
        this.ops = ops;
        
        if (invokeData != null) {
	        this.invokeData = invokeData;
	        this.programAddress = invokeData.getOwnerAddress();
	    	this.invokeHash = invokeData.hashCode();
	        this.result.setRepository(invokeData.getRepository());
        }
     }

    public byte getCurrentOp() {
    	if(ops.length == 0)
    		return 0;
        return ops[pc];
    }

    public void setLastOp(byte op) {
        this.lastOp = op;
    }

    public void stackPush(byte[] data) {
        DataWord stackWord = new DataWord(data);
        stack.push(stackWord);
    }

    public void stackPushZero() {
        DataWord stackWord = new DataWord(0);
        stack.push(stackWord);
    }

    public void stackPushOne() {
        DataWord stackWord = new DataWord(1);
        stack.push(stackWord);
    }

    public void stackPush(DataWord stackWord) {
        stack.push(stackWord);
    }
    
    public Stack<DataWord> getStack() {
    	return this.stack;
    }

    public int getPC() {
        return pc;
    }

    public void setPC(DataWord pc) {

        this.pc = pc.intValue();

        if (this.pc == ops.length) {
            stop();
        }
        
        if (this.pc > ops.length) {
            stop();
            throw new RuntimeException("pc overflow pc=" + pc);
        }
    }

    public void setPC(int pc) {
        this.pc = pc;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void stop() {
        stopped = true;
    }

    public void setHReturn(ByteBuffer buff) {
        result.setHReturn(buff.array());
    }

    public void step() {
        ++pc;
        if (pc >= ops.length) stop();
    }

    public byte[] sweep(int n) {

        if (pc + n > ops.length) {
            stop();
            throw new RuntimeException("pc overflow sweep n: " + n + " pc: " + pc);
        }

        byte[] data = Arrays.copyOfRange(ops, pc, pc + n);
        pc += n;
        if (pc >= ops.length) stop();

        return data;
    }

    public DataWord stackPop() {
        if (stack.size() == 0) {
            stop();
            throw new RuntimeException("attempted pull action for empty stack");
        }
        return stack.pop();
    }
    
    public void require(int stackSize) {
    	if(stack.size() != stackSize) {
            stop();
            throw new RuntimeException("stack too small");
    	}
    }

    public int getMemSize() {
        int memSize = 0;
        if (memory != null) memSize = memory.limit();
        return memSize;
    }

    public void memorySave(DataWord addrB, DataWord value) {
        memorySave(addrB.intValue(), value.getData());
    }

    public void memorySave(int addr, byte[] value) {
    	memorySave(addr, value.length, value);
    }

    /**
     * Allocates a piece of memory and stores value at given offset address
     * 
     * @param addr is the offset address
     * @param allocSize size of memory needed to write
     * @param value the data to write to memory
     */
    public void memorySave(int addr, int allocSize, byte[] value) {

        allocateMemory(addr, allocSize);
        System.arraycopy(value, 0, memory.array(), addr, value.length);
    }
    
    public DataWord memoryLoad(DataWord addr) {
    	return memoryLoad(addr.intValue());
    }
    
    public DataWord memoryLoad(int address) {

        allocateMemory(address, DataWord.ZERO.getData().length);

        DataWord newMem = new DataWord();
        System.arraycopy(memory.array(), address, newMem.getData(), 0, newMem.getData().length);

        return newMem;
    }

    public ByteBuffer memoryChunk(DataWord offsetData, DataWord sizeData) {
    	return memoryChunk(offsetData.intValue(), sizeData.intValue());
    }

    /**
     * Returns a piece of memory from a given offset and specified size
     * If the offset + size exceed the current memory-size,
     * the remainder will be filled with empty bytes.
     *
     * @param offset byte address in memory
     * @param size the amount of bytes to return
     * @return ByteBuffer containing the chunk of memory data
     */
    public ByteBuffer memoryChunk(int offset, int size) {

        allocateMemory(offset, size);
        byte[] chunk;
        if (memory != null)
        	chunk = Arrays.copyOfRange(memory.array(), offset, offset+size);
        else
        	chunk = new byte[size];
        return ByteBuffer.wrap(chunk);
    }

    /**
     * Allocates extra memory in the program for
     *  a specified size, calculated from a given offset
     * 
     * @param offset the memory address offset
     * @param size the number of bytes to allocate
     */
    protected void allocateMemory(int offset, int size) {

        int memSize = memory != null ? memory.limit(): 0;
        double newMemSize = Math.max(memSize, Math.ceil((double)(offset + size) / 32) * 32);
        ByteBuffer tmpMem = ByteBuffer.allocate((int)newMemSize);
        if (memory != null)
        	tmpMem.put(memory.array(), 0, memory.limit());
        memory = tmpMem;
    }

    public void suicide(DataWord obtainer) {

        DataWord balance = getBalance(this.getOwnerAddress());
        // 1) pass full endowment to the obtainer
        if (logger.isInfoEnabled())
			logger.info("Transfer to: [ {} ] heritage: [ {} ]",
					Hex.toHexString(obtainer.getLast20Bytes()),
					balance.longValue());

        this.result.getRepository().addBalance(obtainer.getLast20Bytes(), balance.value());
        this.result.getRepository().addBalance(this.getOwnerAddress().getLast20Bytes(), balance.value().negate());

        // 2) mark the account as for delete
        result.addDeleteAccount(this.getOwnerAddress());
    }

    public void createContract(DataWord value, DataWord memStart, DataWord memSize) {

		if (invokeData.byTestingSuite()) {
            logger.info("[testing suite] - omit real create");
            return;
        }

        // [1] FETCH THE CODE FROM THE MEMORY
        ByteBuffer programCode = memoryChunk(memStart, memSize);

        byte[] senderAddress = this.getOwnerAddress().getLast20Bytes();
        if (logger.isInfoEnabled())
            logger.info("creating a new contract inside contract run: [{}]", Hex.toHexString(senderAddress));

        //  actual gas subtract
        int gas = this.getGas().intValue();
        this.spendGas(gas, "internal call");

        // [2] CREATE THE CONTRACT ADDRESS
        byte[] nonce =  result.getRepository().getNonce(senderAddress).toByteArray();
        byte[] newAddress  = HashUtil.calcNewAddr(this.getOwnerAddress().getLast20Bytes(), nonce);
        result.getRepository().createAccount(newAddress);

        // [3] UPDATE THE NONCE
        // (THIS STAGE IS NOT REVERTED BY ANY EXCEPTION)
        result.getRepository().increaseNonce(senderAddress);

        // [4] TRANSFER THE BALANCE
        BigInteger endowment = value.value();
        BigInteger senderBalance = result.getRepository().getBalance(senderAddress);
        if (senderBalance.compareTo(endowment) < 0) {
            stackPushZero();
            return;
        }
        result.getRepository().addBalance(senderAddress, endowment.negate());
        result.getRepository().addBalance(newAddress, endowment);

        RepositoryImpl trackRepositoryImpl = result.getRepository().getTrack();
        trackRepositoryImpl.startTracking();

        // [5] COOK THE INVOKE AND EXECUTE
        ProgramInvoke programInvoke =
                ProgramInvokeFactory.createProgramInvoke(this, new DataWord(newAddress), DataWord.ZERO,
                        new DataWord(gas), BigInteger.ZERO, null, trackRepositoryImpl, this.invokeData.getCallDeep() + 1);

        VM vm = new VM();
        Program program = new Program(programCode.array(), programInvoke);
        vm.play(program);
        ProgramResult result = program.getResult();
        this.result.addDeleteAccounts(result.getDeleteAccounts());

        if (result.getException() != null &&
                result.getException() instanceof Program.OutOfGasException) {
            logger.info("contract run halted by OutOfGas: new contract init ={}" , Hex.toHexString(newAddress));

            trackRepositoryImpl.rollback();
            stackPushZero();
            return;
        }

        // 4. CREATE THE CONTRACT OUT OF RETURN
        byte[] code    = result.getHReturn().array();
        trackRepositoryImpl.saveCode(newAddress, code);

        // IN SUCCESS PUSH THE ADDRESS INTO THE STACK
        stackPush(new DataWord(newAddress));
        trackRepositoryImpl.commit();

        // 5. REFUND THE REMAIN GAS
        long refundGas = gas - result.getGasUsed();
        if (refundGas > 0) {
            this.refundGas(refundGas, "remain gas from the internal call");
            if (logger.isInfoEnabled()){

                logger.info("The remaining gas is refunded, account: [ {} ], gas: [ {} ] ",
                        Hex.toHexString(this.getOwnerAddress().getLast20Bytes()),
                        refundGas);
            }
        }
    }

    /**
     * That method implement internal calls
     * and code invocations
     *
     * @param gas - gas to pay for the call, remaining gas will be refunded to the caller
     * @param toAddressDW - address to call
     * @param endowmentValue - the value that can be transfer along with the code execution
     * @param inDataOffs - start of memory to be input data to the call
     * @param inDataSize - size of memory to be input data to the call
     * @param outDataOffs - start of memory to be output of the call
     * @param outDataSize - size of memory to be output data to the call
     */
    public void callToAddress(DataWord gas, DataWord toAddressDW, DataWord endowmentValue,
                              DataWord inDataOffs, DataWord inDataSize,DataWord outDataOffs, DataWord outDataSize) {

        ByteBuffer data = memoryChunk(inDataOffs, inDataSize);

        // FETCH THE SAVED STORAGE
        byte[] toAddress = toAddressDW.getLast20Bytes();

        // FETCH THE CODE
        byte[] programCode = this.result.getRepository().getCode(toAddress);

        if (logger.isInfoEnabled())
            logger.info("calling for existing contract: address: [ {} ], outDataOffs: [ {} ], outDataSize: [ {} ]  ",
                    Hex.toHexString(toAddress), outDataOffs.longValue(), outDataSize.longValue());

        byte[] senderAddress = this.getOwnerAddress().getLast20Bytes();

        // 2.1 PERFORM THE GAS VALUE TX
        // (THIS STAGE IS NOT REVERTED BY ANY EXCEPTION)
        if (this.getGas().longValue() - gas.longValue() < 0 ) {
            logger.info("No gas for the internal call, \n" +
                    "fromAddress={}, toAddress={}",
                    Hex.toHexString(senderAddress), Hex.toHexString(toAddress));
            this.stackPushZero();
            return;
        }

        BigInteger endowment = endowmentValue.value();
        BigInteger senderBalance = result.getRepository().getBalance(senderAddress);
        if (senderBalance.compareTo(endowment) < 0) {
            stackPushZero();
            return;
        }
        result.getRepository().addBalance(senderAddress, endowment.negate());

        if (invokeData.byTestingSuite()) {
            logger.info("[testing suite] - omit real call");

            stackPushOne();

            this.getResult().addCallCreate(data.array(),
                    toAddressDW.getLast20Bytes(),
                    gas.getNoLeadZeroesData(), endowmentValue.getNoLeadZeroesData());

            return;
        }

        //  actual gas subtract
        this.spendGas(gas.intValue(), "internal call");

        RepositoryImpl trackRepositoryImpl = result.getRepository().getTrack();
        trackRepositoryImpl.startTracking();
        trackRepositoryImpl.addBalance(toAddress, endowmentValue.value());

        ProgramInvoke programInvoke =
                ProgramInvokeFactory.createProgramInvoke(this, toAddressDW,
                        endowmentValue,  gas, result.getRepository().getBalance(toAddress),
                        data.array(),
                        trackRepositoryImpl, this.invokeData.getCallDeep() + 1);

        ProgramResult result = null;

        if (programCode != null && programCode.length != 0) {
            VM vm = new VM();
            Program program = new Program(programCode, programInvoke);
            vm.play(program);
            result = program.getResult();
            this.result.addDeleteAccounts(result.getDeleteAccounts());
        }

        if (result != null &&
            result.getException() != null &&
            result.getException() instanceof Program.OutOfGasException) {
                logger.info("contract run halted by OutOfGas: contract={}" , Hex.toHexString(toAddress));

                trackRepositoryImpl.rollback();
                stackPushZero();
                return;
        }

        // 3. APPLY RESULTS: result.getHReturn() into out_memory allocated
        if (result != null) {
            ByteBuffer buffer = result.getHReturn();
            int allocSize = outDataSize.intValue();
            if (buffer != null && allocSize > 0) {
                int retSize = buffer.limit();
                int offset = outDataOffs.intValue();
                if (retSize > allocSize) {
                    this.memorySave(offset, buffer.array());
                } else {
                    this.memorySave(offset, allocSize, buffer.array());
                }
            }
        }

        // 4. THE FLAG OF SUCCESS IS ONE PUSHED INTO THE STACK
        trackRepositoryImpl.commit();
        stackPushOne();

        // 5. REFUND THE REMAIN GAS
        if (result != null) {
            BigInteger refundGas = gas.value().subtract(BigInteger.valueOf(result.getGasUsed()));
            if (refundGas.compareTo(BigInteger.ZERO) == 1) {

                this.refundGas(refundGas.intValue(), "remaining gas from the internal call");
                logger.info("The remaining gas refunded, account: [ {} ], gas: [ {} ] ",
                		Hex.toHexString(senderAddress), refundGas.toString());
            }
        } else {
            this.refundGas(gas.intValue(), "remaining gas from the internal call");
        }
    }

    public void spendGas(long gasValue, String cause) {
        gasLogger.info("[{}] Spent for cause: [ {} ], gas: [ {} ]", invokeHash, cause, gasValue);

        long afterSpend = invokeData.getGas().longValue() - gasValue - result.getGasUsed();
        if (afterSpend < 0)
            throw new OutOfGasException();
        result.spendGas(gasValue);
    }

    public void refundGas(long gasValue, String cause) {
        gasLogger.info("[{}] Refund for cause: [ {} ], gas: [ {} ]", invokeHash, cause, gasValue);
        result.refundGas(gasValue);
    }

    public void storageSave(DataWord word1, DataWord word2) {
        storageSave(word1.getData(), word2.getData());
    }

    public void storageSave(byte[] key, byte[] val) {
        DataWord keyWord = new DataWord(key);
        DataWord valWord = new DataWord(val);
        result.getRepository().addStorageRow(this.programAddress.getLast20Bytes(), keyWord, valWord);
    }

    public DataWord getOwnerAddress() {
        if (invokeData == null) return DataWord.ZERO_EMPTY_ARRAY;
        return this.programAddress.clone();
    }

    public DataWord getBalance(DataWord address) {
        if (invokeData == null) return DataWord.ZERO_EMPTY_ARRAY;

        BigInteger balance = result.getRepository().getBalance(address.getLast20Bytes());
        DataWord balanceData = new DataWord(balance.toByteArray());

        return balanceData;
    }

    public DataWord getOriginAddress() {
        if (invokeData == null) return DataWord.ZERO_EMPTY_ARRAY;
        return invokeData.getOriginAddress().clone();
    }

    public DataWord getCallerAddress() {
        if (invokeData == null) return DataWord.ZERO_EMPTY_ARRAY;
        return invokeData.getCallerAddress().clone();
    }

    public DataWord getGasPrice() {
        if (invokeData == null) return DataWord.ZERO_EMPTY_ARRAY;
        return invokeData.getMinGasPrice().clone();
    }

    public DataWord getGas() {
        if (invokeData == null) return DataWord.ZERO_EMPTY_ARRAY;
        long afterSpend = invokeData.getGas().longValue() - result.getGasUsed();
        return new DataWord(afterSpend);
    }

    public DataWord getCallValue() {
        if (invokeData == null) return DataWord.ZERO_EMPTY_ARRAY;
        return invokeData.getCallValue().clone();
    }

    public DataWord getDataSize() {
        if (invokeData == null) return DataWord.ZERO_EMPTY_ARRAY;
        return invokeData.getDataSize().clone();
    }

    public DataWord getDataValue(DataWord index) {
        if (invokeData == null) return DataWord.ZERO_EMPTY_ARRAY;
        return invokeData.getDataValue(index);
    }

    public byte[] getDataCopy(DataWord offset, DataWord length) {
        if (invokeData == null) return ByteUtil.EMPTY_BYTE_ARRAY;
        return invokeData.getDataCopy(offset, length);
    }

    public DataWord storageLoad(DataWord key) {
        return result.getRepository().getStorageValue(this.programAddress.getLast20Bytes(), key);
    }

    public DataWord getPrevHash() {
       return invokeData.getPrevHash().clone();
    }

    public DataWord getCoinbase() {
        return invokeData.getCoinbase().clone();
    }

    public DataWord getTimestamp() {
        return  invokeData.getTimestamp().clone();
    }

    public DataWord getNumber() {
        return invokeData.getNumber().clone();
    }

    public DataWord getDifficulty() {
        return  invokeData.getDifficulty().clone();
    }

    public DataWord getGaslimit() {
        return invokeData.getGaslimit().clone();
    }

    public ProgramResult getResult() {
        return result;
    }

    public void setRuntimeFailure(RuntimeException e) {
        result.setException(e);
    }
    
    public String memoryToString() {
        StringBuilder memoryData = new StringBuilder();
        StringBuilder firstLine = new StringBuilder();
        StringBuilder secondLine = new StringBuilder();
        for (int i = 0; memory != null && i < memory.limit(); ++i) {

            byte value = memory.get(i);
            // Check if value is ASCII 
            // (should be until 0x7e - but using 0x7f 
            // to be compatible with cpp-ethereum)
            // See: https://github.com/ethereum/cpp-ethereum/issues/299
            String character = 	((byte)0x20 <= value && value <= (byte)0x7f) ? new String(new byte[]{value}) : "?";
            firstLine.append(character).append("");
            secondLine.append(Utils.oneByteToHexString(value)).append(" ");

            if ((i + 1) % 8 == 0) {
                String tmp = String.format("%4s", Integer.toString(i - 7, 16)).replace(" ", "0");
                memoryData.append("").append(tmp).append(" ");
                memoryData.append(firstLine).append(" ");
                memoryData.append(secondLine);
                if (i+1 < memory.limit()) memoryData.append("\n");
                firstLine.setLength(0);
                secondLine.setLength(0);
            }
        }
        return memoryData.toString();
    }

    public void fullTrace() {

        if (logger.isTraceEnabled() || listener != null) {

            StringBuilder stackData = new StringBuilder();
            for (int i = 0; i < stack.size(); ++i) {
                stackData.append(" ").append(stack.get(i));
                if (i < stack.size() - 1) stackData.append("\n");
            }
            if (stackData.length() > 0) stackData.insert(0, "\n");

            ContractDetails contractDetails = this.result.getRepository().
                    getContractDetails(this.programAddress.getLast20Bytes());
            StringBuilder storageData = new StringBuilder();
            List<DataWord> storageKeys = new ArrayList<>(contractDetails.getStorage().keySet());
    		Collections.sort((List<DataWord>) storageKeys);
            for (DataWord key : storageKeys) {
                storageData.append(" ").append(key).append(" -> ").
                        append(contractDetails.getStorage().get(key)).append("\n");
            }
            if (storageData.length() > 0) storageData.insert(0, "\n");

            StringBuilder memoryData = new StringBuilder();
            StringBuilder oneLine = new StringBuilder();
            for (int i = 0; memory != null && i < memory.limit(); ++i) {

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
            for (int i = 0; i < ops.length; ++i) {

                String tmpString = Integer.toString(ops[i] & 0xFF, 16);
                tmpString = tmpString.length() == 1? "0" + tmpString : tmpString;

                if (i != pc)
                    opsString.append(tmpString);
                else
                    opsString.append(" >>").append(tmpString).append("");

            }
            if (pc >= ops.length) opsString.append(" >>");
            if (opsString.length() > 0) opsString.insert(0, "\n ");

            logger.trace(" -- OPS --     {}", opsString);
            logger.trace(" -- STACK --   {}", stackData);
            logger.trace(" -- MEMORY --  {}", memoryData);
            logger.trace(" -- STORAGE -- {}\n", storageData);
            logger.trace("\n  Spent Gas: [ {} ]/[ {} ]\n  Left Gas:  [ {} ]\n",
                    result.getGasUsed(),
                    invokeData.getGas().longValue(),
                    getGas().longValue());

            StringBuilder globalOutput = new StringBuilder("\n");
            if (stackData.length() > 0) stackData.append("\n");

            if (pc != 0)
                globalOutput.append("[Op: ").append(OpCode.code(lastOp).name()).append("]\n");

            globalOutput.append(" -- OPS --     ").append(opsString).append("\n");
            globalOutput.append(" -- STACK --   ").append(stackData).append("\n");
            globalOutput.append(" -- MEMORY --  ").append(memoryData).append("\n");
            globalOutput.append(" -- STORAGE -- ").append(storageData).append("\n");

            if (result.getHReturn() != null) {
                globalOutput.append("\n  HReturn: ").append(Hex.toHexString(result.getHReturn().array()));
            }

            // soffisticated assumption that msg.data != codedata
            // means we are calling the contract not creating it
            byte[] txData = invokeData.getDataCopy(DataWord.ZERO, getDataSize());
            if (!Arrays.equals(txData, ops)) {
                globalOutput.append("\n  msg.data: ").append(Hex.toHexString( txData ));
            }
            globalOutput.append("\n\n  Spent Gas: ").append(result.getGasUsed());

			if (listener != null) {
				listener.output(globalOutput.toString());
			}
        }
    }

	public void addListener(ProgramListener listener) {
		this.listener = listener;
	}

	public interface ProgramListener {
		public void output(String out);
	}

	@SuppressWarnings("serial")
	public class OutOfGasException extends RuntimeException {
    }
}