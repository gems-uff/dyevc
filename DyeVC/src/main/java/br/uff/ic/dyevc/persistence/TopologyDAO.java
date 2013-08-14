package br.uff.ic.dyevc.persistence;

import br.uff.ic.dyevc.exception.DyeVCException;
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
     * Retrieves the number of repositories related to the specified parameter, either
     * by a pushesTo or by a pullsFrom dependency
     *
     * @param repository Repository to look for
     * @return List of repositories that relates to the specified repository
     * @throws ServiceException
     */
    public int countRelatedRepositories(RepositoryInfo repository) throws ServiceException {
        LoggerFactory.getLogger(TopologyDAO.class).trace("countRelatedRepositories -> Entry");

        String query = "{\"$or\": [{\"pushesTo.hostName\": \"" + repository.getHostName() + "\", "
                + "\"pushesTo.cloneName\": \"" + repository.getCloneName() + "\"}, "
                + "{\"pullsFrom.hostName\": \"" + repository.getHostName() + "\", "
                + "\"pullsFrom.cloneName\": \"" + repository.getCloneName() + "\"}]}";
        
        MongoLabServiceParms parms = new MongoLabServiceParms();
        parms.setQuery(query);
        ArrayList<RepositoryInfo> result = MongoLabProvider.getRepositories(parms);

        LoggerFactory.getLogger(TopologyDAO.class).trace("countRelatedRepositories -> Exit");
        return result.size();
    }
    
    /**
     * Update all the elements in the specified topology. If an element does not
     * yet exists, then create it
     *
     * @param topology Topology containing all the elements that should be
     * updated and/or inserted (upsert)
     * @exception DyeVCException
     */
    public void updateTopology(Topology topology) throws DyeVCException {
        LoggerFactory.getLogger(TopologyDAO.class).trace("updateTopology -> Entry");
        for (String system : topology.getSystems()) {
            for (RepositoryInfo repository : topology.getClonesForSystem(system)) {
                updateRepository(repository);
            }
        }
        LoggerFactory.getLogger(TopologyDAO.class).trace("updateTopology -> Exit");
    }
    
    /**
     * Update a repository in the database. If the repository does not exist, inserts it
     * @param repository The repository to be updated in the database.
     * @throws DyeVCException 
     */
    public void updateRepository(RepositoryInfo repository) throws DyeVCException{
        MongoLabProvider.upsertRepository(repository);
    }
    
    /**
     * Delete a repository in the database. The application should first check
     * if the repository is not referenced anywhere, otherwise there will be inconsistency
     * @param repository The repository to be deleted
     * @throws DyeVCException 
     */
    public void deleteRepository(RepositoryInfo repository) throws DyeVCException{
        if (countRelatedRepositories(repository) == 0) {
            MongoLabProvider.deleteRepository(repository);
        }
    }
}
