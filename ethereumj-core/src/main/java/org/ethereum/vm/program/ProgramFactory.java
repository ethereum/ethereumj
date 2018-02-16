package org.ethereum.vm.program;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Transaction;
import org.ethereum.vm.program.invoke.ProgramInvoke;

/**
 * Factory class for creating a {@link Program} instance. Methods are chainable.
 * The starting point for this is the static {@link #create()} method. Use
 * {@link #getProgram()} when you want to build the instance.
 */
public class ProgramFactory {
    private byte[] codeHash = null;
    private ProgramInvoke programInvoke = null;
    private byte[] ops = null;
    private Transaction transaction = null;
    private SystemProperties config = SystemProperties.getDefault();

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

	return new Program(codeHash, ops, programInvoke, transaction, config);
    };
}
