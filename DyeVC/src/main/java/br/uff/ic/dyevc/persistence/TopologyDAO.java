package br.uff.ic.dyevc.persistence;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.exception.RepositoryReferencedException;
import br.uff.ic.dyevc.exception.ServiceException;
import br.uff.ic.dyevc.model.topology.RepositoryFilter;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.model.topology.Topology;
import br.uff.ic.dyevc.services.MongoLabProvider;
import br.uff.ic.dyevc.utils.DateUtil;
import br.uff.ic.dyevc.utils.SystemUtils;

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

/**
 * Data Access Object to manipulate topology information
 *
 * @author Cristiano
 */
public class TopologyDAO {
    /**
     * Retrieves the topology for a specific system
     *
     * @param systemName the name of the system to retrieve the topology
     * @return the known topology for the specified system
     * @throws ServiceException
     */
    public Topology readTopologyForSystem(String systemName) throws ServiceException {
        LoggerFactory.getLogger(TopologyDAO.class).trace("readTopologyForSystem -> Entry");
        Topology             result = new Topology();
        MongoLabServiceParms parms  = new MongoLabServiceParms();

        RepositoryFilter     filter = new RepositoryFilter();
        filter.setSystemName(systemName);
        parms.setQuery(filter);

        ArrayList<RepositoryInfo> repositories = MongoLabProvider.getRepositories(parms);
        result.resetTopology(repositories);

        LoggerFactory.getLogger(TopologyDAO.class).trace("readTopologyForSystem -> Exit");

        return result;
    }

    /**
     * Retrieves the entire topology for all known systems from the database
     *
     * @return the entire known topology for all systems
     * @throws ServiceException
     */
    public Topology readTopology() throws ServiceException {
        LoggerFactory.getLogger(TopologyDAO.class).trace("readTopology -> Entry");
        Topology                  result       = new Topology();
        ArrayList<RepositoryInfo> repositories = MongoLabProvider.getRepositories(null);
        result.resetTopology(repositories);

        LoggerFactory.getLogger(TopologyDAO.class).trace("readTopology -> Exit");

        return result;
    }

    /**
     * Retrieves a list of repositories that match the filter criteria
     *
     * @param repFilter Filter to be applied
     * @return List of repositories that match the specified filter
     * @throws ServiceException
     */
    public ArrayList<RepositoryInfo> getRepositoriesByQuery(RepositoryFilter repFilter) throws ServiceException {
        LoggerFactory.getLogger(TopologyDAO.class).trace("getRepositoriesByQuery -> Entry");

        MongoLabServiceParms parms = new MongoLabServiceParms();
        parms.setQuery(repFilter);
        ArrayList<RepositoryInfo> result = MongoLabProvider.getRepositories(parms);

        LoggerFactory.getLogger(TopologyDAO.class).trace("getRepositoriesByQuery -> Exit");

        return result;
    }

    /**
     * Retrieves the list of repositories that depends on the repository with the specified id, either by a pushesTo or
     * by a pullsFrom dependency
     *
     * @param id Id of the repository to look for
     * @return List of repositories that relates to the specified repository
     * @throws ServiceException
     */
    public ArrayList<RepositoryInfo> findDependentRepositories(String id) throws ServiceException {
        LoggerFactory.getLogger(TopologyDAO.class).trace("findDependentRepositories -> Entry");

        String               query = "{\"$or\": [{\"pushesTo\": \"" + id + "\"}, " + "{\"pullsFrom\": \"" + id
                                     + "\"}]}";

        MongoLabServiceParms parms = new MongoLabServiceParms();
        parms.setQuery(query);
        ArrayList<RepositoryInfo> result = MongoLabProvider.getRepositories(parms);

        LoggerFactory.getLogger(TopologyDAO.class).trace("findDependentRepositories -> Exit");

        return result;
    }

    /**
     * Update all the repositories in the specified list. If an element does not yet exists, then create it
     *
     * @param repositories List of repositories to be upserted
     * @return The last changed date/time for this group of repositories
     * @exception ServiceException
     */
    public Date upsertRepositories(ArrayList<RepositoryInfo> repositories) throws ServiceException {
        LoggerFactory.getLogger(TopologyDAO.class).trace("upsertRepositories -> Entry");

        Date lastChanged = DateUtil.getLocalTimeInUTC();
        for (RepositoryInfo repository : repositories) {
            repository.setLastChanged(lastChanged);
            upsertRepository(repository);
        }

        LoggerFactory.getLogger(TopologyDAO.class).trace("upsertRepositories -> Exit");

        return lastChanged;
    }

