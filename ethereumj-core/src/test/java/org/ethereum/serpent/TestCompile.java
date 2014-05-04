package org.ethereum.serpent;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
import org.ethereum.util.Utils;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;

import static org.junit.Assert.*;

public class TestCompile {

/*  bin_expr
            ['+', 2, 1, ['<1>', '<0>', 'ADD']],     V
            ['-', 2, 1, ['<1>', '<0>', 'SUB']],     V
            ['*', 2, 1, ['<1>', '<0>', 'MUL']],     V
            ['/', 2, 1, ['<1>', '<0>', 'DIV']],     V
            ['^', 2, 1, ['<1>', '<0>', 'EXP']],     V
            ['%', 2, 1, ['<1>', '<0>', 'MOD']],     V
            ['#/', 2, 1, ['<1>', '<0>', 'SDIV']],   V
            ['#%', 2, 1, ['<1>', '<0>', 'SMOD']],   V
*/

    @Test  /* Test one symbol */
    public void test0() throws FileNotFoundException, RecognitionException {

        CharStream stream = new ANTLRStringStream("" + "A");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.bin_expr_return retVal = parser.bin_expr();

        assertEquals("A", retVal.getTemplate().toString());
    }

    @Test  /* Test ADD 1*/
    public void test1() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "A + B");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.bin_expr_return retVal = parser.bin_expr();

        assertEquals("A B ADD", retVal.getTemplate().toString());
    }

    @Test  /* Test ADD 2*/
    public void test2() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "A + B + C1");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.bin_expr_return retVal = parser.bin_expr();

        assertEquals("A B ADD C1 ADD", retVal.getTemplate().toString());
    }

    @Test  /* Test SUB 1*/
    public void test3() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "A - B");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.bin_expr_return retVal = parser.bin_expr();

        assertEquals("A B SUB", retVal.getTemplate().toString());
    }

    @Test  /* Test MUL 1*/
    public void test4() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "A * B");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.bin_expr_return retVal = parser.bin_expr();

        assertEquals("A B MUL", retVal.getTemplate().toString());
    }


    @Test  /* Test DIV 1*/
    public void test5() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "A / B");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.bin_expr_return retVal = parser.bin_expr();

        assertEquals("A B DIV", retVal.getTemplate().toString());
    }

    @Test  /* Test EXP 1*/
    public void test6() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "A ^ B");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.bin_expr_return retVal = parser.bin_expr();

        assertEquals("A B EXP", retVal.getTemplate().toString());
    }

    @Test  /* Test MOD 1*/
    public void test7() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "A % B");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.bin_expr_return retVal = parser.bin_expr();

        assertEquals("A B MOD", retVal.getTemplate().toString());
    }

    @Test  /* Test SDIV 1*/
    public void test8() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "A #/ B");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.bin_expr_return retVal = parser.bin_expr();

        assertEquals("A B SDIV", retVal.getTemplate().toString());
    }

    @Test  /* Test SMOD 1*/
    public void test9() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "A #% B");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.bin_expr_return retVal = parser.bin_expr();

        assertEquals("A B SMOD", retVal.getTemplate().toString());
    }

    @Test  /* Test multi binary operators 1*/
    public void test10() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "A / B - C + D * ET");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.bin_expr_return retVal = parser.bin_expr();

        assertEquals("A B DIV C SUB D ADD ET MUL", retVal.getTemplate().toString());

    }

    @Test  /* Test multi binary operators 2*/
    public void test11() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "A / B - C + D * ET % ET2 ^ RO + RO2 #/ COOL #% HOT");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.bin_expr_return retVal = parser.bin_expr();

        assertEquals("A B DIV C SUB D ADD ET MUL ET2 MOD RO EXP RO2 ADD COOL SDIV HOT SMOD",
                retVal.getTemplate().toString());
    }

