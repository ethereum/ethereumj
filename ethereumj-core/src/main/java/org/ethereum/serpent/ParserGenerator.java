package org.ethereum.serpent;

import org.antlr.v4.Tool;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * www.ethereumJ.com
 *
 * @author Roman Mandeleil
 * Created on: 25/04/14 17:06
 */
public class ParserGenerator {

    public static void main(String args[]) throws MojoFailureException, MojoExecutionException {

        String userDir = System.getProperty("user.dir");

        String grammarName = userDir + "\\src\\main\\antlr4\\org\\ethereum\\serpent\\Serpent.g4";

        String options[] = {grammarName, "-visitor", "-package", "org.ethereum.serpent"};
        Tool tool = new Tool(options);
        tool.outputDirectory = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\";
        tool.processGrammarsOnCommandLine();

//        org.antlr.Tool.main(new String[]{userDir + "\\src\\main\\antlr4\\org\\ethereum\\serpent\\Serpent.g4"});
    }
}