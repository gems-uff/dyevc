package br.uff.ic.dyevc.persistence;

import br.uff.ic.dyevc.exception.ServiceException;
import java.util.HashMap;

/**
 * Parameters used to invoke services in Mongolab
 *
 * @author Cristiano
 */
@SuppressWarnings("serial")
public class MongoLabServiceParms extends HashMap<String, Object> {

    private static final String PARAM_QUERY = "q";
    private static final String PARAM_RETURN_FIELDS = "f";
    private static final String PARAM_SORT_ORDER = "s";

    public void setQuery(GenericFilter filter) throws ServiceException {
        put(PARAM_QUERY, filter.serialize(true));
    }
    
    public void setReturnFields(GenericFilter filter) throws ServiceException {
        put(PARAM_RETURN_FIELDS, filter.serialize(true));
    }
    
    public void setSortOrder(GenericFilter filter) throws ServiceException {
        put(PARAM_SORT_ORDER, filter.serialize(true));
    }
    
}
