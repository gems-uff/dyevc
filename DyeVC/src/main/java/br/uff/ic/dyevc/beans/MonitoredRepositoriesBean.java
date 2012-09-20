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
public final class MonitoredRepositoriesBean implements Serializable {

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
    
    public void addMonitoredRepository(RepositoryBean repository) {
        if (monitoredRepositories == null) {
            monitoredRepositories= new HashMap();
        }
        this.monitoredRepositories.put(repository.getName(), repository);
    }

    public void removeMonitoredRepository(RepositoryBean repository) {
        this.monitoredRepositories.remove(repository.getName());
    }

    public void removeMonitoredRepository(String repositoryName) {
        this.monitoredRepositories.remove(repositoryName);
    }

    public MonitoredRepositoriesBean() {
        propertySupport = new PropertyChangeSupport(this);
        
        RepositoryBean bean = new RepositoryBean();
        bean.setName("repo name");
        bean.setCloneAddress("http://algum.endereco");
        
        RepositoryBean bean2 = new RepositoryBean();
        bean2.setName("repo name 2");
        bean2.setCloneAddress("http://algum.endereco2");
        
        addMonitoredRepository(bean);
        addMonitoredRepository(bean2);

    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
}
