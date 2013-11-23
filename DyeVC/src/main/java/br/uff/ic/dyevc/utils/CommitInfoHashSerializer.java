package br.uff.ic.dyevc.utils;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.CommitInfo;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ser.std.SerializerBase;
import org.codehaus.jackson.map.SerializerProvider;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.List;

/**
 * Serializer that takes a CommitInfo and outputs only its hash attribute
 * @author Cristiano
 */
public class CommitInfoHashSerializer extends SerializerBase<List> {
    /**
     * Constructs ...
     */
    public CommitInfoHashSerializer() {
        super(List.class);
    }

    @Override
    public void serialize(List value, JsonGenerator generator, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        generator.writeStartArray();

        for (Object object : value) {
            CommitInfo ci = (CommitInfo)object;
            generator.writeString(ci.getHash());
        }

        generator.writeEndArray();
    }
}
