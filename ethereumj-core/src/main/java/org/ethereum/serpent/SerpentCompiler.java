package org.ethereum.serpent;

import org.ethereum.util.ByteUtil;
import org.ethereum.vm.OpCode;

import org.antlr.v4.runtime.tree.ParseTree;

import org.spongycastle.util.Arrays;
import org.spongycastle.util.BigIntegers;

import java.io.ByteArrayOutputStream;

import java.math.BigInteger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Roman Mandeleil
 * @since 13.05.14
 */
public class SerpentCompiler {

    public static String compile(String code) {
        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class, SerpentParser.class,
                code);
        ParseTree tree = parser.parse();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();

        return result;
    }

    public static String compileFullNotion(String code) {
        SerpentParser parser = ParserUtils.getParser(SerpentLexer.class,
                SerpentParser.class, code);
        ParseTree tree = parser.parse_init_code_block();

        String result = new SerpentToAssemblyCompiler().visit(tree);
        result = result.replaceAll("\\s+", " ");
        result = result.trim();
        return result;
    }

    public static byte[] compileFullNotionAssemblyToMachine(String code) {
        byte[] initCode = compileAssemblyToMachine(extractInitBlock(code));
        byte[] codeCode = compileAssemblyToMachine(extractCodeBlock(code));
        return encodeMachineCodeForVMRun(codeCode, initCode);
    }

    public static String extractInitBlock(String code) {
        String result = "";
        Pattern pattern = Pattern.compile("\\[init (.*?) init\\]");
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    public static String extractCodeBlock(String code) {
        String result = "";
        Pattern pattern = Pattern.compile("\\[code (.*?) code\\]");
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    public static byte[] compileAssemblyToMachine(String code) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        String[] lexaArr = code.split("\\s+");

        List<String> lexaList = new ArrayList<>();
        Collections.addAll(lexaList, lexaArr);

        // Encode push_n numbers
        boolean skipping = false;
        for (int i = 0; i < lexaList.size(); ++i) {

            String lexa = lexaList.get(i);

            { // skipping the [asm asm] block
                if (lexa.equals("asm]")) {
                    skipping = false;
                    lexaList.remove(i);
                    --i;
                    continue;
                }
                if (lexa.equals("[asm")) {
                    skipping = true;
                    lexaList.remove(i);
                    --i;
                    continue;
                }
                if (skipping)
                    continue;
            }

            if (OpCode.contains(lexa) ||
                    lexa.contains("REF_") ||
                    lexa.contains("LABEL_")) continue;

            int bytesNum = ByteUtil.numBytes(lexa);

            String num = lexaList.remove(i);
            BigInteger bNum = new BigInteger(num);
            byte[] bytes = BigIntegers.asUnsignedByteArray(bNum);
            if (bytes.length == 0) bytes = new byte[]{0};

            for (int j = bytes.length; j > 0; --j) {
                lexaList.add(i, (bytes[j - 1] & 0xFF) + "");
            }
            lexaList.add(i, "PUSH" + bytesNum);
            i = i + bytesNum;
        }

        // encode ref for 5 bytes
        for (int i = 0; i < lexaList.size(); ++i) {

            String lexa = lexaList.get(i);
            if (!lexa.contains("REF_")) continue;
            lexaList.add(i + 1, lexa);
            lexaList.add(i + 2, lexa);
            lexaList.add(i + 3, lexa);
            lexaList.add(i + 4, lexa);
            i += 4;
        }

        // calc label pos & remove labels
        Map<String, Integer> labels = new HashMap<>();

        for (int i = 0; i < lexaList.size(); ++i) {

            String lexa = lexaList.get(i);
            if (!lexa.contains("LABEL_")) continue;

            String label = lexaList.remove(i);
            String labelNum = label.split("LABEL_")[1];

            int labelPos = i;

            labels.put(labelNum, labelPos);
            --i;
        }

        // encode all ref occurrence
        for (int i = 0; i < lexaList.size(); ++i) {

            String lexa = lexaList.get(i);
            if (!lexa.contains("REF_")) continue;

            String ref = lexaList.remove(i);
            lexaList.remove(i);
            lexaList.remove(i);
            lexaList.remove(i);
            lexaList.remove(i);
            String labelNum = ref.split("REF_")[1];

            Integer pos = labels.get(labelNum);
            int bytesNum = ByteUtil.numBytes(pos.toString());

            lexaList.add(i, pos.toString());

            for (int j = 0; j < (4 - bytesNum); ++j)
                lexaList.add(i, "0");

            lexaList.add(i, "PUSH4");
            ++i;
        }

        for (String lexa : lexaList) {

            if (OpCode.contains(lexa))
                baos.write(OpCode.byteVal(lexa));
            else {
                // TODO: support for number more than one byte
                baos.write(Integer.parseInt(lexa) & 0xFF);
            }
        }

        // wrap plan
        // 1) that is the code
        // 2) need to wrap it with PUSH1 (codesize) PUSH1 (codestart) 000 CODECOPY 000 PUSH1 (codesize) _fURN

        return baos.toByteArray();
    }

    /**
     * Return encoded bytes.
     */
    public static byte[] encodeMachineCodeForVMRun(byte[] code, byte[] init) {

        if (code == null || code.length == 0) throw new RuntimeException("code can't be empty code: " + code);

        int numBytes = ByteUtil.numBytes(code.length + "");
        byte[] lenBytes = BigIntegers.asUnsignedByteArray(BigInteger.valueOf(code.length));

        StringBuilder sb = new StringBuilder();
        for (byte lenByte : lenBytes) {
            sb.append(lenByte).append(" ");
        }

        // calc real code start position (after the init header)
        int pos = 10 + numBytes * 2;
        if (init != null) pos += init.length;

        // @push_len @len PUSH1 @src_start  PUSH1 0 CODECOPY @push_len @len 0 PUSH1 0 RETURN
        String header = String.format("[asm %s %s PUSH1 %d  PUSH1 0 CODECOPY %s %s PUSH1 0 RETURN asm]",
                "PUSH" + numBytes, sb.toString(), pos, "PUSH" + numBytes, sb.toString());

        byte[] headerMachine = compileAssemblyToMachine(header);

        return init != null ? Arrays.concatenate(init, headerMachine, code) :
                Arrays.concatenate(headerMachine, code);
    }
}
