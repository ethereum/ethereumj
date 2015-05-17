package org.ethereum.trie;

import org.ethereum.core.AccountState;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.datasource.LevelDbDataSource;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.db.DatabaseImpl;
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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static org.ethereum.crypto.HashUtil.EMPTY_TRIE_HASH;
import static org.ethereum.crypto.SHA3Helper.sha3;
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
    public void reddisTest() throws URISyntaxException, IOException {

        URL massiveUpload_1 = ClassLoader
                .getSystemResource("trie/massive-upload.dmp");

        File file = new File(massiveUpload_1.toURI());
        List<String> strData = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        String dbName = "state";

        long startTime = System.currentTimeMillis();

        Jedis jedis = new Jedis("localhost");
        jedis.flushAll();
        Pipeline pipeline = jedis.pipelined();
        Set<String> keys = jedis.keys("*");
        System.out.println("before: all " + keys.size());

        for (String aStrData : strData) {
            String[] keyVal = aStrData.split("=");

            if (keyVal[0].equals("*"))
                pipeline.del(keyVal[1].getBytes());
            else
                pipeline.set(keyVal[0].getBytes(), keyVal[1].getBytes());
        }

        pipeline.sync();
        keys = jedis.keys("*");
        System.out.println("all " + keys.size());
        for (String key : keys)
            System.out.println(key + " -> " + jedis.get(key));


        System.out.println("time: " + (System.currentTimeMillis() - startTime));
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


}
