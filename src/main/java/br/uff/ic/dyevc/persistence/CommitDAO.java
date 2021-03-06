package br.uff.ic.dyevc.persistence;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.ServiceException;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.topology.CommitFilter;
import br.uff.ic.dyevc.model.topology.CommitReturnFieldsFilter;
import br.uff.ic.dyevc.services.MongoLabProvider;
import br.uff.ic.dyevc.utils.JsonSerializerUtils;

import org.codehaus.jackson.map.ObjectMapper;

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

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
     * Number of elements to send in bulk commit update or read by hashes. More than this will make command too long and
     * REST API will reject it;
     */
    public static final int BULK_READ_UPDATE_COMMITS_SIZE = 80;

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
        CommitReturnFieldsFilter returnFields = new CommitReturnFieldsFilter();
        returnFields.setHash("1");
        returnFields.setCommitDate("1");
        parms.setReturnFields(returnFields);
        parms.setLimit(COMMIT_LIMIT);

        // Get commits from MongoLab
        Set<CommitInfo> result = MongoLabProvider.getCommits(parms);
        LoggerFactory.getLogger(CommitDAO.class).trace("getCommitsHashesByQuery -> Exit");

        return result;
    }

    /**
     * Retrieves a list of commits that match the filter criteria, with all fields filled.
     *
     * @param commitFilter Filter to be applied
     * @return List of commits that match the specified filter
     * @throws ServiceException
     */
    public Set<CommitInfo> getCommitsByQuery(CommitFilter commitFilter) throws ServiceException {
        return getCommitsByQuery(commitFilter, null);
    }

    /**
     * Retrieves a list of commits that match the filter criteria, filling only fields specified in returnFieldsFilter
     *
     * @param commitFilter Filter to be applied
     * @param returnFieldsFilter Filter that includes fields that must be returned in the response.
     * @return List of commits that match the specified filter
     * @throws ServiceException
     */
    public Set<CommitInfo> getCommitsByQuery(CommitFilter commitFilter, CommitReturnFieldsFilter returnFieldsFilter)
            throws ServiceException {
        LoggerFactory.getLogger(CommitDAO.class).trace("getCommitsByQuery -> Entry");
        MongoLabServiceParms parms = new MongoLabServiceParms();
        parms.setQuery(commitFilter);
        parms.setLimit(COMMIT_LIMIT);

        if (returnFieldsFilter != null) {
            parms.setReturnFields(returnFieldsFilter);
        }

        Set<CommitInfo> result = MongoLabProvider.getCommits(parms);
        LoggerFactory.getLogger(CommitDAO.class).trace("getCommitsByQuery -> Exit");

        return result;
    }

    /**
     * Retrieves the list of specified commits that already exist in the database. All commit fields are returned.
     *
     * @param cis List of commits which hashes to be queried. Hashes that do not have corresponding commits in the
     * database are discarded.
     * @param systemName System name where commits will be searched.
     * @return List of commits that have the specified hashes.
     * @throws ServiceException
     */
    public Set<CommitInfo> getCommitsByHashes(List<CommitInfo> cis, String systemName) throws ServiceException {
        return getCommitsByHashes(cis, systemName, null);
    }

    /**
     * Retrieves the list of specified commits that already exist in the database, filling only the specified fields in
     * returnFieldsFilter.
     *
     * @param cis List of commits which hashes to be queried. Hashes that do not have corresponding commits in the
     * database are discarded.
     * @param systemName System name where commits will be searched.
     * @param returnFieldsFilter filter with fields to be returned. If null, return all fields
     * @return List of commits that have the specified hashes.
     * @throws ServiceException
     */
    public Set<CommitInfo> getCommitsByHashes(List<CommitInfo> cis, String systemName,
            CommitReturnFieldsFilter returnFieldsFilter)
            throws ServiceException {
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

        int             size       = cis.size();

        CommitFilter    filter     = new CommitFilter();
        String          queryStart = "{\"systemName\": \"" + systemName + "\"";
        String          queryEnd   = "}";
        Set<CommitInfo> result     = new HashSet<CommitInfo>();

        if (size < 3 * BULK_READ_UPDATE_COMMITS_SIZE) {
            int i = 0;
            int j = i + BULK_READ_UPDATE_COMMITS_SIZE;
            // size not too long, get just the specified hashes
            while (j <= size) {
                LoggerFactory.getLogger(CommitDAO.class).info(
                    "Getting hashes for commits from {} to {} from a total of {} commits for the system <{}>.", i, j,
                    size, systemName);

                StringBuilder queryMiddle =
                    new StringBuilder(", \"_id\": {$in: ").append(JsonSerializerUtils.serializeHashes(cis.subList(i,
                        j))).append("}");
                filter.setCustomQuery(queryStart + queryMiddle.toString() + queryEnd);

                result.addAll(getCommitsByQuery(filter, returnFieldsFilter));

                i = j;
                j = i + BULK_READ_UPDATE_COMMITS_SIZE;
            }

            if (i < size) {
                LoggerFactory.getLogger(CommitDAO.class).info(
                    "Getting hashes for commits from {} to {} from a total of {} commits for the system <{}>.", i,
                    size, size, systemName);
                StringBuilder queryMiddle =
                    new StringBuilder(", \"_id\": {$in: ").append(JsonSerializerUtils.serializeHashes(cis.subList(i,
                        size))).append("}");
                filter.setCustomQuery(queryStart + queryMiddle.toString() + queryEnd);

                result.addAll(getCommitsByQuery(filter, returnFieldsFilter));
            }
        } else {
            // too many hashes to get, then get all commits at once to avoid multiple roundtrip queries to database
            LoggerFactory.getLogger(CommitDAO.class).info("Getting hashes for all commits in database in system <{}>.",
                                    systemName);
            filter.setCustomQuery(queryStart + queryEnd);

            result.addAll(getCommitsByQuery(filter));
        }

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
     * <p>
     * Retrieves a list of commits that are not found in the specified {@literal repositoryIds}. The search is done
     * according to the {@literal considerAll} parameter. This is:</p>
     * <ul>
     * <li>If considerAll is true, makes an <code>AND</code> search, meaning that commits returned do not exist in ANY
     * of the specified {@literal repositoryIds}.</li>
     * <li>If considerAll is false, makes an <code>OR</code> search, meaning that commits returned do not exist in AT
     * LEAST ONE of the specified {@literal repositoryIds}.</li>
     * </ul>
     *
     * @param repositoryIds The id of the repositories to look for commits not found in. At least one repositoryId
     * should be specified.
     * @param systemName System name where commits will be searched.
     * @param considerAll If true, makes an AND search across the specified {@literal repositoryIds}, otherwise, makes
     * an OR search.
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
     * <p>
     * Retrieves a list of commits that are not found in any of the specified {@literal repositoryIds}.</p>
     * <p>Both tracked and non tracked commits are retrieved.</p>
     *
     * @param repositoryIds The id of the repositories to look for commits not found in. At least one repositoryId
     * should be specified.
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
     * Inserts in the database all commits in the list, in packages with <code>BULK_INSERT_SIZE</code> elements.
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
     * @param systemName System name where commits will be updated.
     * @param commits List that contain the hashes to be updated
     * @param repositoryId The repository Id to be included in foundIn list
     * @param inclusive If true, commits has a list of commits that SHOULD be updated.<br>
     * If false, commits has a list of commits that SHOULD NOT be updated.
     * @exception DyeVCException
     */
    public void updateCommitsWithNewRepository(String systemName, List<CommitInfo> commits, String repositoryId,
            boolean inclusive)
            throws DyeVCException {
        LoggerFactory.getLogger(CommitDAO.class).trace("updateCommitsWithNewRepository -> Entry");
        int i    = 0;
        int j    = i + BULK_READ_UPDATE_COMMITS_SIZE;
        int size = commits.size();

        // Create filter for the list of commits to be updated
        MongoLabServiceParms parms = new MongoLabServiceParms();

        // Sets parameter to update multiple documents
        parms.setMulti(true);

        // Sets the update command
        String updateCmd = "{\"$addToSet\" : { \"foundIn\" : \"" + repositoryId + "\" }}";
        while (j <= size) {
            LoggerFactory.getLogger(CommitDAO.class).info(
                "Updating commits from {} to {} from a total of {} commits for system <{}> to include repository {}.",
                i, j, size, systemName, repositoryId);
            updateParms(systemName, i, j, commits, inclusive, parms);

            // Calls Mongo Lab to update commits
            MongoLabProvider.updateCommits(parms, updateCmd);
            i = j;
            j = i + BULK_READ_UPDATE_COMMITS_SIZE;
        }

        if (i < size | size == 0) {
            if (inclusive) {
                LoggerFactory.getLogger(CommitDAO.class).info(
                    "Updating commits from {} to {} from a total of {} commits for the system <{}>.", i, size, size,
                    systemName);
            } else {
                LoggerFactory.getLogger(CommitDAO.class).info(
                    "Updating all commits except {} commits for system <{}> to include repository {}.", size,
                    systemName, repositoryId);
            }

            updateParms(systemName, i, size, commits, inclusive, parms);

            // Calls Mongo Lab to update commits
            MongoLabProvider.updateCommits(parms, updateCmd);
        }

        LoggerFactory.getLogger(CommitDAO.class).trace("updateCommitsWithNewRepository -> Exit");
    }

    /**
     * Updates all commits, removing the specified repository Id from the foundIn list. This method is typically used
     * when a repository is removed from the topology.
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
     * @throws br.uff.ic.dyevc.exception.ServiceException
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
     * Update parms with a new list of hashes to be updated
     *
     * @param begin The beginning index to get hashes from <code>commits</code> (inclusive)
     * @param end The ending index to get hashes from <code>commits</code> (exclusive)
     * @param commits The list of commits to get hashes from
     * @param inclusive If true, commits has a list of commits that WILL be included.<br>
     * If false, commits has a list of commits that WILL NOT be updated.
     * @param parms The parms to be updated
     * @throws ServiceException
     */
    private void updateParms(String systemName, int begin, int end, List<CommitInfo> commits, boolean inclusive,
                             MongoLabServiceParms parms)
            throws ServiceException {

        String hashes = "[]";
        // serializes hashes to json array
        if (!commits.isEmpty()) {
            hashes = JsonSerializerUtils.serializeHashes(commits.subList(begin, end));
        }

        // Sets the list of hashes to be updated
        StringBuilder query = new StringBuilder("{\"systemName\":\"").append(systemName);
        if (inclusive) {
            query.append("\",\"_id\":{\"$in\": ");
        } else {
            query.append("\",\"_id\":{\"$nin\": ");
        }

        query.append(hashes).append("}}");
        parms.setQuery(query.toString());
    }

    /**
     * Remove the specified list of commits from the database
     *
     * @param cis the List of commits to be deleted
     * @param systemName The system name where commits will be deleted
     * @throws br.uff.ic.dyevc.exception.ServiceException
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

    /**
     * Updates a list of commits, changing its tracked attribute to true.
     *
     * @param commits List that contain the hashes to be updated
     * @throws br.uff.ic.dyevc.exception.ServiceException
     */
    public void updateNowTrackedCommits(ArrayList<CommitInfo> commits) throws ServiceException {
        LoggerFactory.getLogger(CommitDAO.class).trace("updateNowTrackedCommits -> Entry");

        if (commits.isEmpty()) {
            LoggerFactory.getLogger(CommitDAO.class).trace("No commits to update tracked attribute.");

            return;
        }

        int    i          = 0;
        int    j          = i + BULK_READ_UPDATE_COMMITS_SIZE;
        int    size       = commits.size();
        String systemName = commits.get(0).getSystemName();

        // Create filter for the list of commits to be updated
        MongoLabServiceParms parms = new MongoLabServiceParms();

        // Sets parameter to update multiple documents
        parms.setMulti(true);

        // Sets the update command
        String updateCmd = "{\"$set\" : { \"tracked\" : true }}";
        while (j <= size) {
            LoggerFactory.getLogger(CommitDAO.class).info(
                "Updating commits from {} to {} from a total of {} commits for the system <{}>.", i, j, size,
                systemName);
            updateParms(systemName, i, j, commits, true, parms);

            // Calls Mongo Lab to update commits
            MongoLabProvider.updateCommits(parms, updateCmd);
            i = j;
            j = i + BULK_READ_UPDATE_COMMITS_SIZE;
        }

        if (i < size) {
            LoggerFactory.getLogger(CommitDAO.class).info(
                "Updating commits from {} to {} from a total of {} commits for the system <{}>.", i, size, size,
                systemName);
            updateParms(systemName, i, size, commits, true, parms);

            // Calls Mongo Lab to update commits
            MongoLabProvider.updateCommits(parms, updateCmd);
        }

        LoggerFactory.getLogger(CommitDAO.class).trace("updateNowTrackedCommits -> Exit");
    }
}
