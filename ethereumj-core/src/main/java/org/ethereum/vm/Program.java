package org.ethereum.vm;

import org.ethereum.crypto.HashUtil;
import org.ethereum.db.ContractDetails;
import org.ethereum.facade.Repository;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.MessageCall.MsgType;
import org.ethereum.vm.PrecompiledContracts.PrecompiledContract;
import org.ethereum.vmtrace.ProgramTrace;
import org.ethereum.vmtrace.ProgramTraceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

import static java.lang.String.format;
import static java.lang.System.getProperty;
import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.util.BIUtil.*;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * @author Roman Mandeleil
 * @since 01.06.2014
 */
public class Program {

    private static final Logger logger = LoggerFactory.getLogger("VM");
    private static final Logger gasLogger = LoggerFactory.getLogger("gas");

    /**
     * This attribute defines the number of recursive calls allowed in the EVM
     * Note: For the JVM to reach this level without a StackOverflow exception,
     * ethereumj may need to be started with a JVM argument to increase
     * the stack size. For example: -Xss10m
     */
    private static final int MAX_DEPTH = 1024;

    //Max size for stack checks
    private static final int MAX_STACKSIZE = 1024;

    ProgramInvokeFactory programInvokeFactory = new ProgramInvokeFactoryImpl();

    private int invokeHash;
    private ProgramListener listener;
    private ProgramTraceListener traceListener = new ProgramTraceListener();

    Stack stack = new Stack();
    Memory memory = new Memory();
    DataWord programAddress;

    ProgramResult result = new ProgramResult();
    ProgramTrace programTrace = new ProgramTrace();

    byte[] ops;
    int pc = 0;
    byte lastOp = 0;
    byte previouslyExecutedOp = 0;
    boolean stopped = false;

    private Set<Integer> jumpdest = new HashSet<>();

    ProgramInvoke invokeData;

    public Program(byte[] ops, ProgramInvoke invokeData) {
        setupTraceListener(this.memory);
        setupTraceListener(this.stack);

        this.ops = (ops == null) ? EMPTY_BYTE_ARRAY : ops;

        if (invokeData != null) {
            this.programAddress = invokeData.getOwnerAddress();
            this.invokeData = invokeData;
            this.invokeHash = invokeData.hashCode();
            
            Repository repository = invokeData.getRepository();
            this.result.setRepository(setupTraceListener(new Storage(this.programAddress, repository)));
            this.programTrace.initStorage(repository.getContractDetails(this.programAddress.getLast20Bytes()));
            
            precompile();
        }
    }
    
    private <T extends ProgramTraceListenerAware> T setupTraceListener(T traceListenerAware) {
        traceListenerAware.setTraceListener(traceListener);
        return traceListenerAware;
    }

    public byte getOp(int pc) {
        if (ops.length <= pc)
            return 0;
        return ops[pc];
    }

    public byte getCurrentOp() {
        if (ops.length == 0)
            return 0;
        return ops[pc];
    }

    /**
     * Last Op can only be set publicly (no getLastOp method), is used for logging.
     */
    public void setLastOp(byte op) {
        this.lastOp = op;
    }

    /**
     * Should be set only after the OP is fully executed.
     */
    public void setPreviouslyExecutedOp(byte op) {
        this.previouslyExecutedOp = op;
    }

    /**
     * Returns the last fully executed OP.
     */
    public byte getPreviouslyExecutedOp() {
        return this.previouslyExecutedOp;
    }

    public void stackPush(byte[] data) {
        DataWord stackWord = new DataWord(data);
        stackPush(stackWord);
    }

    public void stackPushZero() {
        DataWord stackWord = new DataWord(0);
        stackPush(stackWord);
    }

    public void stackPushOne() {
        DataWord stackWord = new DataWord(1);
        stackPush(stackWord);
    }

    public void stackPush(DataWord stackWord) {
        stackMax(0, 1); //Sanity Check
        stack.push(stackWord);
    }

    public Stack getStack() {
        return this.stack;
    }

    public int getPC() {
        return pc;
    }

    public void setPC(DataWord pc) {
        this.setPC(pc.intValue());
    }

