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

        Assert.assertEquals(expected, result);
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

        Assert.assertEquals(expected, result);
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

        Assert.assertEquals(expected, result);
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

        Assert.assertEquals(expected, result);
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

        Assert.assertEquals(expected, result);
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

        Assert.assertEquals(expected, result);
    }

    @Test    // expression test 3
    public void test8(){

        String code = "a = 2 | 2 xor 2 * 2";
        String expected = "2 2 MUL 2 XOR 2 OR 0 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
    }

    @Test    // expression test 4
    public void test9(){

        String code = "a = (2 | 2) xor (2 * 2)";
        String expected = "2 2 MUL 2 2 OR XOR 0 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
    }


    @Test    // expression test 5
    public void test10(){

        String code = "a = !(2 | 2 xor 2 * 2)";
        String expected = "2 2 MUL 2 XOR 2 OR NOT 0 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
    }

    @Test    // expression test 6
    public void test11(){

        String code = "a = 2 + 2 * 2 + 2";
        String expected = "2 2 2 MUL 2 ADD ADD 0 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
    }

    @Test    // expression test 7
    public void test12(){

        String code = "a = 2 / 1 * 2 + 2";
        String expected = "2 2 1 2 DIV MUL ADD 0 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
    }

    @Test    // expression test 8
    public void test13(){

        String code = "a = 2 - 0x1a * 5 + 0xA";
        String expected = "10 5 26 MUL 2 SUB ADD 0 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
    }

    @Test    // expression test 9
    public void test14(){

        String code = "a = 1 > 2 > 3 > 4";
        String expected = "4 3 2 1 GT GT GT 0 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
    }

    @Test    // expression test 10
    public void test15(){

        String code = "a =     not (   1    + 2     *    9 | 8 == 2)";
        String expected = "2 8 EQ 9 2 MUL 1 ADD OR NOT 0 MSTORE";

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
    }

    @Test    // if elif else test 1
    public void test16(){

        String code = "if 1>2: \n" +
                      "  a=2";
        String expected = "2 1 GT NOT REF_1 JUMPI 2 0 MSTORE REF_0 JUMP LABEL_1 LABEL_0";

        /**

            2 1 GT NOT REF_1 JUMPI
                 2 0 MSTORE
                 REF_0 JUMP
            LABEL_1 LABEL_0

          */
        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
    }

    @Test    // if elif else test 2
    public void test17(){

        String code = "if 10 > 2 + 5: \n" +
                      "  a=2";
        String expected = "5 2 ADD 10 GT NOT REF_1 JUMPI 2 0 MSTORE REF_0 JUMP LABEL_1 LABEL_0";

        /**

         5 2 ADD 10 GT NOT REF_1 JUMPI
            2 0 MSTORE
            REF_0 JUMP
         LABEL_1 LABEL_0

          */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
    }

    @Test    // if elif else test 3
    public void test18(){

        String code = "if 10 > 2 + 5: \n" +
                      "  a=2\n" +
                      "else: \n" +
                      "  c=3\n";
        String expected = "5 2 ADD 10 GT NOT REF_1 JUMPI 2 0 MSTORE REF_0 JUMP LABEL_1 3 32 MSTORE LABEL_0";

        /**
            5 2 ADD 10 GT NOT REF_1 JUMPI
                2 0 MSTORE
                REF_0 JUMP
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

        Assert.assertEquals(expected, result);
    }

    @Test    // if elif else test 4
    public void test19(){

        String code = "if 10 > 2 + 5: \n" +
                      "  a=2\n" +
                      "else: \n" +
                      "  c=123\n" +
                      "  d=0xFFAA";
        String expected = "5 2 ADD 10 GT NOT REF_1 JUMPI 2 0 MSTORE REF_0 JUMP LABEL_1 123 32 MSTORE 65450 64 MSTORE LABEL_0";

        /**
            5 2 ADD 10 GT NOT REF_1 JUMPI
               2 0 MSTORE
               REF_0 JUMP
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

        Assert.assertEquals(expected, result);
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
        String expected = "5 2 ADD 10 GT NOT REF_1 JUMPI 2 0 MSTORE REF_0 JUMP LABEL_1 4 2 2 MUL EQ NOT REF_2 JUMPI 3 0 MSTORE REF_0 JUMP LABEL_2 123 32 MSTORE 65450 64 MSTORE LABEL_0";

        /**
            5 2 ADD 10 GT NOT REF_1 JUMPI
               2 0 MSTORE
               REF_0 JUMP
               LABEL_1
            4 2 2 MUL EQ NOT REF_2 JUMPI
               3 0 MSTORE
               REF_0 JUMP
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

        Assert.assertEquals(expected, result);
    }

    @Test    // if elif else test 6
    public void test21(){

        String code = "if 10 < 2 + 5: \n" +
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
        String expected = "5 2 ADD 10 LT NOT REF_1 JUMPI 2 0 MSTORE REF_0 JUMP LABEL_1 4 2 2 MUL EQ NOT REF_2 JUMPI 3 0 MSTORE REF_0 JUMP LABEL_2 40 10 2 2 MUL ADD EQ NOT REF_3 JUMPI 3 0 MSTORE 9 0 MSTORE 21 0 MSTORE REF_0 JUMP LABEL_3 123 32 MSTORE 65450 64 MSTORE LABEL_0";

        /**

         5 2 ADD 10 LT NOT REF_1 JUMPI
             2 0 MSTORE
             REF_0 JUMP
             LABEL_1
         4 2 2 MUL EQ NOT REF_2 JUMPI
             3 0 MSTORE
             REF_0 JUMP
             LABEL_2
         40 10 2 2 MUL ADD EQ NOT REF_3 JUMPI
             3 0 MSTORE
             9 0 MSTORE
             21 0 MSTORE
             REF_0 JUMP
             LABEL_3
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

        Assert.assertEquals(expected, result);
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
        String expected = "5 2 ADD 10 GT NOT REF_1 JUMPI 2 0 MSTORE REF_0 JUMP LABEL_1 4 2 2 MUL EQ NOT REF_2 JUMPI 3 0 MSTORE 3 0 MLOAD EQ NOT REF_4 JUMPI 123 32 MSTORE REF_3 JUMP LABEL_4 LABEL_3 REF_0 JUMP LABEL_2 40 10 2 2 MUL ADD EQ NOT REF_5 JUMPI 3 0 MSTORE 9 0 MSTORE 21 0 MSTORE REF_0 JUMP LABEL_5 123 64 MSTORE 65450 96 MSTORE LABEL_0";

        /**
             5 2 ADD 10 GT NOT REF_1 JUMPI
                  2 0 MSTORE
                  REF_0 JUMP
                  LABEL_1
             4 2 2 MUL EQ NOT REF_2 JUMPI
                  3 0 MSTORE
                  3 0 MLOAD EQ NOT REF_4 JUMPI
                    123 32 MSTORE
                    REF_3 JUMP
                    LABEL_4
                    LABEL_3
                    REF_0
                    JUMP LABEL_2
             40 10 2 2 MUL ADD EQ NOT REF_5 JUMPI
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

        Assert.assertEquals(expected, result);
    }

    @Test    // if elif else test 8
    public void test23(){

        String code = "if (10 >= 2 + 5) && (2 * 7 > 96): \n" +
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
        String expected = "96 7 2 MUL GT 5 2 ADD 10 LT NOT NOT NOT MUL NOT REF_1 JUMPI 2 0 MSTORE REF_0 JUMP LABEL_1 4 2 2 MUL EQ NOT REF_2 JUMPI 3 0 MSTORE 3 0 MLOAD EQ NOT REF_4 JUMPI 123 32 MSTORE REF_3 JUMP LABEL_4 LABEL_3 REF_0 JUMP LABEL_2 40 10 2 2 MUL ADD EQ NOT REF_5 JUMPI 3 0 MSTORE 9 0 MSTORE 21 0 MSTORE REF_0 JUMP LABEL_5 123 64 MSTORE 65450 96 MSTORE LABEL_0";

        /**
            96 7 2 MUL GT 5 2 ADD 10 LT NOT NOT NOT MUL NOT REF_1 JUMPI
                2 0 MSTORE
                REF_0 JUMP
                LABEL_1
            4 2 2 MUL EQ NOT REF_2 JUMPI
                3 0 MSTORE
                3 0 MLOAD EQ NOT REF_4 JUMPI
                   123 32 MSTORE
                   REF_3 JUMP
                   LABEL_4
                   LABEL_3
                REF_0 JUMP
                LABEL_2
            40 10 2 2 MUL ADD EQ NOT REF_5 JUMPI
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

        Assert.assertEquals(expected, result);
    }

    @Test    // if elif else test 9
    public void test24(){

        String code = "a = 20\n" +
                      "b = 40\n" +
                      "if a == 20: \n" +
                      "  a = 30\n" +
                      "if b == 40: \n" +
                      "  b = 50\n";
        String expected = "20 0 MSTORE 40 32 MSTORE 20 0 MLOAD EQ NOT REF_1 JUMPI 30 0 MSTORE REF_0 JUMP LABEL_1 LABEL_0 40 32 MLOAD EQ NOT REF_3 JUMPI 50 32 MSTORE REF_2 JUMP LABEL_3 LABEL_2";

        /**

            20 0 MSTORE
            40 32 MSTORE
            20 0 MLOAD EQ NOT REF_1 JUMPI
                30 0 MSTORE REF_0 JUMP
                LABEL_1
                LABEL_0
            40 32 MLOAD EQ NOT REF_3 JUMPI
                50 32 MSTORE
                REF_2 JUMP
            LABEL_3 LABEL_2

         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
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
        String expected = "20 0 MSTORE 40 32 MSTORE 20 0 MLOAD EQ NOT REF_1 JUMPI 30 0 MSTORE REF_0 JUMP LABEL_1 LABEL_0 70 0 MSTORE 40 32 MLOAD EQ NOT REF_3 JUMPI 50 32 MSTORE REF_2 JUMP LABEL_3 LABEL_2";

        /**

         20 0 MSTORE
         40 32 MSTORE
         20 0 MLOAD EQ NOT REF_1 JUMPI
            30 0 MSTORE
            REF_0 JUMP
            LABEL_1
            LABEL_0
         70 0 MSTORE
         40 32 MLOAD EQ NOT REF_3 JUMPI
            50 32 MSTORE
            REF_2 JUMP
         LABEL_3 LABEL_2

         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
    }

    @Test    // if elif else test 11
    public void test26(){

        String code =   "if 2>1: \n" +
                        " if 3>2: \n" +
                        "  if 4>3:\n" +
                        "   if 5>4:\n" +
                        "     a = 10\n";
        String expected = "1 2 GT NOT REF_7 JUMPI 2 3 GT NOT REF_6 JUMPI 3 4 GT NOT REF_5 JUMPI 4 5 GT NOT REF_4 JUMPI 10 0 MSTORE REF_3 JUMP LABEL_4 LABEL_3 REF_2 JUMP LABEL_5 LABEL_2 REF_1 JUMP LABEL_6 LABEL_1 REF_0 JUMP LABEL_7 LABEL_0";

        /**

         1 2 GT NOT REF_7 JUMPI
           2 3 GT NOT REF_6 JUMPI
             3 4 GT NOT REF_5 JUMPI
               4 5 GT NOT REF_4 JUMPI
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

        Assert.assertEquals(expected, result);
    }

    @Test    // if elif else test 12
    public void test27(){

        String code =   "if 2>1: \n" +
                " if 3>2: \n" +
                "  if 4>3:\n" +
                "   if 5>4:\n" +
                "     a = 10\n";
        String expected = "1 2 GT NOT REF_7 JUMPI 2 3 GT NOT REF_6 JUMPI 3 4 GT NOT REF_5 JUMPI 4 5 GT NOT REF_4 JUMPI 10 0 MSTORE REF_3 JUMP LABEL_4 LABEL_3 REF_2 JUMP LABEL_5 LABEL_2 REF_1 JUMP LABEL_6 LABEL_1 REF_0 JUMP LABEL_7 LABEL_0";

        /**

         1 2 GT NOT REF_7 JUMPI
           2 3 GT NOT REF_6 JUMPI
             3 4 GT NOT REF_5 JUMPI
               4 5 GT NOT REF_4 JUMPI
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

        Assert.assertEquals(expected, result);
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
        String expected = "1 2 GT NOT REF_7 JUMPI 2 3 GT NOT REF_6 JUMPI 3 4 GT NOT REF_5 JUMPI 4 5 GT NOT REF_4 JUMPI 10 0 MSTORE REF_3 JUMP LABEL_4 20 32 MSTORE LABEL_3 REF_2 JUMP LABEL_5 LABEL_2 REF_1 JUMP LABEL_6 LABEL_1 REF_0 JUMP LABEL_7 LABEL_0";

        /**

         1 2 GT NOT REF_7 JUMPI
           2 3 GT NOT REF_6 JUMPI
             3 4 GT NOT REF_5 JUMPI
               4 5 GT NOT REF_4 JUMPI
                     10 0 MSTORE
                     REF_3 JUMP
               LABEL_4
                     20 32 MSTORE
               LABEL_3 REF_2 JUMP LABEL_5 LABEL_2 REF_1 JUMP LABEL_6 LABEL_1 REF_0 JUMP LABEL_7 LABEL_0

         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
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

    @Test    // if elif else test 17
    public void test32(){

        String code =   "if 2*2==4:         \n" +
                        "    if 3*3==9:     \n" +
                        "      if 4*4==16:  \n" +
                        "        a=20       \n" +
                        "    else:          \n" +
                        "        b=20       \n";
        String expected = "4 2 2 MUL EQ NOT REF_5 JUMPI 9 3 3 MUL EQ NOT REF_4 JUMPI 16 4 4 MUL EQ NOT REF_3 JUMPI 20 0 MSTORE REF_2 JUMP LABEL_3 LABEL_2 REF_1 JUMP LABEL_4 20 32 MSTORE LABEL_1 REF_0 JUMP LABEL_5 LABEL_0";

        /**

         4 2 2 MUL EQ NOT REF_5 JUMPI
           9 3 3 MUL EQ NOT REF_4 JUMPI
             16 4 4 MUL EQ NOT REF_3 JUMPI
                  20 0 MSTORE
                  REF_2 JUMP
             LABEL_3 LABEL_2 REF_1 JUMP LABEL_4
                  20 32 MSTORE
             LABEL_1 REF_0 JUMP LABEL_5 LABEL_0
         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
    }



    @Test    // if elif else test 18
    public void test33(){

        String code =   "if 2*2==4:         \n" +
                        "    if 3*3==9:     \n" +
                        "      if 4*4==16:  \n" +
                        "        a=20       \n" +
                        "    else:          \n" +
                        "                   \n";
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

    @Test    // if elif else test 19
    public void test34(){

        String code =   "if 2>1:    \n" +
                        " if 3>2:   \n" +
                        "  if 4>3:  \n" +
                        "   if 5>4: \n" +
                        "     a = 10\n" +
                        "   else:   \n" +
                        "     b = 20\n" +
                        "  elif 2*2 != 4: \n" +
                        "     a = 15\n";
        String expected = "1 2 GT NOT REF_8 JUMPI 2 3 GT NOT REF_7 JUMPI 3 4 GT NOT REF_5 JUMPI 4 5 GT NOT REF_4 JUMPI 10 0 MSTORE REF_3 JUMP LABEL_4 20 32 MSTORE LABEL_3 REF_2 JUMP LABEL_5 4 2 2 MUL EQ NOT NOT REF_6 JUMPI 15 0 MSTORE REF_2 JUMP LABEL_6 LABEL_2 REF_1 JUMP LABEL_7 LABEL_1 REF_0 JUMP LABEL_8 LABEL_0";

        /**

         1 2 GT NOT REF_8 JUMPI
           2 3 GT NOT REF_7 JUMPI
             3 4 GT NOT REF_5 JUMPI
               4 5 GT NOT REF_4 JUMPI
                   10 0 MSTORE
                   REF_3 JUMP
               LABEL_4
                   20 32 MSTORE
                   LABEL_3 REF_2 JUMP
             LABEL_5
               4 2 2 MUL EQ NOT NOT REF_6 JUMPI
                  15 0 MSTORE
               REF_2 JUMP LABEL_6 LABEL_2 REF_1 JUMP LABEL_7 LABEL_1 REF_0 JUMP LABEL_8 LABEL_0

         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
    }

    @Test    // if elif else test 20
    public void test35(){

        String code =   "if 2>1:    \n" +
                        "   a=20    \n" +
                        "elif 5<1:  \n" +
                        "   a=30    \n" +
                        "elif 6>6:  \n" +
                        "   a=40    \n" +
                        "else:      \n" +
                        "   a=50    \n" ;

        String expected = "1 2 GT NOT REF_1 JUMPI 20 0 MSTORE REF_0 JUMP LABEL_1 1 5 LT NOT REF_2 JUMPI 30 0 MSTORE REF_0 JUMP LABEL_2 6 6 GT NOT REF_3 JUMPI 40 0 MSTORE REF_0 JUMP LABEL_3 50 0 MSTORE LABEL_0";

        /**

         1 2 GT NOT REF_1 JUMPI
            20 0 MSTORE
            REF_0 JUMP
            LABEL_1
         1 5 LT NOT REF_2 JUMPI
            30 0 MSTORE
            REF_0 JUMP
            LABEL_2
         6 6 GT NOT REF_3 JUMPI
             40 0 MSTORE
             REF_0 JUMP
             LABEL_3
         50 0 MSTORE
         LABEL_0

         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
    }

    @Test    // while test 1
    public void test36(){

        String code =   "a = 20        \n" +
                        "while a>0:    \n" +
                        "  a = a - 1   \n"  ;

        String expected = "20 0 MSTORE LABEL_0 0 0 MLOAD GT EQ NOT REF_1 JUMPI 1 0 MLOAD SUB 0 MSTORE REF_0 JUMP LABEL_1";

        /**

         20 0 MSTORE
         LABEL_0
         0 0 MLOAD GT EQ NOT REF_1 JUMPI
            1 0 MLOAD SUB 0 MSTORE
            REF_0 JUMP
         LABEL_1

         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
    }

    @Test    // while test 2
    public void test37(){

        String code =   "x = 248              \n" +
                        "while x > 1:         \n" +
                        "   if (x % 2) == 0:  \n" +
                        "       x = x / 2     \n" +
                        "   else:             \n " +
                        "       x = 3 * x + 1 \n"  ;

        String expected = "248 0 MSTORE LABEL_0 1 0 MLOAD GT EQ NOT REF_1 JUMPI 0 2 0 MLOAD MOD EQ NOT REF_3 JUMPI 2 0 MLOAD DIV 0 MSTORE REF_2 JUMP LABEL_3 1 0 MLOAD 3 MUL ADD 0 MSTORE LABEL_2 REF_0 JUMP LABEL_1";

        /**

         248 0 MSTORE
         LABEL_0
         1 0 MLOAD GT EQ NOT REF_1 JUMPI
            0 2 0 MLOAD MOD EQ NOT REF_3 JUMPI
            2 0 MLOAD DIV 0 MSTORE
            REF_2 JUMP
         LABEL_3
            1 0 MLOAD 3 MUL ADD 0 MSTORE
            LABEL_2

         REF_0 JUMP
         LABEL_1

         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
    }

    @Test    // while test 3
    public void test38(){

        String code =   "x = 0xFF            \n" +
                        "while x > 1:        \n" +
                        "   if (x % 2) == 0: \n" +
                        "      x = x / 2     \n" +
                        "x = x +2            \n" +
                        "x = 3 * x + 1        \n"  ;

        String expected = "255 0 MSTORE LABEL_0 1 0 MLOAD GT EQ NOT REF_1 JUMPI 0 2 0 MLOAD MOD EQ NOT REF_3 JUMPI 2 0 MLOAD DIV 0 MSTORE REF_2 JUMP LABEL_3 LABEL_2 REF_0 JUMP LABEL_1 2 0 MLOAD ADD 0 MSTORE 1 0 MLOAD 3 MUL ADD 0 MSTORE";

        /**

         255 0 MSTORE
         LABEL_0
         1 0 MLOAD GT EQ NOT REF_1 JUMPI
             0 2 0 MLOAD MOD EQ NOT REF_3 JUMPI
                 2 0 MLOAD DIV 0 MSTORE REF_2 JUMP
                 LABEL_3 LABEL_2
         REF_0 JUMP
         LABEL_1
         2 0 MLOAD ADD 0 MSTORE
         1 0 MLOAD 3 MUL ADD 0 MSTORE

         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
    }

    @Test    // while test 4
    public void test39(){

        String code =   "x = 0xFF\n" +
                        "while x > 1:\n" +
                        "x = x +2\n" +
                        "x = 3 * x + 1"  ;

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


    @Test    // while test 5
    public void test40(){

        String code =   "x = 0xFF\n" +
                        "while (x > 1) && (x > 2) && (x >    3) && (2 <9):\n" +
                        "   x = x -2\n"  ;

        String expected = "255 0 MSTORE LABEL_0 9 2 LT 3 0 MLOAD GT 2 0 MLOAD GT 1 0 MLOAD GT NOT NOT MUL NOT NOT MUL NOT NOT MUL EQ NOT REF_1 JUMPI 2 0 MLOAD SUB 0 MSTORE REF_0 JUMP LABEL_1";

        /**

         255 0 MSTORE
         LABEL_0
         9 2 LT 3 0 MLOAD GT 2 0 MLOAD GT 1 0 MLOAD GT NOT NOT MUL NOT NOT MUL NOT NOT MUL EQ NOT REF_1 JUMPI
           2 0 MLOAD SUB 0 MSTORE
           REF_0 JUMP
         LABEL_1

         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
    }

    @Test    // special functions test 1
    public void test41(){

        String code =   "a = msg.datasize\n" +
                        "b = msg.sender\n" +
                        "c = msg.value\n" +
                        "d = tx.gasprice\n" +
                        "e = tx.origin\n" +
                        "f = tx.gas\n" +
                        "g = contract.balance\n" +
                        "h = block.prevhash\n" +
                        "i = block.coinbase\n" +
                        "j = block.timestamp\n" +
                        "k = block.number\n" +
                        "l = block.difficulty\n" +
                        "m = block.gaslimit\n"  ;

        String expected = "32 CALLDATASIZE DIV 0 MSTORE CALLER 32 MSTORE CALLVALUE 64 MSTORE GASPRICE 96 MSTORE ORIGIN 128 MSTORE GAS 160 MSTORE BALANCE 192 MSTORE PREVHASH 224 MSTORE COINBASE 256 MSTORE TIMESTAMP 288 MSTORE NUMBER 320 MSTORE DIFFICULTY 352 MSTORE GASLIMIT 384 MSTORE";

        /**

         32 CALLDATASIZE DIV 0 MSTORE
         CALLER 32 MSTORE
         CALLVALUE 64 MSTORE
         GASPRICE 96 MSTORE
         ORIGIN 128 MSTORE
         GAS 160 MSTORE
         BALANCE 192 MSTORE
         PREVHASH 224 MSTORE
         COINBASE 256 MSTORE
         TIMESTAMP 288 MSTORE
         NUMBER 320 MSTORE
         DIFFICULTY 352 MSTORE
         GASLIMIT 384 MSTORE

         */

        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        Assert.assertEquals(expected, result);
    }
/*
 * 
a = msg.datasize
b = msg.sender
c = msg.value
d = tx.gasprice
e = tx.origin
f = tx.gas
g = contract.balance
h = block.prevhash
i = block.coinbase
j = block.timestamp
k = block.number
l = block.difficulty
m = block.gaslimit

*/

    /**
     *
     * todo: return testing
     * todo: memory_storage testing
     * todo: message access testing
     *
     */
}
