package br.uff.ic.dyevc.persistence;

import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.RepositoryReferencedException;
import br.uff.ic.dyevc.model.topology.RepositoryFilter;
import br.uff.ic.dyevc.exception.ServiceException;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.model.topology.Topology;
import br.uff.ic.dyevc.services.MongoLabProvider;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;

/**
 * Data Access Object to manipulate topology information
 *
 * @author Cristiano
 */
public class TopologyDAO {

    /**
     * Retrieves the entire topology for all known systems from the database
     *
     * @return the entire known topology for all systems
     * @throws ServiceException
     */
    public Topology readTopology() throws ServiceException {
        LoggerFactory.getLogger(TopologyDAO.class).trace("readTopology -> Entry");
        Topology result = new Topology();
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
     * Retrieves the list of repositories that depends on the repository with the
     * specified id, either by a pushesTo or by a pullsFrom dependency
     *
     * @param id Id of the repository to look for
     * @return List of repositories that relates to the specified repository
     * @throws ServiceException
     */
    public ArrayList<RepositoryInfo> findDependentRepositories(String id) throws ServiceException {
        LoggerFactory.getLogger(TopologyDAO.class).trace("countRelatedRepositories -> Entry");

        String query = "{\"$or\": [{\"pushesTo\": \"" + id + "\"}, "
                + "{\"pullsFrom\": \"" + id + "\"}]}";
        
        MongoLabServiceParms parms = new MongoLabServiceParms();
        parms.setQuery(query);
        ArrayList<RepositoryInfo> result = MongoLabProvider.getRepositories(parms);

        LoggerFactory.getLogger(TopologyDAO.class).trace("countRelatedRepositories -> Exit");
        return result;
    }
    
    /**
     * Update all the repositories in the specified list. If an element does not
     * yet exists, then create it
     *
     * @param repositories List of repositories to be upserted
     * @exception DyeVCException
     */
    public void upsertRepositories(ArrayList<RepositoryInfo> repositories) throws DyeVCException {
        LoggerFactory.getLogger(TopologyDAO.class).trace("upsertRepositories -> Entry");
            for (RepositoryInfo repository : repositories) {
                upsertRepository(repository);
        }
        LoggerFactory.getLogger(TopologyDAO.class).trace("upsertRepositories -> Exit");
    }
    
    /**
     * Update a repository in the database. If the repository does not exist, inserts it
     * @param repository The repository to be updated in the database.
     * @throws DyeVCException 
     */
    public void upsertRepository(RepositoryInfo repository) throws DyeVCException{
        MongoLabProvider.upsertRepository(repository);
    }
    
    /**
     * Delete the repository in the database with the specified id. The application first checks
     * if the repository is not referenced anywhere, otherwise throws an exception
     * @param id Id of the repository to be deleted
     * @throws RepositoryReferencedException when other repositories reference this one
     * @throws DyeVCException 
     */
    public void deleteRepository(String id) throws ServiceException, RepositoryReferencedException {
        ArrayList<RepositoryInfo> dependentRepositories = findDependentRepositories(id);
        if (dependentRepositories.isEmpty()) {
            MongoLabProvider.deleteRepository(id);
        } else {
            throw new RepositoryReferencedException(dependentRepositories);
        }
    }
}