    public void setPC(int pc) {
        this.pc = pc;

        if (this.pc >= ops.length)
            stop();
    }

    public boolean isStopped() {
        return stopped;
    }

    public void stop() {
        stopped = true;
    }

    public void setHReturn(byte[] buff) {
        result.setHReturn(buff);
    }

    public void step() {
        ++pc;
        if (pc >= ops.length) stop();
    }

    public byte[] sweep(int n) {

        if (pc + n > ops.length)
            stop();

        byte[] data = Arrays.copyOfRange(ops, pc, pc + n);
        pc += n;
        if (pc >= ops.length) stop();

        return data;
    }

    public DataWord stackPop() {
        return stack.pop();
    }

    /**
     * Verifies that the stack is at least <code>stackSize</code>
     *
     * @param stackSize int
     * @throws StackTooSmallException If the stack is
     *                                smaller than <code>stackSize</code>
     */
    public void stackRequire(int stackSize) {
        if (stack.size() < stackSize) {
            throw Program.Exception.tooSmallStack(stackSize, stack.size());
        }
    }

    public void stackMax(int argsReqs, int returnReqs) {
        if ((stack.size() - argsReqs + returnReqs) > MAX_STACKSIZE) {
            throw new StackTooLargeException("Expected: overflow 1024 elements stack limit");
        }
    }

    public int getMemSize() {
        return memory.size();
    }

    public void memorySave(DataWord addrB, DataWord value) {
        memory.write(addrB.intValue(), value.getData());
    }

    public void memorySave(int addr, byte[] value) {
        memory.write(addr, value);
    }

    public void memoryExpand(DataWord outDataOffs, DataWord outDataSize) {

        if (outDataSize.isZero())
            return;

        memory.extend(outDataOffs.intValue(), outDataSize.intValue());
    }

    /**
     * Allocates a piece of memory and stores value at given offset address
     *
     * @param addr      is the offset address
     * @param allocSize size of memory needed to write
     * @param value     the data to write to memory
     */
    public void memorySave(int addr, int allocSize, byte[] value) {
        memory.extendAndWrite(addr, allocSize, value);
    }

    public DataWord memoryLoad(DataWord addr) {
        return memory.readWord(addr.intValue());
    }

    public DataWord memoryLoad(int address) {

        return memory.readWord(address);
    }

    public byte[] memoryChunk(int offset, int size) {
        return memory.read(offset, size);
    }

    /**
     * Allocates extra memory in the program for
     * a specified size, calculated from a given offset
     *
     * @param offset the memory address offset
     * @param size   the number of bytes to allocate
     */
    public void allocateMemory(int offset, int size) {
        memory.extend(offset, size);
    }


    public void suicide(DataWord obtainerDW) {

        byte[] owner = getOwnerAddress().getLast20Bytes();
        byte[] obtainer = obtainerDW.getLast20Bytes();
        BigInteger balance = result.getRepository().getBalance(owner);

        if (logger.isInfoEnabled())
            logger.info("Transfer to: [{}] heritage: [{}]",
                    Hex.toHexString(obtainer),
                    balance);

        transfer(result.getRepository(), owner, obtainer, balance);
        result.addDeleteAccount(this.getOwnerAddress());
    }

