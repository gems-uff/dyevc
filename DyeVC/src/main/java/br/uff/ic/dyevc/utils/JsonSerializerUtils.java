package br.uff.ic.dyevc.utils;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.exception.ServiceException;
import br.uff.ic.dyevc.model.CommitInfo;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.Version;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.List;

/**
 * Serializes a given object to Json string representation.
 * @author Cristiano
 */
public final class JsonSerializerUtils {
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
            throw new ServiceException("Could not serialize value to json. Value received: " + obj, ex);
        }
    }

    /**
     * Serializes the specified list of CommitInfo as an array containing its hashes.
     * @param cis List of CommitInfo objects to be serialized
     * @return The Json string representation for the specified object hashes
     * @throws ServiceException
     */
    public static String serializeHashes(List<CommitInfo> cis) throws ServiceException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("MyModule", new Version(1, 0, 0, null));
        module.addSerializer(new CommitInfoHashSerializer());
        mapper.registerModule(module);

        try {
            return mapper.writeValueAsString(cis);
        } catch (IOException ex) {
            throw new ServiceException("Could not serialize value to jason", ex);
        }
    }
}
