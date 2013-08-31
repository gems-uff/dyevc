package br.uff.ic.dyevc.persistence;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.ServiceException;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.topology.CommitFilter;
import br.uff.ic.dyevc.services.MongoLabProvider;

import org.codehaus.jackson.map.ObjectMapper;

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Data Access Object to manipulate commit information
 *
 * @author Cristiano
 */
public class CommitDAO {
    /**
     * Number of elements to send in bulk inserts;
     */
    private static final int BULK_INSERT_SIZE = 3000;

    /**
     * Limit of commits to be returned in queries
     */
    private static final int COMMIT_LIMIT = Integer.MAX_VALUE;

    /**
     * Object mapper to serialize objects
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Retrieves a set of commits that match the filter criteria. The commits returned have only hash and commitDate
     * attributes filled.
     *
     * @param commitFilter Filter to be applied
     * @return List of commits that match the specified filter, with only hash and commitDate attributes filled
     * @throws ServiceException
     */
    public Set<CommitInfo> getCommitHashesByQuery(CommitFilter commitFilter) throws ServiceException {
        LoggerFactory.getLogger(CommitDAO.class).trace("getCommitsHashesByQuery -> Entry");
        MongoLabServiceParms parms = new MongoLabServiceParms();
        parms.setQuery(commitFilter);

        // Sets fields to be returned (hash and commitDate)
        CommitFilter returnFields = new CommitFilter();
        returnFields.setHash("1");
        returnFields.setCommitDate(new Date(1));
        parms.setReturnFields(returnFields);
        parms.setLimit(COMMIT_LIMIT);

        // Get commits from MongoLab
        Set<CommitInfo> result = MongoLabProvider.getCommits(parms);
        LoggerFactory.getLogger(CommitDAO.class).trace("getCommitsHashesByQuery -> Exit");

        return result;
    }

    /**
     * Retrieves a list of commits that match the filter criteria
     *
     * @param commitFilter Filter to be applied
     * @return List of commits that match the specified filter
     * @throws ServiceException
     */
    public Set<CommitInfo> getCommitsByQuery(CommitFilter commitFilter) throws ServiceException {
        LoggerFactory.getLogger(CommitDAO.class).trace("getCommitsByQuery -> Entry");
        MongoLabServiceParms parms = new MongoLabServiceParms();
        parms.setQuery(commitFilter);
        Set<CommitInfo> result = MongoLabProvider.getCommits(parms);
        LoggerFactory.getLogger(CommitDAO.class).trace("getCommitsByQuery -> Exit");

        return result;
    }

    /**
     * Inserts all commits in the list, in packages with <code>BULK_INSERT_SIZE</code>
     * elements
     *
     * @param commits List of commits to be inserted
     * @exception DyeVCException
     */
    public void insertCommits(List<CommitInfo> commits) throws DyeVCException {
        LoggerFactory.getLogger(CommitDAO.class).trace("insertCommits -> Entry");
        int    i          = 0;
        int    j          = i + BULK_INSERT_SIZE;
        int    size       = commits.size();
        String systemName = commits.get(0).getSystemName();
        while (j <= size) {
            LoggerFactory.getLogger(CommitDAO.class).info(
                "Inserting commits from {} to {} from a total of {} commits into <{}>.", i, j, size, systemName);
            MongoLabProvider.insertCommits(commits.subList(i, j));
            i = j;
            j = i + BULK_INSERT_SIZE;
        }

        if (i < size) {
            LoggerFactory.getLogger(CommitDAO.class).info(
                "Inserting commits from {} to {} from a total of {} commits into system <{}>.", i, size, size,
                systemName);
            MongoLabProvider.insertCommits(commits.subList(i, size));
        }

        LoggerFactory.getLogger(CommitDAO.class).trace("insertCommits -> Exit");
    }

    /**
     * Updates a list of commits, including the specified repositoryId in foundIn list
     *
     * @param commits List that contain the hashes to be updated
     * @param repositoryId The repository Id to be included in foundIn list
     * @exception DyeVCException
     */
    public void updateCommitsWithNewRepository(List<CommitInfo> commits, String repositoryId) throws DyeVCException {
        LoggerFactory.getLogger(CommitDAO.class).trace("updateCommitsWithNewRepository -> Entry");
        int    i          = 0;
        int    j          = i + BULK_INSERT_SIZE;
        int    size       = commits.size();
        String systemName = commits.get(0).getSystemName();

        // Create filter for the list of commits to be updated
        MongoLabServiceParms parms  = new MongoLabServiceParms();
        CommitFilter         filter = new CommitFilter();
        filter.setSystemName(systemName);

        // Sets parameter to update multiple documents
        parms.setMulti(true);

        // Sets the update command
        String updateCmd = "{\"$addToSet\" : { \"foundIn\" : \"" + repositoryId + "\" }";
        while (j <= size) {
            LoggerFactory.getLogger(CommitDAO.class).info(
                "Updating commits from {} to {} from a total of {} commits for the system <{}>.", i, j, size,
                systemName);
            updateParms(i, j, commits, filter, parms);

            // Calls Mongo Lab to update commits
            MongoLabProvider.updateCommits(parms, updateCmd);
            i = j;
            j = i + BULK_INSERT_SIZE;
        }

        if (i < size) {
            LoggerFactory.getLogger(CommitDAO.class).info(
                "Updating commits from {} to {} from a total of {} commits for the system <{}>.", i, size, size,
                systemName);
            updateParms(i, size, commits, filter, parms);

            // Calls Mongo Lab to update commits
            MongoLabProvider.updateCommits(parms, updateCmd);
        }

        LoggerFactory.getLogger(CommitDAO.class).trace("updateCommitsWithNewRepository -> Exit");
    }