    public void createContract(DataWord value, DataWord memStart, DataWord memSize) {

        if (invokeData.getCallDeep() == MAX_DEPTH) {
            stackPushZero();
            return;
        }

        byte[] senderAddress = this.getOwnerAddress().getLast20Bytes();
        BigInteger endowment = value.value();
        BigInteger senderBalance = result.getRepository().getBalance(senderAddress);
        if (senderBalance.compareTo(endowment) < 0) {
            stackPushZero();
            return;
        }

        // [1] FETCH THE CODE FROM THE MEMORY
        byte[] programCode = memoryChunk(memStart.intValue(), memSize.intValue());

        if (logger.isInfoEnabled())
            logger.info("creating a new contract inside contract run: [{}]", Hex.toHexString(senderAddress));

        //  actual gas subtract
        DataWord gasLimit = this.getGas();
        this.spendGas(gasLimit.longValue(), "internal call");

        // [2] CREATE THE CONTRACT ADDRESS
        byte[] nonce = result.getRepository().getNonce(senderAddress).toByteArray();
        byte[] newAddress = HashUtil.calcNewAddr(this.getOwnerAddress().getLast20Bytes(), nonce);

        if (invokeData.byTestingSuite()) {
            // This keeps track of the contracts created for a test
            this.getResult().addCallCreate(programCode, EMPTY_BYTE_ARRAY,
                    gasLimit.getNoLeadZeroesData(),
                    value.getNoLeadZeroesData());
        }


        // [3] UPDATE THE NONCE
        // (THIS STAGE IS NOT REVERTED BY ANY EXCEPTION)
        if (!invokeData.byTestingSuite()) {
            result.getRepository().increaseNonce(senderAddress);
        }

        Repository track = result.getRepository().startTracking();

        //In case of hashing collisions, check for any balance before createAccount()
        BigInteger oldBalance = result.getRepository().getBalance(newAddress);
        track.createAccount(newAddress);
        track.addBalance(newAddress, oldBalance);

        // [4] TRANSFER THE BALANCE
        track.addBalance(senderAddress, endowment.negate());
        BigInteger newBalance = BigInteger.ZERO;
        if (!invokeData.byTestingSuite()) {
            newBalance = track.addBalance(newAddress, endowment);
        }


        // [5] COOK THE INVOKE AND EXECUTE
        ProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(
                this, new DataWord(newAddress), DataWord.ZERO, gasLimit,
                newBalance, null, track, this.invokeData.getBlockStore(), invokeData.byTestingSuite());

        ProgramResult result = null;

        if (programCode.length != 0) {
            VM vm = new VM();
            Program program = new Program(programCode, programInvoke);
            vm.play(program);
            result = program.getResult();
            this.result.addDeleteAccounts(result.getDeleteAccounts());
            this.result.addLogInfos(result.getLogInfoList());
        }

        if (result != null &&
                result.getException() != null) {
            logger.debug("contract run halted by Exception: contract: [{}], exception: [{}]",
                    Hex.toHexString(newAddress),
                    result.getException());


            track.rollback();
            stackPushZero();
            return;
        }

        if (programCode.length == 0) {
            result = new ProgramResult();
            result.setHReturn(new byte[]{});
        }

        // 4. CREATE THE CONTRACT OUT OF RETURN
        byte[] code = result.getHReturn();

        long storageCost = code.length * GasCost.CREATE_DATA_BYTE;
        long afterSpend = invokeData.getGas().longValue() - storageCost - result.getGasUsed();
        if (afterSpend < 0) {
            track.saveCode(newAddress, EMPTY_BYTE_ARRAY);
        } else {

            result.spendGas(code.length * GasCost.CREATE_DATA);
            track.saveCode(newAddress, code);
        }

        track.commit();

        // IN SUCCESS PUSH THE ADDRESS INTO THE STACK
        stackPush(new DataWord(newAddress));

        // 5. REFUND THE REMAIN GAS

        long refundGas = gasLimit.longValue() - result.getGasUsed();
        if (refundGas > 0) {
            this.refundGas(refundGas, "remain gas from the internal call");
            if (gasLogger.isInfoEnabled()) {
                gasLogger.info("The remaining gas is refunded, account: [{}], gas: [{}] ",
                        Hex.toHexString(this.getOwnerAddress().getLast20Bytes()),
                        refundGas);
            }
        }
    }

