package br.uff.ic.dyevc.persistence;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.exception.ServiceException;
import br.uff.ic.dyevc.utils.JsonSerializer;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ObjectMapper;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 * Represents a filter to be used in queries. Subclasses can create fields for each filter they want. If a custom
 * query is needed, then the {@link #customQuery} field can be used. In this case, no fields are analyzed and sole the
 * custom query string is used to filter.
 *
 * @author Cristiano
 */
public abstract class GenericFilter {
    /** Field description */
    protected String customQuery = null;

    /**
     * Gets the custom query, using the native database syntax in Json format.
     * @return the custom query
     */
    public String getCustomQuery() {
        return customQuery;
    }

    /**
     * Sets the custom query, using the native database syntax in Json format.
     * @param customQuery the customQuery to set, in Json format.
     */
    public void setCustomQuery(String customQuery) {
        this.customQuery = customQuery;
    }

    /**
     * Serialize this filter to Json. If the {@link #customQuery} field is set, return it. Otherwise serialize all
     * non null filters to Json format.
     *
     * @param filterNulls If true, does not include null values
     * @return
     */
    public String serialize(boolean filterNulls) throws ServiceException {
        if (customQuery != null) {
            return customQuery;
        }

        return JsonSerializer.serialize(this, filterNulls);
    }
}
