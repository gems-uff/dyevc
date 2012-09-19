/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.beans;

import br.uff.ic.dyevc.application.IConstants;
import java.beans.*;
import java.io.Serializable;

/**
 *
 * @author Cristiano
 */
public class ApplicationPropertiesBean implements Serializable {
    
    public static final String PROP_APP_VERSION = "appVersion";
    private static final long serialVersionUID = -5459049343284022481L;
    private String appVersion;
    private PropertyChangeSupport propertySupport;
    
    public ApplicationPropertiesBean() {
        propertySupport = new PropertyChangeSupport(this);
        appVersion = "DyeVC version: " + IConstants.VERSION_NUMBER;
    }
    
    public String getAppVersion() {
        return appVersion;
    }
    
    public void setAppVersion(String value) {
        String oldValue = appVersion;
        appVersion = value;
        propertySupport.firePropertyChange(PROP_APP_VERSION, oldValue, appVersion);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
}
