package br.uff.ic.dyevc.model;

import java.beans.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Cristiano
 */
public final class MonitoredRepositories implements Serializable {

    private static final long serialVersionUID = -7567721142354738718L;
    private PropertyChangeSupport propertySupport;
    private HashMap monitoredRepositories;
    public static final String MONITORED_PROJECTS = "monitoredProjects";

    /**
     * Get the value of monitoredProjects
     *
     * @return the value of monitoredProjects
     */
    public List<Repository> getMonitoredProjects() {
        List<Repository> output = Collections.EMPTY_LIST;

        if (monitoredRepositories != null) {
            output = new ArrayList(monitoredRepositories.values());
        }
        return output;
    }
    
    /**
     * Get an instance of a monitored repository by name
     * @param name Name of the desired monitored repository
     *
     * @return the required monitored repository
     */
    public Repository getMonitoredProjectByName(String name) {
        Repository output = null;
        if (monitoredRepositories != null) {
            output = (Repository)monitoredRepositories.get(name);
        }
        return output;
    }
    
    public void addMonitoredRepository(Repository repository) {
        if (monitoredRepositories == null) {
            monitoredRepositories= new HashMap();
        }
        List<Repository> oldValue = getMonitoredProjects();
        this.monitoredRepositories.put(repository.getName(), repository);
        propertySupport.firePropertyChange(MONITORED_PROJECTS, oldValue, getMonitoredProjects());
    }

    public void removeMonitoredRepository(Repository repository) {
        List<Repository> oldValue = getMonitoredProjects();
        this.monitoredRepositories.remove(repository.getName());
        propertySupport.firePropertyChange(MONITORED_PROJECTS, oldValue, getMonitoredProjects());
    }

    public void removeMonitoredRepository(String repositoryName) {
        List<Repository> oldValue = getMonitoredProjects();
        this.monitoredRepositories.remove(repositoryName);
        propertySupport.firePropertyChange(MONITORED_PROJECTS, oldValue, getMonitoredProjects());
    }

    public MonitoredRepositories() {
        propertySupport = new PropertyChangeSupport(this);
        
//        Repository bean = new Repository();
//        bean.setName("repo name");
//        bean.setCloneAddress("http://algum.endereco");
//        
//        Repository bean2 = new Repository();
//        bean2.setName("repo name 2");
//        bean2.setCloneAddress("http://algum.endereco2");
//        
//        addMonitoredRepository(bean);
//        addMonitoredRepository(bean2);

    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
}