    /**
     * That method is for internal code invocations
     * <p>
     * - Normal calls invoke a specified contract which updates itself
     * - Stateless calls invoke code from another contract, within the context of the caller
     *
     * @param msg is the message call object
     */
    public void callToAddress(MessageCall msg) {

        if (invokeData.getCallDeep() == MAX_DEPTH) {
            stackPushZero();
            return;
        }

        byte[] data = memoryChunk(msg.getInDataOffs().intValue(), msg.getInDataSize().intValue());

        // FETCH THE SAVED STORAGE
        byte[] codeAddress = msg.getCodeAddress().getLast20Bytes();
        byte[] senderAddress = this.getOwnerAddress().getLast20Bytes();
        byte[] contextAddress = msg.getType() == MsgType.STATELESS ? senderAddress : codeAddress;

        // FETCH THE CODE
        byte[] programCode = this.result.getRepository().getCode(codeAddress);

        if (logger.isInfoEnabled())
            logger.info(msg.getType().name() + " for existing contract: address: [{}], outDataOffs: [{}], outDataSize: [{}]  ",
                    Hex.toHexString(contextAddress), msg.getOutDataOffs().longValue(), msg.getOutDataSize().longValue());

        Repository trackRepository = result.getRepository().startTracking();

        // 2.1 PERFORM THE VALUE (endowment) PART
        BigInteger endowment = msg.getEndowment().value(); //TODO #POC9 add 1024 stack check <=
        BigInteger senderBalance = trackRepository.getBalance(senderAddress);
        if (isNotCovers(senderBalance, endowment)) {
            stackPushZero();
            this.refundGas(msg.getGas().longValue(), "refund gas from message call");
            return;
        }

        trackRepository.addBalance(senderAddress, endowment.negate());

        BigInteger contextBalance = BigInteger.ZERO;
        if (!invokeData.byTestingSuite()) {
            contextBalance = trackRepository.addBalance(contextAddress, endowment);
        }

        if (invokeData.byTestingSuite()) {
            // This keeps track of the calls created for a test
            this.getResult().addCallCreate(data, contextAddress,
                    msg.getGas().getNoLeadZeroesData(),
                    msg.getEndowment().getNoLeadZeroesData());
        }

        ProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(
                this, new DataWord(contextAddress), msg.getEndowment(),
                msg.getGas(), contextBalance, data, trackRepository, this.invokeData.getBlockStore(), invokeData.byTestingSuite());

        ProgramResult result = null;

        if (programCode != null && programCode.length != 0) {
            VM vm = new VM();
            Program program = new Program(programCode, programInvoke);
            vm.play(program);
            result = program.getResult();
            this.getProgramTrace().merge(program.getProgramTrace());
            this.result.addDeleteAccounts(result.getDeleteAccounts());
            this.result.addLogInfos(result.getLogInfoList());
            this.result.futureRefundGas(result.getFutureRefund());
        }

        if (result != null &&
                result.getException() != null) {
            gasLogger.debug("contract run halted by Exception: contract: [{}], exception: [{}]",
                    Hex.toHexString(contextAddress),
                    result.getException());


            trackRepository.rollback();
            stackPushZero();
            return;
        }

        // 3. APPLY RESULTS: result.getHReturn() into out_memory allocated
        if (result != null) {
            byte[] buffer = result.getHReturn();
            int allocSize = msg.getOutDataSize().intValue();
            if (buffer != null && allocSize > 0) {
                int retSize = buffer.length;
                int offset = msg.getOutDataOffs().intValue();
                if (retSize > allocSize)
                    this.memorySave(offset, buffer);
                else
                    this.memorySave(offset, allocSize, buffer);
            }
        }

        // 4. THE FLAG OF SUCCESS IS ONE PUSHED INTO THE STACK
        trackRepository.commit();
        stackPushOne();

        // 5. REFUND THE REMAIN GAS
        if (result != null) {
            BigInteger refundGas = msg.getGas().value().subtract(toBI(result.getGasUsed()));
            if (isPositive(refundGas)) {
                this.refundGas(refundGas.longValue(), "remaining gas from the internal call");
                if (gasLogger.isInfoEnabled())
                    gasLogger.info("The remaining gas refunded, account: [{}], gas: [{}] ",
                            Hex.toHexString(senderAddress),
                            refundGas.toString());
            }
        } else {
            this.refundGas(msg.getGas().longValue(), "remaining gas from the internal call");
        }
    }

    public void spendGas(long gasValue, String cause) {
        gasLogger.info("[{}] Spent for cause: [{}], gas: [{}]", invokeHash, cause, gasValue);

        long afterSpend = invokeData.getGas().longValue() - gasValue - result.getGasUsed();
        if (afterSpend < 0) {
            throw Program.Exception.notEnoughSpendingGas(cause, gasValue, this);
        }
        result.spendGas(gasValue);
    }

    public void spendAllGas() {
        spendGas(invokeData.getGas().longValue() - result.getGasUsed(), "Spending all remaining");
    }

