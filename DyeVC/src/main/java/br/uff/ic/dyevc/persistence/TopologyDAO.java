package br.uff.ic.dyevc.persistence;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.RepositoryReferencedException;
import br.uff.ic.dyevc.exception.ServiceException;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.topology.RepositoryFilter;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.model.topology.Topology;
import br.uff.ic.dyevc.services.MongoLabProvider;
import br.uff.ic.dyevc.utils.DateUtil;

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
     * Retrieves the list of repositories that depends on the repository with
     * the specified id, either by a pushesTo or by a pullsFrom dependency
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
     * Update all the repositories in the specified list. If an element does not
     * yet exists, then create it
     *
     * @param repositories List of repositories to be upserted
     * @return The last changed date/time for this group of repositories
     * @exception DyeVCException
     */
    public Date upsertRepositories(ArrayList<RepositoryInfo> repositories) throws DyeVCException {
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
     * Update a repository in the database. If the repository does not exist,
     * inserts it
     *
     * @param repository The repository to be updated in the database.
     * @throws DyeVCException
     * @return The last changed date/time for this repository
     */
    public Date upsertRepository(RepositoryInfo repository) throws DyeVCException {
        Date lastChanged = repository.getLastChanged();
        if (lastChanged == null) {
            lastChanged = DateUtil.getLocalTimeInUTC();
            repository.setLastChanged(lastChanged);
        }

        MongoLabProvider.upsertRepository(repository);

        return lastChanged;
    }

    /**
     * Deletes the monitored repository  with the specified id in the specified system from the database. The
     * application first checks if the repository is not referenced anywhere,
     * otherwise throws an exception. If the monitored repository does not have
     * a system name configured, ignores it and does nothing.
     *
     * @param systemName System where the repository belongs to
     * @param repId the Id of the repository to be deleted
     * @throws br.uff.ic.dyevc.exception.ServiceException when an error occurred calling the provider
     * @throws RepositoryReferencedException when other repositories reference
     * this one
     * @return The date/time this repository was deleted.
     */
    public Date deleteRepository(String systemName, String repId)
            throws ServiceException, RepositoryReferencedException {
        Date lastChanged = null;

        // Only repositories with system names have to be deleted from the database
        if (!("".equals(systemName) || "no name".equals(systemName))) {

            // Verify if there is any repository that pushes to or pulls from this repository.
            ArrayList<RepositoryInfo> dependentRepositories = findDependentRepositories(repId);
            if (dependentRepositories.isEmpty()) {

                // No one depends upon this repository. Verifies if any repository depends upon this repository's push or pull
                // list. If not, than these repositories can be deleted as well
                RepositoryFilter filter = new RepositoryFilter();
                filter.setSystemName(systemName);
                filter.setId(repId);

                ArrayList<RepositoryInfo> reps = getRepositoriesByQuery(filter);

                // Only one repository should be returned from above query. If no record is returned, than this repository
                // was already deleted.
                if (reps.isEmpty()) {
                    return null;
                }

                RepositoryInfo info    = reps.get(0);
                Set<String>    related = info.getPullsFrom();
                related.addAll(info.getPushesTo());

                MongoLabProvider.deleteRepository(repId);

                for (String id : related) {
                    try {
                        deleteRepository(systemName, id);
                    } catch (RepositoryReferencedException re) {
                        LoggerFactory.getLogger(TopologyDAO.class).warn(
                            "Repository <" + id + ">, referenced by <" + repId
                            + "> could not be deleted from the topology, because it is still referenced.", re);
                    } catch (ServiceException se) {
                        LoggerFactory.getLogger(TopologyDAO.class).warn(
                            "Repository <" + id + ">, referenced by <" + repId
                            + "> could not be deleted from the topology, due to the following error:", se);
                    }
                }

                lastChanged = DateUtil.getLocalTimeInUTC();
            } else {
                throw new RepositoryReferencedException(dependentRepositories);
            }
        }

        return lastChanged;
    }
}
