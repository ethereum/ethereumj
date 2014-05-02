package org.ethereum.serpent;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;

import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 29/04/14 12:34
 */
public class SerpentCompiler{

    public static String compile(String code) throws FileNotFoundException, RecognitionException {

        CharStream stream =
                new ANTLRStringStream(code);

        SerpentLexer lex = new SerpentLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SerpentParser parser = new SerpentParser(tokens);


        String userDir = System.getProperty("user.dir");
        String templateFileName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent2Asm.stg";

        StringTemplateGroup template = new StringTemplateGroup(new FileReader(templateFileName),
                AngleBracketTemplateLexer.class);
        parser.setTemplateLib(template);

        SerpentParser.program_return retVal = parser.program();



        return retVal.getTemplate().toString().trim();
    }
}
