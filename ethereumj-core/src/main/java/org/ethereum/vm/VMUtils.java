package org.ethereum.vm;

import org.ethereum.config.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.util.Base64.getDecoder;
import static java.util.Base64.getEncoder;
import static org.springframework.util.StringUtils.isEmpty;

public final class VMUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger("VM");
    private static final SystemProperties CONFIG = new SystemProperties();
    
    private VMUtils() {
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }
    
    private static File createProgramTraceFile(String txHash) {
        File result = null;
        
        if (CONFIG.vmTrace() && !isEmpty(CONFIG.vmTraceDir())) {
            
            String pathname = format("%s/%s/%s/%s.json", getProperty("user.dir"), CONFIG.databaseDir(), CONFIG.vmTraceDir(), txHash);
            File file = new File(pathname);

            if (file.exists()) {
                if (file.isFile() && file.canWrite()) {
                    result = file;
                }
            } else if (file.getParentFile().mkdirs()) {
                try {
                    file.createNewFile();
                    result = file;
                } catch (IOException e) {
                    // ignored
                }
            }
        }
        
        return result;
    }

    private static void writeStringToFile(File file, String data) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            if (data != null) {
                out.write(data.getBytes("UTF-8"));
            }
        } catch (Exception e){
            LOGGER.error(format("Cannot write to file '%s': ", file.getAbsolutePath()), e);
        } finally {
            closeQuietly(out);
        }
    }
    
    public static void saveProgramTraceFile(String txHash, String content) {
        File file = createProgramTraceFile(txHash);
        if (file != null) {
            writeStringToFile(file, content);
        }
    }

    private static final int BUF_SIZE = 4096;

    private static void write(InputStream in, OutputStream out, int bufSize) throws IOException {
        try {
            byte[] buf = new byte[bufSize];
            for (int count = in.read(buf); count != -1; count = in.read(buf)) {
                out.write(buf, 0, count);
            }
        } finally {
            closeQuietly(in);
            closeQuietly(out);
        }
    }

    private static byte[] compress(String content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes("UTF-8"));
        DeflaterOutputStream out = new DeflaterOutputStream(baos, new Deflater(), BUF_SIZE);

        write(in, out, BUF_SIZE);

        return baos.toByteArray();
    }

    private static String decompress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);

        ByteArrayInputStream in = new ByteArrayInputStream(data);
        InflaterOutputStream out = new InflaterOutputStream(baos, new Inflater(), BUF_SIZE);

        write(in, out, BUF_SIZE);

        return new String(baos.toByteArray(), "UTF-8");
    }

    public static String zipAndEncode(String content) {
        try {
            return getEncoder().encodeToString(compress(content));
        } catch (Exception e) {
            LOGGER.error("Cannot zip or encode: ", e);
            return content;
        }
    }

    public static String unzipAndDecode(String content) {
        try {
            return decompress(getDecoder().decode(content));
        } catch (Exception e) {
            LOGGER.error("Cannot unzip or decode: ", e);
            return content;
        }
    }
}
