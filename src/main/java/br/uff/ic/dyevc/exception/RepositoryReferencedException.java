package br.uff.ic.dyevc.exception;

import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import java.util.ArrayList;

/**
 * Exception to mark that a repository cannot be deleted from the database because
 * it is referenced by other repositories. The list of repositories that references it
 * can be obtained using the method "getRelatedRepositories".
 * 
 * @author Cristiano
 */
@SuppressWarnings("serial")
public class RepositoryReferencedException extends DyeVCException{
    ArrayList<RepositoryInfo> relatedRepositories;

    public ArrayList<RepositoryInfo> getRelatedRepositories() {
        return relatedRepositories;
    }
    
    public RepositoryReferencedException(ArrayList<RepositoryInfo> repoList) {
        super();
        this.relatedRepositories = repoList;
    }
    
}
