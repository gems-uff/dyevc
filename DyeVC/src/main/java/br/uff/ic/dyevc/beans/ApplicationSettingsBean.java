package br.uff.ic.dyevc.beans;

import br.uff.ic.dyevc.application.IConstants;
import java.beans.*;
import java.io.Serializable;

/**
 *
 * @author Cristiano
 */
public class ApplicationSettingsBean implements Serializable {
    
    private static final long serialVersionUID = 6840556021845882092L;
    private PropertyChangeSupport propertySupport;
    private static final String WORKING_PATH = System.getProperty("user.home") + IConstants.DIR_SEPARATOR + ".dyevc";;
    public static final String PROP_WORKING_PATH = "workingpath";

    /**
     * Get the value of workingPath
     *
     * @return the value of workingPath
     */
    public String getWorkingPath() {
        return WORKING_PATH;
    }

    private int refreshInterval;
    public static final String PROP_REFRESHINTERVAL = "refreshinterval";

    /**
     * Get the value of refreshInterval
     *
     * @return the value of refreshInterval
     */
    public int getRefreshInterval() {
        return refreshInterval;
    }

    /**
     * Set the value of refreshInterval
     *
     * @param refreshInterval new value of refreshInterval
     */
    public void setRefreshInterval(int refreshInterval) {
        int oldRefreshInterval = this.refreshInterval;
        this.refreshInterval = refreshInterval;
        propertySupport.firePropertyChange(PROP_REFRESHINTERVAL, oldRefreshInterval, refreshInterval);
    }
    
    private String lastUsedPath;
    public static final String PROP_LAST_USED_PATH = "lastusedpath";

    public String getLastUsedPath() {
        return lastUsedPath;
    }

    public void setLastUsedPath(String lastUsedPath) {
        this.lastUsedPath = lastUsedPath;
    }
    
    public ApplicationSettingsBean() {
        propertySupport = new PropertyChangeSupport(this);
    }
    

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
}