    public void refundGas(long gasValue, String cause) {
        gasLogger.info("[{}] Refund for cause: [{}], gas: [{}]", invokeHash, cause, gasValue);
        result.refundGas(gasValue);
    }

    public void futureRefundGas(long gasValue) {
        logger.info("Future refund added: [{}]", gasValue);
        result.futureRefundGas(gasValue);
    }

    public void resetFutureRefund() {
        result.resetFutureRefund();
    }

    public void storageSave(DataWord word1, DataWord word2) {
        storageSave(word1.getData(), word2.getData());
    }

    public void storageSave(byte[] key, byte[] val) {
        DataWord keyWord = new DataWord(key);
        DataWord valWord = new DataWord(val);
        result.getRepository().addStorageRow(this.programAddress.getLast20Bytes(), keyWord, valWord);
    }

    public byte[] getCode() {
        return ops;
    }

    public byte[] getCodeAt(DataWord address) {

        byte[] code = invokeData.getRepository().getCode(address.getLast20Bytes());
        if (code == null) code = ByteUtil.EMPTY_BYTE_ARRAY;

        return code;
    }

    public DataWord getOwnerAddress() {
        if (invokeData == null) return DataWord.ZERO_EMPTY_ARRAY;
        return this.programAddress.clone();
    }

    public DataWord getBlockHash(int index) {

        return index < this.getNumber().longValue() && index >= Math.max(256, this.getNumber().intValue()) - 256 ?
                new DataWord(this.invokeData.getBlockStore().getBlockHashByNumber(index)) :
                DataWord.ZERO;

    }


    public DataWord getBalance(DataWord address) {
        if (invokeData == null) return DataWord.ZERO_EMPTY_ARRAY;

        BigInteger balance = result.getRepository().getBalance(address.getLast20Bytes());

        return new DataWord(balance.toByteArray());
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
        if (invokeData == null) return EMPTY_BYTE_ARRAY;
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
        return invokeData.getTimestamp().clone();
    }

    public DataWord getNumber() {
        return invokeData.getNumber().clone();
    }

