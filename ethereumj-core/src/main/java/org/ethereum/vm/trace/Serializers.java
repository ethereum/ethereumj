package org.ethereum.vm.trace;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import org.ethereum.vm.DataWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;

public final class Serializers {

    private static final Logger LOGGER = LoggerFactory.getLogger("vmtrace");

    public static class DataWordSerializer extends JsonSerializer<DataWord> {

        @Override
        public void serialize(DataWord gas, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeString(gas.value().toString());
        }
    }

    public static class ByteArraySerializer extends JsonSerializer<byte[]> {

        @Override
        public void serialize(byte[] memory, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeString(Hex.toHexString(memory));
        }
    }

    public static class OpCodeSerializer extends JsonSerializer<Byte> {

        @Override
        public void serialize(Byte op, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeString(org.ethereum.vm.OpCode.code(op).name());
        }
    }


    public static String serializeFieldsOnly(Object value, boolean pretty) {
        try {
            ObjectMapper mapper = createMapper(pretty);
            mapper.setVisibilityChecker(fieldsOnlyVisibilityChecker(mapper));

            return mapper.writeValueAsString(value);
        } catch (Exception e) {
            LOGGER.error("JSON serialization error: ", e);
            return "{}";
        }
    }

    private static VisibilityChecker<?> fieldsOnlyVisibilityChecker(ObjectMapper mapper) {
        return mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE);
    }

    public static ObjectMapper createMapper(boolean pretty) {
        ObjectMapper mapper = new ObjectMapper();
        if (pretty) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
        return mapper;
    }
}
