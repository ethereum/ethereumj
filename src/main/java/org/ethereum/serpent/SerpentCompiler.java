package org.ethereum.serpent;

import org.antlr.v4.runtime.tree.ParseTree;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 13/05/14 19:37
 */
public class SerpentCompiler {

    public static String compile(String code){
        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        return result;
    }
}
