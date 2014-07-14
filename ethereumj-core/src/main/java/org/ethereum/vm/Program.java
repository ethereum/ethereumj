package org.ethereum.vm;

import org.ethereum.crypto.HashUtil;
import org.ethereum.db.ContractDetails;
import org.ethereum.db.Repository;
import org.ethereum.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
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

    Stack<DataWord> stack = new Stack<DataWord>();
    ByteBuffer memory = null;
    byte[] programAddress;

    ProgramResult result = new ProgramResult();

    byte[]   ops;
    int      pc = 0;
    byte     lastOp = 0;
    boolean  stopped = false;

    ProgramInvoke invokeData;

    public Program(byte[] ops, ProgramInvoke invokeData) {

    	this.invokeHash = invokeData.hashCode();
        result.setRepository(invokeData.getRepository());

        if (ops == null) throw new RuntimeException("program can not run with ops: null");

        this.invokeData = invokeData;
        this.ops = ops;
        this.programAddress = invokeData.getOwnerAddress().getNoLeadZeroesData();
    }

    public byte getCurrentOp() {
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

    public int getPC() {
        return pc;
    }

    public void setPC(DataWord pc) {
        this.pc = pc.value().intValue();

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

    public int getMemSize() {
        int memSize = 0;
        if (memory != null) memSize = memory.limit();
        return memSize;
    }

    public void memorySave(DataWord addrB, DataWord value) {
        memorySave(addrB.data, value.data);
    }

    public void memorySave(byte[] addr, byte[] value) {

        int address = new BigInteger(1, addr).intValue();
        allocateMemory(address, value);
        System.arraycopy(value, 0, memory.array(), address, value.length);
    }

    public DataWord memoryLoad(DataWord addr) {

        int address = new BigInteger(1, addr.getData()).intValue();
        allocateMemory(address, DataWord.ZERO.data);

        byte[] data = new byte[32];
        System.arraycopy(memory.array(), address,  data , 0  ,32);

        return new DataWord(data);
    }

    public ByteBuffer memoryChunk(DataWord offsetData, DataWord sizeData) {

        int offset = offsetData.value().intValue();
        int size   = sizeData.value().intValue();
        allocateMemory(offset, new byte[sizeData.intValue()]);

        byte[] chunk = new byte[size];

        if (memory != null) {
            if (memory.limit() < offset + size) size = memory.limit() - offset;
            System.arraycopy(memory.array(), offset, chunk, 0, size);
        }
        return ByteBuffer.wrap(chunk);
    }

    private void allocateMemory(int address, byte[] value) {

        int memSize = 0;
        if (memory != null) memSize = memory.limit();

        // check if you need to allocate
        if (memSize < (address + value.length)) {

            long overlap = memSize - address;

            int sizeToAllocate = 0;
            if (memSize > address) {
                sizeToAllocate = memSize + value.length;
            } else {
                sizeToAllocate = memSize + (address - memSize) + value.length;
            }

            if (overlap > 0) sizeToAllocate -= overlap;

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

    public void suicide(DataWord obtainer) {

        DataWord balance = getBalance(this.getOwnerAddress());
        // 1) pass full endowment to the obtainer
        if (logger.isInfoEnabled())
            logger.info("Transfer to: [ {} ] heritage: [ {} ]", Hex.toHexString(obtainer.getNoLeadZeroesData())
                        , balance.longValue());

        this.result.getRepository().addBalance(obtainer.getNoLeadZeroesData(), balance.value());

        // 2) mark the account as for delete
        result.addDeleteAccount(getOwnerAddress());
    }

    public void createContract(DataWord value, DataWord memStart, DataWord memSize) {

		if (invokeData.byTestingSuite()) {
            logger.info("[testing suite] - omit real create");
            return;
        }

        // [1] FETCH THE CODE FROM THE MEMORY
        ByteBuffer programCode = memoryChunk(memStart, memSize);

        byte[] senderAddress = this.getOwnerAddress().getNoLeadZeroesData();
        if (logger.isInfoEnabled())
            logger.info("creating a new contract inside contract run: [{}]", Hex.toHexString(senderAddress));

        //  actual gas subtract
        int gas = this.getGas().intValue();
        this.spendGas(gas, "internal call");

        // [2] CREATE THE CONTRACT ADDRESS
        byte[] nonce =  result.getRepository().getNonce(senderAddress).toByteArray();
        byte[] newAddress  = HashUtil.calcNewAddr(this.getOwnerAddress().getNoLeadZeroesData(), nonce);
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


        Repository trackRepository = result.getRepository().getTrack();
        trackRepository.startTracking();

        // [5] COOK THE INVOKE AND EXECUTE
        ProgramInvoke programInvoke =
                ProgramInvokeFactory.createProgramInvoke(this, new DataWord(newAddress), DataWord.ZERO,
                        new DataWord(gas), BigInteger.ZERO, null, trackRepository);

        VM vm = new VM();
        Program program = new Program(programCode.array(), programInvoke);
        vm.play(program);
        ProgramResult result = program.getResult();
        this.result.addDeleteAccounts(result.getDeleteAccounts());

        if (result.getException() != null &&
                result.getException() instanceof Program.OutOfGasException) {
            logger.info("contract run halted by OutOfGas: new contract init ={}" , Hex.toHexString(newAddress));

            trackRepository.rollback();
            stackPushZero();
            return;
        }

        // 4. CREATE THE CONTRACT OUT OF RETURN
        byte[] code    = result.getHReturn().array();
        trackRepository.saveCode(newAddress, code);

        // IN SUCCESS PUSH THE ADDRESS INTO THE STACK
        stackPush(new DataWord(newAddress));
        trackRepository.commit();

        // 5. REFUND THE REMAIN GAS
        int refundGas = gas - result.getGasUsed();
        if (refundGas > 0) {
            this.refundGas(refundGas, "remain gas from the internal call");
            if (logger.isInfoEnabled()){

                logger.info("The remain gas refunded, account: [ {} ], gas: [ {} ] ",
                        Hex.toHexString(this.getOwnerAddress().getNoLeadZeroesData()),
                        refundGas);
            }
        }
    }

    /**
     * That method implement internal calls
     * and code invocations
     *
     * @param gas - gas to pay for the call, remain gas will be refunded to the caller
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
        byte[] toAddress = toAddressDW.getNoLeadZeroesData();

        // FETCH THE CODE
        byte[] programCode = this.result.getRepository().getCode(toAddress);

        if (logger.isInfoEnabled())
            logger.info("calling for existing contract: address={}",
                    Hex.toHexString(toAddress));

        byte[] senderAddress = this.getOwnerAddress().getNoLeadZeroesData();

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
                    toAddressDW.getNoLeadZeroesData(),
                    gas.getNoLeadZeroesData(), endowmentValue.getNoLeadZeroesData());

            return;
        }

        //  actual gas subtract
        this.spendGas(gas.intValue(), "internal call");


        Repository trackRepository = result.getRepository().getTrack();
        trackRepository.startTracking();
        trackRepository.addBalance(toAddress, endowmentValue.value());

        ProgramInvoke programInvoke =
                ProgramInvokeFactory.createProgramInvoke(this, toAddressDW,
                        endowmentValue,  gas, result.getRepository().getBalance(toAddress),
                        data.array(),
                        trackRepository);

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

                trackRepository.rollback();
                stackPushZero();
                return;
        }

        // 3. APPLY RESULTS: result.getHReturn() into out_memory allocated
        if (result != null) {
            ByteBuffer buffer = result.getHReturn();
            if (buffer != null) {
                int retSize = buffer.array().length;
                int allocSize = outDataSize.intValue();
                if (retSize > allocSize) {
                    byte[] outArray = Arrays.copyOf(buffer.array(), allocSize);
                    this.memorySave(outArray, buffer.array());
                } else {
                    this.memorySave(outDataOffs.getData(), buffer.array());
                }
            }
        }

        // 4. THE FLAG OF SUCCESS IS ONE PUSHED INTO THE STACK
        trackRepository.commit();
        stackPushOne();

        // 5. REFUND THE REMAIN GAS
        if (result != null) {
            BigInteger refundGas = gas.value().subtract(BigInteger.valueOf(result.getGasUsed()));
            if (refundGas.compareTo(BigInteger.ZERO) == 1) {

                this.refundGas(refundGas.intValue(), "remain gas from the internal call");
                logger.info("The remain gas refunded, account: [ {} ], gas: [ {} ] ",
                        refundGas.toString(), refundGas.toString());
            }
        } else {

            this.refundGas(gas.intValue(), "remain gas from the internal call");
        }
    }

    public void spendGas(int gasValue, String cause) {
        gasLogger.info("[{}] Spent for cause: [ {} ], gas: [ {} ]", invokeHash, cause, gasValue);

        long afterSpend = invokeData.getGas().longValue() - gasValue - result.getGasUsed();
        if (afterSpend < 0)
            throw new OutOfGasException();
        result.spendGas(gasValue);
    }

    public void refundGas(int gasValue, String cause) {
        gasLogger.info("[{}] Refund for cause: [ {} ], gas: [ {} ]", invokeHash, cause, gasValue);
        result.refundGas(gasValue);
    }

    public void storageSave(DataWord word1, DataWord word2) {
        storageSave(word1.getData(), word2.getData());
    }

    public void storageSave(byte[] key, byte[] val) {
        DataWord keyWord = new DataWord(key);
        DataWord valWord = new DataWord(val);
        result.getRepository().addStorageRow(this.programAddress, keyWord, valWord);
    }

    public DataWord getOwnerAddress() {
        if (invokeData == null) return new DataWord( new byte[0]);
        return invokeData.getOwnerAddress();
    }

    public DataWord getBalance(DataWord address) {
        if (invokeData == null) return new DataWord( new byte[0]);

        BigInteger balance = result.getRepository().getBalance(address.getNoLeadZeroesData());
        DataWord balanceData = new DataWord(balance.toByteArray());

        return balanceData;
    }

    public DataWord getOriginAddress() {
        if (invokeData == null) return new DataWord( new byte[0]);
        return invokeData.getOriginAddress();
    }

    public DataWord getCallerAddress() {
        if (invokeData == null) return new DataWord( new byte[0]);
        return invokeData.getCallerAddress();
    }

    public DataWord getGasPrice() {
        if (invokeData == null) return new DataWord( new byte[0]);
        return invokeData.getMinGasPrice();
    }

    public DataWord getGas() {
        if (invokeData == null) return new DataWord( new byte[0]);
        long afterSpend = invokeData.getGas().longValue() - result.getGasUsed();
        return new DataWord(afterSpend);
    }

    public DataWord getCallValue() {
        if (invokeData == null) return new DataWord( new byte[0]);
        return invokeData.getCallValue();
    }

    public DataWord getDataSize() {
        if (invokeData == null) return new DataWord( new byte[0]);
        return invokeData.getDataSize();
    }

    public DataWord getDataValue(DataWord index) {
        if (invokeData == null) return new DataWord( new byte[0]);
        return invokeData.getDataValue(index);
    }

    public byte[] getDataCopy(DataWord offset, DataWord length) {
        if (invokeData == null) return new byte[0];
        return invokeData.getDataCopy(offset, length);
    }

    public DataWord storageLoad(DataWord key) {
        return result.getRepository().getStorageValue(this.programAddress, key);
    }

    public DataWord getPrevHash() {
       return invokeData.getPrevHash();
    }

    public DataWord getCoinbase() {
        return invokeData.getCoinbase();
    }

    public DataWord getTimestamp() {
        return  invokeData.getTimestamp();
    }

    public DataWord getNumber() {
        return invokeData.getNumber();
    }

    public DataWord getDifficulty() {
        return  invokeData.getDifficulty();
    }

    public DataWord getGaslimit() {
        return invokeData.getGaslimit();
    }


    public ProgramResult getResult() {
        return result;
    }

    public void setRuntimeFailure(RuntimeException e) {
        result.setException(e);
    }

    public void fullTrace() {

        if (logger.isDebugEnabled() || listener != null) {

            StringBuilder stackData = new StringBuilder();
            for (int i = 0; i < stack.size(); ++i) {
                stackData.append(" ").append(stack.get(i));
                if (i < stack.size() - 1) stackData.append("\n");
            }
            if (stackData.length() > 0) stackData.insert(0, "\n");

            ContractDetails contractDetails = this.result.getRepository().getContractDetails(this.programAddress);
            StringBuilder storageData = new StringBuilder();
            for (DataWord key : contractDetails.getStorage().keySet()) {
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

            logger.debug(" -- OPS --     {}", opsString);
            logger.debug(" -- STACK --   {}", stackData);
            logger.debug(" -- MEMORY --  {}", memoryData);
            logger.debug(" -- STORAGE -- {}\n", storageData);
            logger.debug("\n  Spent Gas: [ {} ]/[ {} ]\n  Left Gas:  [ {} ]\n",
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

	public class OutOfGasException extends RuntimeException {
    }
}
