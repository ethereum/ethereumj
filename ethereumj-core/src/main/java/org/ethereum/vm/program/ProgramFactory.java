package org.ethereum.vm.program;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Transaction;
import org.ethereum.vm.program.invoke.ProgramInvoke;
import org.ethereum.vm.trace.DefaultProgramTrace;
import org.ethereum.vm.trace.ProgramTrace;

/**
 * Factory class for creating a {@link Program} instance. Methods are chainable.
 * The starting point for this is the static {@link #create()} method. Use
 * {@link #getProgram()} when you want to build the instance.
 */
public class ProgramFactory {
    /**
     * Code hash.
     */
    private byte[] codeHash = null;

    /**
     * Program invoke.
     */
    private ProgramInvoke programInvoke = null;

    /**
     * Ops.
     */
    private byte[] ops = null;

    /**
     * Transaction.
     */
    private Transaction transaction = null;

    /**
     * Config.
     */
    private SystemProperties config = SystemProperties.getDefault();

    /**
     * Memory.
     */
    private Memory memory = new DefaultMemory();

    /**
     * Stack.
     */
    private Stack stack = new DefaultStack();

    /**
     * Storage. Sensible default relies on knowing {@link #programInvoke}, thus the
     * case is left as null for now, and handled later.
     */
    private Storage storage = null;

    /**
     * Program trace. Sensible default relies on knowing {@link #programInvoke},
     * thus the case is left as null for now and handled later.
     */
    private ProgramTrace programTrace = null;

    /**
     * Internal constructor.
     */
    private ProgramFactory() {
        // Intentionally left blank to make factory private.
    }

    /**
     * Assign a code hash to the Program.
     * 
     * @param codeHash
     *            Code hash.
     * @return Factory.
     */
    public ProgramFactory withCodeHash(byte[] codeHash) {
        this.codeHash = codeHash;
        return this;
    }

    /**
     * Assign a {@link ProgramInvoke}.
     * 
     * @param programInvoke
     *            Program invoke.
     * @return Factory.
     */
    public ProgramFactory withProgramInvoke(ProgramInvoke programInvoke) {
        this.programInvoke = programInvoke;
        return this;
    }

    /**
     * Assign ops.
     * 
     * @param ops
     *            Ops.
     * @return Factory.
     */
    public ProgramFactory withOps(byte[] ops) {
        this.ops = ops;
        return this;
    }

    /**
     * Assign a transaction.
     * 
     * @param transaction
     *            Transaction.
     * @return Factory.
     */
    public ProgramFactory withTransaction(Transaction transaction) {
        this.transaction = transaction;
        return this;
    }

    /**
     * Assign configuration.
     * 
     * @param config
     *            Configuration.
     * @return Factory.
     */
    public ProgramFactory withConfig(SystemProperties config) {
        this.config = config;
        return this;
    }

    /**
     * Assign memory.
     * 
     * @param memory
     *            Memory.
     * @return Factory.
     */
    public ProgramFactory withMemory(Memory memory) {
        this.memory = memory;
        return this;
    }

    /**
     * Assign stack.
     * 
     * @param stack
     *            Stack.
     * @return Factory.
     */
    public ProgramFactory withStack(Stack stack) {
        this.stack = stack;
        return this;
    }

    /**
     * Assign storage.
     * 
     * @param storage
     *            Storage.
     * @return Factory.
     */
    public ProgramFactory withStorage(Storage storage) {
        this.storage = storage;
        return this;
    }

    /**
     * Assign program trace.
     * 
     * @param programTrace
     *            Program trace.
     * @return Factory.
     */
    public ProgramFactory withProgramTrace(ProgramTrace programTrace) {
        this.programTrace = programTrace;
        return this;
    }

    /**
     * Create a new factory for building {@link Program} instances.
     * 
     * @return New program factory.
     */
    public static ProgramFactory create() {
        return new ProgramFactory();
    }

    /**
     * Build and get program from factory. This is not chainable.
     * 
     * @throws IllegalArgumentException
     *             Required parameters were not set.
     * @return Program with assigned values.
     */
    public Program getProgram() {
        if (programInvoke == null || ops == null) {
            throw new IllegalArgumentException("Can't create program without programInvoke and ops");
        }

        // Default case requires programInvoke, thus it is created here.
        if (storage == null) {
            storage = new DefaultStorage(programInvoke);
        }

        if (programTrace == null) {
            programTrace = new DefaultProgramTrace(config, programInvoke);
        }

        return new Program(codeHash, ops, programInvoke, transaction, config, memory, stack, storage, programTrace);
    };
}
