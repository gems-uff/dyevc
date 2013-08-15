package br.uff.ic.dyevc.model;

import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.persistence.TopologyDAO;
import br.uff.ic.dyevc.utils.PreferencesUtils;
import br.uff.ic.dyevc.utils.StringUtils;
import br.uff.ic.dyevc.utils.SystemUtils;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Models a list of monitored repositories as an AbstractListModel
 *
 * @author Cristiano
 */
public final class MonitoredRepositories extends AbstractTableModel {

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
     * Get an instance of a monitored repository by clone address
     * @param cloneAddress Clone address of the desired monitored repository
     * @return the required monitored repository
     */
    public static MonitoredRepository getMonitoredProjectByPath(String cloneAddress) {
        for (MonitoredRepository monitoredRepository : monitoredRepositories) {
            if (monitoredRepository.getNormalizedCloneAddress()
                    .equals(StringUtils.normalizePath(cloneAddress))) {
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
            fireTableRowsUpdated(index, index);
        } else {
            index = monitoredRepositories.size();
            monitoredRepositories.add(repository);
            fireTableRowsInserted(index, index);
        }
    }

    /**
     * Remove an instance of a monitored repository
     * 
     * @param repository the instance to be removed
     * 
     * @return true, if the instance existed and false otherwise
     */
    public boolean removeMonitoredRepository(MonitoredRepository repository) throws DyeVCException {
        int index = monitoredRepositories.indexOf(repository);
        boolean rv = false;
        if (index >= 0) {
            TopologyDAO dao = new TopologyDAO();
            dao.deleteRepository(repository.getId());
            PreferencesUtils.persistRepositories();
            rv = monitoredRepositories.remove(repository);
            fireTableRowsDeleted(index, index);
        }
        return rv;
    }

    /**
     * Closes the connection established in each of the monitored repositories. 
     */
    public void closeRepositories() {
        for (MonitoredRepository rep: monitoredRepositories) {
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
        switch(column) {
            case 0:
                result = "Status";
                break;
            case 1:
                result = "System Name";
                break;
            case 2:
                result = "Clone Name";
                break;
            case 3:
                result = "ID";
                break;
            case 4:
                result = "Clone Path";
                break;
        }
        return result;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        Class result;
        switch(columnIndex) {
            case 0:
                result = MonitoredRepository.class;
                break;
            default:
                result = String.class;
        }
        return result;
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        MonitoredRepository rep = monitoredRepositories.get(rowIndex);
        Object result = null;
        switch(columnIndex) {
            case 0:
                result = rep;
                break;
            case 1:
                result = rep.getSystemName();
                break;
            case 2:
                result = rep.getName();
                break;
            case 3:
                result = rep.getId();
                break;
            case 4:
                result = rep.getCloneAddress();
                break;
        }
        return result;
    }
}
