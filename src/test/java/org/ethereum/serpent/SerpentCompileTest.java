package org.ethereum.serpent;

import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Assert;
import org.junit.Test;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 13/05/14 10:07
 */
public class SerpentCompileTest {


    @Test    // assign test 1
    public void test1(){

        String code = "a=2";
        String expected = "2 0 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // assign test 2
    public void test2(){

        String code = "a=2\n" +
                      "b=6";
        String expected = "2 0 MSTORE 6 32 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // assign test 3
    public void test3(){

        String code = "a=2\n" +
                      "b=6\n" +
                      "c=b";
        String expected = "2 0 MSTORE 6 32 MSTORE 32 MLOAD 64 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }


    @Test    // assign test 4
    public void test4(){

        String code = "a=2\n" +
                      "b=6\n" +
                      "c=b\n" +
                      "a=c";
        String expected = "2 0 MSTORE 6 32 MSTORE 32 MLOAD 64 MSTORE 64 MLOAD 0 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // assign test 5
    public void test5(){

        String code = "a=2\n" +
                "b=6\n" +
                "c=b\n" +
                "a=c\n" +
                "a=d";
        String expected = "exception";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = null;

        try {
            result = new SerpentToAssemblyCompiler().visit(tree);
        } catch (Exception e) {

            Assert.assertTrue(e instanceof SerpentToAssemblyCompiler.UnassignVarException);
            return;
        }

        // No exception was thrown
        Assert.fail();
    }


    @Test    // expression test 1
    public void test6(){

        String code = "a = 2 * 2";
        String expected = "2 2 MUL 0 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // expression test 2
    public void test7(){

        String code = "a = 2 * 2 xor 2 * 2";
        String expected = "2 2 MUL 2 2 MUL XOR 0 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // expression test 3
    public void test8(){

        String code = "a = 2 | 2 xor 2 * 2";
        String expected = "2 2 2 2 MUL XOR OR 0 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // expression test 4
    public void test9(){

        String code = "a = (2 | 2) xor (2 * 2)";
        String expected = "2 2 OR 2 2 MUL XOR 0 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }


    @Test    // expression test 5
    public void test10(){

        String code = "a = !(2 | 2 xor 2 * 2)";
        String expected = "2 2 2 2 MUL XOR OR NOT 0 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // expression test 6
    public void test11(){

        String code = "a = 2 + 2 * 2 + 2";
        String expected = "2 2 2 MUL ADD 2 ADD 0 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // expression test 7
    public void test12(){

        String code = "a = 2 / 2 * 2 + 2";
        String expected = "2 2 DIV 2 MUL 2 ADD 0 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // expression test 8
    public void test13(){

        String code = "a = 2 - 0x1a * 5 + 0xA";
        String expected = "2 26 5 MUL SUB 10 ADD 0 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // expression test 9
    public void test14(){

        String code = "a = 1 > 2 > 3 > 4";
        String expected = "1 2 GT 3 GT 4 GT 0 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // expression test 10
    public void test15(){

        String code = "a =     not (   1    + 2     *    9 | 8 == 2)";
        String expected = "1 2 9 MUL ADD 8 2 EQ OR NOT 0 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // if elif else test 1
    public void test16(){

        String code = "if 1>2: \n" +
                      "  a=2";
        String expected = "1 2 GT NOT REF_1 JUMPI 2 0 MSTORE REF_0 JUMP LABEL_1 LABEL_0";

        /**
                     1 2 GT NOT REF_1 JUMPI
                         2 0 MSTORE
                         REF_0 JUMP
                         LABEL_1
                     LABEL_0
          */
        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // if elif else test 2
    public void test17(){

        String code = "if 10 > 2 + 5: \n" +
                      "  a=2";
        String expected = "10 2 5 ADD GT NOT REF_1 JUMPI 2 0 MSTORE REF_0 JUMP LABEL_1 LABEL_0";

        /**

         10 2 5 ADD GT NOT REF_1 JUMPI
             2 0 MSTORE
             REF_0 JUMP
             LABEL_1
         LABEL_0

          */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // if elif else test 3
    public void test18(){

        String code = "if 10 > 2 + 5: \n" +
                      "  a=2\n" +
                      "else: \n" +
                      "  c=3\n";
        String expected = "10 2 5 ADD GT NOT REF_1 JUMPI 2 0 MSTORE REF_0 JUMP LABEL_1 3 32 MSTORE LABEL_0";

        /**
             10 2 5 ADD GT NOT REF_1 JUMPI
                2 0 MSTORE REF_0 JUMP
             LABEL_1
                3 32 MSTORE
             LABEL_0
         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // if elif else test 4
    public void test19(){

        String code = "if 10 > 2 + 5: \n" +
                      "  a=2\n" +
                      "else: \n" +
                      "  c=123\n" +
                      "  d=0xFFAA";
        String expected = "10 2 5 ADD GT NOT REF_1 JUMPI 2 0 MSTORE REF_0 JUMP LABEL_1 123 32 MSTORE 65450 64 MSTORE LABEL_0";

        /**
             10 2 5 ADD GT NOT REF_1 JUMPI
                2 0 MSTORE REF_0 JUMP
             LABEL_1
                123 32 MSTORE
                65450 64 MSTORE
             LABEL_0
         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // if elif else test 5
    public void test20(){

        String code = "if 10 > 2 + 5: \n" +
                      "  a=2\n" +
                      "elif 2*2==4: \n" +
                      "  a=3\n" +
                      "else: \n" +
                      "  c=123\n" +
                      "  d=0xFFAA";
        String expected = "10 2 5 ADD GT NOT REF_1 JUMPI 2 0 MSTORE REF_0 JUMP LABEL_1 2 2 MUL 4 EQ NOT REF_2 JUMPI 3 0 MSTORE REF_0 JUMP LABEL_2 123 32 MSTORE 65450 64 MSTORE LABEL_0";

        /**
         10 2 5 ADD GT NOT REF_1 JUMPI
                2 0 MSTORE REF_0 JUMP
            LABEL_1
                2 2 MUL 4 EQ NOT REF_2 JUMPI
                3 0 MSTORE REF_0 JUMP
            LABEL_2
           123 32 MSTORE
           65450 64 MSTORE
         LABEL_0
         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // if elif else test 6
    public void test21(){

        String code = "if 10 > 2 + 5: \n" +
                      "  a=2\n" +
                      "elif 2*2==4: \n" +
                      "  a=3\n" +
                      "elif 2*2+10==40: \n" +
                      "  a=3\n" +
                      "  a=9\n" +
                      "  a=21\n" +
                      "else: \n" +
                      "  c=123\n" +
                      "  d=0xFFAA";
        String expected = "10 2 5 ADD GT NOT REF_1 JUMPI 2 0 MSTORE REF_0 JUMP LABEL_1 2 2 MUL 4 EQ NOT REF_2 JUMPI 3 0 MSTORE REF_0 JUMP LABEL_2 2 2 MUL 10 ADD 40 EQ NOT REF_3 JUMPI 3 0 MSTORE 9 0 MSTORE 21 0 MSTORE REF_0 JUMP LABEL_3 123 32 MSTORE 65450 64 MSTORE LABEL_0";

        /**

         10 2 5 ADD GT NOT REF_1 JUMPI
             2 0 MSTORE REF_0 JUMP
             LABEL_1
          2 2 MUL 4 EQ NOT REF_2 JUMPI
             3 0 MSTORE
             REF_0 JUMP
             LABEL_2
          2 2 MUL 10 ADD 40 EQ NOT REF_3 JUMPI
             3 0 MSTORE
             9 0 MSTORE
             21 0 MSTORE
             REF_0 JUMP
             LABEL_3
           123 32 MSTORE
           65450 64 MSTORE
         LABEL_0         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }


    @Test    // if elif else test 7
    public void test22(){

        String code = "if 10 > 2 + 5: \n" +
                "  a=2\n" +
                "elif 2*2==4: \n" +
                "  a=3\n" +
                "  if a==3:\n" +
                "     q=123\n" +
                "elif 2*2+10==40: \n" +
                "  a=3\n" +
                "  a=9\n" +
                "  a=21\n" +
                "else: \n" +
                "  c=123\n" +
                "  d=0xFFAA";
        String expected = "10 2 5 ADD GT NOT REF_1 JUMPI 2 0 MSTORE REF_0 JUMP LABEL_1 2 2 MUL 4 EQ NOT REF_2 JUMPI 3 0 MSTORE 0 MLOAD 3 EQ NOT REF_4 JUMPI 123 32 MSTORE REF_3 JUMP LABEL_4 LABEL_3 REF_0 JUMP LABEL_2 2 2 MUL 10 ADD 40 EQ NOT REF_5 JUMPI 3 0 MSTORE 9 0 MSTORE 21 0 MSTORE REF_0 JUMP LABEL_5 123 64 MSTORE 65450 96 MSTORE LABEL_0";

        /**
                10 2 5 ADD GT NOT REF_1 JUMPI
                        2 0 MSTORE
                        REF_0 JUMP
                        LABEL_1
                2 2 MUL 4 EQ NOT REF_2 JUMPI
                        3 0 MSTORE
                        0 MLOAD 3 EQ NOT REF_4 JUMPI
                                123 32 MSTORE
                                REF_3 JUMP
                         LABEL_4
                         LABEL_3
                         REF_0 JUMP
                         LABEL_2
                2 2 MUL 10 ADD 40 EQ NOT REF_5 JUMPI
                         3 0 MSTORE
                         9 0 MSTORE
                         21 0 MSTORE
                         REF_0 JUMP
                         LABEL_5
                  123 64 MSTORE
                  65450 96 MSTORE
                LABEL_0
         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // if elif else test 8
    public void test23(){

        String code = "if (10 > 2 + 5) && (2 * 7 > 96): \n" +
                "  a=2\n" +
                "elif 2*2==4: \n" +
                "  a=3\n" +
                "  if a==3:\n" +
                "     q=123\n" +
                "elif 2*2+10==40: \n" +
                "  a=3\n" +
                "  a=9\n" +
                "  a=21\n" +
                "else: \n" +
                "  c=123\n" +
                "  d=0xFFAA";
        String expected = "10 2 5 ADD GT 2 7 MUL 96 GT NOT NOT MUL NOT REF_1 JUMPI 2 0 MSTORE REF_0 JUMP LABEL_1 2 2 MUL 4 EQ NOT REF_2 JUMPI 3 0 MSTORE 0 MLOAD 3 EQ NOT REF_4 JUMPI 123 32 MSTORE REF_3 JUMP LABEL_4 LABEL_3 REF_0 JUMP LABEL_2 2 2 MUL 10 ADD 40 EQ NOT REF_5 JUMPI 3 0 MSTORE 9 0 MSTORE 21 0 MSTORE REF_0 JUMP LABEL_5 123 64 MSTORE 65450 96 MSTORE LABEL_0";

        /**
             10 2 5 ADD GT 2 7 MUL 96 GT NOT NOT MUL NOT REF_1 JUMPI
                 2 0 MSTORE
                 REF_0 JUMP
                 LABEL_1
             2 2 MUL 4 EQ NOT REF_2 JUMPI
                3 0 MSTORE
                0 MLOAD 3 EQ NOT REF_4 JUMPI
                     123 32 MSTORE REF_3 JUMP
                LABEL_4
                LABEL_3
                REF_0 JUMP
                LABEL_2
             2 2 MUL 10 ADD 40 EQ NOT REF_5 JUMPI
                3 0 MSTORE
                9 0 MSTORE
                21 0 MSTORE
                REF_0 JUMP
                LABEL_5
             123 64 MSTORE
             65450 96 MSTORE

             LABEL_0

         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // if elif else test 9
    public void test24(){

        String code = "a = 20\n" +
                      "b = 40\n" +
                      "if a == 20: \n" +
                      "  a = 30\n" +
                      "if b == 40: \n" +
                      "  b = 50\n";
        String expected = "20 0 MSTORE 40 32 MSTORE 0 MLOAD 20 EQ NOT REF_1 JUMPI 30 0 MSTORE REF_0 JUMP LABEL_1 LABEL_0 32 MLOAD 40 EQ NOT REF_3 JUMPI 50 32 MSTORE REF_2 JUMP LABEL_3 LABEL_2";

        /**

         20 0 MSTORE
         40 32 MSTORE
         0 MLOAD 20 EQ NOT REF_1 JUMPI
             30 0 MSTORE
             REF_0 JUMP
             LABEL_1
             LABEL_0
         32 MLOAD 40 EQ NOT REF_3 JUMPI
             50 32 MSTORE
             REF_2 JUMP
             LABEL_3
             LABEL_2

         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // if elif else test 10
    public void test25(){

        String code =   "a = 20\n" +
                        "b = 40\n" +
                        "if a == 20: \n" +
                        "  a = 30\n" +
                        "a = 70\n" +
                        "if b == 40: \n" +
                        "  b = 50\n";
        String expected = "20 0 MSTORE 40 32 MSTORE 0 MLOAD 20 EQ NOT REF_1 JUMPI 30 0 MSTORE REF_0 JUMP LABEL_1 LABEL_0 70 0 MSTORE 32 MLOAD 40 EQ NOT REF_3 JUMPI 50 32 MSTORE REF_2 JUMP LABEL_3 LABEL_2";

        /**

         20 0 MSTORE
         40 32 MSTORE
         0 MLOAD 20 EQ NOT REF_1 JUMPI
         30 0 MSTORE
         REF_0 JUMP
         LABEL_1
         LABEL_0
         70 0 MSTORE
         32 MLOAD 40 EQ NOT REF_3 JUMPI
         50 32 MSTORE
         REF_2 JUMP
         LABEL_3
         LABEL_2

         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // if elif else test 11
    public void test26(){

        String code =   "if 2>1: \n" +
                        " if 3>2: \n" +
                        "  if 4>3:\n" +
                        "   if 5>4:\n" +
                        "     a = 10\n";
        String expected = "2 1 GT NOT REF_7 JUMPI 3 2 GT NOT REF_6 JUMPI 4 3 GT NOT REF_5 JUMPI 5 4 GT NOT REF_4 JUMPI 10 0 MSTORE REF_3 JUMP LABEL_4 LABEL_3 REF_2 JUMP LABEL_5 LABEL_2 REF_1 JUMP LABEL_6 LABEL_1 REF_0 JUMP LABEL_7 LABEL_0";

        /**

         2 1 GT NOT REF_7 JUMPI
           3 2 GT NOT REF_6 JUMPI
             4 3 GT NOT REF_5 JUMPI
               5 4 GT NOT REF_4 JUMPI
                  10 0 MSTORE
                  REF_3 JUMP
         LABEL_4 LABEL_3 REF_2 JUMP LABEL_5 LABEL_2 REF_1 JUMP LABEL_6 LABEL_1 REF_0 JUMP LABEL_7 LABEL_0

         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }

    @Test    // if elif else test 12
    public void test27(){

        String code =   "if 2>1: \n" +
                " if 3>2: \n" +
                "  if 4>3:\n" +
                "   if 5>4:\n" +
                "     a = 10\n";
        String expected = "2 1 GT NOT REF_7 JUMPI 3 2 GT NOT REF_6 JUMPI 4 3 GT NOT REF_5 JUMPI 5 4 GT NOT REF_4 JUMPI 10 0 MSTORE REF_3 JUMP LABEL_4 LABEL_3 REF_2 JUMP LABEL_5 LABEL_2 REF_1 JUMP LABEL_6 LABEL_1 REF_0 JUMP LABEL_7 LABEL_0";

        /**

         2 1 GT NOT REF_7 JUMPI
         3 2 GT NOT REF_6 JUMPI
         4 3 GT NOT REF_5 JUMPI
         5 4 GT NOT REF_4 JUMPI
         10 0 MSTORE
         REF_3 JUMP
         LABEL_4 LABEL_3 REF_2 JUMP LABEL_5 LABEL_2 REF_1 JUMP LABEL_6 LABEL_1 REF_0 JUMP LABEL_7 LABEL_0

         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }


    @Test    // if elif else test 13
    public void test28(){

        String code =   "if 2>1: \n" +
                        " if 3>2: \n" +
                        "  if 4>3:\n" +
                        "   if 5>4:\n" +
                        "     a = 10\n" +
                        "   else:\n" +
                        "     b = 20\n";
        String expected = "2 1 GT NOT REF_7 JUMPI 3 2 GT NOT REF_6 JUMPI 4 3 GT NOT REF_5 JUMPI 5 4 GT NOT REF_4 JUMPI 10 0 MSTORE REF_3 JUMP LABEL_4 20 32 MSTORE LABEL_3 REF_2 JUMP LABEL_5 LABEL_2 REF_1 JUMP LABEL_6 LABEL_1 REF_0 JUMP LABEL_7 LABEL_0";

        /**

          2 1 GT NOT REF_7 JUMPI
            3 2 GT NOT REF_6 JUMPI
             4 3 GT NOT REF_5 JUMPI
              5 4 GT NOT REF_4 JUMPI
                10 0 MSTORE REF_3 JUMP
                LABEL_4
                20 32 MSTORE
                LABEL_3
         REF_2 JUMP LABEL_5 LABEL_2 REF_1 JUMP LABEL_6 LABEL_1 REF_0 JUMP LABEL_7 LABEL_0
         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(result, expected);
    }


    @Test    // if elif else test 14
    public void test29(){

        String code =   "  if 2>4:  \n" +
                        " a=20      \n" +
                        "           \n" +
                        "           \n";
        String expected = "";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);

        ParseTree tree = null;
        try {
            tree = parser.parse();
        } catch (Throwable e) {

            Assert.assertTrue(e instanceof ParserUtils.AntlrParseException);
            return;
        }

        Assert.fail("Should be indent error thrown");
    }

    @Test    // if elif else test 15
    public void test30(){

        String code =   "if 2>4:  \n" +
                        "    a=20   \n" +
                        "  else:  \n" +
                        "    a=40   \n";
        String expected = "";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);

        ParseTree tree = null;
        try {
            tree = parser.parse();
        } catch (Throwable e) {

            Assert.assertTrue(e instanceof ParserUtils.AntlrParseException);
            return;
        }

        Assert.fail("Should be indent error thrown");
    }


    @Test    // if elif else test 16
    public void test31(){

        String code =   "if 2>4:    \n" +
                        "    a=20   \n" +
                        " elif 2<9: \n" +
                        "    a=40   \n" +
                        "else:      \n" +
                        "    a=40   \n";
        String expected = "";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);

        ParseTree tree = null;
        try {
            tree = parser.parse();
        } catch (Throwable e) {

            Assert.assertTrue(e instanceof ParserUtils.AntlrParseException);
            return;
        }

        Assert.fail("Should be indent error thrown");
    }

    /**
     * todo: more testing for if-elif-else  4 tests
     * todo: more testing for special functions 10
     * todo: more testing for while function 10
     */
}
