package br.uff.ic.dyevc.persistence;

import br.uff.ic.dyevc.exception.DyeVCException;
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

    public Topology readTopology() throws DyeVCException {
        LoggerFactory.getLogger(TopologyDAO.class).trace("Constructor -> Entry");
        Topology result = Topology.getTopology();
        ArrayList<RepositoryInfo> repositories = MongoLabProvider.getRepositories(null);
        result.resetTopology(repositories);

        LoggerFactory.getLogger(TopologyDAO.class).trace("Constructor -> Exit");
        return result;
    }
    
}
