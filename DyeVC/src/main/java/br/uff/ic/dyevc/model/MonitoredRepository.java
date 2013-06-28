package br.uff.ic.dyevc.model;

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.tools.vcs.git.GitConnector;
import br.uff.ic.dyevc.utils.PreferencesUtils;
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
     * Status of the monitored repository.
     *
     * @see RepositoryStatus
     */
    private RepositoryStatus repStatus;
    
    /**
     * Connection with the working clone for this monitored repository
     */
    private GitConnector workingCloneConnection;
    
    /**
     * Connection with the clone for this monitored repository
     */
    private GitConnector cloneConnection;

    /**
     * Get the value of cloneAddress
     *
     * @return the value of cloneAddress
     */
    public String getCloneAddress() {
        return cloneAddress;
    }
    

    /**
     * Get the path to working clone
     *
     * @return the path to working clone
     */
    public String getWorkingCloneAddress() {
        ApplicationSettingsBean settings = PreferencesUtils.loadPreferences();

        String pathTemp = settings.getWorkingPath()
                + IConstants.DIR_SEPARATOR + getId();
        return pathTemp;
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

    public MonitoredRepository(String id) {
        this.name = "";
        this.cloneAddress = "";
        this.id = id;
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
                .append("}").toString();
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

    /**
     * Gets the git connector for this monitored repository
     * @return the git connector for this monitored repository
     * @throws VCSException
     */
    public synchronized GitConnector getConnection() throws VCSException {
        if (cloneConnection == null) {
            cloneConnection = new GitConnector(this.cloneAddress, this.id);
        }
        return cloneConnection;
    }

    /**
     * Gets the git connection for the working clone of this monitored repository
     * @return the git connector for the working clone of this monitored repository
     * @throws VCSException
     */
    public synchronized GitConnector getWorkingCloneConnection() throws VCSException {
        if (workingCloneConnection == null) {
            workingCloneConnection = new GitConnector(this.getWorkingCloneAddress(), this.id);
        }
        return workingCloneConnection;
    }

    /**
     * Sets the git connection for the working clone of this monitored repository.
     * If a connection is already set, then close it before setting the new one.
     * @param connection the connection to be set
     * @throws VCSException
     */
    public synchronized void setWorkingCloneConnection(GitConnector connection) throws VCSException {
        if (this.workingCloneConnection != null) this.workingCloneConnection.close();
        this.workingCloneConnection = connection;
    }
    
    /**
     * Close connection established with the working clone
     * If a connection is not established for a particular monitored, does nothing.
     */
    public void close() {
        if (workingCloneConnection != null) {
            workingCloneConnection.close();
        }
    }
}
