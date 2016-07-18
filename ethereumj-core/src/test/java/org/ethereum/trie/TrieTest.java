package org.ethereum.trie;

import org.ethereum.core.AccountState;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.datasource.LevelDbDataSource;
import org.ethereum.db.DatabaseImpl;
import org.ethereum.util.RLP;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static org.ethereum.crypto.HashUtil.EMPTY_TRIE_HASH;
import static org.junit.Assert.*;

public class TrieTest {

    private static final Logger logger = LoggerFactory.getLogger("test");

    private static String LONG_STRING = "1234567890abcdefghijklmnopqrstuvwxxzABCEFGHIJKLMNOPQRSTUVWXYZ";
    private static String ROOT_HASH_EMPTY = Hex.toHexString(EMPTY_TRIE_HASH);

    private static String c = "c";
    private static String ca = "ca";
    private static String cat = "cat";
    private static String dog = "dog";
    private static String doge = "doge";
    private static String test = "test";
    private static String dude = "dude";

    private HashMapDB mockDb = new HashMapDB();
    private HashMapDB mockDb_2 = new HashMapDB();

//      ROOT: [ '\x16', A ]
//      A: [ '', '', '', '', B, '', '', '', C, '', '', '', '', '', '', '', '' ]
//      B: [ '\x00\x6f', D ]
//      D: [ '', '', '', '', '', '', E, '', '', '', '', '', '', '', '', '', 'verb' ]
//      E: [ '\x17', F ]
//      F: [ '', '', '', '', '', '', G, '', '', '', '', '', '', '', '', '', 'puppy' ]
//      G: [ '\x35', 'coin' ]
//      C: [ '\x20\x6f\x72\x73\x65', 'stallion' ]

    @After
    public void closeMockDb() throws IOException {
        mockDb.close();
        mockDb_2.close();
    }

