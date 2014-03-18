package br.uff.ic.dyevc.persistence;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.exception.ServiceException;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;

/**
 * Parameters used to invoke services in Mongolab
 *
 * @author Cristiano
 */
@SuppressWarnings("serial")
public class MongoLabServiceParms extends HashMap<String, Object> {
    /** Field description */
    public static final String PARAM_QUERY = "q";

    /** Field description */
    public static final String PARAM_RETURN_FIELDS = "f";

    /** Field description */
    public static final String PARAM_SORT_ORDER = "s";

    /** Field description */
    public static final String PARAM_LIMIT = "l";

    /** Field description */
    public static final String PARAM_MULTI = "m";

    /** Field description */
    public static final String PARAM_COUNT = "c";

    /**
     * Method description
     *
     *
     * @param query
     *
     * @throws ServiceException
     */
    public void setQuery(String query) throws ServiceException {
        put(PARAM_QUERY, query);
    }

    /**
     * Method description
     *
     *
     * @param filter
     *
     * @throws ServiceException
     */
    public void setQuery(GenericFilter filter) throws ServiceException {
        if (filter != null) {
            put(PARAM_QUERY, filter.serialize(true));
        }
    }

    /**
     * Method description
     *
     *
     *
     * @param limit
     *
     */
    public void setLimit(int limit) {
        put(PARAM_LIMIT, Integer.toString(limit));
    }

    /**
     * Method description
     *
     *
     *
     *
     * @param value
     *
     */
    public void setMulti(boolean value) {
        put(PARAM_MULTI, Boolean.toString(value));
    }

    /**
     * Method description
     *
     *
     * @param filter
     *
     * @throws ServiceException
     */
    public void setReturnFields(GenericFilter filter) throws ServiceException {
        put(PARAM_RETURN_FIELDS, filter.serialize(true));
    }

    /**
     * Method description
     *
     *
     * @param filter
     *
     * @throws ServiceException
     */
    public void setSortOrder(GenericFilter filter) throws ServiceException {
        put(PARAM_SORT_ORDER, filter.serialize(true));
    }
}
