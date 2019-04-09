package org.ethereum.core;

import org.ethereum.core.genesis.GenesisJson;
import org.ethereum.core.genesis.GenesisLoader;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * @author alexbraz
 * @since 29/03/2019
 */
public class ChainTest {

    private static final Logger logger = LoggerFactory.getLogger("test");

    Block genesis = GenesisLoader.loadGenesis(getClass().getResourceAsStream("/genesis/olympic.json"));
    GenesisJson genesisJson = GenesisLoader.loadGenesisJson((InputStream) getClass().getResourceAsStream("/genesis/olympic.json"));

    @Test
    public void testContainsBlock() {
        Chain c = new Chain();
        c.add(genesis);
        assertEquals(genesis, c.getLast());
    }

    @Test
    public void testBlockHashNotNull() {
        Chain c = new Chain();
        c.add(genesis);
        assertNotNull(c.getLast().getHash());
    }

    @Test
    public void testDifficultyGenesisCorrectLoadedAndConverted() {
        Chain c = new Chain();
        c.add(genesis);
        assertEquals(new BigInteger(genesisJson.getDifficulty().replace("0x", ""), 16).intValue(), c.getLast().getDifficultyBI().intValue());
    }

    @Test
    public void testParentOnTheChain() {
        Chain c = new Chain();
        c.add(genesis);
        Block block = new Block(genesis.getHeader(), genesis.getTransactionsList(), null);
        assertFalse(c.isParentOnTheChain(block));
    }

    @Test
    public void testParentOnTheChain2() {
        Chain c = new Chain();
        c.add(genesis);
        assertFalse(c.isParentOnTheChain(genesis));
    }




}
