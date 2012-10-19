package br.uff.ic.dyevc.model;

import br.uff.ic.dyevc.tools.vcs.GitConnector;
import java.beans.*;
import java.io.Serializable;
import java.util.List;
import org.eclipse.jgit.lib.Repository;

/**
 *
 * @author Cristiano
 */
public class MonitoredRepository implements Serializable {

    public static final String NAME = "name";
    private static final long serialVersionUID = -8604175800390199323L;
    private String name;
    private String cloneAddress;
    private List<RepositoryRelationship> origins;
    public static final String PROP_CLONEADDRESS = "cloneAddress";

    /**
     * Get the value of cloneAddress
     *
     * @return the value of cloneAddress
     */
    public String getCloneAddress() {
        return cloneAddress;
    }

    /**
     * Set the value of cloneAddress
     *
     * @param cloneAddress new value of cloneAddress
     */
    public void setCloneAddress(String cloneAddress) {
        String oldCloneAddress = this.cloneAddress;
        this.cloneAddress = cloneAddress;
        propertySupport.firePropertyChange(PROP_CLONEADDRESS, oldCloneAddress, cloneAddress);
    }
    private PropertyChangeSupport propertySupport;

    public MonitoredRepository() {
        this.name = "";
        this.cloneAddress = "";
        this.originUrl = "";
        propertySupport = new PropertyChangeSupport(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        String oldValue = name;
        name = value;
        propertySupport.firePropertyChange(NAME, oldValue, name);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
    private String originUrl;

    @Override
    public String toString() {
        return "Repository{" + "name=" + name + ", cloneAddress=" + cloneAddress + '}';
    }

    
}
