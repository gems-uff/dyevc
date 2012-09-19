/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.beans;

import java.beans.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Cristiano
 */
public class MonitoredRepositoriesBean implements Serializable {

    private static final long serialVersionUID = -7567721142354738718L;
    private PropertyChangeSupport propertySupport;
    private HashMap monitoredRepositories;
    public static final String MONITORED_REPOSITORIES = "monitoredRepositories";

    /**
     * Get the value of monitoredProjects
     *
     * @return the value of monitoredProjects
     */
    public List<RepositoryBean> getMonitoredProjects() {
        List<RepositoryBean> output = Collections.EMPTY_LIST;

        if (monitoredRepositories != null) {
            output = new ArrayList(monitoredRepositories.values());
        }
        return output;
    }

    /**
     * Set the value of monitoredProjects
     *
     * @param monitoredProjects new value of monitoredProjects
     */
    public void setMonitoredProjects(List<RepositoryBean> monitoredProjects) {
        List<RepositoryBean> oldValue = getMonitoredProjects();
        this.monitoredRepositories = new HashMap();
        for (Iterator<RepositoryBean> it = monitoredProjects.iterator(); it.hasNext();) {
            RepositoryBean repositoryBean = it.next();

            this.monitoredRepositories.put(repositoryBean.getName(), repositoryBean);
        }
        propertySupport.firePropertyChange(MONITORED_REPOSITORIES, oldValue, monitoredProjects);
    }

    public MonitoredRepositoriesBean() {
        propertySupport = new PropertyChangeSupport(this);

    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
}