    /**
     * Updates all commits, removing the specified repository Id from the foundIn list. This method is typically
     * used when a repository is removed from the topology.
     *
     * @param systemName The system name where commits will be updated
     * @param repositoryId The repository Id to be included in foundIn list
     * @exception DyeVCException
     */
    public void removeRepositoryFromCommits(String systemName, String repositoryId) throws DyeVCException {
        LoggerFactory.getLogger(CommitDAO.class).trace("removeRepositoryFromCommits -> Entry");

        // Create filter for the list of commits to be updated
        MongoLabServiceParms parms  = new MongoLabServiceParms();
        CommitFilter         filter = new CommitFilter();
        filter.setSystemName(systemName);

        // Sets parameter to update multiple documents
        parms.setMulti(true);

        // Sets the update command
        String updateCmd = "{\"$pull\" : { \"foundIn\" : \"" + repositoryId + "\" }";

        // Calls Mongo Lab to update commits
        MongoLabProvider.updateCommits(parms, updateCmd);
        LoggerFactory.getLogger(CommitDAO.class).trace("removeRepositoryFromCommits -> Exit");
    }

    /**
     * Deletes all commits for which foundIn list contains no elements
     *
     * @param systemName The system name where commits will be deleted
     * @exception DyeVCException
     */
    public void deleteOrphanedCommits(String systemName) throws DyeVCException {
        LoggerFactory.getLogger(CommitDAO.class).trace("deleteOrphanedCommits -> Entry");

        // Create filter for the list of commits to be updated
        MongoLabServiceParms parms = new MongoLabServiceParms();

        // Sets the query to delete commits (documents from the specified system name and empty foundIn list
        String deleteCmd = "{\"systemName\":\"" + systemName + "\",\"foundIn\":{\"$size\":0}}";
        parms.setQuery(deleteCmd);

        // Calls Mongo Lab to update commits
        MongoLabProvider.deleteCommits(parms);
        LoggerFactory.getLogger(CommitDAO.class).trace("removeRepositoryFromCommits -> Exit");
    }

//  /**
//   * Deletes the monitored repository from the database. The
//   * application first checks if the repository is not referenced anywhere,
//   * otherwise throws an exception. If the monitored repository does not have
//   * a system name configured, ignores it and does nothing.
//   *
//   * @param repository Monitored repository to be deleted from the database
//   * @throws RepositoryReferencedException when other repositories reference
//   * this one
//   * @throws DyeVCException
//   */
//  public void deleteRepository(MonitoredRepository repository) throws ServiceException, RepositoryReferencedException {
//      String systemName = repository.getSystemName();
//      if (!("".equals(systemName) || "no name".equals(systemName))) {
//          // Only repositories with system names have to be deleted from the database
//          ArrayList<RepositoryInfo> dependentRepositories = findDependentRepositories(repository.getId());
//          if (dependentRepositories.isEmpty()) {
//              MongoLabProvider.deleteRepository(repository.getId());
//          } else {
//              throw new RepositoryReferencedException(dependentRepositories);
//          }
//      }
//  }

    /**
     * Gets a list of commits and return their hashes as a serialized json array of strings.
     * @param i The first element in the list to be considered (inclusive)
     * @param j The last element in the list to be considered (exclusive)
     * @param commits The list to get the hashes from
     * @return The serialized hashes as a json array of strings
     */
    private String serializeHashesToJsonArray(int i, int j, List<CommitInfo> commits) throws ServiceException {
        ArrayList<String> hashesToUpdate = new ArrayList<String>();
        String            result         = "[]";
        for (int c = i; c < j; c++) {
            hashesToUpdate.add(commits.get(c).getHash());
        }

        try {
            result = mapper.writeValueAsString(hashesToUpdate);
        } catch (IOException ex) {
            throw new ServiceException("Could not serialize hashes from commits: " + this, ex);
        }

        return result;
    }

    /**
     * Update parms with a new list of hashes to be updated
     * @param begin The beginning index to get hashes from <code>commits</code> (inclusive)
     * @param end The ending index to get hashes from <code>commits</code> (exclusive)
     * @param commits The list of commits to get hashes from
     * @param filter The filter to be included in parms
     * @param parms The parms to be updated
     * @throws ServiceException
     */
    private void updateParms(int begin, int end, List<CommitInfo> commits, CommitFilter filter,
                             MongoLabServiceParms parms)
            throws ServiceException {

        // serializes hashes to json array
        String hashes = serializeHashesToJsonArray(begin, end, commits);

        // Sets the list of hashes to be updated
        filter.setHash(hashes);
        parms.setQuery(filter);
    }
}
