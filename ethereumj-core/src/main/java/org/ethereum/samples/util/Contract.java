/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.samples.util;

import org.ethereum.core.CallTransaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.facade.Ethereum;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.vm.program.ProgramResult;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.ethereum.util.ByteUtil.toHexString;

public class Contract {

    private static final SolidityCompiler.Option[] DEFAULT_COMPILATION_OPTIONS = {SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN};

    private byte[] address;
    private final CompilationResult.ContractMetadata metadata;
    private final CallTransaction.Contract accessor;

    public Contract(byte[] address, CompilationResult.ContractMetadata metadata) {
        requireNonNull(metadata, "Contract metadata object couldn't be null.");

        this.address = address;
        this.metadata = metadata;
        this.accessor = new CallTransaction.Contract(metadata.abi);
    }

    public boolean isDeployed() {
        return address != null;
    }

    public Contract deployedAt(byte[] address) {
        this.address = address;
        return this;
    }

    public byte[] getAddress() {
        return address;
    }

    public String getAbi() {
        return metadata.abi;
    }

    public byte[] getBinaryCode() {
        return Hex.decode(metadata.bin);
    }

    public static Contract compile(byte[] source, SolidityCompiler compiler, SolidityCompiler.Option... compilationOpts) throws IOException {

        SolidityCompiler.Option[] options = Stream.concat(Stream.of(DEFAULT_COMPILATION_OPTIONS), Stream.of(compilationOpts))
                .distinct()
                .toArray(SolidityCompiler.Option[]::new);

        SolidityCompiler.Result result = compiler.compileSrc(source, true, true, options);

        if (result.isFailed()) {
            throw new RuntimeException("Contract compilation failed:\n" + result.errors);
        }
        CompilationResult res = CompilationResult.parse(result.output);
        if (res.getContracts().isEmpty()) {
            throw new RuntimeException("Compilation failed, no contracts returned:\n" + result.errors);
        }

        CompilationResult.ContractMetadata metadata = res.getContracts().iterator().next();
        if (isEmpty(metadata.bin)) {
            throw new RuntimeException("Compilation failed, no binary returned:\n" + result.errors);
        }

        return new Contract(null, metadata);
    }

    public Caller newCaller(ECKey callerKey, Ethereum ethereum, TransactionSubmitter submitter) {
        return new Caller(callerKey, ethereum, submitter);
    }

    public static Register newRegister(SolidityCompiler compiler) {
        return new Register(compiler);
    }

    public class Caller {

        private final Ethereum ethereum;
        private final TransactionSubmitter submitter;

        private final ECKey key;

        public Caller(ECKey callerKey, Ethereum ethereum, TransactionSubmitter submitter) {

            if (!isDeployed()) {
                throw new RuntimeException("Couldn't create caller for non deployed contract.");
            }

            this.ethereum = ethereum;
            this.submitter = submitter;
            this.key = callerKey;
        }

        public CompletableFuture<byte[]> call(String funcName, Object... args) {
            CallTransaction.Function func = accessor.getByName(funcName);
            if (func == null) {
                throw new RuntimeException(format("There is no function with name '%s'.", funcName));
            }

            if (func.constant) {
                ProgramResult result = ethereum.callConstantFunction(toHexString(getAddress()), key, func, args);
                return completedFuture(result.getHReturn());
            }

            return submitter.invokeTransaction(key, getAddress(), func.encode(args))
                    .submit()
                    .thenApply(receipt -> receipt.getExecutionResult());
        }
    }

    public static class Register {

        private final Map<String, Contract> contractById = new ConcurrentHashMap<>();
        private final SolidityCompiler compiler;

        public Register(SolidityCompiler compiler) {
            this.compiler = compiler;
        }

        public Register add(String id, Contract contract) {
            contractById.put(id, contract);
            return this;
        }

        public Register addDeployed(String id, byte[] address, byte[] source) {
            try {
                return add(id, Contract.compile(source, compiler).deployedAt(address));
            } catch (Exception e) {
                throw new RuntimeException("Contract registration error: ", e);
            }
        }

        public Register add(String id, byte[] source) {
            return addDeployed(id, null, source);
        }

        public Contract get(String id) {
            Contract contract = contractById.get(id);
            if (contract == null) {
                throw new RuntimeException(format("There is no contract with id '%s' in the register.", id));
            }
            return contract;
        }

        public Collection<Contract> contracts() {
            return contractById.values();
        }
    }
}
