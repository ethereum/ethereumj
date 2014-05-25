package org.ethereum.serpent;

import org.antlr.v4.runtime.tree.ParseTree;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.OpCode;
import org.spongycastle.util.BigIntegers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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


    public static String compileAssemblyToMachine(String code){

        StringBuffer assemblyCode = new StringBuffer();
        String[] lexaArr = code.split("\\s+");

        List<String> lexaList = new ArrayList<String>();
        Collections.addAll(lexaList, lexaArr);

        // Encode push_n numbers
        for (int i = 0; i < lexaList.size(); ++i){

            String lexa  = lexaList.get(i);

            if (OpCode.contains(lexa) ||
                lexa.contains("REF_") ||
                lexa.contains("LABEL_")) continue;

            int bytesNum = ByteUtil.numBytes( lexa );

            String num = lexaList.remove(i);
            BigInteger bNum = new BigInteger(num);
            byte[] bytes = BigIntegers.asUnsignedByteArray(bNum);
            if (bytes.length == 0)bytes = new byte[]{0};

            for (int j = bytes.length; j > 0 ; --j){

                lexaList.add(i, (bytes[j-1] & 0xFF) +"");
            }

            lexaList.add(i, "PUSH" + bytesNum);
            i = i + bytesNum;

        }

        // calc label pos & remove labels
        HashMap<String, Integer> labels = new HashMap<String, Integer>();
        for (int i = 0; i < lexaList.size(); ++i){

            String lexa  = lexaList.get(i);
            if (!lexa.contains("LABEL_")) continue;

            String label = lexaList.remove(i);
            String labelNum = label.split("LABEL_")[1];

            labels.put(labelNum, i);

            ++i;
        }

        // encode all ref occurrence
        for (int i = 0; i < lexaList.size(); ++i){

            String lexa  = lexaList.get(i);
            if (!lexa.contains("REF_")) continue;

            String ref = lexaList.remove(i);
            String labelNum = ref.split("REF_")[1];

            Integer pos = labels.get(labelNum);
            int bytesNum = ByteUtil.numBytes( pos.toString() );

            lexaList.add(i, pos.toString());

            for (int j = 0; j < (4 - bytesNum) ; ++j)
                lexaList.add(i, "0");

            lexaList.add(i, "PUSH4");
            ++i;
        }

        for (String lexa : lexaList){

            if (OpCode.contains(lexa))
                assemblyCode.append( OpCode.byteVal(lexa) );
            else{

                assemblyCode.append( lexa );
            }
            assemblyCode.append(" ");
        }

        return assemblyCode.toString();
    }




}
