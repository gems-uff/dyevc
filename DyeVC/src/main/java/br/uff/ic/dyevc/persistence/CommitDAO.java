package br.uff.ic.dyevc.persistence;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.ServiceException;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.topology.CommitFilter;
import br.uff.ic.dyevc.services.MongoLabProvider;
import br.uff.ic.dyevc.utils.JsonSerializerUtils;

import org.codehaus.jackson.map.ObjectMapper;

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
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
     * Number of elements to send in bulk commit update by hashes. More than this will make command too long and mongodb
     * will reject it;
     */
    private static final int BULK_UPDATE_COMMITS_SIZE = 80;

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
     * Retrieves the list of specified commits that already exist in the database.
     *
     * @param cis List of commits which hashes to be queried. Hashes that do not have corresponding commits in the
     * database are discarded.
     * @param systemName System name where commits will be searched.
     * @return List of commits that have the specified hashes.
     * @throws ServiceException
     */
    public Set<CommitInfo> getCommitsByHashes(List<CommitInfo> cis, String systemName) throws ServiceException {
        LoggerFactory.getLogger(CommitDAO.class).trace("getCommitsByHashes -> Entry");

        if (cis == null) {
            throw new ServiceException(
                "The list of commits must not be null in order to retrieve commits from the database.");
        }

        if (cis.isEmpty()) {
            return new HashSet<CommitInfo>();
        }

        if ((systemName == null) || ("".equals(systemName))) {
            throw new ServiceException("System name must be specified");
        }

        CommitFilter  filter = new CommitFilter();
        StringBuilder query  = new StringBuilder("{\"systemName\": \"").append(systemName).append("\"");
        query.append(", \"_id\": {$in: ").append(JsonSerializerUtils.serializeHashes(cis)).append("}}");
        filter.setCustomQuery(query.toString());
        Set<CommitInfo> result = getCommitsByQuery(filter);

        LoggerFactory.getLogger(CommitDAO.class).trace("getCommitsByHashes -> Exit");

        return result;
    }

    /**
     * Retrieves the number of commits that match the specified filter
     *
     * @param commitFilter Filter to be applied
     * @return The number of commits that match the specified filter
     * @throws ServiceException
     */
    public int countCommitsByQuery(CommitFilter commitFilter) throws ServiceException {
        LoggerFactory.getLogger(CommitDAO.class).trace("getCommitsByQuery -> Entry");
        MongoLabServiceParms parms = new MongoLabServiceParms();
        parms.setQuery(commitFilter);
        Integer result = MongoLabProvider.countCommits(parms);
        LoggerFactory.getLogger(CommitDAO.class).trace("getCommitsByQuery -> Exit");

        return result;
    }

    /**
     * <p>Retrieves a list of commits that are not found in the specified {@literal repositoryIds}. The search is done according to the
     * {@literal considerAll} parameter. This is:</p>
     * <ul>
     *  <li>If considerAll is true, makes an <code>AND</code> search, meaning that commits returned do not exist in ANY
     * of the specified {@literal repositoryIds}.</li>
     *  <li>If considerAll is false, makes an <code>OR</code> search, meaning that commits returned do not exist in
     * AT LEAST ONE of the specified {@literal repositoryIds}.</li>
     * </ul>
     *
     * @param repositoryIds The id of the repositories to look for commits not found in. At least one repositoryId should
     * be specified.
     * @param systemName System name where commits will be searched.
     * @param considerAll If true, makes an AND search across the specified {@literal repositoryIds}, otherwise, makes an OR search.
     * @return List of commits that are not found in the specified repositoryIds
     * @throws ServiceException
     */
    public Set<CommitInfo> getCommitsNotFoundInRepositories(Set<String> repositoryIds, String systemName,
            boolean considerAll)
            throws ServiceException {
        LoggerFactory.getLogger(CommitDAO.class).trace("getCommitsNotFoundInRepositories -> Entry");

        if ((repositoryIds == null) || (repositoryIds.isEmpty())) {
            throw new ServiceException(
                "At least one repository Id must be specified in order to look of non-existing commits");
        }

        if ((systemName == null) || ("".equals(systemName))) {
            throw new ServiceException("System name must be specified");
        }

        CommitFilter  filter = new CommitFilter();
        StringBuilder query  = new StringBuilder("{\"systemName\": \"").append(systemName).append("\"");
        if (considerAll) {
            query.append(", \"foundIn\": {$nin: ").append(
                JsonSerializerUtils.serializeWithoutNulls(repositoryIds)).append("}}");
        } else {
            query.append(", \"$or\": [");
            Iterator<String> it = repositoryIds.iterator();
            int              i  = 0;
            while (it.hasNext()) {
                i++;
                String repositoryId = it.next();
                query.append("{\"foundIn\": {\"$nin\": [\"").append(repositoryId).append("\"]}}");

                if (i < repositoryIds.size()) {
                    query.append(",");
                }
            }

            query.append("]}");
        }

        filter.setCustomQuery(query.toString());
        Set<CommitInfo> result = getCommitsByQuery(filter);
        LoggerFactory.getLogger(CommitDAO.class).trace("getCommitsNotFoundInRepositories -> Exit");

        return result;
    }

    /**
     * <p>Retrieves a list of commits that are not found in any of the specified {@literal repositoryIds}.</p>
     *
     * @param repositoryIds The id of the repositories to look for commits not found in. At least one repositoryId should
     * be specified.
     * @param systemName System name where commits will be searched.
     * @return List of commits that are not found in any of the specified repositoryIds
     * @throws ServiceException
     */
    public Set<CommitInfo> getCommitsNotFoundInRepositories(Set<String> repositoryIds, String systemName)
            throws ServiceException {
        return getCommitsNotFoundInRepositories(repositoryIds, systemName, true);
    }

    /**
     * Retrieves a list of commits that are not found in the specified repository.
     *
     * @param repositoryId The id of the repository to look for commits not found in.
     * @param systemName System name where commits will be searched.
     * @return List of commits that are not found in the specified repositoryId
     * @throws ServiceException
     */
    public Set<CommitInfo> getCommitsNotFoundInRepository(String repositoryId, String systemName)
            throws ServiceException {
        LoggerFactory.getLogger(CommitDAO.class).trace("getCommitsNotFoundInRepository -> Entry");

        if ((repositoryId == null) || ("".equals(repositoryId))) {
            throw new ServiceException("Repository id cannot be null or empty.");
        }

        Set<String> repAsSet = new HashSet<String>();
        repAsSet.add(repositoryId);
        Set<CommitInfo> result = getCommitsNotFoundInRepositories(repAsSet, systemName);

        LoggerFactory.getLogger(CommitDAO.class).trace("getCommitsNotFoundInRepository -> Exit");

        return result;
    }

    /**
     * Inserts in the database all commits in the list, in packages with <code>BULK_INSERT_SIZE</code>
     * elements.
     *
     * @param commits List of commits to be inserted
     * @exception DyeVCException
     */
    public void insertCommits(List<CommitInfo> commits) throws DyeVCException {
        LoggerFactory.getLogger(CommitDAO.class).trace("upsertCommits -> Entry");
        int    i          = 0;
        int    j          = i + BULK_INSERT_SIZE;
        int    size       = commits.size();
        String systemName = commits.get(0).getSystemName();

        while (j <= size) {
            LoggerFactory.getLogger(CommitDAO.class).info(
                "Upserting commits from {} to {} from a total of {} commits into <{}>.", i, j, size, systemName);
            MongoLabProvider.insertCommits(commits.subList(i, j));
            i = j;
            j = i + BULK_INSERT_SIZE;
        }

        if (i < size) {
            LoggerFactory.getLogger(CommitDAO.class).info(
                "Upserting commits from {} to {} from a total of {} commits into system <{}>.", i, size, size,
                systemName);
            MongoLabProvider.insertCommits(commits.subList(i, size));
        }

        LoggerFactory.getLogger(CommitDAO.class).trace("upsertCommits -> Exit");
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
        int    j          = i + BULK_UPDATE_COMMITS_SIZE;
        int    size       = commits.size();
        String systemName = commits.get(0).getSystemName();

        // Create filter for the list of commits to be updated
        MongoLabServiceParms parms  = new MongoLabServiceParms();
        CommitFilter         filter = new CommitFilter();
        filter.setSystemName(systemName);

        // Sets parameter to update multiple documents
        parms.setMulti(true);

        // Sets the update command
        String updateCmd = "{\"$addToSet\" : { \"foundIn\" : \"" + repositoryId + "\" }}";
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
     * @throws br.uff.ic.dyevc.exception.ServiceException
     */
    public void removeRepositoryFromAllCommits(String systemName, String repositoryId) throws ServiceException {
        LoggerFactory.getLogger(CommitDAO.class).trace("removeRepositoryFromAllCommits -> Entry");

        // Create filter for the list of commits to be updated
        MongoLabServiceParms parms  = new MongoLabServiceParms();
        CommitFilter         filter = new CommitFilter();
        filter.setSystemName(systemName);

        // Sets parameter to update multiple documents
        parms.setMulti(true);

        // Sets the update command
        String updateCmd = "{\"$pull\" : { \"foundIn\" : \"" + repositoryId + "\" }}";

        // Calls Mongo Lab to update commits
        MongoLabProvider.updateCommits(parms, updateCmd);
        LoggerFactory.getLogger(CommitDAO.class).trace("removeRepositoryFromAllCommits -> Exit");
    }

    /**
     * Deletes all commits for which foundIn list contains no elements
     *
     * @param systemName The system name where commits will be deleted
     * @exception DyeVCException
     */
    public void deleteOrphanedCommits(String systemName) throws ServiceException {
        LoggerFactory.getLogger(CommitDAO.class).trace("deleteOrphanedCommits -> Entry");

        // Create filter for the list of commits to be updated
        MongoLabServiceParms parms = new MongoLabServiceParms();

        // Sets the query to delete commits (documents from the specified system name and empty foundIn list
        String deleteCmd = "{\"systemName\":\"" + systemName + "\",\"foundIn\":{\"$size\":0}}";
        parms.setQuery(deleteCmd);

        // Calls Mongo Lab to update commits
        MongoLabProvider.deleteCommits(parms);
        LoggerFactory.getLogger(CommitDAO.class).trace("deleteOrphanedCommits -> Exit");
    }

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
        StringBuilder query = new StringBuilder("{\"systemName\":\"dyevc\",\"_id\":{\"$in\": ");
        query.append(hashes).append("}}");
        parms.setQuery(query.toString());
    }

    /**
     * Remove the specified list of commits from the database
     * @param cis the List of commits to be deleted
     * @param systemName The system name where commits will be deleted
     */
    public void deleteCommits(ArrayList<CommitInfo> cis, String systemName) throws ServiceException {
        LoggerFactory.getLogger(CommitDAO.class).trace("deleteCommits -> Entry");

        // Create filter for the list of commits to be updated
        MongoLabServiceParms parms = new MongoLabServiceParms();

        // Sets the query to delete commits (documents from the specified system name and empty foundIn list
        StringBuilder query = new StringBuilder("{\"systemName\": \"").append(systemName).append("\"");
        query.append(", \"_id\": {$in: ").append(JsonSerializerUtils.serializeHashes(cis)).append("}}");
        parms.setQuery(query.toString());

        // Calls Mongo Lab to update commits
        MongoLabProvider.deleteCommits(parms);
        LoggerFactory.getLogger(CommitDAO.class).trace("deleteCommits -> Exit");
    }
}
