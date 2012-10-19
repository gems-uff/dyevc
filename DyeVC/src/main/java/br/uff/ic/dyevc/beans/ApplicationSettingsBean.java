/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.beans;

import java.beans.*;
import java.io.Serializable;

/**
 *
 * @author Cristiano
 */
public class ApplicationSettingsBean implements Serializable {
    
    private static final long serialVersionUID = 6840556021845882092L;
    private PropertyChangeSupport propertySupport;
    private String workingPath;
    public static final String PROP_WORKING_PATH = "workingpath";

    /**
     * Get the value of workingPath
     *
     * @return the value of workingPath
     */
    public String getWorkingPath() {
        return workingPath;
    }

    /**
     * Set the value of workingPath
     *
     * @param workingPath new value of workingPath
     */
    public void setWorkingPath(String workingPath) {
        String oldWorkingPath = this.workingPath;
        this.workingPath = workingPath;
        propertySupport.firePropertyChange(PROP_WORKING_PATH, oldWorkingPath, workingPath);
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
