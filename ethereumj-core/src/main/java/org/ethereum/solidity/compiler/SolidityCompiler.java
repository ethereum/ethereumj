package org.ethereum.solidity.compiler;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.ethereum.config.SystemProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class SolidityCompiler {

    @Autowired
    SystemProperties config = SystemProperties.getDefault();

    private static Solc solc = null;

    private static final SolidityCompiler INSTANCE = new SolidityCompiler();

    public SolidityCompiler() {
        solc = new Solc(config);
    }

    public enum Options {
        AST("ast"),
        BIN("bin"),
        INTERFACE("interface"),
        ABI("abi");

        private String name;

        Options(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class Result {
        public String errors;
        public String output;

        public Result(String errors, String output) {
            this.errors = errors;
            this.output = output;
        }

        public boolean isFailed() {
            return isNotBlank(errors);
        }
    }

    private static class ParallelReader extends Thread {

        private InputStream stream;
        private StringBuilder content = new StringBuilder();

        ParallelReader(InputStream stream) {
            this.stream = stream;
        }

        public String getContent() {
            return getContent(true);
        }

        public synchronized String getContent(boolean waitForComplete) {
            if (waitForComplete) {
                while(stream != null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return content.toString();
        }

        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                synchronized (this) {
                    stream = null;
                    notifyAll();
                }
            }
        }
    }

    public static Result compile(byte[] source, boolean combinedJson, Options... options) throws IOException {
        return getInstance().compile(source, false, combinedJson, options);
    }

    public Result compile(byte[] source, boolean optimize, boolean combinedJson, Options... options) throws IOException {
        List<String> commandParts = new ArrayList<>();
        commandParts.add(solc.getExecutable().getCanonicalPath());
        if (optimize) {
            commandParts.add("--optimize");
        }
        if (combinedJson) {
            commandParts.add("--combined-json");
            commandParts.add(Joiner.on(',').join(options));
        } else {
            for (Options option : options) {
                commandParts.add("--" + option.getName());
            }
        }

        ProcessBuilder processBuilder = new ProcessBuilder(commandParts)
                .directory(solc.getExecutable().getParentFile());
        processBuilder.environment().put("LD_LIBRARY_PATH",
                solc.getExecutable().getParentFile().getCanonicalPath());

        Process process = processBuilder.start();

        try (BufferedOutputStream stream = new BufferedOutputStream(process.getOutputStream())) {
            stream.write(source);
        }

        ParallelReader error = new ParallelReader(process.getErrorStream());
        ParallelReader output = new ParallelReader(process.getInputStream());
        error.start();
        output.start();

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return new Result(error.getContent(), output.getContent());
    }

    public static SolidityCompiler getInstance() {
        return INSTANCE;
    }
}
