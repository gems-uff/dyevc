package br.uff.ic.dyevc.persistence;

import br.uff.ic.dyevc.model.topology.RepositoryFilter;
import br.uff.ic.dyevc.exception.ServiceException;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.model.topology.Topology;
import br.uff.ic.dyevc.services.MongoLabProvider;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;

/**
 * Data Access Object to manipulate topology information
 * @author Cristiano
 */
public class TopologyDAO {

    public Topology readTopology() throws ServiceException {
        LoggerFactory.getLogger(TopologyDAO.class).trace("readTopology -> Entry");
        Topology result = new Topology();
        ArrayList<RepositoryInfo> repositories = MongoLabProvider.getRepositories(null);
        result.resetTopology(repositories);

        LoggerFactory.getLogger(TopologyDAO.class).trace("readTopology -> Exit");
        return result;
    }
    
    public ArrayList<RepositoryInfo> getRepositoriesByQuery(RepositoryFilter repFilter) throws ServiceException {
        LoggerFactory.getLogger(TopologyDAO.class).trace("getRepositoriesByQuery -> Entry");
        
        MongoLabServiceParms parms = new MongoLabServiceParms();
        parms.setQuery(repFilter);
        ArrayList<RepositoryInfo> result = MongoLabProvider.getRepositories(parms);

        LoggerFactory.getLogger(TopologyDAO.class).trace("getRepositoriesByQuery -> Exit");
        return result;
    }
    
}