    @Test
    public void testEmptyKey() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update("", dog);
        assertEquals(dog, new String(trie.get("")));
    }

    @Test
    public void testInsertShortString() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, dog);
        assertEquals(dog, new String(trie.get(cat)));
    }

    @Test
    public void testInsertLongString() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(cat)));
    }

    @Test
    public void testInsertMultipleItems1() {
        TrieImpl trie = new TrieImpl(mockDb);
        trie.update(ca, dude);
        assertEquals(dude, new String(trie.get(ca)));

        trie.update(cat, dog);
        assertEquals(dog, new String(trie.get(cat)));

        trie.update(dog, test);
        assertEquals(test, new String(trie.get(dog)));

        trie.update(doge, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(doge)));

        trie.update(test, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(test)));

        // Test if everything is still there
        assertEquals(dude, new String(trie.get(ca)));
        assertEquals(dog, new String(trie.get(cat)));
        assertEquals(test, new String(trie.get(dog)));
        assertEquals(LONG_STRING, new String(trie.get(doge)));
        assertEquals(LONG_STRING, new String(trie.get(test)));
    }

    @Test
    public void testInsertMultipleItems2() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, dog);
        assertEquals(dog, new String(trie.get(cat)));

        trie.update(ca, dude);
        assertEquals(dude, new String(trie.get(ca)));

        trie.update(doge, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(doge)));

        trie.update(dog, test);
        assertEquals(test, new String(trie.get(dog)));

        trie.update(test, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(test)));

        // Test if everything is still there
        assertEquals(dog, new String(trie.get(cat)));
        assertEquals(dude, new String(trie.get(ca)));
        assertEquals(LONG_STRING, new String(trie.get(doge)));
        assertEquals(test, new String(trie.get(dog)));
        assertEquals(LONG_STRING, new String(trie.get(test)));
    }

    @Test
    public void testUpdateShortToShortString() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, dog);
        assertEquals(dog, new String(trie.get(cat)));

        trie.update(cat, dog + "1");
        assertEquals(dog + "1", new String(trie.get(cat)));
    }

    @Test
    public void testUpdateLongToLongString() {
        TrieImpl trie = new TrieImpl(mockDb);
        trie.update(cat, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(cat)));
        trie.update(cat, LONG_STRING + "1");
        assertEquals(LONG_STRING + "1", new String(trie.get(cat)));
    }

    @Test
    public void testUpdateShortToLongString() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, dog);
        assertEquals(dog, new String(trie.get(cat)));

        trie.update(cat, LONG_STRING + "1");
        assertEquals(LONG_STRING + "1", new String(trie.get(cat)));
    }

    @Test
    public void testUpdateLongToShortString() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(cat)));

        trie.update(cat, dog + "1");
        assertEquals(dog + "1", new String(trie.get(cat)));
    }

    @Test
    public void testDeleteShortString1() {
        String ROOT_HASH_BEFORE = "a9539c810cc2e8fa20785bdd78ec36cc1dab4b41f0d531e80a5e5fd25c3037ee";
        String ROOT_HASH_AFTER = "fc5120b4a711bca1f5bb54769525b11b3fb9a8d6ac0b8bf08cbb248770521758";
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, dog);
        assertEquals(dog, new String(trie.get(cat)));

        trie.update(ca, dude);
        assertEquals(dude, new String(trie.get(ca)));
        assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));

        trie.delete(ca);
        assertEquals("", new String(trie.get(ca)));
        assertEquals(ROOT_HASH_AFTER, Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testDeleteShortString2() {
        String ROOT_HASH_BEFORE = "a9539c810cc2e8fa20785bdd78ec36cc1dab4b41f0d531e80a5e5fd25c3037ee";
        String ROOT_HASH_AFTER = "b25e1b5be78dbadf6c4e817c6d170bbb47e9916f8f6cc4607c5f3819ce98497b";
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(ca, dude);
        assertEquals(dude, new String(trie.get(ca)));

        trie.update(cat, dog);
        assertEquals(dog, new String(trie.get(cat)));
        assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));

        trie.delete(cat);
        assertEquals("", new String(trie.get(cat)));
        assertEquals(ROOT_HASH_AFTER, Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testDeleteShortString3() {
        String ROOT_HASH_BEFORE = "778ab82a7e8236ea2ff7bb9cfa46688e7241c1fd445bf2941416881a6ee192eb";
        String ROOT_HASH_AFTER = "05875807b8f3e735188d2479add82f96dee4db5aff00dc63f07a7e27d0deab65";
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, dude);
        assertEquals(dude, new String(trie.get(cat)));

        trie.update(dog, test);
        assertEquals(test, new String(trie.get(dog)));
        assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));

        trie.delete(dog);
        assertEquals("", new String(trie.get(dog)));
        assertEquals(ROOT_HASH_AFTER, Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testDeleteLongString1() {
        String ROOT_HASH_BEFORE = "318961a1c8f3724286e8e80d312352f01450bc4892c165cc7614e1c2e5a0012a";
        String ROOT_HASH_AFTER = "63356ecf33b083e244122fca7a9b128cc7620d438d5d62e4f8b5168f1fb0527b";
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(cat)));

        trie.update(dog, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(dog)));
        assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));

        trie.delete(dog);
        assertEquals("", new String(trie.get(dog)));
        assertEquals(ROOT_HASH_AFTER, Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testDeleteLongString2() {
        String ROOT_HASH_BEFORE = "e020de34ca26f8d373ff2c0a8ac3a4cb9032bfa7a194c68330b7ac3584a1d388";
        String ROOT_HASH_AFTER = "334511f0c4897677b782d13a6fa1e58e18de6b24879d57ced430bad5ac831cb2";
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(ca, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(ca)));

        trie.update(cat, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(cat)));
        assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));

        trie.delete(cat);
        assertEquals("", new String(trie.get(cat)));
        assertEquals(ROOT_HASH_AFTER, Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testDeleteLongString3() {
        String ROOT_HASH_BEFORE = "e020de34ca26f8d373ff2c0a8ac3a4cb9032bfa7a194c68330b7ac3584a1d388";
        String ROOT_HASH_AFTER = "63356ecf33b083e244122fca7a9b128cc7620d438d5d62e4f8b5168f1fb0527b";
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(cat)));

        trie.update(ca, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(ca)));
        assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));

        trie.delete(ca);
        assertEquals("", new String(trie.get(ca)));
        assertEquals(ROOT_HASH_AFTER, Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testDeleteCompletellyDiferentItems() {
        TrieImpl trie = new TrieImpl(mockDb);

        String val_1 = "2a";
        String val_2 = "09";
        String val_3 = "a9";

        trie.update(Hex.decode(val_1), Hex.decode(val_1));
        trie.update(Hex.decode(val_2), Hex.decode(val_2));

        String root1 = Hex.toHexString(trie.getRootHash());

        trie.update(Hex.decode(val_3), Hex.decode(val_3));
        trie.delete(Hex.decode(val_3));
        String root1_ = Hex.toHexString(trie.getRootHash());

        Assert.assertEquals(root1, root1_);
    }


    @Test
    public void testDeleteMultipleItems1() {
        String ROOT_HASH_BEFORE = "3a784eddf1936515f0313b073f99e3bd65c38689021d24855f62a9601ea41717";
        String ROOT_HASH_AFTER1 = "60a2e75cfa153c4af2783bd6cb48fd6bed84c6381bc2c8f02792c046b46c0653";
        String ROOT_HASH_AFTER2 = "a84739b4762ddf15e3acc4e6957e5ab2bbfaaef00fe9d436a7369c6f058ec90d";
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, dog);
        assertEquals(dog, new String(trie.get(cat)));

        trie.update(ca, dude);
        assertEquals(dude, new String(trie.get(ca)));

        trie.update(doge, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(doge)));

        trie.update(dog, test);
        assertEquals(test, new String(trie.get(dog)));

        trie.update(test, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(test)));
        assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));

        trie.delete(dog);
        assertEquals("", new String(trie.get(dog)));
        assertEquals(ROOT_HASH_AFTER1, Hex.toHexString(trie.getRootHash()));

        trie.delete(test);
        assertEquals("", new String(trie.get(test)));
        assertEquals(ROOT_HASH_AFTER2, Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testDeleteMultipleItems2() {
        String ROOT_HASH_BEFORE = "cf1ed2b6c4b6558f70ef0ecf76bfbee96af785cb5d5e7bfc37f9804ad8d0fb56";
        String ROOT_HASH_AFTER1 = "f586af4a476ba853fca8cea1fbde27cd17d537d18f64269fe09b02aa7fe55a9e";
        String ROOT_HASH_AFTER2 = "c59fdc16a80b11cc2f7a8b107bb0c954c0d8059e49c760ec3660eea64053ac91";

        TrieImpl trie = new TrieImpl(mockDb);
        trie.update(c, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(c)));

        trie.update(ca, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(ca)));

        trie.update(cat, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(cat)));
        assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));

        trie.delete(ca);
        assertEquals("", new String(trie.get(ca)));
        assertEquals(ROOT_HASH_AFTER1, Hex.toHexString(trie.getRootHash()));

        trie.delete(cat);
        assertEquals("", new String(trie.get(cat)));
        assertEquals(ROOT_HASH_AFTER2, Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testDeleteAll() {
        String ROOT_HASH_BEFORE = "a84739b4762ddf15e3acc4e6957e5ab2bbfaaef00fe9d436a7369c6f058ec90d";
        TrieImpl trie = new TrieImpl(mockDb);
        assertEquals(ROOT_HASH_EMPTY, Hex.toHexString(trie.getRootHash()));

        trie.update(ca, dude);
        trie.update(cat, dog);
        trie.update(doge, LONG_STRING);
        assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));

        trie.delete(ca);
        trie.delete(cat);
        trie.delete(doge);
        assertEquals(ROOT_HASH_EMPTY, Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testTrieEquals() {
        TrieImpl trie1 = new TrieImpl(mockDb);
        TrieImpl trie2 = new TrieImpl(mockDb);

        trie1.update(doge, LONG_STRING);
        trie2.update(doge, LONG_STRING);
        assertTrue("Expected tries to be equal", trie1.equals(trie2));
        assertEquals(Hex.toHexString(trie1.getRootHash()), Hex.toHexString(trie2.getRootHash()));

        trie1.update(dog, LONG_STRING);
        trie2.update(cat, LONG_STRING);
        assertFalse("Expected tries not to be equal", trie1.equals(trie2));
        assertNotEquals(Hex.toHexString(trie1.getRootHash()), Hex.toHexString(trie2.getRootHash()));
    }

    @Ignore
    @Test
    public void testTrieSync() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(dog, LONG_STRING);
        assertEquals("Expected no data in database", mockDb.getAddedItems(), 0);

        trie.sync();
        assertNotEquals("Expected data to be persisted", mockDb.getAddedItems(), 0);
    }

    @Ignore
    @Test
    public void TestTrieDirtyTracking() {
        TrieImpl trie = new TrieImpl(mockDb);
        trie.update(dog, LONG_STRING);
        assertTrue("Expected trie to be dirty", trie.getCache().isDirty());

        trie.sync();
        assertFalse("Expected trie not to be dirty", trie.getCache().isDirty());

        trie.update(test, LONG_STRING);
        trie.getCache().undo();
        assertFalse("Expected trie not to be dirty", trie.getCache().isDirty());
    }

    @Test
    public void TestTrieReset() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, LONG_STRING);
        assertNotEquals("Expected cached nodes", 0, trie.getCache().getNodes().size());

        trie.getCache().undo();

        assertEquals("Expected no nodes after undo", 0, trie.getCache().getNodes().size());
    }

    @Test
    public void testTrieCopy() {
        TrieImpl trie = new TrieImpl(mockDb);
        trie.update("doe", "reindeer");
        TrieImpl trie2 = trie.copy();
        assertNotEquals(trie.hashCode(), trie2.hashCode()); // avoid possibility that its just a reference copy
        assertEquals(Hex.toHexString(trie.getRootHash()), Hex.toHexString(trie2.getRootHash()));
        assertTrue(trie.equals(trie2));
    }

    @Test
    public void testTrieUndo() {
        TrieImpl trie = new TrieImpl(mockDb);
        trie.update("doe", "reindeer");
        assertEquals("11a0327cfcc5b7689b6b6d727e1f5f8846c1137caaa9fc871ba31b7cce1b703e", Hex.toHexString(trie.getRootHash()));
        trie.sync();

        trie.update("dog", "puppy");
        assertEquals("05ae693aac2107336a79309e0c60b24a7aac6aa3edecaef593921500d33c63c4", Hex.toHexString(trie.getRootHash()));

        trie.undo();
        assertEquals("11a0327cfcc5b7689b6b6d727e1f5f8846c1137caaa9fc871ba31b7cce1b703e", Hex.toHexString(trie.getRootHash()));
    }

    // Using tests from: https://github.com/ethereum/tests/blob/master/trietest.json

    @Test
    public void testSingleItem() {
        TrieImpl trie = new TrieImpl(mockDb);
        trie.update("A", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        assertEquals("d23786fb4a010da3ce639d66d5e904a11dbc02746d1ce25029e53290cabf28ab", Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testDogs() {
        TrieImpl trie = new TrieImpl(mockDb);
        trie.update("doe", "reindeer");
        assertEquals("11a0327cfcc5b7689b6b6d727e1f5f8846c1137caaa9fc871ba31b7cce1b703e", Hex.toHexString(trie.getRootHash()));

        trie.update("dog", "puppy");
        assertEquals("05ae693aac2107336a79309e0c60b24a7aac6aa3edecaef593921500d33c63c4", Hex.toHexString(trie.getRootHash()));

        trie.update("dogglesworth", "cat");
        assertEquals("8aad789dff2f538bca5d8ea56e8abe10f4c7ba3a5dea95fea4cd6e7c3a1168d3", Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testPuppy() {
        TrieImpl trie = new TrieImpl(mockDb);
        trie.update("do", "verb");
        trie.update("doge", "coin");
        trie.update("horse", "stallion");
        trie.update("dog", "puppy");

        assertEquals("5991bb8c6514148a29db676a14ac506cd2cd5775ace63c30a4fe457715e9ac84", Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testEmptyValues() {
        TrieImpl trie = new TrieImpl(mockDb);
        trie.update("do", "verb");
        trie.update("ether", "wookiedoo");
        trie.update("horse", "stallion");
        trie.update("shaman", "horse");
        trie.update("doge", "coin");
        trie.update("ether", "");
        trie.update("dog", "puppy");
        trie.update("shaman", "");

        assertEquals("5991bb8c6514148a29db676a14ac506cd2cd5775ace63c30a4fe457715e9ac84", Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testFoo() {
        TrieImpl trie = new TrieImpl(mockDb);
        trie.update("foo", "bar");
        trie.update("food", "bat");
        trie.update("food", "bass");

        assertEquals("17beaa1648bafa633cda809c90c04af50fc8aed3cb40d16efbddee6fdf63c4c3", Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testSmallValues() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update("be", "e");
        trie.update("dog", "puppy");
        trie.update("bed", "d");
        assertEquals("3f67c7a47520f79faa29255d2d3c084a7a6df0453116ed7232ff10277a8be68b", Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testTesty() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update("test", "test");
        assertEquals("85d106d4edff3b7a4889e91251d0a87d7c17a1dda648ebdba8c6060825be23b8", Hex.toHexString(trie.getRootHash()));

        trie.update("te", "testy");
        assertEquals("8452568af70d8d140f58d941338542f645fcca50094b20f3c3d8c3df49337928", Hex.toHexString(trie.getRootHash()));
    }

    private final String randomDictionary = "spinneries, archipenko, prepotency, herniotomy, preexpress, relaxative, insolvably, debonnaire, apophysate, virtuality, cavalryman, utilizable, diagenesis, vitascopic, governessy, abranchial, cyanogenic, gratulated, signalment, predicable, subquality, crystalize, prosaicism, oenologist, repressive, impanelled, cockneyism, bordelaise, compigne, konstantin, predicated, unsublimed, hydrophane, phycomyces, capitalise, slippingly, untithable, unburnable, deoxidizer, misteacher, precorrect, disclaimer, solidified, neuraxitis, caravaning, betelgeuse, underprice, uninclosed, acrogynous, reirrigate, dazzlingly, chaffiness, corybantes, intumesced, intentness, superexert, abstrusely, astounding, pilgrimage, posttarsal, prayerless, nomologist, semibelted, frithstool, unstinging, ecalcarate, amputating, megascopic, graphalloy, platteland, adjacently, mingrelian, valentinus, appendical, unaccurate, coriaceous, waterworks, sympathize, doorkeeper, overguilty, flaggingly, admonitory, aeriferous, normocytic, parnellism, catafalque, odontiasis, apprentice, adulterous, mechanisma, wilderness, undivorced, reinterred, effleurage, pretrochal, phytogenic, swirlingly, herbarized, unresolved, classifier, diosmosing, microphage, consecrate, astarboard, predefying, predriving, lettergram, ungranular, overdozing, conferring, unfavorite, peacockish, coinciding, erythraeum, freeholder, zygophoric, imbitterer, centroidal, appendixes, grayfishes, enological, indiscreet, broadcloth, divulgated, anglophobe, stoopingly, bibliophil, laryngitis, separatist, estivating, bellarmine, greasiness, typhlology, xanthation, mortifying, endeavorer, aviatrices, unequalise, metastatic, leftwinger, apologizer, quatrefoil, nonfouling, bitartrate, outchiding, undeported, poussetted, haemolysis, asantehene, montgomery, unjoinable, cedarhurst, unfastener, nonvacuums, beauregard, animalized, polyphides, cannizzaro, gelatinoid, apologised, unscripted, tracheidal, subdiscoid, gravelling, variegated, interabang, inoperable, immortelle, laestrygon, duplicatus, proscience, deoxidised, manfulness, channelize, nondefense, ectomorphy, unimpelled, headwaiter, hexaemeric, derivation, prelexical, limitarian, nonionized, prorefugee, invariably, patronizer, paraplegia, redivision, occupative, unfaceable, hypomnesia, psalterium, doctorfish, gentlefolk, overrefine, heptastich, desirously, clarabelle, uneuphonic, autotelism, firewarden, timberjack, fumigation, drainpipes, spathulate, novelvelle, bicorporal, grisliness, unhesitant, supergiant, unpatented, womanpower, toastiness, multichord, paramnesia, undertrick, contrarily, neurogenic, gunmanship, settlement, brookville, gradualism, unossified, villanovan, ecospecies, organising, buckhannon, prefulfill, johnsonese, unforegone, unwrathful, dunderhead, erceldoune, unwadeable, refunction, understuff, swaggering, freckliest, telemachus, groundsill, outslidden, bolsheviks, recognizer, hemangioma, tarantella, muhammedan, talebearer, relocation, preemption, chachalaca, septuagint, ubiquitous, plexiglass, humoresque, biliverdin, tetraploid, capitoline, summerwood, undilating, undetested, meningitic, petrolatum, phytotoxic, adiphenine, flashlight, protectory, inwreathed, rawishness, tendrillar, hastefully, bananaquit, anarthrous, unbedimmed, herborized, decenniums, deprecated, karyotypic, squalidity, pomiferous, petroglyph, actinomere, peninsular, trigonally, androgenic, resistance, unassuming, frithstool, documental, eunuchised, interphone, thymbraeus, confirmand, expurgated, vegetation, myographic, plasmagene, spindrying, unlackeyed, foreknower, mythically, albescence, rebudgeted, implicitly, unmonastic, torricelli, mortarless, labialized, phenacaine, radiometry, sluggishly, understood, wiretapper, jacobitely, unbetrayed, stadholder, directress, emissaries, corelation, sensualize, uncurbable, permillage, tentacular, thriftless, demoralize, preimagine, iconoclast, acrobatism, firewarden, transpired, bluethroat, wanderjahr, groundable, pedestrian, unulcerous, preearthly, freelanced, sculleries, avengingly, visigothic, preharmony, bressummer, acceptable, unfoolable, predivider, overseeing, arcosolium, piriformis, needlecord, homebodies, sulphation, phantasmic, unsensible, unpackaged, isopiestic, cytophagic, butterlike, frizzliest, winklehawk, necrophile, mesothorax, cuchulainn, unrentable, untangible, unshifting, unfeasible, poetastric, extermined, gaillardia, nonpendent, harborside, pigsticker, infanthood, underrower, easterling, jockeyship, housebreak, horologium, undepicted, dysacousma, incurrable, editorship, unrelented, peritricha, interchaff, frothiness, underplant, proafrican, squareness, enigmatise, reconciled, nonnumeral, nonevident, hamantasch, victualing, watercolor, schrdinger, understand, butlerlike, hemiglobin, yankeeland";

    @Test
    public void testMasiveUpdate() {
        boolean massiveUpdateTestEnabled = false;

        if (massiveUpdateTestEnabled) {
            List<String> randomWords = Arrays.asList(randomDictionary.split(","));
            HashMap<String, String> testerMap = new HashMap<>();

            TrieImpl trie = new TrieImpl(mockDb);
            Random generator = new Random();

            // Random insertion
            for (int i = 0; i < 100000; ++i) {

                int randomIndex1 = generator.nextInt(randomWords.size());
                int randomIndex2 = generator.nextInt(randomWords.size());

                String word1 = randomWords.get(randomIndex1).trim();
                String word2 = randomWords.get(randomIndex2).trim();

                trie.update(word1, word2);
                testerMap.put(word1, word2);
            }

            int half = testerMap.size() / 2;
            for (int r = 0; r < half; ++r) {

                int randomIndex = generator.nextInt(randomWords.size());
                String word1 = randomWords.get(randomIndex).trim();

                testerMap.remove(word1);
                trie.delete(word1);
            }

            trie.cleanCache();
            trie.sync();

            // Assert the result now
            Iterator<String> keys = testerMap.keySet().iterator();
            while (keys.hasNext()) {

                String mapWord1 = keys.next();
                String mapWord2 = testerMap.get(mapWord1);
                String treeWord2 = new String(trie.get(mapWord1));

                Assert.assertEquals(mapWord2, treeWord2);
            }
        }
    }

    @Ignore
    @Test
    public void testMasiveDetermenisticUpdate() throws IOException, URISyntaxException {

        // should be root: cfd77c0fcb037adefce1f4e2eb94381456a4746379d2896bb8f309c620436d30

        URL massiveUpload_1 = ClassLoader
                .getSystemResource("trie/massive-upload.dmp");

        File file = new File(massiveUpload_1.toURI());
        List<String> strData = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        // *** Part - 1 ***
        // 1. load the data from massive-upload.dmp
        //    which includes deletes/upadtes (5000 operations)
        TrieImpl trieSingle = new TrieImpl(mockDb_2);
        for (String aStrData : strData) {

            String[] keyVal = aStrData.split("=");

            if (keyVal[0].equals("*"))
                trieSingle.delete(keyVal[1].trim());
            else
                trieSingle.update(keyVal[0].trim(), keyVal[1].trim());
        }


        System.out.println("root_1:  => " + Hex.toHexString(trieSingle.getRootHash()));

        // *** Part - 2 ***
        // pre. we use the same data from massive-upload.dmp
        //      which includes deletes/upadtes (100000 operations)
        // 1. part of the data loaded
        // 2. the trie cache sync to the db
        // 3. the rest of the data loaded with part of the trie not in the cache
        TrieImpl trie = new TrieImpl(mockDb);

        for (int i = 0; i < 2000; ++i) {

            String[] keyVal = strData.get(i).split("=");

            if (keyVal[0].equals("*"))
                trie.delete(keyVal[1].trim());
            else
                trie.update(keyVal[0].trim(), keyVal[1].trim());
        }

        trie.cleanCache();
        trie.sync();

        TrieImpl trie2 = new TrieImpl(mockDb, trie.getRootHash());

        for (int i = 2000; i < strData.size(); ++i) {

            String[] keyVal = strData.get(i).split("=");

            if (keyVal[0].equals("*"))
                trie2.delete(keyVal[1].trim());
            else
                trie2.update(keyVal[0].trim(), keyVal[1].trim());
        }

        System.out.println("root_2:  => " + Hex.toHexString(trie2.getRootHash()));

        assertEquals(trieSingle.getRootHash(), trie2.getRootHash());

    }

    @Test  //  tests saving keys to the file  //
    public void testMasiveUpdateFromDB() {
        boolean massiveUpdateFromDBEnabled = false;

        if (massiveUpdateFromDBEnabled) {
            List<String> randomWords = Arrays.asList(randomDictionary.split(","));
            Map<String, String> testerMap = new HashMap<>();

            TrieImpl trie = new TrieImpl(mockDb);
            Random generator = new Random();

            // Random insertion
            for (int i = 0; i < 50000; ++i) {

                int randomIndex1 = generator.nextInt(randomWords.size());
                int randomIndex2 = generator.nextInt(randomWords.size());

                String word1 = randomWords.get(randomIndex1).trim();
                String word2 = randomWords.get(randomIndex2).trim();

                trie.update(word1, word2);
                testerMap.put(word1, word2);
            }

            trie.cleanCache();
            trie.sync();

            // Assert the result now
            Iterator<String> keys = testerMap.keySet().iterator();
            while (keys.hasNext()) {

                String mapWord1 = keys.next();
                String mapWord2 = testerMap.get(mapWord1);
                String treeWord2 = new String(trie.get(mapWord1));

                Assert.assertEquals(mapWord2, treeWord2);
            }

            TrieImpl trie2 = new TrieImpl(mockDb, trie.getRootHash());

            // Assert the result now
            keys = testerMap.keySet().iterator();
            while (keys.hasNext()) {

                String mapWord1 = keys.next();
                String mapWord2 = testerMap.get(mapWord1);
                String treeWord2 = new String(trie2.get(mapWord1));

                Assert.assertEquals(mapWord2, treeWord2);
            }
        }
    }


    @Test
    public void testRollbackTrie() throws URISyntaxException, IOException {

        TrieImpl trieSingle = new TrieImpl(mockDb);

        URL massiveUpload_1 = ClassLoader
                .getSystemResource("trie/massive-upload.dmp");

        File file = new File(massiveUpload_1.toURI());
        List<String> strData = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        List<byte[]> roots = new ArrayList<>();
        Map<String, String> trieDumps = new HashMap<>();

        for (int i = 0; i < 100; ++i) {

            String[] keyVal = strData.get(i).split("=");

            if (keyVal[0].equals("*"))
                trieSingle.delete(keyVal[1].trim());
            else
                trieSingle.update(keyVal[0].trim(), keyVal[1].trim());

            byte[] hash = trieSingle.getRootHash();
            roots.add(hash);

            String key = Hex.toHexString(hash);
            String dump = trieSingle.getTrieDump();
            trieDumps.put(key, dump);
        }

        // compare all 100 rollback dumps and
        // the originaly saved dumps
        for (int i = 1; i < roots.size(); ++i) {

            byte[] root = roots.get(i);
            logger.info("rollback over root : {}", Hex.toHexString(root));

            trieSingle.setRoot(root);
            String currDump = trieSingle.getTrieDump();
            String originDump = trieDumps.get(Hex.toHexString(root));
//            System.out.println(currDump);
            Assert.assertEquals(currDump, originDump);
        }

    }


    @Ignore
    @Test
    public void testGetFromRootNode() {
        TrieImpl trie1 = new TrieImpl(mockDb);
        trie1.update(cat, LONG_STRING);
        trie1.sync();
        TrieImpl trie2 = new TrieImpl(mockDb, trie1.getRootHash());
        assertEquals(LONG_STRING, new String(trie2.get(cat)));
    }


/*
        0x7645b9fbf1b51e6b980801fafe6bbc22d2ebe218 0x517eaccda568f3fa24915fed8add49d3b743b3764c0bc495b19a47c54dbc3d62 0x 0x1
        0x0000000000000000000000000000000000000000000000000000000000000010 0x947e70f9460402290a3e487dae01f610a1a8218fda
        0x0000000000000000000000000000000000000000000000000000000000000014 0x40
        0x0000000000000000000000000000000000000000000000000000000000000016 0x94412e0c4f0102f3f0ac63f0a125bce36ca75d4e0d
        0x0000000000000000000000000000000000000000000000000000000000000017 0x01
*/

    @Test
    public void storageHashCalc_1() {

        byte[] key1 = Hex.decode("0000000000000000000000000000000000000000000000000000000000000010");
        byte[] key2 = Hex.decode("0000000000000000000000000000000000000000000000000000000000000014");
        byte[] key3 = Hex.decode("0000000000000000000000000000000000000000000000000000000000000016");
        byte[] key4 = Hex.decode("0000000000000000000000000000000000000000000000000000000000000017");

        byte[] val1 = Hex.decode("947e70f9460402290a3e487dae01f610a1a8218fda");
        byte[] val2 = Hex.decode("40");
        byte[] val3 = Hex.decode("94412e0c4f0102f3f0ac63f0a125bce36ca75d4e0d");
        byte[] val4 = Hex.decode("01");

        TrieImpl storage = new TrieImpl(new HashMapDB());
        storage.update(key1, val1);
        storage.update(key2, val2);
        storage.update(key3, val3);
        storage.update(key4, val4);

        String hash = Hex.toHexString(storage.getRootHash());

        System.out.println(hash);
        Assert.assertEquals("517eaccda568f3fa24915fed8add49d3b743b3764c0bc495b19a47c54dbc3d62", hash);
    }


    @Test
    public void testFromDump_1() throws URISyntaxException, IOException, ParseException {


        // LOAD: real dump from real state run
        URL dbDump = ClassLoader
                .getSystemResource("dbdump/dbdump.json");

        File dbDumpFile = new File(dbDump.toURI());
        byte[] testData = Files.readAllBytes(dbDumpFile.toPath());
        String testSrc = new String(testData);

        JSONParser parser = new JSONParser();
        JSONArray dbDumpJSONArray = (JSONArray) parser.parse(testSrc);

        KeyValueDataSource keyValueDataSource = new LevelDbDataSource("testState");
        keyValueDataSource.init();

        DatabaseImpl db = new DatabaseImpl(keyValueDataSource);

        for (Object aDbDumpJSONArray : dbDumpJSONArray) {

            JSONObject obj = (JSONObject) aDbDumpJSONArray;
            byte[] key = Hex.decode(obj.get("key").toString());
            byte[] val = Hex.decode(obj.get("val").toString());

            db.put(key, val);
        }

        // TEST: load trie out of this run up to block#33
        byte[] rootNode = Hex.decode("bb690805d24882bc7ccae6fc0f80ac146274d5b81c6a6e9c882cd9b0a649c9c7");
        TrieImpl trie = new TrieImpl(db.getDb(), rootNode);

        // first key added in genesis
        byte[] val1 = trie.get(Hex.decode("51ba59315b3a95761d0863b05ccc7a7f54703d99"));
        AccountState accountState1 = new AccountState(val1);

        assertEquals(BigInteger.valueOf(2).pow(200), accountState1.getBalance());
        assertEquals("c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470", Hex.toHexString(accountState1.getCodeHash()));
        assertEquals(BigInteger.ZERO, accountState1.getNonce());
        assertEquals(null, accountState1.getStateRoot());

        // last key added up to block#33
        byte[] val2 = trie.get(Hex.decode("a39c2067eb45bc878818946d0f05a836b3da44fa"));
        AccountState accountState2 = new AccountState(val2);

        assertEquals(new BigInteger("1500000000000000000"), accountState2.getBalance());
        assertEquals("c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470", Hex.toHexString(accountState2.getCodeHash()));
        assertEquals(BigInteger.ZERO, accountState2.getNonce());
        assertEquals(null, accountState2.getStateRoot());

        db.close();
    }


    @Test // update the trie with blog key/val
    // each time dump the entire trie
    public void testSample_1() {

        TrieImpl trie = new TrieImpl(mockDb);

        trie.update("dog", "puppy");
        String dmp = trie.getTrieDump();
        System.out.println(dmp);
        System.out.println();
        Assert.assertEquals("ed6e08740e4a267eca9d4740f71f573e9aabbcc739b16a2fa6c1baed5ec21278", Hex.toHexString(trie.getRootHash()));

        trie.update("do", "verb");
        dmp = trie.getTrieDump();
        System.out.println(dmp);
        System.out.println();
        Assert.assertEquals("779db3986dd4f38416bfde49750ef7b13c6ecb3e2221620bcad9267e94604d36", Hex.toHexString(trie.getRootHash()));

        trie.update("doggiestan", "aeswome_place");
        dmp = trie.getTrieDump();
        System.out.println(dmp);
        System.out.println();
        Assert.assertEquals("8bd5544747b4c44d1274aa99a6293065fe319b3230e800203317e4c75a770099", Hex.toHexString(trie.getRootHash()));
    }


    @Test
    public void testSecureTrie(){

        Trie trie = new SecureTrie(mockDb);

        byte[] k1 = "do".getBytes();
        byte[] v1 = "verb".getBytes();

        byte[] k2 = "ether".getBytes();
        byte[] v2 = "wookiedoo".getBytes();

        byte[] k3 = "horse".getBytes();
        byte[] v3 = "stallion".getBytes();

        byte[] k4 = "shaman".getBytes();
        byte[] v4 = "horse".getBytes();

        byte[] k5 = "doge".getBytes();
        byte[] v5 = "coin".getBytes();

        byte[] k6 = "ether".getBytes();
        byte[] v6 = "".getBytes();

        byte[] k7 = "dog".getBytes();
        byte[] v7 = "puppy".getBytes();

        byte[] k8 = "shaman".getBytes();
        byte[] v8 = "".getBytes();

        trie.update(k1, v1);
        trie.update(k2, v2);
        trie.update(k3, v3);
        trie.update(k4, v4);
        trie.update(k5, v5);
        trie.update(k6, v6);
        trie.update(k7, v7);
        trie.update(k8, v8);

        byte[] root = trie.getRootHash();

        logger.info("root: " + Hex.toHexString(root));

        Assert.assertEquals("29b235a58c3c25ab83010c327d5932bcf05324b7d6b1185e650798034783ca9d",Hex.toHexString(root));
    }

    @Test
    public void testFatTrie(){

        FatTrie trie = new FatTrie(mockDb, mockDb_2);

        byte[] k1 = "do".getBytes();
        byte[] v1 = "verb".getBytes();

        byte[] k2 = "ether".getBytes();
        byte[] v2 = "wookiedoo".getBytes();

        byte[] k3 = "horse".getBytes();
        byte[] v3 = "stallion".getBytes();

        byte[] k4 = "shaman".getBytes();
        byte[] v4 = "horse".getBytes();

        byte[] k5 = "doge".getBytes();
        byte[] v5 = "coin".getBytes();

        byte[] k6 = "ether".getBytes();
        byte[] v6 = "".getBytes();

        byte[] k7 = "dog".getBytes();
        byte[] v7 = "puppy".getBytes();

        byte[] k8 = "shaman".getBytes();
        byte[] v8 = "".getBytes();

        trie.update(k1, v1);
        trie.update(k2, v2);
        trie.update(k3, v3);
        trie.update(k4, v4);
        trie.update(k5, v5);
        trie.update(k6, v6);
        trie.update(k7, v7);
        trie.update(k8, v8);

        byte[] root = trie.getRootHash();

        logger.info("root: " + Hex.toHexString(root));

        Assert.assertEquals("29b235a58c3c25ab83010c327d5932bcf05324b7d6b1185e650798034783ca9d", Hex.toHexString(root));

        String origRoot = Hex.toHexString(trie.getOrigTrie().getRootHash());
        Assert.assertEquals("5991bb8c6514148a29db676a14ac506cd2cd5775ace63c30a4fe457715e9ac84", origRoot);
    }


    @Test
    public void testSerialize_1(){

        long t = System.nanoTime();

        TrieImpl trie = new SecureTrie(new HashMapDB());

        byte[] k1 = "do".getBytes();
        byte[] v1 = "verb".getBytes();

        byte[] k2 = "ether".getBytes();
        byte[] v2 = "wookiedoo".getBytes();

        byte[] k3 = "horse".getBytes();
        byte[] v3 = "stallion".getBytes();

        byte[] k4 = "shaman".getBytes();
        byte[] v4 = "horse".getBytes();

        byte[] k5 = "doge".getBytes();
        byte[] v5 = "coin".getBytes();

        byte[] k6 = "ether".getBytes();
        byte[] v6 = "".getBytes();

        byte[] k7 = "dog".getBytes();
        byte[] v7 = "puppy".getBytes();

        byte[] k8 = "shaman".getBytes();
        byte[] v8 = "".getBytes();

        trie.update(k1, v1);
        trie.update(k2, v2);
        trie.update(k3, v3);
        trie.update(k4, v4);
        trie.update(k5, v5);
        trie.update(k6, v6);
        trie.update(k7, v7);
        trie.update(k8, v8);

        byte[] data = trie.serialize();
        String original = trie.getTrieDump();

        TrieImpl trie2 = new SecureTrie(new HashMapDB());

        long t_ = System.nanoTime();

        trie2.deserialize(data);

        String expected = trie2.getTrieDump();
        assertEquals(original, expected);

        System.out.println("took: " + ((float) (t_ - t) / 1_000_000) + "ms");
        System.out.println("size: " + ((float) (data.length) / 1_000) + "KB");

    }


    @Test
    public void testSerialize_2(){

        // real trie from contract: 13dc5836cd5638d0b81a1ba8377a7852d41b5bbe
        // found on block: #501570 on olympics
        // account.getStateRoot = d7b597b334b78c63ddf7beafc96484f5604472c40d438ea514882fab0813f074

        TrieImpl trie = new SecureTrie(new HashMapDB());
        trie.update(Hex.decode("78ff1fec89d436ee882b865b803c4d97fa5e02b451c05e01d70f6bddcb5d67f4"), RLP.encodeElement(Hex.decode("ed")));
        trie.update(Hex.decode("a0b2c153ba3d83c384d83c4f87f0bc3ea97203e7ca9ddeb1ff7e1370479308ac"), RLP.encodeElement(Hex.decode("b96f021e2c394ab64c683805bb151a551a5edacbeb6efebeeb7382fcb0e1c218")));
        trie.update(Hex.decode("d6a51ffc52a1d9bf92ac575247886c23a2ca20200d8b8625ca79841a94852775"), RLP.encodeElement(Hex.decode("02")));
        trie.update(Hex.decode("030a71a7e3701bd464ec792e1b8c118a2cefb0befd49d5e813f84dd18b0ef8a5"), RLP.encodeElement(Hex.decode("bfbcfeccad273b7a73e6c208eb28c582f3d7aade3a30c58816245263532931f6")));
        trie.update(Hex.decode("3325dbd1c037374d61ef87b70d4b0b960ae6b930145b85f0c7ac08cf9fd5182d"), RLP.encodeElement(Hex.decode("eb6361d2c08a0619254c6bc18ebab8bd764bae3c0918ee0ebedbd7e4106488e7")));
        trie.update(Hex.decode("f5bcd31f13d0d416ee5fa014c679e9bb79a9096a0184fc98bf62742fdede3234"), RLP.encodeElement(Hex.decode("97d63d7567b1fc41c19296d959eba0e7df4900bf2d197c6b7b746d864fdde421")));
        trie.update(Hex.decode("fea13264229220a24a19139af5e60e202e88d4c8f44d952a011ade7e050bd4af"), RLP.encodeElement(Hex.decode("de19d961a72b93fc9e7b809f693ed2d78d82a09d2cb2771edf0368e66ec619cb")));
        trie.update(Hex.decode("1c995075d11e1f5f9278172d90b06685f7de021dc86b063906f4ead9532c4c15"), RLP.encodeElement(Hex.decode("2be750ab8bf9748248c02696679a01519622db1c060b07a74f1b9cbb60451130")));
        trie.update(Hex.decode("ceba11cb3ef8fc954129bbe06b6b2cb6749133e3f51e3cacf16af7e006998618"), RLP.encodeElement(Hex.decode("68e6b4c2eac84308affe8948d3722a421e1a5c69477c264280c68fa43ef67f59")));
        trie.update(Hex.decode("358ef2ac2c4d56c4f3a0b5b90354f8fd77c43d40a8b5b364da35e56965c98262"), RLP.encodeElement(Hex.decode("1734636b595496967419aa0ed57952f1fdd7a5cc9c294341d2e9f28b4a6fbf5f")));
        trie.update(Hex.decode("ccd6731552020c69fbc31ec4a5617fdbc3a53dc0611737beee4020299344f2b2"), RLP.encodeElement(Hex.decode("5715de3a998c313536751a78de280e55ac287b13818a268167d9ace1b25b0de5")));
        trie.update(Hex.decode("50c97c9d66a2e8b7c298b323c2f0fd14537c17df2a4b3754237c7c2ffd3e054c"), RLP.encodeElement(Hex.decode("5e514b52bdeb379411d1164fcd1c6e9e9a1dd3b87bfe745a0328c70635865f75")));
        trie.update(Hex.decode("a64fcdcb4be3b2da15d7639be59c95ba12d8ba14a8332b5b0ebe2a5409a22452"), RLP.encodeElement(Hex.decode("efb83787ccbfef245e8c03eee78c2a094ba42a114b4f2c6d2cd43b49b63cf4aa")));
        trie.update(Hex.decode("e77e915ccf7e165fc78a498515068787edd295247939111b10b70d7ae2ebf671"), RLP.encodeElement(Hex.decode("e1fbdc60b24068dbff0dc81c8f53c333cb84f052fd56847843a1def0c74ec643")));
        trie.update(Hex.decode("88cd888539c2d9147253d3c174066b2092e1bbecbc20f39726df5603a4c3e3a8"), RLP.encodeElement(Hex.decode("091543fc497c20765ab1801993e6409a84349b90e8f50c7ebf5e2fff6995efa4")));
        trie.update(Hex.decode("842fdfae173b3e9e2b09cf9aac13ee6a0d4e8f835326faf59f6009b239738c73"), RLP.encodeElement(Hex.decode("7bcb2d8bd229e62c1d71ca525dd9816a7f60d15fa2208d6c379ad1d48b7b9148")));
        trie.update(Hex.decode("37f4db8820373e001936cce469ff972c9f55b9639b35f441a345daeae7ae0bb2"), RLP.encodeElement(Hex.decode("1d52683910185e1c28858261c2bc63853501d82d2bc017952f5fc2a9d372363e")));
        trie.update(Hex.decode("c8bbe713a0fcfb9dd92cb2c67a6d42f018eb7afccae4e4fa637fc4e51d9a7b31"), RLP.encodeElement(Hex.decode("02a1b8bb0fea7d8825007302c2d12446553c9e870674867bb73feda6e5b96739")));
        trie.update(Hex.decode("e3afc5a830343b3feda00f167d28174a24288eac6365e5303b85045f90ecc884"), RLP.encodeElement(Hex.decode("78145148d5146436611b7edaf12498ed6cebde35134b20d09ac54fd2628230ea")));
        trie.update(Hex.decode("f2e7104d744f30afd4eacdba76432d31f5ed553fd16a98f29dda2d4a9f604034"), RLP.encodeElement(Hex.decode("1aeb1b87814336ea19c7dbbc05c81e0400dbc80dfc8b8d9d11bebdf77d5fe8a5")));
        trie.update(Hex.decode("73446f6c0da7fcb0fd204bbc28c596bf01a5c5726d0d08138651343ba5d90f1b"), RLP.encodeElement(Hex.decode("146cffc03967c9b1189d11593693038264da882a3e8e137bb5fad0e309a390b3")));
        trie.update(Hex.decode("f2d6626c4f49e28f59a754f21858f4c9fe1ec852fd13f1524a8b3a243e3b224c"), RLP.encodeElement(Hex.decode("2077ef0069577a235336bbbeb63d25200ddac8ef5ba104f0ce52ed64caf7a43d")));
        trie.update(Hex.decode("91da3fd0782e51c6b3986e9e672fd566868e71f3dbc2d6c2cd6fbb3e361af2a7"), RLP.encodeElement(Hex.decode("b57e6bccd4a28753d63f1ce429fd84484158eee3949b7bb3476fce7af2c33c3a")));
        trie.update(Hex.decode("360eec2ad9fc9f4917c74fd4d197502385c4c3c49e6d2a2533ea17466c7d79c0"), RLP.encodeElement(Hex.decode("f204e6301695fdcd678bb218e7edf087ecac7877483c183614177ef7f19c3388")));
        trie.update(Hex.decode("7ada1a5758965567615fc411a640ea022c92b807e248fb76ff274875b4a0efca"), RLP.encodeElement(Hex.decode("4ce4ee01f162016e65f01da139d47d8139e09440b4cb347869c91f1dae8dda03")));
        trie.update(Hex.decode("147d33c28476199c7330171a16a7b21aab547e01b6e57faf9d0fd3903e672e78"), RLP.encodeElement(Hex.decode("d192111f97949e0eb7e90c53f5949a8b700db1d5814d39a02a487e5339569953")));
        trie.update(Hex.decode("6801cc20c1d7e6dceaacaf93079a88befc696a57a01324145daebe0d3b230432"), RLP.encodeElement(Hex.decode("01e140fc120999210889c952e4172808c6b8fe8052eb2228b75606b0b4c16c03")));
        trie.update(Hex.decode("51152680ae1279a4f4908f2d6fb2705ba306c3f3a5a83e4f4fb820a7512c7694"), RLP.encodeElement(Hex.decode("ba5e1c4f6e3364c994def32c5b58bf515e50cdb5db407a20092a2cefdb873e7c")));
        trie.update(Hex.decode("296baa91b73ae760a7e1fb55fbbfec9f845ba67f90c590a17f58af03bf6cecd2"), RLP.encodeElement(Hex.decode("47671306eea201372ef30da94989f26cc30e42f751c19a0772bc476e5c9713")));
        trie.update(Hex.decode("dcbefc50b6183bc288da193f083ed7394590de6a9681105f3c6ff53b13018bd9"), RLP.encodeElement(Hex.decode("dba5940b23cc770a39b01a143f4d7fe32dc1d29e35756f6625221020d0a98f6d")));
        trie.update(Hex.decode("8819ef417987f8ae7a81f42cdfb18815282fe989326fbff903d13cf0e03ace29"), RLP.encodeElement(Hex.decode("cc8af1ac56ea8f940a2d355d3a7dcb6235c3a3037570f354e281007afe921c3b")));
        trie.update(Hex.decode("622f899a62672f55213270d74d4e32ebaacb212f9802c7e5682e5996a11d7ac3"), RLP.encodeElement(Hex.decode("f75c99b1d6bff62f6a17262f896a28b26d6ea28b6898c0e43a56ce9fd718c26b")));
        trie.update(Hex.decode("3b6e8e4da56bd66691b837468650ad8a6a1ff21556d914adc3ae10764576339c"), RLP.encodeElement(Hex.decode("731b0777af1501d9066ac66eba1f5466a41cbe4f7beeaae8d39384f7f9f3876d")));
        trie.update(Hex.decode("1e808fb7dac20a83f842b965300a908d77a3f22f433fe3595138a18658a5cf39"), RLP.encodeElement(Hex.decode("c1e683c665109ac36b9c83f62073ea138605a6f19c06f8c0403a9c1928ec01f9")));
        trie.update(Hex.decode("2ca6bc0028151bd227660e0a41e2d9662a09f9316a7222010cd9aa11ca05f458"), RLP.encodeElement(Hex.decode("01bf")));
        trie.update(Hex.decode("f81bee09d7973b6e524b2dbf20e2b7fb92c08b1c35f0301e5d8059fb711a009c"), RLP.encodeElement(Hex.decode("2228")));
        trie.update(Hex.decode("86f0a0d1b88134e61663b0de7ee658cf7d8834a7c40c3bd29c7b36c2e034c75e"), RLP.encodeElement(Hex.decode("cf6361da04807cc7f08e54aa4b030d3ff0b0a269434d5ec3e5f74c3d21c98988")));
        trie.update(Hex.decode("b553a686ba92ce92ee8c4a94723d31822298c179a48674687c95300529de9c6b"), RLP.encodeElement(Hex.decode("58e39f43842001ee164ada4c6c7382c852fcedf688415ba1d5a8a740eff6d018")));
        trie.update(Hex.decode("9bdabe188e50500994514aa7c323de0e01f9729ae95bf7d6d2b47e8f56e021ab"), RLP.encodeElement(Hex.decode("ef19634a4be1f5e774b77c62bf9bba62a64f35ad29890e59290b30528ba066da")));
        trie.update(Hex.decode("ce6e1fdcfa241f39d91b049124ec1bcff3525c953354d1c171111b66346037f3"), RLP.encodeElement(Hex.decode("7ce309a917e93b47ccdddfc6c92f3a7571619f5aa9b68d39b0459ed4d3644d41")));
        trie.update(Hex.decode("c60586824271d9827069a89c87b61a284c7a213877ca83561ea013299f0ca2c9"), RLP.encodeElement(Hex.decode("47c8189cbfd101665c59a13491ef1a44604c0d590d7483833acc4c536f0b33b0")));
        trie.update(Hex.decode("5624a666e76c90579fc78698a80a6ccd59be07e93ba021f0d9096a314bdc0f65"), RLP.encodeElement(Hex.decode("a5c7ded65d9102702f02a7613afecfa5d00e67a7951ca0f1132096c71cc9a4cb")));
        trie.update(Hex.decode("3b0c4deb0b0ac3ddc411233ee3c31d4eb4b0881f067b442e968704062cc5e6f1"), RLP.encodeElement(Hex.decode("01c84b663bc7e06338138b65df46e1a5ede90e9ea1ce7c9443dd250fee21d3b5")));
        trie.update(Hex.decode("3621cdd4af0433146d7287490b2323131f5e1c154559955f3124d6278bc2c0cf"), RLP.encodeElement(Hex.decode("ff8b5aa8c754f2de4b18a1acbf65f5dc74e12e1052bd83300dd8ccd54f6ed766")));
        trie.update(Hex.decode("c9e7d940c1118982292c91621a7db5123bbcce70d3e237ec76508173e2a34972"), RLP.encodeElement(Hex.decode("d8fb9d0b319667d10be2c26a5a8fb431fef22f3510697b81dda9801cf5494cf3")));
        trie.update(Hex.decode("d07f1d62b18f9cfe7162c5627cb2f82b6a9f4130e6425544b3c8a0c555b50f58"), RLP.encodeElement(Hex.decode("d5017434caa41270a97bde574b5c3b8247c6d66822fbf3aca2247d7a2a1deb12")));
        trie.update(Hex.decode("b77b0d94a63fdb594c6e035bb79129542018ca99af4b33881fe77b90f051b4a6"), RLP.encodeElement(Hex.decode("847890ac1cb28fe1411e3fffc196912ca2692ac9ea7eb6a9c3563c64f6e1fb4c")));
        trie.update(Hex.decode("116763741f0cf1b2629883bda097703af36bca80dd402cd9e3ec294b23ef8ac9"), RLP.encodeElement(Hex.decode("a0c8cdcf7b52b8434876ec099ca1e5be1462cac6f809a6b9f5c814d7275d1f36")));
        trie.update(Hex.decode("378df096008a0ff42461de4f4f3b1dc9e13c37a38e72a28dcbb9beab9bf2908f"), RLP.encodeElement(Hex.decode("52877141438b045d8fc812821c1ef2cad13799f070a3d2fb97a1c14679742667")));
        trie.update(Hex.decode("6c2e81bf4fc3db3821f31feb01d54f5fd9bac2c7805941ea66bbee6f997972c7"), RLP.encodeElement(Hex.decode("9a35f58c21a0a6f9f1d482b6127e9f112ef36605f105c571ba11bedcdb9ac844")));
        trie.update(Hex.decode("9476851534a811269cce81fbda53e322cf90aceacb61db338d2b84517b179bba"), RLP.encodeElement(Hex.decode("74542e75e76b1e2f4a0dcfc8bbfefd1963a7e02afeb45dc07aa3a46d0184ebd5")));
        trie.update(Hex.decode("d259f5bf9d832bc0f24b94e2d0736c1be80d5a3383a122c041263e37d105dd85"), RLP.encodeElement(Hex.decode("7a8ad2d517ad1fbb79c35986956d60d319293b657cca24bd9772ddcdf1061db7")));
        trie.update(Hex.decode("11ff2754d0cbc065eafa7b1166af44c53ce492c64ccb08d4ea82757e8d81319d"), RLP.encodeElement(Hex.decode("ef104fd43072e661ff85d83d2dfa1e778bb14311cb86d79d2e585100875f06c0")));
        trie.update(Hex.decode("7b435773862ba589bdc2396abceca0b1542265f30b00c1c686bae5b7e0faef5b"), RLP.encodeElement(Hex.decode("ab352cdbff1fe2ca1e6f83d924c11348e68b04905120b1d327072049633e8acb")));
        trie.update(Hex.decode("3296852a498a2da50875a399b3505610b8945117864c56473f15414d58c1d18a"), RLP.encodeElement(Hex.decode("0200000000000000")));
        trie.update(Hex.decode("bd8cdaa68cb2da20afdc74bc1d4d2e201cc988e2fc2291e7768db9bfdde01708"), RLP.encodeElement(Hex.decode("5ec366b7c8ea45afa770a8569169e1aa2626e465cc30d2ef26bd2adab65c035c")));
        trie.update(Hex.decode("180469a2fb27e56e9e51ce950211867ac03288ee1a7ac6d1d633b83ec3f127f2"), RLP.encodeElement(Hex.decode("7d9aa192c634dd9abff528c8a4667dfd713549e841648bef4da41f5e0d99e20e")));
        trie.update(Hex.decode("7ce96bc362845345f16c04df160d15c1b0cf0735f1669b354feaa72fce764254"), RLP.encodeElement(Hex.decode("f782879493a19fb3d1803fd174806d18329167e865159cf3e2656256fa595e82")));
        trie.update(Hex.decode("d6d194d3f892ee0e7cc436d1bbc81083b03c0facde6872922770412b5dc3b990"), RLP.encodeElement(Hex.decode("a2")));
        trie.update(Hex.decode("87fc283b9cac4ad48ca359a46f2f213574da538b4ba77ceab8b03d5faf415f3a"), RLP.encodeElement(Hex.decode("5915bbd686103d9a3ac8b18f5105b63f4c300ce5b15aeb1696ee88f55ca72030")));
        trie.update(Hex.decode("5c7b99845ff28cf7b598cc78908e7c91331a5cdfd178fb139b93870d92476d3f"), RLP.encodeElement(Hex.decode("ebd026889428a3aa07f9431a87230442df8998fb5edd0b1af7b983f25b7391f4")));
        trie.update(Hex.decode("9c8eb886fd0ec9cc42ee8569c6189041482123c941d255be3507dad25bcf8cad"), RLP.encodeElement(Hex.decode("53baed5c0736ef752ea7217d20c211ecb79c27213ae1aa371cba6114d182a705")));
        trie.update(Hex.decode("be8434199076ab8259c6622062817b57cc109f1c7443de19732cb55f3da4b0e7"), RLP.encodeElement(Hex.decode("38d66385fece92f3178c82e11db8cead52f09493d2467bdb0bf5889809954956")));
        trie.update(Hex.decode("1298d72fe89ffd92870649fefc0e72dc59eb4fa6f8f16f354d41ca01ee307f50"), RLP.encodeElement(Hex.decode("0200000000000000")));
        trie.update(Hex.decode("ce35afdea369595abdb8dd8027b540930273a7ce51defd7faa1bcd14efc7a2a2"), RLP.encodeElement(Hex.decode("e732a097e8610fc21e61297a11e4b96fd921ba2ee74ab1f29aaedc659c425595")));
        trie.update(Hex.decode("4ede78f766cad437eb2be0ab11fe7f201fe8f13e66725fa8f780eea65140bf46"), RLP.encodeElement(Hex.decode("a3")));
        trie.update(Hex.decode("89832631fb3c3307a103ba2c84ab569c64d6182a18893dcd163f0f1c2090733a"), RLP.encodeElement(Hex.decode("6bc823c7e0c5a260fb899664521cab0a8c78d274600e92e35d66b5d64b45e38f")));
        trie.update(Hex.decode("485383cebb2f91b105f38a334c320278e37b131db3b89d8f1c1ca7627e972731"), RLP.encodeElement(Hex.decode("14")));
        trie.update(Hex.decode("2a8e1a5ca5ffce5e94341117c66f11bf05014639719e7f6824900be9c05d7449"), RLP.encodeElement(Hex.decode("23f040439e57cb2639a0bda5ba7bb050bcf24838102f82389bb9f0ac77bebbee")));
        trie.update(Hex.decode("f4935ffd5e2cc678eddfc875d3671db9d81c19227fa1b3fbb52a7dd90b77d951"), RLP.encodeElement(Hex.decode("85d0aa3ec72ee6fef9c421ac4d358fa0c81135724e4be3d4a27e2b5c362fd197")));
        trie.update(Hex.decode("d843733095e1348bb3ddd62ea0e9db588efae739e5efc3dbc58c6515dfd0f4d1"), RLP.encodeElement(Hex.decode("645e616436817384ded92965ef8baba8ec3c92eb1ae9224d81e307f737d18320")));
        trie.update(Hex.decode("bf06841b5a3efef2967be9302b192e2aceb64eb120c08a5f98dc37088e788938"), RLP.encodeElement(Hex.decode("0188")));
        trie.update(Hex.decode("53e34f7c73111fc1edfdf54aac3335eacbe85998cf026344b16a8ee8b3dd81dd"), RLP.encodeElement(Hex.decode("fe0fde8f22ff9a976bed433c16ba9ee4b3d9eb1886763f8379b6b216c9e29d75")));
        trie.update(Hex.decode("a0a96d93fb03ecad2186a58ad33748a4eb7ccb3876b2c2258d86c53fd16869ee"), RLP.encodeElement(Hex.decode("ae")));
        trie.update(Hex.decode("cdc9a181ef0359b1bcebcc54dd7e3168109fa0a919cba49ce0b2eb5e1dc592c8"), RLP.encodeElement(Hex.decode("6bab64deaa8d1181fd195fc94572f6bbd42573ea0b7e773edf0fc09c90e6f6f2")));
        trie.update(Hex.decode("9a0fead3dcc6146981355241e0d355b778fce29e80c5d4102fe1f002cb0fc0a9"), RLP.encodeElement(Hex.decode("0258")));
        trie.update(Hex.decode("fefa07a9b96444e182eb9e881a3888a9bc0af478770c2ae5a3723fe3178503c6"), RLP.encodeElement(Hex.decode("6b0c993d8f3d73d8b8786ae005a31496dc9ba0e39c761f7c0b8ae2a3e6511025")));
        trie.update(Hex.decode("f7007690ff6582c3cc1a52e1f869b70ebedca4fde6b00895d0cd814aa348ca6c"), RLP.encodeElement(Hex.decode("d5d323424a9389f616585b15dbaa4224b1b34c61623b4ee323a7a635ad95ae26")));
        trie.update(Hex.decode("041e3363a75e60461b8f57e5c7c1ccf7a6adf151a3e1c8a1796b18d491b083d5"), RLP.encodeElement(Hex.decode("031af6fa23cfece6c4bbcf3c4bd8af1617d68651cd5fd16cbb8e4c01d0088295")));
        trie.update(Hex.decode("6d663e28ed0f162677234cfdd3c0f87901eff818725625b56261cf4b9f57b418"), RLP.encodeElement(Hex.decode("189e44e642efd1c44ab4efc29365948ffa981e4bf5e4b77d4b783c1451b43b6b")));
        trie.update(Hex.decode("78147c79026dbd76f8938ce98f72c19428b1febbb4b53ac3db1f10b9d2075039"), RLP.encodeElement(Hex.decode("29d1fa27f188d120fba3d6c528354e9e9fe063d18c3c1c62276a05ed029c2ad6")));
        trie.update(Hex.decode("13d9b5a8bdbc8b2b0f6778c8eea4268c605d9ce29bba7f67a03e0a6ddcbd1932"), RLP.encodeElement(Hex.decode("4e851c71b1f6315984549acce1f42185259e58ad8c36e7b6b167b500c977c860")));
        trie.update(Hex.decode("b1dea76bcf51da6b2a6a04c13f94c12c7837520425737c31ce68607a0693d619"), RLP.encodeElement(Hex.decode("cd6f3a53b1c4a6e52e6852a2f5bfd1cd091dd4b09d65ca3a7fd98dca27f4a9c5")));
        trie.update(Hex.decode("f0d927cce9a7be4c6228fd3c3b50a328183ff18ef814ab5b9ee63a6570cb0f34"), RLP.encodeElement(Hex.decode("4e7973e7aa8d6a4f7985176956403cf30ea0c0a473aa04fa86c0dd40b50bad61")));
        trie.update(Hex.decode("6b2a302f78488221efe68cbc919d8c15df2deb0bd934e06e99ddaf98e8afb470"), RLP.encodeElement(Hex.decode("0443")));
        trie.update(Hex.decode("9c63532e349bb25b1b4d7011f22e70eca37f2cbf8dc89d59f357956765e38823"), RLP.encodeElement(Hex.decode("3f4b756890bf4ff527f5ffe39e41d561ed7e963c780267baaecd6126ecaddffe")));
        trie.update(Hex.decode("faf3e92c21503c5aff5d8f3a20f66c284a287d32c14d88256a5f9d51608dda9a"), RLP.encodeElement(Hex.decode("981f11e42eadb6a1bcfc5afdab8156f6251bec3d321232ded389ea2af9d8817d")));
        trie.update(Hex.decode("0861397596494b23faacd97300e19ab3d9cb108a232888aa527df16dd2a36366"), RLP.encodeElement(Hex.decode("5f87abcf1a2222b57f92e5736d0074bd76341da3b43bb847f1df0cf03514670c")));
        trie.update(Hex.decode("2ededc75f93b435db8d8f3f66b5f421164261a0b8c894560f835e8b5606949fc"), RLP.encodeElement(Hex.decode("a518d94f7cb021c2c5e09af8b8ac4fadca0e26e747a3b710b69992b23b1391a8")));
        trie.update(Hex.decode("6172b3ea79ba8b6ecebc4034dbf1c6a98ab1a0e92201b7a46d2d52357e13e2a9"), RLP.encodeElement(Hex.decode("c9470eda1d11d8ae1e4ff4ab433bd7a18fb6cff84e9089bf53b6123cf4c0afa0")));
        trie.update(Hex.decode("18655b9d4a71e1a22c1bfe0316e2c0bcf2df469fdb25955c321215f8d0cc6ced"), RLP.encodeElement(Hex.decode("7b438dfbdc1ba29eba036fe93692b0351c0ce09990ee5b744b89c6e42f4b04f1")));
        trie.update(Hex.decode("5d6016397a73f5e079297ac5a36fef17b4d9c3831618e63ab105738020ddd720"), RLP.encodeElement(Hex.decode("38a820692912b5f7a3bfefc2a1d4826e1da6beaed5fac6de3d22b18132133991")));
        trie.update(Hex.decode("a50eece07c7db1631545c0069bd8f5f54d5935e215d59097edf258a44ba91634"), RLP.encodeElement(Hex.decode("490ea71a6232f8c905bfb8a0832a1becb5828080e5ed2491b066986ea2161646")));
        trie.update(Hex.decode("ac23efcfa44c803e77ef908c0f9b83cdce41cc30d9b6fafd6a1ef9710d663915"), RLP.encodeElement(Hex.decode("6efc67378a790a653654c03866b8198ec12afaef6aaf77670e7ea262c89dcb52")));
        trie.update(Hex.decode("73a6e4f8d115618acfa36d684a4516bf3876903f9e05eeeb702a8dff8149fd82"), RLP.encodeElement(Hex.decode("55fb193be21812f5e1b2febbf3a4eb7c92af6ffd14ec1229e8007fad4fddc8d3")));
        trie.update(Hex.decode("89a5a523e1225375a10ad37677b917864a1b7040fe7e3d013f46bd2331e8c68c"), RLP.encodeElement(Hex.decode("0736b2b190a3aeb4be96519052c8076dc22a54d7cdacff8149ec22e6f0daf252")));
        trie.update(Hex.decode("3dbee6dc138456e25165f55cc312fb63060b4531d1f2e8d0637c076b255e4932"), RLP.encodeElement(Hex.decode("2bebe0d1448b5dc8e6099ae8932be64adf84bba7f793f3b4915672e92b81e3ba")));
        trie.update(Hex.decode("04b6b57bf813c8846a25aa12bf0bbc78259e49be23958ab4e172823d484135f3"), RLP.encodeElement(Hex.decode("080e119c3364d4f21831aece22c644a600aaae7338259f230aecb35ac79ea03c")));
        trie.update(Hex.decode("2433d2568909434cacafbab78ff0903bd367d628f546e6ed23495fb2941aaf77"), RLP.encodeElement(Hex.decode("61ce6806599d7adfe1a6f5951e74ff44a2e741dfab3ab8990140f19a58314588")));
        trie.update(Hex.decode("86ed7d58cf0765dcf831684fe2a8254ad72f8942984c37ccf03dd2ab7ee7f180"), RLP.encodeElement(Hex.decode("2a5c3b346cfa160afb9b5037e0439a778f7f060e505a4d47a736eba6f0bf37c2")));
        trie.update(Hex.decode("8fb36932a408ddf9f44439a862f85d189569923d49003f5ecdb64e639a2f175a"), RLP.encodeElement(Hex.decode("07d0")));
        trie.update(Hex.decode("21914f65c3fbebecb7f89c332b01d58fb5bdb2f37394ded8d5e28b25dd8d0517"), RLP.encodeElement(Hex.decode("d3e4e513cdce0d1271c08db89a8fe134d0b465c415290c5d24d004f32fb7bbac")));
        trie.update(Hex.decode("783cb1db407eed31a5f0a42370d65587e0c173b79de90f5b73a1e1e9572e32be"), RLP.encodeElement(Hex.decode("77568c0d9f6120aa3a130fd7c7f70978585cd763cf99b901097fb9edb945a673")));
        trie.update(Hex.decode("381a2c843553908fc072f145f235929d14b5adff2a36abfcc79cc122567cd27b"), RLP.encodeElement(Hex.decode("2111e76631eacef64045470671b3089545623682276b3edffef8e4c67f5936b0")));
        trie.update(Hex.decode("cf89916934a6e2250afdd4da1778bd2a611933f04b7dfe688c702b58a9c5d32e"), RLP.encodeElement(Hex.decode("0200000000000000")));
        trie.update(Hex.decode("a6d0d52c7929bcf313d0562082af7f35d958764f53a0aa71a6edbc68cd6d7d04"), RLP.encodeElement(Hex.decode("f1f40fe319e7cc29e258e141204b069cd4611b18d001da06bd38ca7eca397bc2")));
        trie.update(Hex.decode("5611dab682c6a3e4301720fc1bc9a9e386066de775e760363351f25ab66d0140"), RLP.encodeElement(Hex.decode("5fbff4fd023940e9590e8c5b7d05c562fe4152708050451852b95a5f7b1adbd6")));
        trie.update(Hex.decode("26c4052a56177e0ab948aea385cf5499c28d54d62fae9e73c98d93b1e71f1593"), RLP.encodeElement(Hex.decode("905bb0eff15c1af61c9c49bd9c87a6251002cd7b94056d9effadb234f9a3c721")));
        trie.update(Hex.decode("0b9c37979e60e7c8a1a74edef69c96fd72788523620fbc0da38fe9cb0eebf2ec"), RLP.encodeElement(Hex.decode("f10c26a570d16a647fb88a106cb3995d7eeeeceb410239c2747005a6a58971da")));
        trie.update(Hex.decode("b7c774451310d1be4108bc180d1b52823cb0ee0274a6c0081bcaf94f115fb96d"), RLP.encodeElement(Hex.decode("18ffc61e9310c604fbae11fae34bc8f1cecd8aca67e568620a43a6279181632c")));
        trie.update(Hex.decode("fd1669e72c27f06328b42110bae344d6c1c73a2bc06edc90ce5ef9a9e30354e7"), RLP.encodeElement(Hex.decode("06")));
        trie.update(Hex.decode("83efe2e37363e31b2edcd0a32a779cfef0ac7623b3109cce24e3fa3edbcc707d"), RLP.encodeElement(Hex.decode("dd8af4c2b61b6562cf5e3deacffdd4b72c11fe560c5b8bc84975f2f5c06d82bb")));
        trie.update(Hex.decode("da65ae202896d818d419f9107db555747f4a0a03f3d98f9c6676de9d0c8aa8c9"), RLP.encodeElement(Hex.decode("2c")));
        trie.update(Hex.decode("48c47e9d041b2e5e741b507e98668e016e93dd88a3f88bf3e16855d85ec4ea5e"), RLP.encodeElement(Hex.decode("abf927fd4ec98fc3dc1cdde57802dfb16b8f909e59286d55a6f3034be9253c63")));
        trie.update(Hex.decode("aecf791778fa9732a7e2a563d087ca581fcd2fabaa99b151c4ecbf68cb0a2070"), RLP.encodeElement(Hex.decode("8403fec66f46ff350ae078136f87cba6af46e65bf041a39f1c765d2c1c2a5fdb")));
        trie.update(Hex.decode("a008509e90ffd896235731e0fefd31b525a7ed3e2ee5968eb9bb84f46a5956b2"), RLP.encodeElement(Hex.decode("e8")));
        trie.update(Hex.decode("3d5e748900c6841fb55e83e41955a6e06eff901de2101f3a1a9adb1798084383"), RLP.encodeElement(Hex.decode("d7c036c2531d04e3df82722709cbd26f9d1fe40adbea72825331c6107136d387")));
        trie.update(Hex.decode("6a1fe8cdec8de5cc45143044ef2d4e89420a789444ed5348823a1ce74d4f456a"), RLP.encodeElement(Hex.decode("627b590bad15ff62b86981275f54031c9b83a8352b5aff382cb6cb7dd07dc2fd")));
        trie.update(Hex.decode("dcd1b1a8af9c0632097c6d6ddb573f7350786fed38c6e5b7c28e8486bc6e6ada"), RLP.encodeElement(Hex.decode("9fa7e53477023001394ef9a7d65f5d2dffd9b7c975e7b40497a9d582b2665664")));
        trie.update(Hex.decode("4b06cbdab1e5e9639cb12593762e47c04d86425a31da31e01d6dfbb37ad782f3"), RLP.encodeElement(Hex.decode("b1000121002cff4b770f600d718ed52dc36e0a45b88423d9447df1feedbc9091")));
        trie.update(Hex.decode("f32cc8b14e08d8c8f8ad1f18ea9100f54ad2c5b051fe43795649b3fa13d9fc41"), RLP.encodeElement(Hex.decode("c94398add77ec0eb3132e058db8c532b82af6a12301270545a6e5a22653da106")));
        trie.update(Hex.decode("9af4f54171403ea1930bf8a8d25851985b1a4b04d4fcbcb99c15b1e72cb68fc8"), RLP.encodeElement(Hex.decode("07d0")));
        trie.update(Hex.decode("9d575e4fd35c8b8f3b86a8f7dd7069d8feeda529aa79911bff0521753b5409e9"), RLP.encodeElement(Hex.decode("3bf8351734c52a9111ab8c20f918de82b9bc18681caf842c3bc7e711746337f8")));
        trie.update(Hex.decode("9543c3aefc134aee37fe00ea242719e01625ea8460f2ff862b43b5b1ef01a9b3"), RLP.encodeElement(Hex.decode("dd5484674f236a987aa08fccac7e7997ece7caddc497baafa715cb5f69e5a854")));
        trie.update(Hex.decode("31e6bde236e689b9fadf9e18a60ef39083c704798132ca7cfb1fa22908ab4789"), RLP.encodeElement(Hex.decode("8625e2f6de3a418bc5159096ee6ea3cc8533d1f5e4fb6ab4effaafd12350ceed")));
        trie.update(Hex.decode("31e6da9fe21142ea17a1a32f5edd195a18f2948521fda60227923ffd9076d3fb"), RLP.encodeElement(Hex.decode("0223")));
        trie.update(Hex.decode("ac72e4d4db823c67eadd09f2b0d9ec7c60a4a2eeea6c444f5a3ff160239e17db"), RLP.encodeElement(Hex.decode("8cfa2f4d0c3670d5fb8b167750e5a88c407c637e0de1fb42d279a92843ee8f84")));
        trie.update(Hex.decode("7abec6df791e43dac2e0e06b0c9931c594195fccce4f2201103a4b98b370f7d9"), RLP.encodeElement(Hex.decode("2ae36c10c735328ee212bb773e466f4b9a5524a122814bc9d77578f1aa0a89c1")));
        trie.update(Hex.decode("8c27b444fb7e5dd3298a8b8b059d99cad37bacf424b201d157943a9f660214d6"), RLP.encodeElement(Hex.decode("07d0")));
        trie.update(Hex.decode("24ad96f9780444beeac16d249af6aab100174d019fab98cd6c835de63e0cf8f0"), RLP.encodeElement(Hex.decode("0200000000000000")));
        trie.update(Hex.decode("67ccae3e064f93a16611d9c0c287fb74e837843c85a745775267e3b327f0b555"), RLP.encodeElement(Hex.decode("80f9e29d991bd4071bfacc14a2bcf270501f17ea660829647ccb06d53836f418")));
        trie.update(Hex.decode("69a670dfb9517b11da6590c620fd8c839a7161da59ffb3f488352185b0961bf9"), RLP.encodeElement(Hex.decode("24b5ebe4f18329e0c9b306dda9fe906a9982d4f9319f5550859070faf24e2131")));
        trie.update(Hex.decode("cba8353054b4cb3c5d5a8c487c996b24283ba4ddf30ce7321b8388e727db22de"), RLP.encodeElement(Hex.decode("9e83b92297d131c884fb87799ab7dc951192a3148215eb52258da0aac3a59d30")));
        trie.update(Hex.decode("c869a575416e61043f309f4a42b12b2e163ace23a9a26d67af9615ca240c74bb"), RLP.encodeElement(Hex.decode("7a0eb1ddc2714ab5b561ef51bf0035b79a7d18e0fcf3ce811b34a58b3edfa0a3")));
        trie.update(Hex.decode("c7a6e4fa14071b54b9c59b3302d7df346e57db40d6682a8da72e77c128a01ac7"), RLP.encodeElement(Hex.decode("b29eb2eda891c8b488cb9209bcecbc65ce7f16f178847195507af6be88e5d08c")));
        trie.update(Hex.decode("7c1232c1f0c8ed24c9711ef8c455e04e307eefbc4250ebebe59babddf7962509"), RLP.encodeElement(Hex.decode("0a6d51897565c0b66b9b52684c947b46979a392e35c7efe83ef46f66a13e5d51")));
        trie.update(Hex.decode("29606eb3aa0a5bc5898ecd2065b6eedb84d3717217f544a9b82b68134c1ffa42"), RLP.encodeElement(Hex.decode("2f08ca67d48e3c0c11fab10cf601246ac0dd186555f624e59bc383d8f0a5a575")));
        trie.update(Hex.decode("bff4442b8ed600beeb8e26b1279a0f0d14c6edfaec26d968ee13c86f7d4c2ba8"), RLP.encodeElement(Hex.decode("3d595622e5444dd258670ab405b82a467117bd9377dc8fa8c4530528242fe0c5")));
        trie.update(Hex.decode("6361d5226731b3b53167d25beb46c45ebaa3fd003fe2ceb0dad6ba70383fff8a"), RLP.encodeElement(Hex.decode("9316f98767855aaf0e7db2e2e746f4e66a98f256019747f54129fa3615622b8b")));
        trie.update(Hex.decode("daf46c0a594b23f4f3c140699ddaf84acfcd259377285343def0d785a25d6aa2"), RLP.encodeElement(Hex.decode("fe1fdeaa6edc41df19687b88faf83968c9228ad4c2c8d40a82546e6d9789cc65")));
        trie.update(Hex.decode("d575a98c0b656ff38ae7c942f6c4c24ded100ebc370e9dd1d57e49a62c283723"), RLP.encodeElement(Hex.decode("0fe68533177d79b8ffa4762c60f1dc631817189fbc5624148f57c2a7943f935a")));
        trie.update(Hex.decode("48639f04672bea3810915db8fe8567c48171a6ea66246d6b4ad4669f02146a24"), RLP.encodeElement(Hex.decode("0400000000000000")));
        trie.update(Hex.decode("58ed4f692f2bb6d5184b548ae205b4aaa37d21623894c464c3ac84ea9fd0092a"), RLP.encodeElement(Hex.decode("466f8e611f24d5980040805b307263dc14a20e0355721aadde6a75bc6915055c")));
        trie.update(Hex.decode("fcb2fb09b7511bcf5df4bd033599f197b7c1bce900f521dfa6c088ddeff4e911"), RLP.encodeElement(Hex.decode("baa402be574e25fb1677181f0ba72aca899ffaac6e03f142f54accdb2b7f7941")));
        trie.update(Hex.decode("d71c2dfafd52ba693c6201730648b893129e0248a20921d5bb658ffb2bd54ddf"), RLP.encodeElement(Hex.decode("596b53bf85615c61fa28cebefe7d686dfae8a82120591bb65b2c981e54352dc6")));
        trie.update(Hex.decode("12185e6c9ece9f07b29e6fbacf47ec70416e975f39cb29e25eee8858bb627a30"), RLP.encodeElement(Hex.decode("a017fb23158974682d12707b4c6a2854246abb92e37b605c858350fb9854c521")));
        trie.update(Hex.decode("fa85c90331442d78d5ce507d8caf1e4e1775ba90bd8f97ceff150ff92818f137"), RLP.encodeElement(Hex.decode("8040cdc959b0dc0a452016cd139ca4efd02d8359f6ca57fe7840d38558eec2ba")));
        trie.update(Hex.decode("cfa36931e69b0a55ad27ee1c013e8970c9d0fe6f0e673cb7e1e07e0a34eb6f69"), RLP.encodeElement(Hex.decode("1b78e610f40810b10a26ff58576e0779471682eb21db8b0003a03434614a88d1")));
        trie.update(Hex.decode("939b18b31c8b5e40da27f4955d35fc165b7b822eff569477b4291cbe94bc660a"), RLP.encodeElement(Hex.decode("32")));
        trie.update(Hex.decode("6e7d9f961962a5a6117357edb51e71ffbf111fe29b1057f8ebd0638676d566ca"), RLP.encodeElement(Hex.decode("7d7a338fb7b121e5272beaafd65fb70afa7347fdc9ea18d215baef1191845b07")));
        trie.update(Hex.decode("6a0e82e183ca9a167ead62e7995456543a9311a3920ff523b7addf5f9b54d8b4"), RLP.encodeElement(Hex.decode("012c")));
        trie.update(Hex.decode("fbb4a8915dcf50c603964154976cc75490c48bd6950fce7ae9632eff52e97f41"), RLP.encodeElement(Hex.decode("2a3de05f73c0fab6bcb7c4573dabdd3f24952b5418f94112736a3585689bbef7")));
        trie.update(Hex.decode("cbe287abdd69b2ba73a92d3435c16b478e1769f2f3a79209d2641c232ed51be6"), RLP.encodeElement(Hex.decode("f81dfec1489dcb7a16803ccce8b962d8ee4da8ab4fac80d4cd9336a6e646aaf9")));
        trie.update(Hex.decode("54eb8300f2e8d7c42c40c885f88178308957a2c52a6bd8c762cbea0ff5cec069"), RLP.encodeElement(Hex.decode("6208591121b9c9a327f00000aa9ae0e6f9ab95100dfde7eb46e6c064ca3ff5f8")));
        trie.update(Hex.decode("a35b11358fa16fadb87678b3bca2e85b5201c14871713b862fd44f43fbec4000"), RLP.encodeElement(Hex.decode("5ac0")));
        trie.update(Hex.decode("03f8c8c110abb8e9b547a57c2e45370bcc65d43e4878967b54355649e4076b24"), RLP.encodeElement(Hex.decode("fbe458dc8185db4241a003db399637d2439583a1b105fced68b96b4854500e1f")));
        trie.update(Hex.decode("a65b005ea11e4b98949188e40ebf551162adbe6b6221960af56fb0e7ca5054ca"), RLP.encodeElement(Hex.decode("f0d240e28d05cc46198121bd45838abd22631d4a898a6c47ce54f388dec6137d")));
        trie.update(Hex.decode("d62b1d424e064382297724107c5a4965424dda50a160acd1597a378cf9ecd756"), RLP.encodeElement(Hex.decode("b4e4c88951fd60678be774fc9a2de3e5c3e2963976a9d035f6d07c17ef5aa558")));
        trie.update(Hex.decode("67e9ca75f9adb2d5a7570c3e4ec10c843155abdad1938d7231b2bd7291322d93"), RLP.encodeElement(Hex.decode("0e7e5e912b0a54e814f6c6b83595407f4f856017a46f0c071b5fb04330a7aea2")));
        trie.update(Hex.decode("cc19374816314db1929d9284de315d32f184d8622a25fd651075662848cf9fa6"), RLP.encodeElement(Hex.decode("77d1383303b82dbb9bae8589eb221f0f193f75cb18ddbfc47edab5fd19173207")));
        trie.update(Hex.decode("92078fef73a5349729d794f99a1eaee161f4fa33f9932fb8a60d839a388c2a07"), RLP.encodeElement(Hex.decode("6e91b53717e244076aa768eb805ef41b10a21f7727068fd89d8cc38866d6383a")));
        trie.update(Hex.decode("6103dc6613edb74b975fe22aa86df53ec789eb301b6b140921b099d9531bd631"), RLP.encodeElement(Hex.decode("0d1057997231d2f177f0e3cede6f8de841ddf6f252ea230c9b4d127ee5b9a384")));
        trie.update(Hex.decode("b3541cd0fbcf375ca8357a8bc6a4c672c54ac4231e4674132e378cc979097d08"), RLP.encodeElement(Hex.decode("d9912116bb27d00da93f462cc7036c7f81f8718851a963244e79c6a1fdb66759")));
        trie.update(Hex.decode("3a82be56bf5f985774548df063933187528fed2aaa8721ec5b6b313acebe90ac"), RLP.encodeElement(Hex.decode("fd3bebb3d05db2dd79642f522dd2e1b88637ebf9a38c262ed69b155d722adc6e")));
        trie.update(Hex.decode("094c8b5844dcc432c4777759e0a7ec9557106f4701b29c558d9db0c4dd157d29"), RLP.encodeElement(Hex.decode("d44742bcf090eaff20328ccb720e800736159897471cdd4b89aff9a9b0764da7")));
        trie.update(Hex.decode("7999e90614ba2e6296a6fb640f88750642ddd6edc91ade5f82127ddbb7624fa1"), RLP.encodeElement(Hex.decode("6fb466dcfe789780c7331cf61a3a9cc8c11b2a63e73cfdd661dd72e6267a15ff")));
        trie.update(Hex.decode("60e51f54abb68cb24e087978d38ce101b8dc171133bcb9b96305bb26b900d575"), RLP.encodeElement(Hex.decode("5d3ca420c1c41ca402233b1904010f4cd9b2a44d4a94fe6fdb78d6e63a997893")));
        trie.update(Hex.decode("302e1876ba84d3c76b11907728f247a6824d52ff26739f3d1d6b3386fcf759dd"), RLP.encodeElement(Hex.decode("a3")));
        trie.update(Hex.decode("d639f0091d67da72ef09dbf612510f2e266fa713f4a03fe30e89f03eacce1ca6"), RLP.encodeElement(Hex.decode("a3")));
        trie.update(Hex.decode("8a8afcd202d24bf1f840c7a83c0df1ac094a2300baa32adf949485d3bd7fc586"), RLP.encodeElement(Hex.decode("08000000000000")));
        trie.update(Hex.decode("0b392b34ccbb7c9bbf025dfdc66f904834f5ecc1b5f72d89ec49ca7d8cc1387c"), RLP.encodeElement(Hex.decode("0ff05b954de3c24943b74b5b7ad5a4828fb872f2f3edad28f07de3d299c4df71")));
        trie.update(Hex.decode("d72c46e8335aecccc90178b616679ab2ba9ac599c32b549334d8b451e3a42298"), RLP.encodeElement(Hex.decode("47c806cf736d5f7f3e2a327c7186646da415a10c9f8d1c9bfffa2fa11d197b6e")));
        trie.update(Hex.decode("f63492a2a6ad002283b8705d3038d3f01ecb8fe6b64b3c26ed35a6750e1bdb7f"), RLP.encodeElement(Hex.decode("30d0bd5b89b9d7abb20febcec23bad082b2155bc7f93263a0bee629caf517c3a")));
        trie.update(Hex.decode("d9d16d34ffb15ba3a3d852f0d403e2ce1d691fb54de27ac87cd2f993f3ec330f"), RLP.encodeElement(Hex.decode("7f5026f174d59f6f01ff3735773b5e3adef0b9c98f8a8e84e0000f034cfbf35a")));
        trie.update(Hex.decode("08acb25e4a7ca9fa58a94313c83633aa8d9aa96c9a0537e5e38383bf6dfbce42"), RLP.encodeElement(Hex.decode("a1a4efc15b9efe1e3d21263c77c2934c14aca6de05879a0aad2edb9284b6f69a")));
        trie.update(Hex.decode("0be576372c3ef7e3cd0aa75923d16e2d18275cd70bf3ba87833799b7a95a184b"), RLP.encodeElement(Hex.decode("7182cc84a8affefbaa01559665811ab19c3896b5073bb55e57f64e20cb92d7e9")));
        trie.update(Hex.decode("50fe34c022126cae27567047fd17fa0e1cbe1d60cfd08b15c009a7b3e10d1e6b"), RLP.encodeElement(Hex.decode("cc1003282a0f980c362d9e9e3a5a14cc4c04ee0ecced124755e1a6611e3d07d8")));
        trie.update(Hex.decode("37c05ec84c65d4d1d6ba21b7995528f46834cbe7c5852ad74897b3f5e1eaecd2"), RLP.encodeElement(Hex.decode("0200000000000000")));
        trie.update(Hex.decode("0b5bdcbbcc3fbbee343b4a4624010fe3c15a7538add010bd658d461d692b8e79"), RLP.encodeElement(Hex.decode("a43b031e7ad0eb2ecca5d343e041166e339d9af385497c09143163fb96b743d3")));
        trie.update(Hex.decode("e20bfa1ffeccabfcd290e73e73e23cf42fc18d48cd52b933051212e821b9f9ac"), RLP.encodeElement(Hex.decode("179896f0acf7f9a02ea862010db56cf5c2b95b1f91a8287a32ee0c614f611cf3")));
        trie.update(Hex.decode("192615349b9632b29b7e7173739e7850db01ed5dfc5b608367339fbd45ea9970"), RLP.encodeElement(Hex.decode("055148a2c445d269ea5d9fb8e4e383e4095382367f5799d4176e90c76e321456")));
        trie.update(Hex.decode("1b7a50cd84ccf9568c77d99eba62e9bff1b70966d6ec5f46f3668bcbadf1e799"), RLP.encodeElement(Hex.decode("54986a2dde5e317a6cd7571ab459c6d29b2b49dcbbd727aea0ad6ab7bed4f568")));
        trie.update(Hex.decode("7921a1e7df10b8cd592b9863f791049daa3cd71473ba8c91784358003a586dac"), RLP.encodeElement(Hex.decode("9cf00610c3e36873a5b7b09885a7588d6bf8c2b6e4b344d4c761564359d0125d")));
        trie.update(Hex.decode("c393947046d370829c18efd0e64bb9d0802c9f81fe920722ea378cb4a1adf71f"), RLP.encodeElement(Hex.decode("e64340be1db061224d5e98d35a42b95a651e1cb4edff0f7d7a1fbd8326fea831")));
        trie.update(Hex.decode("f5d5acfb38c6b4919338f1ddd5614ce41f7b289404f253d10c89c822fe04a06b"), RLP.encodeElement(Hex.decode("7ed80f56a1a493812bf4d0409364d619f28851bc4d79135c0d4549591b34793c")));
        trie.update(Hex.decode("9b913e477cc7a2bbdfe014eea2c4241ca785aa1b02ecfcc9903dec631d4fb36d"), RLP.encodeElement(Hex.decode("0631")));
        trie.update(Hex.decode("05a7ae71b6d28ebf7ddc01220c049bc67470e9edd7ad72984cb455f7b698132d"), RLP.encodeElement(Hex.decode("6e2afaeda565d9448ceda6722980b96225e6c00bb1b62328dab27c50fde7478b")));
        trie.update(Hex.decode("cebda077fd56f36c5764c0f6a322a889c10287d74f0fa83ca935cec165a4449d"), RLP.encodeElement(Hex.decode("29ccb16c338f9a113c80a79f87ca68b858b552d198a720819d15e3960fbe7334")));
        trie.update(Hex.decode("222eb9f62ce3a73228ea0649200f40f6f10a6fcc6d8c881fe3e4e869e4d6440d"), RLP.encodeElement(Hex.decode("e826f21381791d493b556d781c6927c41885351c1e1c2c94ca23f0bde3da90c5")));
        trie.update(Hex.decode("ba867a5bc5f9623e81801652259a3fbadedd1a7b9da929ac04fe3989ee04b266"), RLP.encodeElement(Hex.decode("0200000000000000")));
        trie.update(Hex.decode("b9f1c89736648c27c2d2b134f0db57fa429a6752adae74b94032b9eada04731d"), RLP.encodeElement(Hex.decode("8e77a467c5d07f2a1b656d9f71c829322b2c36e217efa4870f5524b3386320c8")));
        trie.update(Hex.decode("d049d992ab0b304d330bcde2807f6e8d57dc1ecfff57cbd8e4721ad3fe7bae63"), RLP.encodeElement(Hex.decode("c2c92319b6fdf75e2cbba98ef5c3c7b4d8c3153fd42718fddba93889a13a883e")));
        trie.update(Hex.decode("f94a372441ae2ff8cba44e9cb227815c4eeb9329a33e8827626d7793c4e41c57"), RLP.encodeElement(Hex.decode("a1e6bbde761ffe7ec8b232d142523dea0523dc7ac82b36fe9c15418265b64617")));
        trie.update(Hex.decode("10a81eed9d63d16face5e76357905348e6253d3394086026bb2bf2145d7cc249"), RLP.encodeElement(Hex.decode("5f551443e6bdb02d6c4552bf0faebcf4d4e881fc9c4101a2144a444cea6c2d52")));
        trie.update(Hex.decode("590ff3cea0cdb0acbbb1174141f9abed2cf1a324c0f3a2e4ea6b83fc3ebc989e"), RLP.encodeElement(Hex.decode("de7c89a97911ef1dd15cf270b7da2afb9ca3262d7479fdd648c141ed009d3324")));
        trie.update(Hex.decode("6689763f3779a83455fc7db0772a1079f60edec201de3983c67cecbcd61715b5"), RLP.encodeElement(Hex.decode("0200000000000000")));
        trie.update(Hex.decode("9ccbe4d4aa90a8df289456e07cd8083ccbde822647a9f36e7ef7108ac9b8e242"), RLP.encodeElement(Hex.decode("a6debc084e574ac598d28a33b73e5f2f5ef1744b4bdf066e15a9880418ab0655")));
        trie.update(Hex.decode("56db4655001de8320f4d4eea10ea52f1ba855b8f4c5af2361803f68f0e7d3a4c"), RLP.encodeElement(Hex.decode("173f7ee7f372a7a8ab3c75cc4e8fc4650360bce87138b680bec496f707f42969")));
        trie.update(Hex.decode("9d0f1ea8a0bc1902d54e9da00bfb4ec43f076a099af6ee3af649bc695e4409f1"), RLP.encodeElement(Hex.decode("7a160b4f7db46924b17c3c103517448e1e58ef7938db0cb1cdb5f637e9ce4dd5")));
        trie.update(Hex.decode("2214f09ad300772d1218b915ec1a8535f2f37cb871d8f0c407c65e88c20fa810"), RLP.encodeElement(Hex.decode("6f1e4abde6e54ede07bfe45b2b34e33ceae11042ac3fd526bc3bb05c7bb064b6")));
        trie.update(Hex.decode("a171873522e2a30e0e96ee2c258bc61df24aa1f2c1314138d78796e1b20e9cb6"), RLP.encodeElement(Hex.decode("9458a2b5c6ae05b4abe49de477567bf82ca3c26d525dc0a3aefc090c4b149e78")));
        trie.update(Hex.decode("fa3f3f57fd31ffb2bbf395fba72d8782ae250a311d3359fc7cc50d359ec6fbb4"), RLP.encodeElement(Hex.decode("a3")));
        trie.update(Hex.decode("d0be641177b504ac6f6cd768ed677ed4d244b185f490f73b2458232ebf7988e5"), RLP.encodeElement(Hex.decode("27c1bde7872d2ae7c4f721b1aa447365c68d4eb403c81e0afc020c26e7ef4730")));
        trie.update(Hex.decode("af357fe3f86a001cd4fc4bce46ce97cde5777deb8f7649ccb586ec8baba76e75"), RLP.encodeElement(Hex.decode("5e823ab2c1bf1be0114146d6b62999301b0a6e936d0919a7fc49283c95cdb3ed")));
        trie.update(Hex.decode("1d2fc30c1b6dbc7ebab419c2da53f721b67d627218f7b494d6ea277006a39362"), RLP.encodeElement(Hex.decode("969dcf7bae4b96dade5aed90ec0e34ed636aa0b863d2cf62ddcce1e4e3d82853")));
        trie.update(Hex.decode("20afa5cf190f3753d2a9ce6d794dba6f8e3708dd37729315fd240d28011f8f10"), RLP.encodeElement(Hex.decode("e8")));
        trie.update(Hex.decode("2e574a1b77bc156f8f41d52d622242d4dfe56c72fccdb495ae1fa6a7bdad6358"), RLP.encodeElement(Hex.decode("f05b14bcf1e259b7df87556b897a55db5e3bb36ba8192ed53dbbb11267676ba0")));
        trie.update(Hex.decode("abbb5caa7dda850e60932de0934eb1f9d0f59695050f761dc64e443e5030a569"), RLP.encodeElement(Hex.decode("0f69b5")));
        trie.update(Hex.decode("ed0b3be5140bba331646769062d2dd7bae705b412277bf5416af6e43ac8743af"), RLP.encodeElement(Hex.decode("c840ad5f74907e926d0bf0b17a45e666c29fe44b83c9ab52c581f63cb69dfef4")));
        trie.update(Hex.decode("6ff4532551a45020bf3a55432255f3780d156e2d9710381ce80c03f3ff32c086"), RLP.encodeElement(Hex.decode("fb79748fa3056de9b6df8cd0693c203f8024eedfb88c0abcaa4406184b5db243")));
        trie.update(Hex.decode("e6edc032cc056ab68478b497a667a808adbf6df00b4c43787864d1b460494230"), RLP.encodeElement(Hex.decode("9c4a7f595750db7cdee1143a90f356dc06eef459cfe77988e16dad5b69db0b88")));
        trie.update(Hex.decode("bdf2721bc264ed946e3ca81b8719f775e176e2cc5d8594bd30e61cae0a6568ef"), RLP.encodeElement(Hex.decode("01")));
        trie.update(Hex.decode("a0e040a13cf79dab9e8cf67262ace8bd08d00503d50056c73cb1761fd900b0e6"), RLP.encodeElement(Hex.decode("f0d9ddca9db6ce766aa9d7ed0568f63847f7eee977e5dc22db941de423356e4c")));
        trie.update(Hex.decode("92e9682c7f71d25785ebff3c38a9938a064943191db0ad59ba4582ad94fc6b4a"), RLP.encodeElement(Hex.decode("fa14d6f57d5506d9177de1a74e3617d821491913498b623e84e06a1aa84b079c")));
        trie.update(Hex.decode("12d9b033881eb9dcc64b46cb58ed79971995fe3a069a053fb76a571bbe2491d0"), RLP.encodeElement(Hex.decode("07d0")));
        trie.update(Hex.decode("4212f3a602e9322e1af1500f8110e111eb73d6e00f6555a24ef7524b14935614"), RLP.encodeElement(Hex.decode("6128431b538d14c8ddec5daa1fef7b1acea564d4a009e237cf56f28f96c31af3")));
        trie.update(Hex.decode("8f41c913c26c95e480f76107a116b7582fff3d1fdbcec2e4f633c3c13df13bea"), RLP.encodeElement(Hex.decode("24e4ad17ef84b132881a87ddf7692b355fdee42231ef7162b627ee96f278840b")));
        trie.update(Hex.decode("b4b15fe68b6e2abd1d246e674d30126a582fa4f886baa7b7c274a860fb920157"), RLP.encodeElement(Hex.decode("b13d98f933cbd602a3d9d4626260077678ab210d1e63b3108b231c1758ff9971")));
        trie.update(Hex.decode("1ac5e616f340b89a0b10998ad3f6740c22526d187ee573296b7d51843ea697ce"), RLP.encodeElement(Hex.decode("07d0")));
        trie.update(Hex.decode("a5327fa25e3643986af0f03b5bc0a1226ae71abcc9a5127160201e12b29757e4"), RLP.encodeElement(Hex.decode("14")));
        trie.update(Hex.decode("1b0cbc4ec1bfa349b087eb466fffc80cca22e74787483586e058bf165723fd0f"), RLP.encodeElement(Hex.decode("97a3b210c751ac91549bf23b4335d021fc6ae0655ee906bac89bf7c56df1d56b")));
        trie.update(Hex.decode("15358e5915365f6a021ea6ad2e5e974d75f3adfc4221906e99cddab8d596ca72"), RLP.encodeElement(Hex.decode("28b7a9fd0580ea1b7c8760ef8a8100ee2c03954dead0800988350d121ea65b0d")));
        trie.update(Hex.decode("0a8e7d91d29e3fe3e4558181b0c545349910854deaa81d35efa376a0944e93bb"), RLP.encodeElement(Hex.decode("34276bcf9e79d733276ae872488dc69996d5b8ff967d05650bcb32081351ca47")));
        trie.update(Hex.decode("19db59b00ce10c49b45afa4b3189f709287b2b76c47596c9c0ce227500828000"), RLP.encodeElement(Hex.decode("8b6f345cb113cca410009b46c2116d7b627bc7a40e430d9a4b833757947ae39c")));
        trie.update(Hex.decode("8c8998c70d860b06ebe80ce22500ec1192083a59ca111194cf2a7612d84b0e2a"), RLP.encodeElement(Hex.decode("0165")));
        trie.update(Hex.decode("a31a346bb5c2d4f944f3d37ca1bdd14f8de294987de966e5a640717990c4fe7c"), RLP.encodeElement(Hex.decode("659c5c79e5ed668418489df5fbbd7a4d4225344dc47d8b3fc01b4682a758cc54")));
        trie.update(Hex.decode("093179cfe44b38850d63a5cea59e3a599d472d7a69d1da14474c689ac096dbed"), RLP.encodeElement(Hex.decode("c156781fb99cd32bbe675b4ad5348ed2d9e42e9ca07680b364b144289418b95d")));
        trie.update(Hex.decode("d5d6e669fbe713032e168b8af5f50d41f43705a62dfd33f259c6ba7f4a90a3c2"), RLP.encodeElement(Hex.decode("96f5cdc4d871312f0caa5d8a3a5ba9d1326c26c565e1252115fe4dae54577bee")));
        trie.update(Hex.decode("675f2c29f86724f27e31e0652872d283e16918b871b06fd13828fb25e59bf5bd"), RLP.encodeElement(Hex.decode("bbff1307b55c639ed138a1f7490d0ce81e19efc4c0c4ae41058553b85a5c961d")));
        trie.update(Hex.decode("6cde3cea4b3a3fb2488b2808bae7556f4a405e50f65e1794383bc026131b13c3"), RLP.encodeElement(Hex.decode("8d6a670b0b85c3b528e1f75daa4baa147be723f0efa9091e120d23ae49f4fdb4")));
        trie.update(Hex.decode("fe0cb14ccfc17da78df8a10e3180d59d4cc8421592aebea977c6c7d2879a9c0e"), RLP.encodeElement(Hex.decode("20ae73ce79e6676a35ef4b53f0adea47eb6632144fc3ad0d5062507c739be841")));
        trie.update(Hex.decode("c78f54cd0169fcd0233b1b17b96fb0a7a8ba6c3d8060f2960be81b0ef5f92d7e"), RLP.encodeElement(Hex.decode("92024e281b4e3f50f3c05777da8064dd7573681b821931b3eadd88a886c13253")));
        trie.update(Hex.decode("69ce135cfacc44b225ac9267067f6cd2eaf9cc5b926bc5e2ebe89dc625283f17"), RLP.encodeElement(Hex.decode("5f7778c320cf1a2d5eec762d0d954d286e1a0c968764345e0b9665341abf57be")));
        trie.update(Hex.decode("6add646517a5b0f6793cd5891b7937d28a5b2981a5d88ebc7cd776088fea9041"), RLP.encodeElement(Hex.decode("51a9c235a8ebf057fd676da94e5cf9c76becdf2bd95e95d27036666c33cfd0cd")));
        trie.update(Hex.decode("125cf4dd4e6a5f3996ba89d9aff59318f1e74573203f1c1c0e034bdf7fb64c88"), RLP.encodeElement(Hex.decode("85ac2b3e52fbce1745f91ceda1e372114a58179af5c96eef8c4966c3cf89574c")));
        trie.update(Hex.decode("5f3e824fa06b7b5693776eebf59aadd54a8f47a3d66ccbe12202dd8af469d208"), RLP.encodeElement(Hex.decode("0fb374ef1bf9587d18f25cc363e2d8bcb92e82098d43659287ae1e75e0e35f95")));
        trie.update(Hex.decode("ee54f2dae77536408986fe4186fe4de0699eb6566bfb5ee1ba3d3d1cb6258283"), RLP.encodeElement(Hex.decode("bbcb9999d415b26b69a54d1cff25c5a12530a62dfe7ba415182af848640e74a9")));
        trie.update(Hex.decode("7ecd1dfb10f46e235b21eea01a25dce6d898fd766dd0fc6cca6cb8b3197f1734"), RLP.encodeElement(Hex.decode("f4fca70720a9463a2563df331c7bbb05d84069cef8927318e67fde5e09b2b573")));
        trie.update(Hex.decode("3ed1179554a155cdd834f9e4b68f437da54ab4fdbb59309e001254552da2ecee"), RLP.encodeElement(Hex.decode("f67519329b53ab76c5471835e0cfd12aa417260f468efca710519eb23dfb3763")));
        trie.update(Hex.decode("531bc7c0e737651b04671d30ff63c72ec8c07f38ea689b4241707e67330f555a"), RLP.encodeElement(Hex.decode("2c")));
        trie.update(Hex.decode("0a15caec7d9ac40ca58599d91b8ad75ed1d3d9264d9cab126456c66eec9782c7"), RLP.encodeElement(Hex.decode("e807f47248a2ef8f1d334da958483ebba348b857b4ddccec6f3f0c024e34ddce")));
        trie.update(Hex.decode("8285abe05bbea1fa30d297a91bf5e1318860967f7af040f3a080a88992d7a40f"), RLP.encodeElement(Hex.decode("0200000000000000")));
        trie.update(Hex.decode("dd77f061f56b695c1bacecce109ee28de5e0e3ad4b57b0925044208ad4797616"), RLP.encodeElement(Hex.decode("321db3646207d0dc9512388689b2324cb19b21c60a52b8e960925ed5c2d61844")));
        trie.update(Hex.decode("18395c43a55fbfcdcd81a6817f4d9913d44df63c8b9ae5c7fb60e3962e6ab54a"), RLP.encodeElement(Hex.decode("e8017bbf84a7501fef61f9facad120f9d69006658eb98a3c3781326734f98cda")));
        trie.update(Hex.decode("a73fb944fb67969f43c7ee0e75bb3f5c7e5ba84a5d1c8ea0918620554497d7cb"), RLP.encodeElement(Hex.decode("05")));
        trie.update(Hex.decode("c3a24b0501bd2c13a7e57f2db4369ec4c223447539fc0724a9d55ac4a06ebd4d"), RLP.encodeElement(Hex.decode("d83cc985395738ca471224d57b1ec6408edbc626c13312a07aca372dfdd25497")));
        trie.update(Hex.decode("0316069c49e578cb43dfcee622c71f2f5308d2cfba3f32869de5ef658ad48f74"), RLP.encodeElement(Hex.decode("843b3fa9fc89c04718a10444003a270f5b7e7d0535c5479378a20d7c9cdd2bb6")));
        trie.update(Hex.decode("471365a866fbadea75fabbdacbd2a8a77ed4d9cf90fcebf3ea4249c01b32cc4d"), RLP.encodeElement(Hex.decode("e9af49abe149f2fb390b10fc4ad863879356a41b069c737136239b5721e642f8")));
        trie.update(Hex.decode("e0c034c6d1144ea53b5643198964dda5f5e1b650987cbaf48ddb8736c892299d"), RLP.encodeElement(Hex.decode("7a812b7843555406aaa014603d686c1266d1ad9333510f0f58f9a25d16769e59")));
        trie.update(Hex.decode("57c240e8db79398b4991afb6babb74c715dfab0a7d1986d780aadf54b63c6497"), RLP.encodeElement(Hex.decode("8d9f03f6ead1d797ba221dd48007a0d7dc86eadbaa581c2e8be9626e13756fee")));
        trie.update(Hex.decode("9e206cd43cb2e264eee99afbaba1bfd73d81d44728e06e3455394fcbd7d01bc5"), RLP.encodeElement(Hex.decode("821c22bb530a92e14aaddf01aa246db2cae1d0d0f82b4316e40c77bdaf70711a")));
        trie.update(Hex.decode("2542169f9313a4f6f00a17a5ae33b7db001a26fa18225a46a42a8789ce87a414"), RLP.encodeElement(Hex.decode("ff4a7d156d4c333cfceed474141fdf3afd231db41992ba19f7e5da793fa57852")));
        trie.update(Hex.decode("88b84bbe4400d8e440a220c693c98747ba697d81c9810ed924ee0e20955a8405"), RLP.encodeElement(Hex.decode("012c")));
        trie.update(Hex.decode("e94618bb32c4d70f2fe96196e54f1d7ac63f4f40798298f340bf10773e8fcf8f"), RLP.encodeElement(Hex.decode("04e8d5709351e92bc5ba79b5cb60a1d4ccb70429c316c76cdbe038e376ce139d")));
        trie.update(Hex.decode("769ea0371829b2b8788a251925a8a25e29a712c8d70ed97c481d61fc7d0d922b"), RLP.encodeElement(Hex.decode("f872cc0e9fe9d9b7e176f397a0ee51d7b5350f671113aff46cbe4327b08c90dd")));
        trie.update(Hex.decode("3652e4da9d46f5e1e272f20c01c38ac2734a6fae8919d8e7aec22d86af4b046a"), RLP.encodeElement(Hex.decode("1a9eb57f32f309455f7300d7f45c96cd537d3daf1f5f63a455918b549727a9a7")));
        trie.update(Hex.decode("c1b8f9f4556d374814f6c5686421bae5fc2e716f82b7902c57de65e9e5d84c92"), RLP.encodeElement(Hex.decode("62c42269b4492bfbf61560a4d1838d0b4012e77249c3e9f8972f09d8ca2476bc")));
        trie.update(Hex.decode("553b4de627f3a89baec218b5125432d0c1b53bd7c89e3e9788cc5b15556dde98"), RLP.encodeElement(Hex.decode("9ece50ae57e3d03358fdf2d364bb907c206bfede7f4aee66a924b31bff54c86c")));
        trie.update(Hex.decode("06a247e2f8f8874cb665ad7486e27ce1b030b1ec1e4bcb589e325161cbb1b7a5"), RLP.encodeElement(Hex.decode("31fbc7682150c18aa8102a6c78a78651369578981e7318c04d1cd8ed44ac7ccc")));
        trie.update(Hex.decode("52026a5800c29747821a2d8303897cc42d22a78f9690f48966c4a307fa4038fb"), RLP.encodeElement(Hex.decode("a8581f5c48ec4ed99d0876e3a5d630aa598a94d7134a4e3d238796042355a6b6")));
        trie.update(Hex.decode("f358302d35ec8f83b334e9b475df6af1d77e8976464552977db497edcc90fff7"), RLP.encodeElement(Hex.decode("0200000000000000")));
        trie.update(Hex.decode("b202a0ed153e26e057915869efc93ee01265616776f99b254b28fa5483124825"), RLP.encodeElement(Hex.decode("0200000000000000")));
        trie.update(Hex.decode("44abdef35b2434e1a2d0055ac7ebaf365ff01ce695f525cde968b73d28823d02"), RLP.encodeElement(Hex.decode("33373d8b68d8f593523c9670e6bb7e6b6402c58bf2cc19940c4ea34d818dbc42")));
        trie.update(Hex.decode("ec958002ecc366e11df15f6825b4854609422642b9d9a88e968ea0d535132231"), RLP.encodeElement(Hex.decode("f39cc93b6eea2b3134c0ffcfc799ccc8a14e6ea5ce96995db157ea2454234961")));
        trie.update(Hex.decode("65e7463d1968d9f9d5ef404892f1087ed0bcca438100e2d60c19667bdbee94eb"), RLP.encodeElement(Hex.decode("da10410a25355ceb2fb8b9ba80fba64438c8e101cbb0d5f3ceab230e3739d35a")));
        trie.update(Hex.decode("0000000000000000000000000000000000000000000000000000000000000001"), RLP.encodeElement(Hex.decode("0e")));
        trie.update(Hex.decode("0aaa2af6ff398e745bea70cd515dd451d5d5e2dd44f20a82a804e31edf401a82"), RLP.encodeElement(Hex.decode("eb81155da80c058324ef7a0d100fffa85f5ea459b6f8a48fa110c0875a1f5e63")));
        trie.update(Hex.decode("c28cde59f5a8fc16f69dcb6ee067f244b49aa96d4fe0cc27816d3b5a67092ce7"), RLP.encodeElement(Hex.decode("e7465139177791313600f02a3eb67ffb97120716acc102cd54d1033b56c77acd")));
        trie.update(Hex.decode("0000000000000000000000000000000000000000000000000000000000000000"), RLP.encodeElement(Hex.decode("f1e4b1b0d357ded7a34c08dcac1a5d8d1eda795c")));
        trie.update(Hex.decode("a0ae325f570230fecf2f555caa8405de4e8ae982e1f34f4b99cce9f4f9bc3971"), RLP.encodeElement(Hex.decode("d9ffc42ad1dc9a16b60b4f0f01636929274b785ab6ffccfc2a36f6342603ae1f")));
        trie.update(Hex.decode("b3398202bc31e9a88f20d82b1a808803a3208d5c497776f2bd8ddbebfdfd6aaf"), RLP.encodeElement(Hex.decode("07d0")));
        trie.update(Hex.decode("f2c7a34eea2e31f58bff529e48b02ee22a228f1a09c12f6d36e135e93c7011f7"), RLP.encodeElement(Hex.decode("24b425acee456e2bed8b0842f6add9ddee4a91f9c9410ca15ad57218fa664d26")));
        trie.update(Hex.decode("fbce4e3d4b2463464fa01672dec25533c4b06662b818b039fdd7331514eb272e"), RLP.encodeElement(Hex.decode("0c09cc0c27469bba03fb06651a5bc5ece3ca599ed7cc30eea93dce06083c021c")));
        trie.update(Hex.decode("468bdc175b5a1c07c5e3a146a2d381fbea1a91a3e6dbb9032745c5689dc6ba19"), RLP.encodeElement(Hex.decode("7de630df28f2af1b1dcad560d1a49bb727aa5aaef32eaaafe0e57f899a776e0b")));
        trie.update(Hex.decode("8dabbdb0e48ec9603197879d48560a37703375ec882e276c43f327de8c9950e3"), RLP.encodeElement(Hex.decode("7f11ae961ae384a517e3a5254279a074f4b12729753a61225202441b4c7ac322")));
        trie.update(Hex.decode("013a111c34b13736890c5fe19a53323c37195f650be51f0eeaa45cd7ca0487ca"), RLP.encodeElement(Hex.decode("c072a79ca94a0613ae00e6cd9977c166326a340916d8271e031a12106e385b83")));
        trie.update(Hex.decode("cfc2cb487fab58de32458584e7164dffd57b9c7111e96adb6d573cd88d582dae"), RLP.encodeElement(Hex.decode("5c934e03370987a896bf201e4e404c1140244ce8dab57f6edd552739fac38e1d")));
        trie.update(Hex.decode("0655e543bc3de8d93ccb8b4ab6985fb7f0e826773eebf6bc1922b0688d324e18"), RLP.encodeElement(Hex.decode("feacab32dbe34931811cca0a524ca006aed66eb1ddc5bd6d8f0f66e622e1c743")));
        trie.update(Hex.decode("fc2fb1706d9912bc4f50974f0d2fcbd2f546e1b008f76ca4379acf9247dc381d"), RLP.encodeElement(Hex.decode("b547b39eccf13679ed9f4d744aa18c9351dcc2216b1a2709c0b65fd0238288aa")));
        trie.update(Hex.decode("35f4566157fbeac2085956e37a9c834f0f57296f02462bba81091bbacb136a67"), RLP.encodeElement(Hex.decode("08085b4e26386d1d5652430da84a8b215bfe4efac0209740cdb60f75e46c90d4")));
        trie.update(Hex.decode("679795a0195a1b76cdebb7c51d74e058aee92919b8c3389af86ef24535e8a28c"), RLP.encodeElement(Hex.decode("2f")));
        trie.update(Hex.decode("67dbeac43b1c2ee825222002a822230dd5cd6c6d2e484fb5d63131f2044f903d"), RLP.encodeElement(Hex.decode("fc05004f95628130d3ced0a2c9833b6597ef75a5db37a8cf1d5747cf48104477")));
        trie.update(Hex.decode("2b31c1f0efad7b83695cc8b4a523b763f9a7a55289695212e6f4b743d41dde7a"), RLP.encodeElement(Hex.decode("45b04d293055b46ab127368d1f2cc69d316a6aa15f2793c1ec503c8239a84602")));
        trie.update(Hex.decode("42620a7c1a8fef8c2867c068aaba3e93ec28b8a1152257a44c0ae42b79b91d87"), RLP.encodeElement(Hex.decode("c0cfb250358c9e01d0c36b3582dbef668606071565c66ecbc7a42515d535f61a")));
        trie.update(Hex.decode("92d1833386a1c2686641879f3ed1a27501212472888b0edb562b3cb142650dd9"), RLP.encodeElement(Hex.decode("58187e42651db92e25718e78ce40c6540bac75336fe0b0b9178af6f706009efa")));

        // change on #505829
        trie.delete(Hex.decode("5df1567644a3c48bf9a1b9d2e185cb9731053ab04882571d2d46c27935d11f1d"));

        String root = Hex.toHexString(trie.getRootHash());
        assertEquals("d7b597b334b78c63ddf7beafc96484f5604472c40d438ea514882fab0813f074", root);

        byte[] serializedData = trie.serialize();
        SecureTrie trie2 = new SecureTrie(new HashMapDB());
        trie2.deserialize(serializedData);

        String root2 = Hex.toHexString(trie2.getRootHash());
        assertEquals("d7b597b334b78c63ddf7beafc96484f5604472c40d438ea514882fab0813f074", root2);

        String dump1 = trie.getTrieDump();
        String dump2 = trie2.getTrieDump();

        assertEquals(dump1, dump2);

        // bug that updates the value somewhere before 505k flush
        trie2.update(Hex.decode("8c8998c70d860b06ebe80ce22500ec1192083a59ca111194cf2a7612d84b0e2a"), RLP.encodeElement(Hex.decode("0166")));
        String root3 = Hex.toHexString(trie2.getRootHash());
        assertEquals("9946c12c75fb0e6c657bb94be25661a3f284a823d4e3193ae8961f7335f4d7d1", root3);
    }

    // this case relates to a bug which led us to conflict on Morden network (block #486248)
    // first part of the new Value was converted to String by #asString() during key deletion
    // and some lines after String.getBytes() returned byte array which differed to array before converting
    @Test
    public void testBugFix() throws ParseException, IOException, URISyntaxException {

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("6e929251b981389774af84a07585724c432e2db487381810719c3dd913192ae2", "00000000000000000000000000000000000000000000000000000000000000be");
        dataMap.put("6e92718d00dae27b2a96f6853a0bf11ded08bc658b2e75904ca0344df5aff9ae", "00000000000000000000000000000000000000000000002f0000000000000000");

        TrieImpl trie = new TrieImpl(new HashMapDB());

        for (Map.Entry<String, String> e : dataMap.entrySet()) {
            trie.update(Hex.decode(e.getKey()), Hex.decode(e.getValue()));
        }

        assertArrayEquals(trie.get(Hex.decode("6e929251b981389774af84a07585724c432e2db487381810719c3dd913192ae2")),
                Hex.decode("00000000000000000000000000000000000000000000000000000000000000be"));

        assertArrayEquals(trie.get(Hex.decode("6e92718d00dae27b2a96f6853a0bf11ded08bc658b2e75904ca0344df5aff9ae")),
                Hex.decode("00000000000000000000000000000000000000000000002f0000000000000000"));

        trie.delete(Hex.decode("6e9286c946c6dd1f5d97f35683732dc8a70dc511133a43d416892f527dfcd243"));

        assertArrayEquals(trie.get(Hex.decode("6e929251b981389774af84a07585724c432e2db487381810719c3dd913192ae2")),
                Hex.decode("00000000000000000000000000000000000000000000000000000000000000be"));

        assertArrayEquals(trie.get(Hex.decode("6e92718d00dae27b2a96f6853a0bf11ded08bc658b2e75904ca0344df5aff9ae")),
                Hex.decode("00000000000000000000000000000000000000000000002f0000000000000000"));
    }
}
