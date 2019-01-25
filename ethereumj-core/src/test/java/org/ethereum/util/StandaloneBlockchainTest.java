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
package org.ethereum.util;

import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.ECKey;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.Solc;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.util.blockchain.SolidityCallResult;
import org.ethereum.util.blockchain.SolidityContract;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import static org.ethereum.util.blockchain.EtherUtil.Unit.ETHER;
import static org.ethereum.util.blockchain.EtherUtil.convert;

/**
 * Created by Anton Nashatyrev on 06.07.2016.
 */
public class StandaloneBlockchainTest {

    @AfterClass
    public static void cleanup() {
        SystemProperties.resetToDefault();
    }

    @Test
    public void constructorTest() {
        StandaloneBlockchain sb = new StandaloneBlockchain().withAutoblock(true);
        SolidityContract a = sb.submitNewContract(
                "contract A {" +
                        "  uint public a;" +
                        "  uint public b;" +
                        "  function A(uint a_, uint b_) {a = a_; b = b_; }" +
                        "}",
                "A", 555, 777
        );
        Assert.assertEquals(BigInteger.valueOf(555), a.callConstFunction("a")[0]);
        Assert.assertEquals(BigInteger.valueOf(777), a.callConstFunction("b")[0]);

        SolidityContract b = sb.submitNewContract(
                "contract A {" +
                        "  string public a;" +
                        "  uint public b;" +
                        "  function A(string a_, uint b_) {a = a_; b = b_; }" +
                        "}",
                "A", "This string is longer than 32 bytes...", 777
        );
        Assert.assertEquals("This string is longer than 32 bytes...", b.callConstFunction("a")[0]);
        Assert.assertEquals(BigInteger.valueOf(777), b.callConstFunction("b")[0]);
    }

    @Test
    public void fixedSizeArrayTest() {
        StandaloneBlockchain sb = new StandaloneBlockchain().withAutoblock(true);
        {
            SolidityContract a = sb.submitNewContract(
                    "contract A {" +
                            "  uint public a;" +
                            "  uint public b;" +
                            "  address public c;" +
                            "  address public d;" +
                            "  function f(uint[2] arr, address[2] arr2) {a = arr[0]; b = arr[1]; c = arr2[0]; d = arr2[1];}" +
                            "}");
            ECKey addr1 = new ECKey();
            ECKey addr2 = new ECKey();
            a.callFunction("f", new Integer[]{111, 222}, new byte[][] {addr1.getAddress(), addr2.getAddress()});
            Assert.assertEquals(BigInteger.valueOf(111), a.callConstFunction("a")[0]);
            Assert.assertEquals(BigInteger.valueOf(222), a.callConstFunction("b")[0]);
            Assert.assertArrayEquals(addr1.getAddress(), (byte[])a.callConstFunction("c")[0]);
            Assert.assertArrayEquals(addr2.getAddress(), (byte[])a.callConstFunction("d")[0]);
        }

        {
            ECKey addr1 = new ECKey();
            ECKey addr2 = new ECKey();
            SolidityContract a = sb.submitNewContract(
                    "contract A {" +
                            "  uint public a;" +
                            "  uint public b;" +
                            "  address public c;" +
                            "  address public d;" +
                            "  function A(uint[2] arr, address a1, address a2) {a = arr[0]; b = arr[1]; c = a1; d = a2;}" +
                            "}", "A",
                    new Integer[]{111, 222}, addr1.getAddress(), addr2.getAddress());
            Assert.assertEquals(BigInteger.valueOf(111), a.callConstFunction("a")[0]);
            Assert.assertEquals(BigInteger.valueOf(222), a.callConstFunction("b")[0]);
            Assert.assertArrayEquals(addr1.getAddress(), (byte[]) a.callConstFunction("c")[0]);
            Assert.assertArrayEquals(addr2.getAddress(), (byte[]) a.callConstFunction("d")[0]);

            String a1 = "0x1111111111111111111111111111111111111111";
            String a2 = "0x2222222222222222222222222222222222222222";
        }
    }