    /**
     * Update a repository in the database. If the repository does not exist, inserts it
     *
     * @param repository The repository to be updated in the database.
     * @throws ServiceException
     * @return The last changed date/time for this repository
     */
    public Date upsertRepository(RepositoryInfo repository) throws ServiceException {
        Date lastChanged = repository.getLastChanged();
        if (lastChanged == null) {
            lastChanged = DateUtil.getLocalTimeInUTC();
            repository.setLastChanged(lastChanged);
        }

        MongoLabProvider.upsertRepository(repository);

        return lastChanged;
    }

    /**
     * <p>
     * Deletes the monitored repository with the specified id in the specified system from the database.</p>
     * <p>
     * The application first checks if the repository is not referenced anywhere, throwing an exception in that case.
     * </p>
     * <p>
     * If the monitored repository does not have a system name configured, ignores it and does nothing, returning
     * null.</p>
     * <p>
     * If the repository is monitored by someone else, just removes this hostname from the list, keeping it in the
     * database</p>
     *
     * @param systemName System where the repository belongs to
     * @param repId the Id of the repository to be deleted
     * @param removeFromMonitoring If true, remove this hostname from the list of hostnames that monitor the repository.
     * @throws br.uff.ic.dyevc.exception.ServiceException when an error occurred calling the provider
     * @throws RepositoryReferencedException when other repositories reference this one
     * @return The date/time this repository was deleted or null if it was not deleted.
     */
    public Date deleteRepository(String systemName, String repId, boolean removeFromMonitoring)
            throws ServiceException, RepositoryReferencedException {
        Date lastChanged = null;

        // Only repositories with system names have to be deleted from the database
        if (("".equals(systemName) || "no name".equals(systemName))) {
            return null;
        }

        RepositoryFilter filter = new RepositoryFilter();
        filter.setSystemName(systemName);
        filter.setId(repId);

        ArrayList<RepositoryInfo> reps = getRepositoriesByQuery(filter);

        // Only one repository should be returned from above query. If no record is returned, than this repository
        // was already deleted.
        if (reps.isEmpty()) {
            return null;
        }

        // Removes this hostname from the list of hostnames that monitor this repository
        RepositoryInfo            info                  = reps.get(0);
        Set<String>               monitoredBy           = info.getMonitoredBy();
        boolean                   changed               = false;
        ArrayList<RepositoryInfo> dependentRepositories = null;

        if (removeFromMonitoring) {
            changed = monitoredBy.remove(SystemUtils.getLocalHostname());
        }

        if (monitoredBy.isEmpty()) {
            // if no one else monitors this repository, then it can be removed from topology
            // Verify if there is any repository that pushes to or pulls from this repository.
            dependentRepositories = findDependentRepositories(repId);

            if (dependentRepositories.isEmpty()) {
                // No one depends upon this repository. Verifies if any repository depends upon this repository's push or pull
                // list. If not, than these repositories can be deleted as well
                lastChanged = doRemove(info);
            } else if (changed) {
                // if someone else monitors this repository and its monitoredBy list was changed, updates it
                lastChanged = upsertRepository(info);
            }
        }

        if ((dependentRepositories != null) &&!dependentRepositories.isEmpty()) {
            throw new RepositoryReferencedException(dependentRepositories);
        }

        return lastChanged;
    }

    /**
     * Removes the specified repository from the database
     *
     * @param info the repository to be removed
     * @return The date/time the removal occurred
     * @throws ServiceException
     */
    private Date doRemove(RepositoryInfo info) throws ServiceException {
        Set<String> related = info.getPullsFrom();
        related.addAll(info.getPushesTo());

        CommitDAO commitDao = new CommitDAO();

        MongoLabProvider.deleteRepository(info.getId());
        commitDao.removeRepositoryFromAllCommits(info.getSystemName(), info.getId());

        for (String id : related) {
            try {
                deleteRepository(info.getSystemName(), id, false);

            } catch (RepositoryReferencedException re) {
                LoggerFactory.getLogger(TopologyDAO.class).warn("Repository <" + id + ">, referenced by <"
                                        + info.getId()
                                        + "> could not be deleted from the topology, because it is still referenced.");
            } catch (ServiceException se) {
                LoggerFactory.getLogger(TopologyDAO.class).warn("Repository <" + id + ">, referenced by <"
                                        + info.getId()
                                        + "> could not be deleted from the topology, due to the following error:", se);
            }
        }

        commitDao.deleteOrphanedCommits(info.getSystemName());

        return DateUtil.getLocalTimeInUTC();
    }
}
