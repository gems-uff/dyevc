package br.uff.ic.dyevc.persistence;

import br.uff.ic.dyevc.exception.ServiceException;
import java.io.IOException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 *
 * @author Cristiano
 */
public abstract class GenericFilter {

    /**
     * Serialize this filter to Json
     *
     * @param filterNulls If true, does not include null values
     * @return
     */
    public String serialize(boolean filterNulls) throws ServiceException {
        try {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        return mapper.writeValueAsString(this);
        } catch (IOException ex) {
            throw new ServiceException("Could not set query parameter. Value received: " + this, ex);
        }
    }
}
