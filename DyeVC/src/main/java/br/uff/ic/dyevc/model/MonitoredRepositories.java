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
    public List<MonitoredRepository> getMonitoredProjects() {
        List<MonitoredRepository> output = Collections.EMPTY_LIST;

        if (monitoredRepositories != null) {
            output = new ArrayList(monitoredRepositories.values());
        }
        return output;
    }
    
    /**
     * Get an instance of a monitored repository by name
     * @param id Name of the desired monitored repository
     *
     * @return the required monitored repository
     */
    public MonitoredRepository getMonitoredProjectById(String id) {
        MonitoredRepository output = null;
        if (monitoredRepositories != null) {
            output = (MonitoredRepository)monitoredRepositories.get(id);
        }
        return output;
    }
    
    public void addMonitoredRepository(MonitoredRepository repository) {
        if (monitoredRepositories == null) {
            monitoredRepositories= new HashMap();
        }
        List<MonitoredRepository> oldValue = getMonitoredProjects();
        this.monitoredRepositories.put(repository.getId(), repository);
        propertySupport.firePropertyChange(MONITORED_PROJECTS, oldValue, getMonitoredProjects());
    }

    public void removeMonitoredRepository(MonitoredRepository repository) {
        List<MonitoredRepository> oldValue = getMonitoredProjects();
        this.monitoredRepositories.remove(repository.getId());
        propertySupport.firePropertyChange(MONITORED_PROJECTS, oldValue, getMonitoredProjects());
    }

    public void removeMonitoredRepository(String repositoryId) {
        List<MonitoredRepository> oldValue = getMonitoredProjects();
        this.monitoredRepositories.remove(repositoryId);
        propertySupport.firePropertyChange(MONITORED_PROJECTS, oldValue, getMonitoredProjects());
    }

    public MonitoredRepositories() {
        propertySupport = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
}
