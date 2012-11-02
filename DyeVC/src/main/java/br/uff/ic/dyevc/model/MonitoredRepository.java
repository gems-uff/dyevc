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
    private String id;
    private String cloneAddress;
    private boolean needsAuthentication;
    private String user;
    private String password;
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
        this.user = "";
        this.password = "";
        this.id = "";
        this.needsAuthentication = false;
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
    @Override
    public String toString() {
        return new StringBuilder("Repository{name=").append(name)
                .append(", id=").append(id)
                .append(", cloneAddress=").append(cloneAddress)
                .append(", needsAuthentication=").append(needsAuthentication)
                .append("}").toString();
    }

    public boolean needsAuthentication() {
        return needsAuthentication;
    }

    public void setNeedsAuthentication(boolean needsAuthentication) {
        this.needsAuthentication = needsAuthentication;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
}
