package org.ethereum.json;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * An extended {@link com.fasterxml.jackson.databind.ObjectMapper ObjectMapper} class to
 * customize ethereum state dumps.
 *
 * @author Alon Muroch
 */
public class EtherObjectMapper extends ObjectMapper {

    @Override
    public String writeValueAsString(Object value)
            throws JsonProcessingException {
        // alas, we have to pull the recycler directly here...
        SegmentedStringWriter sw = new SegmentedStringWriter(_jsonFactory._getBufferRecycler());
        try {
            JsonGenerator ge = _jsonFactory.createGenerator(sw);
            // set ethereum custom pretty printer
            EtherPrettyPrinter pp = new EtherPrettyPrinter();
            ge.setPrettyPrinter(pp);

            _configAndWriteValue(ge, value);
        } catch (JsonProcessingException e) { // to support [JACKSON-758]
            throw e;
        } catch (IOException e) { // shouldn't really happen, but is declared as possibility so:
            throw JsonMappingException.fromUnexpectedIOE(e);
        }
        return sw.getAndClear();
    }

    /**
     * An extended {@link com.fasterxml.jackson.core.util.DefaultPrettyPrinter} class to customize
     * an ethereum {@link com.fasterxml.jackson.core.PrettyPrinter Pretty Printer} Generator
     *
     * @author Alon Muroch
     */
    public class EtherPrettyPrinter extends DefaultPrettyPrinter {

        public EtherPrettyPrinter() {
            super();
        }

        @Override
        public void writeObjectFieldValueSeparator(JsonGenerator jg)
                throws IOException, JsonGenerationException {
            /**
             * Custom object separator (Default is " : ") to make it easier to compare state dumps with other
             * ethereum client implementations
             */
            jg.writeRaw(": ");
        }
    }
}
