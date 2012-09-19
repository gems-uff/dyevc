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
public class RepositoryBean implements Serializable {

    public static final String NAME = "name";
    private static final long serialVersionUID = -8604175800390199323L;
    private String name;
    private String cloneAddress;
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

    public RepositoryBean() {
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
}
