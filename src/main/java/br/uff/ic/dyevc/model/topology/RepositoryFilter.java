package br.uff.ic.dyevc.model.topology;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.persistence.GenericFilter;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Filter to be used in repository queries. All filters are inclusive (and operator
 * is implicit) and null fields are discarded.
 * @author Cristiano
 */

public class RepositoryFilter extends GenericFilter {
    private String systemName;
    private String hostName;
    private String cloneName;
    private String clonePath;
    @JsonProperty(value = "_id")
    private String id;

    /**
     * Constructs ...
     */
    public RepositoryFilter() {}

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getCloneName() {
        return cloneName;
    }

    public void setCloneName(String cloneName) {
        this.cloneName = cloneName;
    }

    public String getClonePath() {
        return clonePath;
    }

    public void setClonePath(String path) {
        this.clonePath = path;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
