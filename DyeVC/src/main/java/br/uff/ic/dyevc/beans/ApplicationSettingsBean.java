package br.uff.ic.dyevc.beans;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.application.IConstants;

//~--- JDK imports ------------------------------------------------------------

import java.beans.*;

import java.io.Serializable;

/**
 *
 * @author Cristiano
 */
public class ApplicationSettingsBean implements Serializable {
    private static final long     serialVersionUID = 6840556021845882092L;
    private PropertyChangeSupport propertySupport;
    private static final String   WORKING_PATH = System.getProperty("user.home") + IConstants.DIR_SEPARATOR + ".dyevc";;

    /**
     * Get the value of workingPath
     *
     * @return the value of workingPath
     */
    public String getWorkingPath() {
        return WORKING_PATH;
    }

    private int     refreshInterval;
    private boolean performanceMode;

    /** Published name for the refreshInterval property */
    public static final String PROP_REFRESHINTERVAL = "refreshinterval";

    /** Published name for the performanceMode property */
    public static final String PROP_PERFORMANCE_MODE = "performanceMode";

    /**
     * Get the value of performanceMode
     *
     * @return the value of performanceMode
     */
    public boolean isPerformanceMode() {
        return performanceMode;
    }

    /**
     * Set the value of performanceMode
     *
     * @param performanceMode new value of performanceMode
     */
    public void setPerformanceMode(boolean performanceMode) {
        boolean oldPerformanceMode = this.performanceMode;
        this.performanceMode = performanceMode;
        propertySupport.firePropertyChange(PROP_PERFORMANCE_MODE, oldPerformanceMode, performanceMode);
    }

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

    /** Published name for the lastUsedPath property */
    public static final String PROP_LAST_USED_PATH = "lastusedpath";

    public String getLastUsedPath() {
        return lastUsedPath;
    }

    public void setLastUsedPath(String lastUsedPath) {
        this.lastUsedPath = lastUsedPath;
    }

    private String lastApplicationVersionUsed;

    /** Published name for the lastApplicationVersionUsed property */
    public static final String PROP_LAST_APP_VERSION_USED = "lastAppVersionUsed";

    public String getLastApplicationVersionUsed() {
        return lastApplicationVersionUsed;
    }

    public void setLastApplicationVersionUsed(String lastApplicationVersionUsed) {
        this.lastApplicationVersionUsed = lastApplicationVersionUsed;
    }

    /**
     * Constructs an object of this type.
     */
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