    public DataWord getDifficulty() {
        return invokeData.getDifficulty().clone();
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
        return memory.toString();
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
            if (contractDetails != null) {
                List<DataWord> storageKeys = new ArrayList<>(contractDetails.getStorage().keySet());
                Collections.sort(storageKeys);
                for (DataWord key : storageKeys) {
                    storageData.append(" ").append(key).append(" -> ").
                            append(contractDetails.getStorage().get(key)).append("\n");
                }
                if (storageData.length() > 0) storageData.insert(0, "\n");
            }

            StringBuilder memoryData = new StringBuilder();
            StringBuilder oneLine = new StringBuilder();
            if (memory.size() > 32)
                memoryData.append("... Memory Folded.... ")
                        .append("(")
                        .append(memory.size())
                        .append(") bytes");
            else
                for (int i = 0; i < memory.size(); ++i) {

                    byte value = memory.readByte(i);
                    oneLine.append(ByteUtil.oneByteToHexString(value)).append(" ");

                    if ((i + 1) % 16 == 0) {
                        String tmp = format("[%4s]-[%4s]", Integer.toString(i - 15, 16),
                                Integer.toString(i, 16)).replace(" ", "0");
                        memoryData.append("").append(tmp).append(" ");
                        memoryData.append(oneLine);
                        if (i < memory.size()) memoryData.append("\n");
                        oneLine.setLength(0);
                    }
                }
            if (memoryData.length() > 0) memoryData.insert(0, "\n");

            StringBuilder opsString = new StringBuilder();
            for (int i = 0; i < ops.length; ++i) {

                String tmpString = Integer.toString(ops[i] & 0xFF, 16);
                tmpString = tmpString.length() == 1 ? "0" + tmpString : tmpString;

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
            logger.trace("\n  Spent Gas: [{}]/[{}]\n  Left Gas:  [{}]\n",
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

            if (result.getHReturn() != null)
                globalOutput.append("\n  HReturn: ").append(
                        Hex.toHexString(result.getHReturn()));

            // sophisticated assumption that msg.data != codedata
            // means we are calling the contract not creating it
            byte[] txData = invokeData.getDataCopy(DataWord.ZERO, getDataSize());
            if (!Arrays.equals(txData, ops))
                globalOutput.append("\n  msg.data: ").append(Hex.toHexString(txData));
            globalOutput.append("\n\n  Spent Gas: ").append(result.getGasUsed());

            if (listener != null)
                listener.output(globalOutput.toString());
        }
    }

    public void saveOpTrace() {
        if (this.pc < ops.length) {
            programTrace.addOp(ops[pc], pc, invokeData.getCallDeep(), getGas(), traceListener.resetActions());
        }
    }

    public static void saveProgramTraceToFile(String txHash, String content) {

        if (!CONFIG.vmTrace() || isEmpty(CONFIG.vmTraceDir())) return;

        File file = new File(format("%s/%s/%s/%s.json", getProperty("user.dir"), CONFIG.databaseDir(), CONFIG.vmTraceDir(), txHash));
        Writer fw = null;
        Writer bw = null;
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();

            fw = new FileWriter(file.getAbsoluteFile());
            bw = new BufferedWriter(fw);
            bw.write(content);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ProgramTrace getProgramTrace() {
        return programTrace;
    }

    public void precompile() {
        for (int i = 0; i < ops.length; ++i) {

            OpCode op = OpCode.code(ops[i]);
            if (op == null) continue;

            if (op.equals(OpCode.JUMPDEST)) jumpdest.add(i);

            if (op.asInt() >= OpCode.PUSH1.asInt() && op.asInt() <= OpCode.PUSH32.asInt()) {
                i += op.asInt() - OpCode.PUSH1.asInt() + 1;
            }
        }
    }


    public static String stringify(byte[] code, int index, String result) {
        if (code == null || code.length == 0)
            return result;

        final byte opCode = code[index];
        OpCode op = OpCode.code(opCode);
        if (op == null) {
            throw Program.Exception.invalidOpCode(opCode);
        }

        final byte[] continuedCode;

        if (op == null) throw new IllegalOperationException("Invalid operation: " +
                Hex.toHexString(code, index, 1));

        switch (op) {
            case PUSH1:
            case PUSH2:
            case PUSH3:
            case PUSH4:
            case PUSH5:
            case PUSH6:
            case PUSH7:
            case PUSH8:
            case PUSH9:
            case PUSH10:
            case PUSH11:
            case PUSH12:
            case PUSH13:
            case PUSH14:
            case PUSH15:
            case PUSH16:
            case PUSH17:
            case PUSH18:
            case PUSH19:
            case PUSH20:
            case PUSH21:
            case PUSH22:
            case PUSH23:
            case PUSH24:
            case PUSH25:
            case PUSH26:
            case PUSH27:
            case PUSH28:
            case PUSH29:
            case PUSH30:
            case PUSH31:
            case PUSH32:
                result += ' ' + op.name() + ' ';

                int nPush = op.val() - OpCode.PUSH1.val() + 1;
                byte[] data = Arrays.copyOfRange(code, index + 1, index + nPush + 1);
                result += new BigInteger(1, data).toString() + ' ';

                continuedCode = Arrays.copyOfRange(code, index + nPush + 1, code.length);
                break;

            default:
                result += ' ' + op.name();
                continuedCode = Arrays.copyOfRange(code, index + 1, code.length);
                break;
        }
        return stringify(continuedCode, 0, result);
    }

    public void addListener(ProgramListener listener) {
        this.listener = listener;
    }

    public void validateJumpDest(int nextPC) {
        if (!jumpdest.contains(nextPC)) {
            throw Program.Exception.badJumpDestination(nextPC);
        }
    }

    public void callToPrecompiledAddress(MessageCall msg, PrecompiledContract contract) {

        Repository track = this.getResult().getRepository().startTracking();

        byte[] senderAddress = this.getOwnerAddress().getLast20Bytes();
        byte[] codeAddress = msg.getCodeAddress().getLast20Bytes();
        BigInteger endowment = msg.getEndowment().value();
        BigInteger senderBalance = result.getRepository().getBalance(senderAddress);
        if (senderBalance.compareTo(endowment) < 0) {
            stackPushZero();
            this.refundGas(msg.getGas().longValue(), "refund gas from message call");
            return;
        }

        byte[] data = this.memoryChunk(msg.getInDataOffs().intValue(),
                msg.getInDataSize().intValue());

        transfer(track, senderAddress, codeAddress, msg.getEndowment().value());

        if (invokeData.byTestingSuite()) {
            // This keeps track of the calls created for a test
            this.getResult().addCallCreate(data,
                    msg.getCodeAddress().getLast20Bytes(),
                    msg.getGas().getNoLeadZeroesData(),
                    msg.getEndowment().getNoLeadZeroesData());

            stackPushOne();
            return;
        }


        long requiredGas = contract.getGasForData(data);
        if (requiredGas > msg.getGas().longValue()) {

            this.refundGas(0, "call pre-compiled"); //matches cpp logic
            this.stackPushZero();
            track.rollback();
        } else {

            this.refundGas(msg.getGas().longValue() - requiredGas, "call pre-compiled");
            byte[] out = contract.execute(data);

            this.memorySave(msg.getOutDataOffs().intValue(), out);
            this.stackPushOne();
            track.commit();
        }
    }

    public interface ProgramListener {
        public void output(String out);
    }

    @SuppressWarnings("serial")
    public static class OutOfGasException extends RuntimeException {

        public OutOfGasException(String message, Object... args) {
            super(format(message, args));
        }
    }

    @SuppressWarnings("serial")
    public static class IllegalOperationException extends RuntimeException {

        public IllegalOperationException(String message, Object... args) {
            super(format(message, args));
        }
    }

    @SuppressWarnings("serial")
    public static class BadJumpDestinationException extends RuntimeException {

        public BadJumpDestinationException(String message, Object... args) {
            super(format(message, args));
        }
    }

    @SuppressWarnings("serial")
    public static class StackTooSmallException extends RuntimeException {

        public StackTooSmallException(String message, Object... args) {
            super(format(message, args));
        }
    }

    public static class Exception {

        public static OutOfGasException notEnoughOpGas(OpCode op, long opGas, long programGas) {
            return new OutOfGasException("Not enough gas for '%s' operation executing: opGas[%d], programGas[%d];", op, opGas, programGas);
        }

        public static OutOfGasException notEnoughOpGas(OpCode op, DataWord opGas, DataWord programGas) {
            return notEnoughOpGas(op, opGas.longValue(), programGas.longValue());
        }

        public static OutOfGasException notEnoughOpGas(OpCode op, BigInteger opGas, BigInteger programGas) {
            return notEnoughOpGas(op, opGas.longValue(), programGas.longValue());
        }

        public static OutOfGasException notEnoughSpendingGas(String cause, long gasValue, Program program) {
            return new OutOfGasException("Not enough gas for '%s' cause spending: invokeGas[%d], gas[%d], usedGas[%d];",
                    cause, program.invokeData.getGas().longValue(), gasValue, program.result.getGasUsed());
        }

        public static OutOfGasException gasOverflow(BigInteger actualGas, BigInteger gasLimit) {
            return new OutOfGasException("Gas value overflow: actualGas[%d], gasLimit[%d];", actualGas.longValue(), gasLimit.longValue());
        }

        public static IllegalOperationException invalidOpCode(byte... opCode) {
            return new IllegalOperationException("Invalid operation code: opCode[%s];", Hex.toHexString(opCode, 0, 1));
        }

        public static BadJumpDestinationException badJumpDestination(int pc) {
            return new BadJumpDestinationException("Operation with pc isn't 'JUMPDEST': PC[%d];", pc);
        }

        public static StackTooSmallException tooSmallStack(int expectedSize, int actualSize) {
            return new StackTooSmallException("Expected stack size %d but actual %d;", expectedSize, actualSize);
        }
    }

    @SuppressWarnings("serial")
    public class StackTooLargeException extends RuntimeException {
        public StackTooLargeException(String message) {
            super(message);
        }
    }

    /**
     * used mostly for testing reasons
     */
    public byte[] getMemory() {
        return memory.read(0, memory.size());
    }

    /**
     * used mostly for testing reasons
     */
    public void initMem(byte[] data) {
        this.memory.write(0, data);
    }


}
