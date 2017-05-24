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
package org.ethereum.vm;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.AccountState;
import org.ethereum.crypto.HashUtil;
import org.ethereum.core.Repository;

import org.ethereum.vm.program.Program;
import org.ethereum.vm.program.invoke.ProgramInvokeMockImpl;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

/**
 * @author Roman Mandeleil
 * @since 16.06.2014
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VMComplexTest {

    private static Logger logger = LoggerFactory.getLogger("TCK-Test");

    @Ignore //TODO #POC9
    @Test // contract call recursive
    public void test1() {

        /**
         *       #The code will run
         *       ------------------

                 a = contract.storage[999]
                 if a > 0:
                     contract.storage[999] = a - 1

                     # call to contract: 77045e71a7a2c50903d88e564cd72fab11e82051
                     send((tx.gas / 10 * 8), 0x77045e71a7a2c50903d88e564cd72fab11e82051, 0)
                 else:
                     stop
         */

        int expectedGas = 436;

        DataWord key1 = new DataWord(999);
        DataWord value1 = new DataWord(3);

        // Set contract into Database
        String callerAddr = "cd2a3d9f938e13cd947ec05abc7fe734df8dd826";
        String contractAddr = "77045e71a7a2c50903d88e564cd72fab11e82051";
        String code =
                "6103e75460005260006000511115630000004c576001600051036103e755600060006000600060007377045e71a7a2c50903d88e564cd72fab11e820516008600a5a0402f1630000004c00565b00";

        byte[] contractAddrB = Hex.decode(contractAddr);
        byte[] callerAddrB = Hex.decode(callerAddr);
        byte[] codeB = Hex.decode(code);

        byte[] codeKey = HashUtil.sha3(codeB);
        AccountState accountState = new AccountState(SystemProperties.getDefault())
                .withCodeHash(codeKey);

        ProgramInvokeMockImpl pi = new ProgramInvokeMockImpl();
        pi.setOwnerAddress(contractAddrB);
        Repository repository = pi.getRepository();

        repository.createAccount(callerAddrB);
        repository.addBalance(callerAddrB, new BigInteger("100000000000000000000"));

        repository.createAccount(contractAddrB);
        repository.saveCode(contractAddrB, codeB);
        repository.addStorageRow(contractAddrB, key1, value1);

        // Play the program
        VM vm = new VM();
        Program program = new Program(codeB, pi);

        try {
            while (!program.isStopped())
                vm.step(program);
        } catch (RuntimeException e) {
            program.setRuntimeFailure(e);
        }

        System.out.println();
        System.out.println("============ Results ============");

        BigInteger balance = repository.getBalance(callerAddrB);

        System.out.println("*** Used gas: " + program.getResult().getGasUsed());
        System.out.println("*** Contract Balance: " + balance);

        // todo: assert caller balance after contract exec

        repository.close();
        assertEquals(expectedGas, program.getResult().getGasUsed());
    }

    @Ignore //TODO #POC9
    @Test // contractB call contractA with data to storage
    public void test2() {

        /**
         *       #The code will run
         *       ------------------

                 contract A: 77045e71a7a2c50903d88e564cd72fab11e82051
                 ---------------
                     a = msg.data[0]
                     b = msg.data[1]

                     contract.storage[a]
                     contract.storage[b]


                 contract B: 83c5541a6c8d2dbad642f385d8d06ca9b6c731ee
                 -----------
                     a = msg((tx.gas / 10 * 8), 0x77045e71a7a2c50903d88e564cd72fab11e82051, 0, [11, 22, 33], 3, 6)

         */

        long expectedVal_1 = 11;
        long expectedVal_2 = 22;

        // Set contract into Database
        String callerAddr = "cd2a3d9f938e13cd947ec05abc7fe734df8dd826";

        String contractA_addr = "77045e71a7a2c50903d88e564cd72fab11e82051";
        String contractB_addr = "83c5541a6c8d2dbad642f385d8d06ca9b6c731ee";

        String code_a = "60006020023560005260016020023560205260005160005560205160015500";
        String code_b = "6000601f5360e05960e05952600060c05901536060596020015980602001600b9052806040016016905280606001602190526080905260007377045e71a7a2c50903d88e564cd72fab11e820516103e8f1602060000260a00160200151600052";

        byte[] caller_addr_bytes = Hex.decode(callerAddr);

        byte[] contractA_addr_bytes = Hex.decode(contractA_addr);
        byte[] codeA = Hex.decode(code_a);

        byte[] contractB_addr_bytes = Hex.decode(contractB_addr);
        byte[] codeB = Hex.decode(code_b);

        ProgramInvokeMockImpl pi = new ProgramInvokeMockImpl();
        pi.setOwnerAddress(contractB_addr_bytes);
        Repository repository = pi.getRepository();

        repository.createAccount(contractA_addr_bytes);
        repository.saveCode(contractA_addr_bytes, codeA);

        repository.createAccount(contractB_addr_bytes);
        repository.saveCode(contractB_addr_bytes, codeB);

        repository.createAccount(caller_addr_bytes);
        repository.addBalance(caller_addr_bytes, new BigInteger("100000000000000000000"));


        // ****************** //
        //  Play the program  //
        // ****************** //
        VM vm = new VM();
        Program program = new Program(codeB, pi);

        try {
            while (!program.isStopped())
                vm.step(program);
        } catch (RuntimeException e) {
            program.setRuntimeFailure(e);
        }


        System.out.println();
        System.out.println("============ Results ============");


        System.out.println("*** Used gas: " + program.getResult().getGasUsed());


        DataWord value_1 = repository.getStorageValue(contractA_addr_bytes, new DataWord(00));
        DataWord value_2 = repository.getStorageValue(contractA_addr_bytes, new DataWord(01));


        repository.close();
        assertEquals(expectedVal_1, value_1.longValue());
        assertEquals(expectedVal_2, value_2.longValue());

        // TODO: check that the value pushed after exec is 1
    }

    @Ignore
    @Test // contractB call contractA with return expectation
    public void test3() {

        /**
         *       #The code will run
         *       ------------------

         contract A: 77045e71a7a2c50903d88e564cd72fab11e82051
         ---------------

           a = 11
           b = 22
           c = 33
           d = 44
           e = 55
           f = 66

           [asm  192 0 RETURN asm]



         contract B: 83c5541a6c8d2dbad642f385d8d06ca9b6c731ee
         -----------
             a = msg((tx.gas / 10 * 8), 0x77045e71a7a2c50903d88e564cd72fab11e82051, 0, [11, 22, 33], 3, 6)

         */

        long expectedVal_1 = 11;
        long expectedVal_2 = 22;
        long expectedVal_3 = 33;
        long expectedVal_4 = 44;
        long expectedVal_5 = 55;
        long expectedVal_6 = 66;

        // Set contract into Database
        byte[] caller_addr_bytes = Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826");

        byte[] contractA_addr_bytes = Hex.decode("77045e71a7a2c50903d88e564cd72fab11e82051");
        byte[] contractB_addr_bytes = Hex.decode("83c5541a6c8d2dbad642f385d8d06ca9b6c731ee");

        byte[] codeA = Hex.decode("600b60005260166020526021604052602c6060526037608052604260a05260c06000f2");
        byte[] codeB = Hex.decode("6000601f5360e05960e05952600060c05901536060596020015980602001600b9052806040016016905280606001602190526080905260007377045e71a7a2c50903d88e564cd72fab11e820516103e8f1602060000260a00160200151600052");

        ProgramInvokeMockImpl pi = new ProgramInvokeMockImpl();
        pi.setOwnerAddress(contractB_addr_bytes);
        Repository repository = pi.getRepository();
        repository.createAccount(contractA_addr_bytes);
        repository.saveCode(contractA_addr_bytes, codeA);

        repository.createAccount(contractB_addr_bytes);
        repository.saveCode(contractB_addr_bytes, codeB);

        repository.createAccount(caller_addr_bytes);
        repository.addBalance(caller_addr_bytes, new BigInteger("100000000000000000000"));

        // ****************** //
        //  Play the program  //
        // ****************** //
        VM vm = new VM();
        Program program = new Program(codeB, pi);

        try {
            while (!program.isStopped())
                vm.step(program);
        } catch (RuntimeException e) {
            program.setRuntimeFailure(e);
        }

        System.out.println();
        System.out.println("============ Results ============");
        System.out.println("*** Used gas: " + program.getResult().getGasUsed());

        DataWord value1 = program.memoryLoad(new DataWord(32));
        DataWord value2 = program.memoryLoad(new DataWord(64));
        DataWord value3 = program.memoryLoad(new DataWord(96));
        DataWord value4 = program.memoryLoad(new DataWord(128));
        DataWord value5 = program.memoryLoad(new DataWord(160));
        DataWord value6 = program.memoryLoad(new DataWord(192));

        repository.close();

        assertEquals(expectedVal_1, value1.longValue());
        assertEquals(expectedVal_2, value2.longValue());
        assertEquals(expectedVal_3, value3.longValue());
        assertEquals(expectedVal_4, value4.longValue());
        assertEquals(expectedVal_5, value5.longValue());
        assertEquals(expectedVal_6, value6.longValue());

        // TODO: check that the value pushed after exec is 1
    }

    @Test // CREATE magic
    public void test4() {

        /**
         *       #The code will run
         *       ------------------

         contract A: 77045e71a7a2c50903d88e564cd72fab11e82051
         -----------

             a = 0x7f60c860005461012c6020540000000000000000000000000000000000000000
             b = 0x0060005460206000f20000000000000000000000000000000000000000000000
             create(100, 0 41)


         contract B: (the contract to be created the addr will be defined to: 8e45367623a2865132d9bf875d5cfa31b9a0cd94)
         -----------
             a = 200
             b = 300

         */

        // Set contract into Database
        byte[] caller_addr_bytes = Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826");

        byte[] contractA_addr_bytes = Hex.decode("77045e71a7a2c50903d88e564cd72fab11e82051");

        byte[] codeA = Hex.decode("7f7f60c860005461012c602054000000000000" +
                "00000000000000000000000000006000547e60" +
                "005460206000f2000000000000000000000000" +
                "0000000000000000000000602054602960006064f0");

        ProgramInvokeMockImpl pi = new ProgramInvokeMockImpl();
        pi.setOwnerAddress(contractA_addr_bytes);

        Repository repository = pi.getRepository();

        repository.createAccount(contractA_addr_bytes);
        repository.saveCode(contractA_addr_bytes, codeA);

        repository.createAccount(caller_addr_bytes);

        // ****************** //
        //  Play the program  //
        // ****************** //
        VM vm = new VM();
        Program program = new Program(codeA, pi);

        try {
            while (!program.isStopped())
                vm.step(program);
        } catch (RuntimeException e) {
            program.setRuntimeFailure(e);
        }

        logger.info("============ Results ============");

        System.out.println("*** Used gas: " + program.getResult().getGasUsed());
        // TODO: check that the value pushed after exec is the new address
        repository.close();
    }

    @Test // CALL contract with too much gas
    @Ignore
    public void test5() {
        // TODO CALL contract with gas > gasRemaining && gas > Long.MAX_VALUE
    }

    @Ignore
    @Test // contractB call itself with code from contractA
    public void test6() {
        /**
         *       #The code will run
         *       ------------------

         contract A: 945304eb96065b2a98b57a48a06ae28d285a71b5
         ---------------

         PUSH1 0 CALLDATALOAD SLOAD NOT PUSH1 9 JUMPI STOP
         PUSH1 32 CALLDATALOAD PUSH1 0 CALLDATALOAD SSTORE

         contract B: 0f572e5295c57f15886f9b263e2f6d2d6c7b5ec6
         -----------
             { (MSTORE 0 0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff)
               (MSTORE 32 0xaaffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffaa)
               [[ 0 ]] (CALLSTATELESS 1000000 0x945304eb96065b2a98b57a48a06ae28d285a71b5 23 0 64 64 0)
             }
         */

        // Set contract into Database
        byte[] caller_addr_bytes = Hex.decode("cd1722f3947def4cf144679da39c4c32bdc35681");

        byte[] contractA_addr_bytes = Hex.decode("945304eb96065b2a98b57a48a06ae28d285a71b5");
        byte[] contractB_addr_bytes = Hex.decode("0f572e5295c57f15886f9b263e2f6d2d6c7b5ec6");

        byte[] codeA = Hex.decode("60003554156009570060203560003555");
        byte[] codeB = Hex.decode("7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff6000527faaffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffaa6020526000604060406000601773945304eb96065b2a98b57a48a06ae28d285a71b5620f4240f3600055");

        ProgramInvokeMockImpl pi = new ProgramInvokeMockImpl();
        pi.setOwnerAddress(contractB_addr_bytes);
        pi.setGasLimit(10000000000000l);

        Repository repository = pi.getRepository();
        repository.createAccount(contractA_addr_bytes);
        repository.saveCode(contractA_addr_bytes, codeA);
        repository.addBalance(contractA_addr_bytes, BigInteger.valueOf(23));

        repository.createAccount(contractB_addr_bytes);
        repository.saveCode(contractB_addr_bytes, codeB);
        repository.addBalance(contractB_addr_bytes, new BigInteger("1000000000000000000"));

        repository.createAccount(caller_addr_bytes);
        repository.addBalance(caller_addr_bytes, new BigInteger("100000000000000000000"));

        // ****************** //
        //  Play the program  //
        // ****************** //
        VM vm = new VM();
        Program program = new Program(codeB, pi);

        try {
            while (!program.isStopped())
                vm.step(program);
        } catch (RuntimeException e) {
            program.setRuntimeFailure(e);
        }

        System.out.println();
        System.out.println("============ Results ============");
        System.out.println("*** Used gas: " + program.getResult().getGasUsed());

        DataWord memValue1 = program.memoryLoad(new DataWord(0));
        DataWord memValue2 = program.memoryLoad(new DataWord(32));

        DataWord storeValue1 = repository.getStorageValue(contractB_addr_bytes, new DataWord(00));

        repository.close();

        assertEquals("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", memValue1.toString());
        assertEquals("aaffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffaa", memValue2.toString());

        assertEquals("0x1", storeValue1.shortHex());

        // TODO: check that the value pushed after exec is 1
    }
}
