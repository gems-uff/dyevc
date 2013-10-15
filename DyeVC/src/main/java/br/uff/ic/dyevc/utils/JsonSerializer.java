package br.uff.ic.dyevc.utils;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.exception.ServiceException;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ObjectMapper;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 * Serializes a given object to Json string representation.
 * @author Cristiano
 */
public final class JsonSerializer {
    /**
     * Serializes the specified object to Json string representation without including null fields.
     * @param obj Object to be serialized
     * @return The Json string representation for the specified object, excluding null fields
     * @throws ServiceException
     */
    public static String serializeWithoutNulls(Object obj) throws ServiceException {
        return serialize(obj, true);
    }

    /**
     * Serializes the specified object to Json string representation.
     * @param obj Object to be serialized
     * @param filterNulls If true, will not serialize null fields
     * @return The Json string representation for the specified object
     * @throws ServiceException
     */
    public static String serialize(Object obj, boolean filterNulls) throws ServiceException {
        ObjectMapper mapper = new ObjectMapper();
        if (filterNulls) {
            mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        }

        try {
            return mapper.writeValueAsString(obj);
        } catch (IOException ex) {
            throw new ServiceException("Could not set query parameter. Value received: " + obj, ex);
        }
    }
}
