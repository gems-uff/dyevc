package br.uff.ic.dyevc.model;

import java.beans.*;
import java.io.Serializable;

/**
 * Models a monitored repository. A monitored repository is a repository 
 * configured to be monitored from time to time.
 *
 * @author Cristiano
 */
public class MonitoredRepository implements Serializable {

    /**
     * How the attribute "name" is called
     */
    public static final String NAME = "name";
    
    /**
     * How the attribute "cloneAddress" is called
     */
    public static final String PROP_CLONEADDRESS = "cloneAddress";
    private static final long serialVersionUID = -8604175800390199323L;
    
    /**
     * Name of the repository.
     */
    private String name;
    
    /**
     * Identification of the repository.
     */
    private String id;
    
    /**
     * Address where the monitored repository is located.
     */
    private String cloneAddress;
    
    /**
     * Specifies if the monitored repository needs authentication. If true, user 
     * and password attributes must be provided.
     */
    private boolean needsAuthentication;
    
    /**
     * User to connect to the monitored repository.
     */
    private String user;
    
    /**
     * Password to connect to the monitored repository.
     */
    private String password;
    
    /**
     * Status of the monitored repository.
     * 
     * @see RepositoryStatus
     */
    private RepositoryStatus repStatus;

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
        this.repStatus = new RepositoryStatus("");
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

    public RepositoryStatus getRepStatus() {
        return repStatus;
    }

    public void setRepStatus(RepositoryStatus repStatus) {
        this.repStatus = repStatus;
    }
    
}
