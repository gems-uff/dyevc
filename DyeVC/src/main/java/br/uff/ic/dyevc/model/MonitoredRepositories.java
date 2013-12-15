package br.uff.ic.dyevc.model;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.exception.RepositoryReferencedException;
import br.uff.ic.dyevc.exception.ServiceException;
import br.uff.ic.dyevc.persistence.TopologyDAO;
import br.uff.ic.dyevc.utils.PreferencesManager;
import br.uff.ic.dyevc.utils.StringUtils;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Models a list of monitored repositories as an AbstractListModel
 *
 * @author Cristiano
 */
public final class MonitoredRepositories extends AbstractTableModel {
    private static final long                      serialVersionUID      = -7567721142354738718L;
    private static final List<MonitoredRepository> monitoredRepositories =
        Collections.synchronizedList(new ArrayList<MonitoredRepository>());
    private static final List<MonitoredRepository> markedForDeletion =
        Collections.synchronizedList(new ArrayList<MonitoredRepository>());
    private static MonitoredRepositories instance = null;

    /** Field description */
    public static final String MONITORED_PROJECTS = "monitoredProjects";

    public static synchronized MonitoredRepositories getInstance() {
        if (instance == null) {
            instance = new MonitoredRepositories();
            PreferencesManager.getInstance().loadMonitoredRepositories();
        }

        return instance;
    }

    /**
     * Get the value of monitoredProjects
     *
     * @return the value of monitoredProjects
     */
    public static List<MonitoredRepository> getMarkedForDeletion() {
        return markedForDeletion;
    }

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
     * Get an instance of a monitored repository by clone address
     *
     * @param cloneAddress Clone address of the desired monitored repository
     * @return the required monitored repository
     */
    public static MonitoredRepository getMonitoredProjectByPath(String cloneAddress) {
        for (MonitoredRepository monitoredRepository : monitoredRepositories) {
            if (monitoredRepository.getNormalizedCloneAddress().equals(StringUtils.normalizePath(cloneAddress))) {
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
    public synchronized void addMonitoredRepository(MonitoredRepository repository) {
        int index = -1;
        for (MonitoredRepository rep : monitoredRepositories) {
            if (rep.getId().equals(repository.getId())) {
                index = monitoredRepositories.indexOf(rep);

                break;
            }
        }

        if (index >= 0) {
            monitoredRepositories.set(index, repository);
            fireTableRowsUpdated(index, index);
        } else {
            if (repository.isMarkedForDeletion()) {
                markedForDeletion.add(repository);
            } else {
                index = monitoredRepositories.size();
                monitoredRepositories.add(repository);
                fireTableRowsInserted(index, index);
            }
        }
    }

    /**
     * Stops monitoring a repository and remove it from the database. If it is
     * still referenced, then only marks it for deletion. The TopologyMonitor
     * will later verify if it can be removed
     *
     * @param repository the repository to be removed
     *
     * @return true, if the repository existed and false otherwise
     * @throws RepositoryReferencedException when there are references to this
     * repositories
     * @throws ServiceException when it is not possible to access the underlying
     * database
     */
    public synchronized boolean removeMonitoredRepository(MonitoredRepository repository)
            throws RepositoryReferencedException, ServiceException {
        int     index = monitoredRepositories.indexOf(repository);
        boolean rv    = false;
        if (index >= 0) {
            TopologyDAO dao = new TopologyDAO();
            try {
                Date lastChanged = dao.deleteRepository(repository.getSystemName(), repository.getId(), true);
                repository.setLastChanged(lastChanged);
            } catch (RepositoryReferencedException rre) {
                monitoredRepositories.remove(repository);
                repository.setMarkedForDeletion(true);
                markedForDeletion.add(repository);
                PreferencesManager.getInstance().persistRepositories();
                fireTableRowsDeleted(index, index);

                throw rre;
            }

            rv = monitoredRepositories.remove(repository);
            PreferencesManager.getInstance().persistRepositories();
            fireTableRowsDeleted(index, index);
        }

        return rv;
    }

    /**
     * Remove an instance of a monitored repository previously marked for
     * deletion that is not referenced anymore. If the repository is still still
     * referenced, then throws an exception.
     *
     * @param repository the repository to be removed
     *
     * @return true, if the instance existed and false otherwise
     * @throws br.uff.ic.dyevc.exception.RepositoryReferencedException
     * @throws br.uff.ic.dyevc.exception.ServiceException
     */
    public synchronized boolean removeMarkedForDeletion(MonitoredRepository repository)
            throws RepositoryReferencedException, ServiceException {
        int     index = markedForDeletion.indexOf(repository);
        boolean rv    = false;
        if (index >= 0) {
            TopologyDAO dao = new TopologyDAO();
            try {
                Date lastChanged = dao.deleteRepository(repository.getSystemName(), repository.getId(), true);
                repository.setLastChanged(lastChanged);
            } catch (RepositoryReferencedException rre) {
                throw rre;
            }

            rv = markedForDeletion.remove(repository);
            PreferencesManager.getInstance().persistRepositories();
            fireTableRowsDeleted(index, index);
        }

        return rv;
    }

    /**
     * Closes the connection established in each of the monitored repositories.
     */
    public synchronized void closeRepositories() {
        for (MonitoredRepository rep : monitoredRepositories) {
            rep.close();
        }
    }

    @Override
    public int getRowCount() {
        return monitoredRepositories.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public String getColumnName(int column) {
        String result = null;
        switch (column) {
        case 0 :
            result = "Status";

            break;

        case 1 :
            result = "System Name";

            break;

        case 2 :
            result = "Clone Name";

            break;

        case 3 :
            result = "ID";

            break;

        case 4 :
            result = "Clone Path";

            break;
        }

        return result;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        Class result;
        switch (columnIndex) {
        case 0 :
            result = MonitoredRepository.class;

            break;

        default :
            result = String.class;
        }

        return result;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        MonitoredRepository rep    = monitoredRepositories.get(rowIndex);
        Object              result = null;
        switch (columnIndex) {
        case 0 :
            result = rep;

            break;

        case 1 :
            result = rep.getSystemName();

            break;

        case 2 :
            result = rep.getName();

            break;

        case 3 :
            result = rep.getId();

            break;

        case 4 :
            result = rep.getCloneAddress();

            break;
        }

        return result;
    }
}
