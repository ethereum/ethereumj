package org.ethereum.vm.program;

import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.HashUtil;
import org.ethereum.db.ContractDetails;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.*;
import org.ethereum.vm.MessageCall.MsgType;
import org.ethereum.vm.PrecompiledContracts.PrecompiledContract;
import org.ethereum.vm.program.invoke.ProgramInvoke;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.ethereum.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.ethereum.vm.program.listener.CompositeProgramListener;
import org.ethereum.vm.program.listener.ProgramListenerAware;
import org.ethereum.vm.trace.ProgramTraceListener;
import org.ethereum.vm.trace.ProgramTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.*;

import static java.lang.String.format;
import static java.math.BigInteger.ZERO;
import static org.apache.commons.lang3.ArrayUtils.*;
import static org.ethereum.util.BIUtil.*;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;

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

    private Transaction transaction;

    private ProgramInvoke invoke;
    private ProgramInvokeFactory programInvokeFactory = new ProgramInvokeFactoryImpl();

    private ProgramOutListener listener;
    private ProgramTraceListener traceListener = new ProgramTraceListener();
    private CompositeProgramListener programListener = new CompositeProgramListener();

    private Stack stack;
    private Memory memory;
    private Storage storage;

    private ProgramResult result = new ProgramResult();
    private ProgramTrace trace = new ProgramTrace();

    private byte[] ops;
    private int pc;
    private byte lastOp;
    private byte previouslyExecutedOp;
    private boolean stopped;

    private Set<Integer> jumpdest = new HashSet<>();

    public Program(byte[] ops, ProgramInvoke programInvoke) {
        this.invoke = programInvoke;

        this.ops = nullToEmpty(ops);

        this.memory = setupProgramListener(new Memory());
        this.stack = setupProgramListener(new Stack());
        this.storage = setupProgramListener(new Storage(programInvoke));
        this.trace = new ProgramTrace(programInvoke);

        precompile();
    }

    public Program(byte[] ops, ProgramInvoke programInvoke, Transaction transaction) {
        this(ops, programInvoke);
        this.transaction = transaction;
    }

    public int getCallDeep() {
        return invoke.getCallDeep();
    }

    private InternalTransaction addInternalTx(byte[] nonce, DataWord gasLimit, byte[] senderAddress, byte[] receiveAddress,
                                              BigInteger value, byte[] data, String note) {

        InternalTransaction result = null;
        if (transaction != null) {
            byte[] senderNonce = isEmpty(nonce) ? getStorage().getNonce(senderAddress).toByteArray() : nonce;

            result = getResult().addInternalTransaction(transaction.getHash(), getCallDeep(), senderNonce,
                    getGasPrice(), gasLimit, senderAddress, receiveAddress, value.toByteArray(), data, note);
        }

        return result;
    }

    private <T extends ProgramListenerAware> T setupProgramListener(T traceListenerAware) {
        if (programListener.isEmpty()) {
            programListener.addListener(traceListener);
        }

        traceListenerAware.setTraceListener(traceListener);
        return traceListenerAware;
    }

    public byte getOp(int pc) {
        return (getLength(ops) <= pc) ? 0 : ops[pc];
    }

    public byte getCurrentOp() {
        return isEmpty(ops) ? 0 : ops[pc];
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
        stackPush(new DataWord(data));
    }

    public void stackPushZero() {
        stackPush(new DataWord(0));
    }

    public void stackPushOne() {
        DataWord stackWord = new DataWord(1);
        stackPush(stackWord);
    }

    public void stackPush(DataWord stackWord) {
        verifyStackOverflow(0, 1); //Sanity Check
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

        if (this.pc >= ops.length) {
            stop();
        }
    }

    public boolean isStopped() {
        return stopped;
    }

    public void stop() {
        stopped = true;
    }

    public void setHReturn(byte[] buff) {
        getResult().setHReturn(buff);
    }

    public void step() {
        setPC(pc + 1);
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
    public void verifyStackSize(int stackSize) {
        if (stack.size() < stackSize) {
            throw Program.Exception.tooSmallStack(stackSize, stack.size());
        }
    }

    public void verifyStackOverflow(int argsReqs, int returnReqs) {
        if ((stack.size() - argsReqs + returnReqs) > MAX_STACKSIZE) {
            throw new StackTooLargeException("Expected: overflow " + MAX_STACKSIZE + " elements stack limit");
        }
    }

    public int getMemSize() {
        return memory.size();
    }

    public void memorySave(DataWord addrB, DataWord value) {
        memory.write(addrB.intValue(), value.getData(), value.getData().length, false);
    }

    public void memorySaveLimited(int addr, byte[] data, int dataSize) {
        memory.write(addr, data, dataSize, true);
    }

    public void memorySave(int addr, byte[] value) {
        memory.write(addr, value, value.length, false);
    }

    public void memoryExpand(DataWord outDataOffs, DataWord outDataSize) {
        if (!outDataSize.isZero()) {
            memory.extend(outDataOffs.intValue(), outDataSize.intValue());
        }
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


    public void suicide(DataWord obtainerAddress) {

        byte[] owner = getOwnerAddress().getLast20Bytes();
        byte[] obtainer = obtainerAddress.getLast20Bytes();
        BigInteger balance = getStorage().getBalance(owner);

        if (logger.isInfoEnabled())
            logger.info("Transfer to: [{}] heritage: [{}]",
                    Hex.toHexString(obtainer),
                    balance);

        addInternalTx(null, null, owner, obtainer, balance, null, "suicide");

        transfer(getStorage(), owner, obtainer, balance);
        getResult().addDeleteAccount(this.getOwnerAddress());
    }

    public Repository getStorage() {
        return this.storage;
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void createContract(DataWord value, DataWord memStart, DataWord memSize) {

        if (getCallDeep() == MAX_DEPTH) {
            stackPushZero();
            return;
        }

        byte[] senderAddress = this.getOwnerAddress().getLast20Bytes();
        BigInteger endowment = value.value();
        if (isNotCovers(getStorage().getBalance(senderAddress), endowment)) {
            stackPushZero();
            return;
        }

        // [1] FETCH THE CODE FROM THE MEMORY
        byte[] programCode = memoryChunk(memStart.intValue(), memSize.intValue());

        if (logger.isInfoEnabled())
            logger.info("creating a new contract inside contract run: [{}]", Hex.toHexString(senderAddress));

        //  actual gas subtract
        DataWord gasLimit = getGas();
        spendGas(gasLimit.longValue(), "internal call");

        // [2] CREATE THE CONTRACT ADDRESS
        byte[] nonce = getStorage().getNonce(senderAddress).toByteArray();
        byte[] newAddress = HashUtil.calcNewAddr(getOwnerAddress().getLast20Bytes(), nonce);

        if (byTestingSuite()) {
            // This keeps track of the contracts created for a test
            getResult().addCallCreate(programCode, EMPTY_BYTE_ARRAY,
                    gasLimit.getNoLeadZeroesData(),
                    value.getNoLeadZeroesData());
        }

        // [3] UPDATE THE NONCE
        // (THIS STAGE IS NOT REVERTED BY ANY EXCEPTION)
        if (!byTestingSuite()) {
            getStorage().increaseNonce(senderAddress);
        }

        Repository track = getStorage().startTracking();

        //In case of hashing collisions, check for any balance before createAccount()
        if (track.isExist(newAddress)) {
            BigInteger oldBalance = track.getBalance(newAddress);
            track.createAccount(newAddress);
            track.addBalance(newAddress, oldBalance);
        } else
            track.createAccount(newAddress);

        // [4] TRANSFER THE BALANCE
        track.addBalance(senderAddress, endowment.negate());
        BigInteger newBalance = ZERO;
        if (!byTestingSuite()) {
            newBalance = track.addBalance(newAddress, endowment);
        }


        // [5] COOK THE INVOKE AND EXECUTE
        InternalTransaction internalTx = addInternalTx(nonce, getGasLimit(), senderAddress, null, endowment, programCode, "create");
        ProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(
                this, new DataWord(newAddress), DataWord.ZERO, gasLimit,
                newBalance, null, track, this.invoke.getBlockStore(), byTestingSuite());

        ProgramResult result = ProgramResult.empty();
        if (isNotEmpty(programCode)) {

            VM vm = new VM();
            Program program = new Program(programCode, programInvoke, internalTx);
            vm.play(program);
            result = program.getResult();

            getResult().addInternalTransactions(result.getInternalTransactions());

            if (result.getException() != null) {
                logger.debug("contract run halted by Exception: contract: [{}], exception: [{}]",
                        Hex.toHexString(newAddress),
                        result.getException());

                internalTx.reject();
                result.rejectInternalTransactions();

                track.rollback();
                stackPushZero();
                return;
            }
        }

        // 4. CREATE THE CONTRACT OUT OF RETURN
        byte[] code = result.getHReturn();

        long storageCost = getLength(code) * GasCost.CREATE_DATA;
        long afterSpend = programInvoke.getGas().longValue() - storageCost - result.getGasUsed();
        if (afterSpend < 0) {
            track.saveCode(newAddress, EMPTY_BYTE_ARRAY);
        } else {
            result.spendGas(storageCost);
            track.saveCode(newAddress, code);
        }

        track.commit();
        getResult().addDeleteAccounts(result.getDeleteAccounts());
        getResult().addLogInfos(result.getLogInfoList());

        // IN SUCCESS PUSH THE ADDRESS INTO THE STACK
        stackPush(new DataWord(newAddress));

        // 5. REFUND THE REMAIN GAS
        long refundGas = gasLimit.longValue() - result.getGasUsed();
        if (refundGas > 0) {
            refundGas(refundGas, "remain gas from the internal call");
            if (gasLogger.isInfoEnabled()) {
                gasLogger.info("The remaining gas is refunded, account: [{}], gas: [{}] ",
                        Hex.toHexString(getOwnerAddress().getLast20Bytes()),
                        refundGas);
            }
        }
    }

    /**
     * That method is for internal code invocations
     * <p/>
     * - Normal calls invoke a specified contract which updates itself
     * - Stateless calls invoke code from another contract, within the context of the caller
     *
     * @param msg is the message call object
     */
    public void callToAddress(MessageCall msg) {

        if (getCallDeep() == MAX_DEPTH) {
            stackPushZero();
            refundGas(msg.getGas().longValue(), " call deep limit reach");
            return;
        }

        byte[] data = memoryChunk(msg.getInDataOffs().intValue(), msg.getInDataSize().intValue());

        // FETCH THE SAVED STORAGE
        byte[] codeAddress = msg.getCodeAddress().getLast20Bytes();
        byte[] senderAddress = getOwnerAddress().getLast20Bytes();
        byte[] contextAddress = (msg.getType() == MsgType.STATELESS) ? senderAddress : codeAddress;

        if (logger.isInfoEnabled())
            logger.info(msg.getType().name() + " for existing contract: address: [{}], outDataOffs: [{}], outDataSize: [{}]  ",
                    Hex.toHexString(contextAddress), msg.getOutDataOffs().longValue(), msg.getOutDataSize().longValue());

        Repository track = getStorage().startTracking();

        // 2.1 PERFORM THE VALUE (endowment) PART
        BigInteger endowment = msg.getEndowment().value();
        BigInteger senderBalance = track.getBalance(senderAddress);
        if (isNotCovers(senderBalance, endowment)) {
            stackPushZero();
            refundGas(msg.getGas().longValue(), "refund gas from message call");
            return;
        }


        // FETCH THE CODE
        byte[] programCode = getStorage().isExist(codeAddress) ? getStorage().getCode(codeAddress) : EMPTY_BYTE_ARRAY;

        track.addBalance(senderAddress, endowment.negate());

        BigInteger contextBalance = ZERO;
        if (byTestingSuite()) {
            // This keeps track of the calls created for a test
            getResult().addCallCreate(data, contextAddress,
                    msg.getGas().getNoLeadZeroesData(),
                    msg.getEndowment().getNoLeadZeroesData());
        } else {
            contextBalance = track.addBalance(contextAddress, endowment);
        }

        // CREATE CALL INTERNAL TRANSACTION
        InternalTransaction internalTx = addInternalTx(null, getGasLimit(), senderAddress, contextAddress, endowment, programCode, "call");

        ProgramResult result = null;
        if (isNotEmpty(programCode)) {
            ProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(
                    this, new DataWord(contextAddress), msg.getEndowment(),
                    msg.getGas(), contextBalance, data, track, this.invoke.getBlockStore(), byTestingSuite());



            VM vm = new VM();
            Program program = new Program(programCode, programInvoke, internalTx);
            vm.play(program);
            result = program.getResult();

            getTrace().merge(program.getTrace());
            getResult().merge(result);

            if (result.getException() != null) {
                gasLogger.debug("contract run halted by Exception: contract: [{}], exception: [{}]",
                        Hex.toHexString(contextAddress),
                        result.getException());

                internalTx.reject();
                result.rejectInternalTransactions();

                track.rollback();
                stackPushZero();
                return;
            }
        }

        // 3. APPLY RESULTS: result.getHReturn() into out_memory allocated
        if (result != null) {
            byte[] buffer = result.getHReturn();
            int offset = msg.getOutDataOffs().intValue();
            int size = msg.getOutDataSize().intValue();

            memorySaveLimited(offset, buffer, size);
        }

        // 4. THE FLAG OF SUCCESS IS ONE PUSHED INTO THE STACK
        track.commit();
        stackPushOne();

        // 5. REFUND THE REMAIN GAS
        if (result != null) {
            BigInteger refundGas = msg.getGas().value().subtract(toBI(result.getGasUsed()));
            if (isPositive(refundGas)) {
                refundGas(refundGas.longValue(), "remaining gas from the internal call");
                if (gasLogger.isInfoEnabled())
                    gasLogger.info("The remaining gas refunded, account: [{}], gas: [{}] ",
                            Hex.toHexString(senderAddress),
                            refundGas.toString());
            }
        } else {
            refundGas(msg.getGas().longValue(), "remaining gas from the internal call");
        }
    }

    public void spendGas(long gasValue, String cause) {
        gasLogger.info("[{}] Spent for cause: [{}], gas: [{}]", invoke.hashCode(), cause, gasValue);

        if ((getGas().longValue() - gasValue) < 0) {
            throw Program.Exception.notEnoughSpendingGas(cause, gasValue, this);
        }
        getResult().spendGas(gasValue);
    }

    public void spendAllGas() {
        spendGas(getGas().longValue(), "Spending all remaining");
    }

    public void refundGas(long gasValue, String cause) {
        gasLogger.info("[{}] Refund for cause: [{}], gas: [{}]", invoke.hashCode(), cause, gasValue);
        getResult().refundGas(gasValue);
    }

    public void futureRefundGas(long gasValue) {
        logger.info("Future refund added: [{}]", gasValue);
        getResult().addFutureRefund(gasValue);
    }

    public void resetFutureRefund() {
        getResult().resetFutureRefund();
    }

    public void storageSave(DataWord word1, DataWord word2) {
        storageSave(word1.getData(), word2.getData());
    }

    public void storageSave(byte[] key, byte[] val) {
        DataWord keyWord = new DataWord(key);
        DataWord valWord = new DataWord(val);
        getStorage().addStorageRow(getOwnerAddress().getLast20Bytes(), keyWord, valWord);
    }

    public byte[] getCode() {
        return ops;
    }

    public byte[] getCodeAt(DataWord address) {
        byte[] code = invoke.getRepository().getCode(address.getLast20Bytes());
        return nullToEmpty(code);
    }

    public DataWord getOwnerAddress() {
        return invoke.getOwnerAddress().clone();
    }

    public DataWord getBlockHash(int index) {
        return index < this.getNumber().longValue() && index >= Math.max(256, this.getNumber().intValue()) - 256 ?
                new DataWord(this.invoke.getBlockStore().getBlockHashByNumber(index)) :
                DataWord.ZERO.clone();
    }

    public DataWord getBalance(DataWord address) {
        BigInteger balance = getStorage().getBalance(address.getLast20Bytes());
        return new DataWord(balance.toByteArray());
    }

    public DataWord getOriginAddress() {
        return invoke.getOriginAddress().clone();
    }

    public DataWord getCallerAddress() {
        return invoke.getCallerAddress().clone();
    }

    public DataWord getGasPrice() {
        return invoke.getMinGasPrice().clone();
    }

    public DataWord getGas() {
        return new DataWord(invoke.getGas().longValue() - getResult().getGasUsed());
    }

    public DataWord getCallValue() {
        return invoke.getCallValue().clone();
    }

    public DataWord getDataSize() {
        return invoke.getDataSize().clone();
    }

    public DataWord getDataValue(DataWord index) {
        return invoke.getDataValue(index);
    }

    public byte[] getDataCopy(DataWord offset, DataWord length) {
        return invoke.getDataCopy(offset, length);
    }

    public DataWord storageLoad(DataWord key) {
        return getStorage().getStorageValue(getOwnerAddress().getLast20Bytes(), key);
    }

    public DataWord getPrevHash() {
        return invoke.getPrevHash().clone();
    }

    public DataWord getCoinbase() {
        return invoke.getCoinbase().clone();
    }

    public DataWord getTimestamp() {
        return invoke.getTimestamp().clone();
    }

    public DataWord getNumber() {
        return invoke.getNumber().clone();
    }

    public DataWord getDifficulty() {
        return invoke.getDifficulty().clone();
    }

    public DataWord getGasLimit() {
        return invoke.getGaslimit().clone();
    }

    public ProgramResult getResult() {
        return result;
    }

    public void setRuntimeFailure(RuntimeException e) {
        getResult().setException(e);
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

            ContractDetails contractDetails = getStorage().
                    getContractDetails(getOwnerAddress().getLast20Bytes());
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
            if (memory.size() > 320)
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
                    getResult().getGasUsed(),
                    invoke.getGas().longValue(),
                    getGas().longValue());

            StringBuilder globalOutput = new StringBuilder("\n");
            if (stackData.length() > 0) stackData.append("\n");

            if (pc != 0)
                globalOutput.append("[Op: ").append(OpCode.code(lastOp).name()).append("]\n");

            globalOutput.append(" -- OPS --     ").append(opsString).append("\n");
            globalOutput.append(" -- STACK --   ").append(stackData).append("\n");
            globalOutput.append(" -- MEMORY --  ").append(memoryData).append("\n");
            globalOutput.append(" -- STORAGE -- ").append(storageData).append("\n");

            if (getResult().getHReturn() != null)
                globalOutput.append("\n  HReturn: ").append(
                        Hex.toHexString(getResult().getHReturn()));

            // sophisticated assumption that msg.data != codedata
            // means we are calling the contract not creating it
            byte[] txData = invoke.getDataCopy(DataWord.ZERO, getDataSize());
            if (!Arrays.equals(txData, ops))
                globalOutput.append("\n  msg.data: ").append(Hex.toHexString(txData));
            globalOutput.append("\n\n  Spent Gas: ").append(getResult().getGasUsed());

            if (listener != null)
                listener.output(globalOutput.toString());
        }
    }

    public void saveOpTrace() {
        if (this.pc < ops.length) {
            trace.addOp(ops[pc], pc, getCallDeep(), getGas(), traceListener.resetActions());
        }
    }

    public ProgramTrace getTrace() {
        return trace;
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
        if (isEmpty(code)) {
            return result;
        }

        final byte opCode = code[index];
        OpCode op = OpCode.code(opCode);
        if (op == null) {
            throw Program.Exception.invalidOpCode(opCode);
        }

        final byte[] continuedCode;
        if (op.name().startsWith("PUSH")) {
            result += ' ' + op.name() + ' ';

            int nPush = op.val() - OpCode.PUSH1.val() + 1;
            byte[] data = Arrays.copyOfRange(code, index + 1, index + nPush + 1);
            result += new BigInteger(1, data).toString() + ' ';

            continuedCode = Arrays.copyOfRange(code, index + nPush + 1, code.length);
        } else {
            result += ' ' + op.name();
            continuedCode = Arrays.copyOfRange(code, index + 1, code.length);
        }

        return stringify(continuedCode, 0, result);
    }

    public void addListener(ProgramOutListener listener) {
        this.listener = listener;
    }

    public void verifyJumpDest(int nextPC) {
        if (!jumpdest.contains(nextPC)) {
            throw Program.Exception.badJumpDestination(nextPC);
        }
    }

    public void callToPrecompiledAddress(MessageCall msg, PrecompiledContract contract) {

        if (getCallDeep() == MAX_DEPTH) {
            stackPushZero();
            this.refundGas(msg.getGas().longValue(), " call deep limit reach");
            return;
        }

        Repository track = getStorage().startTracking();

        byte[] senderAddress = this.getOwnerAddress().getLast20Bytes();
        byte[] codeAddress = msg.getCodeAddress().getLast20Bytes();
        byte[] contextAddress = msg.getType() == MsgType.STATELESS ? senderAddress : codeAddress;


        BigInteger endowment = msg.getEndowment().value();
        BigInteger senderBalance = track.getBalance(senderAddress);
        if (senderBalance.compareTo(endowment) < 0) {
            stackPushZero();
            this.refundGas(msg.getGas().longValue(), "refund gas from message call");
            return;
        }

        byte[] data = this.memoryChunk(msg.getInDataOffs().intValue(),
                msg.getInDataSize().intValue());

        // Charge for endowment - is not reversible by rollback
        transfer(track, senderAddress, contextAddress, msg.getEndowment().value());

        if (byTestingSuite()) {
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

    public boolean byTestingSuite() {
        return invoke.byTestingSuite();
    }

    public interface ProgramOutListener {
        void output(String out);
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
                    cause, program.invoke.getGas().longValue(), gasValue, program.getResult().getGasUsed());
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
        this.memory.write(0, data, data.length, false);
    }


}
