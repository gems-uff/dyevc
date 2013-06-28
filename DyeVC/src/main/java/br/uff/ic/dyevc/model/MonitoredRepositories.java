package br.uff.ic.dyevc.model;

import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 * Models a list of monitored repositories as an AbstractListModel
 *
 * @author Cristiano
 */
public final class MonitoredRepositories extends AbstractListModel<MonitoredRepository> {

    private static final long serialVersionUID = -7567721142354738718L;
    private static List<MonitoredRepository> monitoredRepositories = new ArrayList<MonitoredRepository>();
    public static final String MONITORED_PROJECTS = "monitoredProjects";

    /**
     * Get the value of monitoredProjects
     *
     * @return the value of monitoredProjects
     */
    public static List<MonitoredRepository> getMonitoredProjects() {
        return monitoredRepositories;
    }

    /**
     * Get an instance of a monitored repository by name
     *
     * @param id Name of the desired monitored repository
     *
     * @return the required monitored repository
     */
    public static MonitoredRepository getMonitoredProjectById(String id) {
        for (MonitoredRepository monitoredRepository : monitoredRepositories) {
            if (monitoredRepository.getId().equals(id)) {
                return monitoredRepository;
            }
        }

        return null;
    }

    /**
     * Add an instance of a monitored repository
     * 
     * @param repository the instance to be added
     */
    public void addMonitoredRepository(MonitoredRepository repository) {
        int index = monitoredRepositories.indexOf(repository);
        if (index >= 0) {
            monitoredRepositories.set(index, repository);
            fireContentsChanged(this, index, index);
        } else {
            index = monitoredRepositories.size();
            monitoredRepositories.add(repository);
            fireIntervalAdded(this, index, index);
        }
    }

    /**
     * Remove an instance of a monitored repository
     * 
     * @param repository the instance to be removed
     * 
     * @return true, if the instance existed and false otherwise
     */
    public boolean removeMonitoredRepository(MonitoredRepository repository) {
        int index = monitoredRepositories.indexOf(repository);
        boolean rv = monitoredRepositories.remove(repository);
        if (index >= 0) {
            fireIntervalRemoved(this, index, index);
        }
        return rv;
    }

    /**
     * Returns the number of monitored repositories
     * 
     * @return the number of monitored repositories
     */
    @Override
    public int getSize() {
        return monitoredRepositories.size();
    }

    /**
     * Returns the instance of monitored repository located at a specified position.
     * 
     * @param index the position to look at
     * 
     * @return the instance of monitored repository located at index.
     */
    @Override
    public MonitoredRepository getElementAt(int index) {
        List<MonitoredRepository> values = getMonitoredProjects();
        return values.get(index);
    }
    
    /**
     * Closes the connection established in each of the monitored repositories. 
     */
    public void closeRepositories() {
        for (MonitoredRepository rep: monitoredRepositories) {
            rep.close();
        }
    }
}