    @Test
    public void encodeTest1() {
        StandaloneBlockchain sb = new StandaloneBlockchain().withAutoblock(true);
        SolidityContract a = sb.submitNewContract(
                "contract A {" +
                        "  int public a;" +
                        "  function f(int a_) {a = a_;}" +
                        "}");
        a.callFunction("f", "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
        BigInteger r = (BigInteger) a.callConstFunction("a")[0];
        System.out.println(r.toString(16));
        Assert.assertEquals(new BigInteger(Hex.decode("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")), r);
    }
    
    @Test
    public void encodeTest2() {
        StandaloneBlockchain sb = new StandaloneBlockchain().withAutoblock(true);
        SolidityContract a = sb.submitNewContract(
                "contract A {" +
                       "  uint public a;" +
                       "  function f(uint a_) {a = a_;}" +
                "}");
        a.callFunction("f", "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
        BigInteger r = (BigInteger) a.callConstFunction("a")[0];
        System.out.println(r.toString(16));
        Assert.assertEquals(new BigInteger(1, Hex.decode("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")), r);
    }

    @Test
    public void invalidTxTest() {
        // check that invalid tx doesn't break implementation
        StandaloneBlockchain sb = new StandaloneBlockchain();
        ECKey alice = sb.getSender();
        ECKey bob = new ECKey();
        sb.sendEther(bob.getAddress(), BigInteger.valueOf(1000));
        sb.setSender(bob);
        sb.sendEther(alice.getAddress(), BigInteger.ONE);
        sb.setSender(alice);
        sb.sendEther(bob.getAddress(), BigInteger.valueOf(2000));

        sb.createBlock();
    }

    @Test
    public void initBalanceTest() {
        // check StandaloneBlockchain.withAccountBalance method
        StandaloneBlockchain sb = new StandaloneBlockchain();
        ECKey alice = sb.getSender();
        ECKey bob = new ECKey();
        sb.withAccountBalance(bob.getAddress(), convert(123, ETHER));

        BigInteger aliceInitBal = sb.getBlockchain().getRepository().getBalance(alice.getAddress());
        BigInteger bobInitBal = sb.getBlockchain().getRepository().getBalance(bob.getAddress());
        assert convert(123, ETHER).equals(bobInitBal);

        sb.setSender(bob);
        sb.sendEther(alice.getAddress(), BigInteger.ONE);

        sb.createBlock();

        assert convert(123, ETHER).compareTo(sb.getBlockchain().getRepository().getBalance(bob.getAddress())) > 0;
        assert aliceInitBal.add(BigInteger.ONE).equals(sb.getBlockchain().getRepository().getBalance(alice.getAddress()));
    }

    @Test
    public void addContractWithMetadataEvent() throws Exception {
        String contract = "contract A {" +
                "  event Event(uint aaa);" +
                "  function f(uint a) {emit Event(a); }" +
                "}";
        SolidityCompiler.Result result = SolidityCompiler.getInstance().compileSrc(
                contract.getBytes(), false, true,
                SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);
        CompilationResult cresult = CompilationResult.parse(result.output);
        CompilationResult.ContractMetadata contractMetadata = new CompilationResult.ContractMetadata();
        contractMetadata.abi = cresult.getContracts().get(0).abi;
        contractMetadata.bin = cresult.getContracts().get(0).bin;

        StandaloneBlockchain sb = new StandaloneBlockchain().withAutoblock(true);

        SolidityContract newContract = sb.submitNewContract(contractMetadata);
        SolidityCallResult res = newContract.callFunction("f", 123);
        Assert.assertEquals(1, res.getEvents().size());
        Assert.assertEquals("Event", res.getEvents().get(0).function.name);
        Assert.assertEquals(BigInteger.valueOf(123), res.getEvents().get(0).args[0]);
    }
}