/*
            ['==', 2, 1, ['<1>', '<0>', 'EQ']],         V
            ['<', 2, 1, ['<1>', '<0>', 'LT']],          V
            ['<=', 2, 1, ['<1>', '<0>', 'GT', 'NOT']],  V
            ['>', 2, 1, ['<1>', '<0>', 'GT']],          V
            ['>=', 2, 1, ['<1>', '<0>', 'LT', 'NOT']],  V
            ['!', 1, 1, ['<0>', 'NOT']],                V
*/

    @Test  /* Test '==' 1*/
    public void test12() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "A + B == B + A");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.cond_expr_return retVal = parser.cond_expr();

        assertEquals("A B ADD B A ADD EQ",
                retVal.getTemplate().toString());
    }

    @Test  /* Test '<' 1*/
    public void test13() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "A + C < C + A");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.cond_expr_return retVal = parser.cond_expr();

        assertEquals("A C ADD C A ADD LT",
                retVal.getTemplate().toString());
    }

    @Test  /* Test '<=' 1*/
    public void test14() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "A + C <= C + A");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.cond_expr_return retVal = parser.cond_expr();

        assertEquals("A C ADD C A ADD GT NOT",
                retVal.getTemplate().toString());
    }

    @Test  /* Test '>' 1*/
    public void test15() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "A + C > C + A");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.cond_expr_return retVal = parser.cond_expr();

        assertEquals("A C ADD C A ADD GT",
                retVal.getTemplate().toString());
    }


    @Test  /* Test '>=' 1*/
    public void test16() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "A + C >= C + A");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.cond_expr_return retVal = parser.cond_expr();

        assertEquals("A C ADD C A ADD LT NOT",
                retVal.getTemplate().toString());
    }

    @Test  /* Test '!' 1 */
    public void test17() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "!A");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.unr_expr_return retVal = parser.unr_expr();

        assertEquals("A NOT",
                retVal.getTemplate().toString());
    }


    @Test  /* Test '!' 2 */
    public void test18() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "!!A");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.unr_expr_return retVal = parser.unr_expr();

        assertEquals("A NOT NOT",
                retVal.getTemplate().toString());
    }

    @Test  /* Test '!' 3 */
    public void test19() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "A");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.unr_expr_return retVal = parser.unr_expr();

        assertEquals("A NOT NOT",
                retVal.getTemplate().toString());
    }

    @Test  /* Test '!' 4 */
    public void test20() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "!!!!!!!!!A");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.unr_expr_return retVal = parser.unr_expr();

        assertEquals("A NOT",
                retVal.getTemplate().toString());
    }


    @Test  /* Test set var '=' 1 */
    public void test21() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "A=10 \n B=20 \n C=30");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.program_return retVal = parser.program();

        assertEquals("10 0 MSTORE 20 32 MSTORE 30 64 MSTORE",
                retVal.getTemplate().toString().trim());
    }

    @Test  /* Test set var '=' 2 */
    public void test22() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "A=10 \n B=20 \n A=30 \n B=40");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.program_return retVal = parser.program();

        assertEquals("10 0 MSTORE 20 32 MSTORE 30 0 MSTORE 40 32 MSTORE",
                retVal.getTemplate().toString().trim());

    }

    @Test  /* Test if stmt 1 */
    public void test23() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "if a==10:\n b=20 \nelse: \n b=30");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.if_else_stmt_return retVal = parser.if_else_stmt();

        assertEquals("",
                retVal.getTemplate().toString().trim());
    }

    @Test  /* Test contract.storage[x] 1 */
    public void test24() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "contract.storage[0]");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.storage_load_return retVal = parser.storage_load();

        assertEquals("0 SLOAD",
                retVal.getTemplate().toString().trim());
    }

    @Test  /* Test contract.storage[x] 2 */
    public void test25() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "contract.storage[100]");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.storage_load_return retVal = parser.storage_load();

        assertEquals("100 SLOAD",
                retVal.getTemplate().toString().trim());
    }

    @Test  /* Test contract.storage[x]=y 1 */
    public void test26() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "contract.storage[100]=200");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.storage_save_return retVal = parser.storage_save();

        assertEquals("200 100 SSTORE",
                retVal.getTemplate().toString().trim());
    }

    @Test  /* Test contract.storage[x]=y 1_ */
    public void test26_1() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "contract.storage[3+4]=200");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.storage_save_return retVal = parser.storage_save();

        assertEquals("200 3 4 ADD SSTORE",
                retVal.getTemplate().toString().trim());
    }


    @Test  /* Test contract.storage[x]=y 2 */
    public void test27() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "contract.storage[100]=200+100");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.storage_save_return retVal = parser.storage_save();

        assertEquals("200 100 ADD 100 SSTORE", // todo: have to optimize it somewhere in the future
                retVal.getTemplate().toString().trim());
    }

    @Test  /* Test msg.data[x] 1 */
    public void test28() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "msg.data[0]");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.msg_load_return retVal = parser.msg_load();

        assertEquals("0 32 MULL CALLDATALOAD",
                retVal.getTemplate().toString().trim());
    }

    @Test  /* Test msg.data[x] 2 */
    public void test29() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "msg.data[10+  20]");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.msg_load_return retVal = parser.msg_load();

        assertEquals("10 20 ADD 32 MUL CALLDATALOAD",
                retVal.getTemplate().toString().trim());
    }

    @Test  /* Test msg.data[x] 3 */
    public void test30() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "msg.data[0] + msg.data[2]");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.bin_expr_return retVal = parser.bin_expr();

        assertEquals("0 32 MUL CALLDATALOAD 2 32 MUL CALLDATALOAD ADD",
                retVal.getTemplate().toString().trim());
    }


    @Test  /* Test multi  */
    public void test31() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "contract.storage[msg.data[0]] = msg.data[1]\n" +
                        "");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.storage_save_return retVal = parser.storage_save();

        assertEquals("1 32 MUL CALLDATALOAD 0 32 MUL CALLDATALOAD SSTORE",
                retVal.getTemplate().toString().trim());
    }

    @Test  /* Test multi  */
    public void test32() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "!contract.storage[msg.data[0]]");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.unr_expr_return retVal = parser.unr_expr();

        assertEquals("0 32 MUL CALLDATALOAD SLOAD NOT",
                retVal.getTemplate().toString().trim());
    }

    @Test  /* Test get_var 1 */
    public void test33() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "a=20\nb=20\nb==");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.test_1_return retVal = parser.test_1();

        assertEquals("20 0 MSTORE 20 32 MSTORE 32 MLOAD",
                retVal.getTemplate().toString().trim());
    }

    @Test  /* Test get_var 2 */
    public void test34() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "a1=20\nb4=20\n\na4=20\na4==");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.test_1_return retVal = parser.test_1();

        assertEquals("20 0 MSTORE 20 32 MSTORE 20 64 MSTORE 64 MLOAD",
                retVal.getTemplate().toString().trim());
    }

    @Test  /* Test if_stmt with (!)cond */
    public void test35() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "if !contract.storage[msg.data[0]]:\n" +
                        "        contract.storage[msg.data[0]] = msg.data[1]\n" +
                        "    return(1)\n" +
                        "else:\n" +
                        "    return(0)\n");

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.if_else_stmt_return retVal = parser.if_else_stmt();

        assertEquals("",
                retVal.getTemplate().toString().trim());
    }

    @Test  /* Test complex contract with if_else_stmt inside else body */
    public void test36() throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream("" +
                        "    contract.storage[0xcd2a3d9f938e13cd947ec05abc7fe734df8dd826] = 1000000\n" +
                        "    if msg.datasize == 1:\n" +
                        "        addr = msg.data[0]\n" +
                        "        return(contract.storage[addr])\n" +
                        "    else:\n" +
                        "        from = msg.sender\n" +
                        "        fromvalue = contract.storage[from]\n" +
                        "        to = msg.data[0]\n" +
                        "        value = msg.data[1]\n" +
                        "        if fromvalue >= value:\n" +
                        "            contract.storage[from] = fromvalue - value\n" +
                        "            contract.storage[to] = contract.storage[to] + value\n" +
                        "            return(1)\n" +
                        "        else:\n" +
                        "            return(0)\n");


        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.gen_body_return retVal = parser.gen_body();

        assertEquals("",
                retVal.getTemplate().toString().trim());
    }


    @Test  /* Test for hex number */
    public void test37() throws FileNotFoundException, RecognitionException {

        String hexNum = "0xcd2a3d9f938e13cd947ec05abc7fe734df8dd826";

        CharStream stream =
                new ANTLRStringStream(hexNum);

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);

        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.hex_num_return retVal = parser.hex_num();

        assertEquals(Utils.hexStringToDecimalString(hexNum),
                retVal.getTemplate().toString().trim());
    }
}
