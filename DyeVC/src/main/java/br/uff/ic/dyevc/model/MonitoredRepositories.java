package br.uff.ic.dyevc.model;

import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;

/**
 *
 * @author Cristiano
 */
public final class MonitoredRepositories extends AbstractListModel<MonitoredRepository> {

    private static final long serialVersionUID = -7567721142354738718L;
    private List<MonitoredRepository> monitoredRepositories = new ArrayList<MonitoredRepository>();
    public static final String MONITORED_PROJECTS = "monitoredProjects";

    /**
     * Get the value of monitoredProjects
     *
     * @return the value of monitoredProjects
     */
    public List<MonitoredRepository> getMonitoredProjects() {
        return monitoredRepositories;
    }

    /**
     * Get an instance of a monitored repository by name
     *
     * @param id Name of the desired monitored repository
     *
     * @return the required monitored repository
     */
    public MonitoredRepository getMonitoredProjectById(String id) {
        for (MonitoredRepository monitoredRepository : monitoredRepositories) {
            if (monitoredRepository.getId().equals(id)) {
                return monitoredRepository;
            }
        }

        return null;
    }

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

    public boolean removeMonitoredRepository(MonitoredRepository repository) {
        int index = monitoredRepositories.indexOf(repository);
        boolean rv = monitoredRepositories.remove(repository);
        if (index >= 0) {
            fireIntervalRemoved(this, index, index);
        }
        return rv;
    }

    @Override
    public int getSize() {
        return monitoredRepositories.size();
    }

    @Override
    public MonitoredRepository getElementAt(int index) {
        List<MonitoredRepository> values = getMonitoredProjects();
        return values.get(index);
    }
}
