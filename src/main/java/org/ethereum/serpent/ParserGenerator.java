package org.ethereum.serpent;

import java.io.File;

import org.antlr.v4.Tool;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 25/04/14 17:06
 */
public class ParserGenerator {

    public static void main(String args[]) throws MojoFailureException, MojoExecutionException {

        String userDir = System.getProperty("user.dir");

        String grammarName = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g4";

        File inputDir = new File(userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\");

        String options[] = {grammarName, "-visitor",  "-package", "org.ethereum.serpent"};
        Tool tool = new Tool(options);
        tool.outputDirectory = userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\";
        tool.processGrammarsOnCommandLine();

//        org.antlr.Tool.main(new String[]{userDir + "\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g4"});
//        org.antlr.Tool.main(new String[]{userDir + "\\src\\main\\java\\samples\\antlr\\PyEsque.g"});

    }


}