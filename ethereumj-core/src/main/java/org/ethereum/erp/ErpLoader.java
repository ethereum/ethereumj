/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.erp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ethereum.util.ByteUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ErpLoader {
    public static final String SCO_EXTENSION = ".sco.json";
    private static final FilenameFilter SCO_FILE_FILTER = (dir, name) -> name.endsWith(SCO_EXTENSION);

    /**
     * A lightweight object that can be held in memory
     */
    public static class ErpMetadata {
        Long targetBlock;
        File resourceFile;
        String erpId;
        byte[] erpMarker;

        ErpMetadata(String erpId, Long targetBlock, File resourceFile) {
            this.erpId = erpId;
            this.targetBlock = targetBlock;
            this.resourceFile = resourceFile;
            this.erpMarker = ByteUtil.hexStringToBytes(asciiToHex(erpId));
        }

        private static String asciiToHex(String asciiValue){
            char[] chars = asciiValue.toCharArray();
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < chars.length; i++) {
                hex.append(Integer.toHexString((int) chars[i]));
            }
            return hex.toString();
        }

        public byte[] getErpMarker() {
            return erpMarker;
        }

        public String getId() {
            return erpId;
        }

        public long getTargetBlock() {
            return targetBlock;
        }
    }

    /**
     * This method returns a collection of lightweight objects, but it parses each
     * file to pull out the erpId and the target block as well as sanity check the actions
     *
     * @param erpResourceDir The resource directory where ERP objects are stored
     * @return A collection of ERPs that are available
     * @throws IOException if any of the ERP files could not be loaded
     */
    public Collection<ErpMetadata> loadErpMetadata(String erpResourceDir) throws IOException {
        // this is somewhat inefficient, but the goal is to ensure that all important
        // data is loaded from the SCO object itself
        List<ErpMetadata> allMetadata = new LinkedList<>();
        for (File f : loadERPResourceFiles(erpResourceDir)) {
            final StateChangeObject sco = loadStateChangeObject(f);
            allMetadata.add(new ErpMetadata(sco.erpId, sco.targetBlock, f));
        }
        return allMetadata;
    }

    /**
     * Loads the StateChangeObject from a file synchronously
     * @param metadata The ERP metadata object
     * @return A StateChangeObject that can be executed against a repo by the ErpExecutor
     * @throws IOException if the StateChangeObject cannot be loaded.
     */
    public StateChangeObject loadStateChangeObject(ErpMetadata metadata) throws IOException {
        return loadStateChangeObject(metadata.resourceFile);
    }

    File[] loadERPResourceFiles(String erpResourceDir) throws IOException {
        URL url = getClass().getResource(erpResourceDir);

        File[] files = url != null
            ? new File(url.getPath()).listFiles(SCO_FILE_FILTER)
            : null;

        // an empty array is ok (which would mean there are no files in the directory)
        if (files == null)
            throw new IOException("The specified resource directory does not exists: " + erpResourceDir);

        return files;
    }

    StateChangeObject loadStateChangeObject(File f) throws IOException   {
        return StateChangeObject.parse(loadRawStateChangeObject(f));
    }

    RawStateChangeObject loadRawStateChangeObject(File f) throws IOException   {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(f, RawStateChangeObject.class);
    }
}
