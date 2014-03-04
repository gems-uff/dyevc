package br.uff.ic.dyevc.monitor;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.MonitorException;
import br.uff.ic.dyevc.exception.RepositoryReferencedException;
import br.uff.ic.dyevc.exception.ServiceException;
import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.gui.core.MessageManager;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.topology.CommitFilter;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.model.topology.Topology;
import br.uff.ic.dyevc.persistence.CommitDAO;
import br.uff.ic.dyevc.persistence.TopologyDAO;
import br.uff.ic.dyevc.tools.vcs.git.GitCommitTools;
import br.uff.ic.dyevc.utils.ApplicationVersionUtils;
import br.uff.ic.dyevc.utils.RepositoryConverter;
import br.uff.ic.dyevc.utils.SystemUtils;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;

import org.eclipse.jgit.transport.URIish;

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * This class updates the topology.
 *
 * @author Cristiano
 */
public class TopologyUpdater {
    private final TopologyDAO           topologyDAO;
    private final CommitDAO             commitDAO;
    private final MonitoredRepositories monitoredRepositories;
    private RepositoryConverter         converter;
    private MonitoredRepository         repositoryToUpdate;
    private GitCommitTools              tools;
    private boolean                     discardCache;

    /**
     * Creates a new object of this type.
     */
    public TopologyUpdater() {
        LoggerFactory.getLogger(TopologyUpdater.class).trace("Constructor -> Entry.");
        topologyDAO           = new TopologyDAO();
        commitDAO             = new CommitDAO();
        monitoredRepositories = MonitoredRepositories.getInstance();
        LoggerFactory.getLogger(TopologyUpdater.class).trace("Constructor -> Exit.");
    }

    /**
     * Updates the topology.
     *
     * @param repositoryToUpdate the repository to be updated
     * @param discardCache If true, discards existing snapshot, if any, and works as if it is the first time the
     * @param versionChanged  If true, indicates that application version has changed.
     * @param previousVersion The version of the application that was running before this one. Used to make specific
     * version arrangements if versionChanged is true.
     * repository is monitored.
     */
    public void update(MonitoredRepository repositoryToUpdate, boolean discardCache, boolean versionChanged,
                       String previousVersion) {
        LoggerFactory.getLogger(TopologyUpdater.class).trace("Topology updater is running.");

        if (!repositoryToUpdate.hasSystemName()) {
            MessageManager.getInstance().addMessage("Repository <" + repositoryToUpdate.getName() + "> with id <"
                    + repositoryToUpdate.getId()
                    + "> has no system name configured and will not be added to the topology.");

            return;
        }

        this.repositoryToUpdate = repositoryToUpdate;
        this.converter          = new RepositoryConverter(repositoryToUpdate);
        this.discardCache       = discardCache;

        if (true) {
            doVersionSpecificProcessing(previousVersion);
        }

        StringBuilder message = new StringBuilder("Updating topology ");
        if (discardCache) {
            message.append("and discarding cache ");
        }

        message.append("for repository <").append(repositoryToUpdate.getId()).append("> with id <").append(
            repositoryToUpdate.getName()).append(">. Check console for details.");

        MessageManager.getInstance().addMessage(message.toString());

        updateRepositoryTopology();

        updateCommitTopology();

        MessageManager.getInstance().addMessage("Finished update topology for repository <"
                + repositoryToUpdate.getId() + "> with id <" + repositoryToUpdate.getName() + ">");
        LoggerFactory.getLogger(TopologyUpdater.class).trace("Topology updater finished running.");
    }

    /**
     * Updates the topology for the specified repository in the database, including any new referenced repositories that
     * do not yet exist and refreshes local topology cache.
     *
     * @see RepositoryConverter
     */
    private void updateRepositoryTopology() {
        LoggerFactory.getLogger(TopologyUpdater.class).trace("updateRepositoryTopology -> Entry.");
        String systemName = repositoryToUpdate.getSystemName();

        // TODO implement lock mechanism
        try {
            LoggerFactory.getLogger(TopologyUpdater.class).info("{}:{}({}) -> Started updating repository.",
                                    systemName, repositoryToUpdate.getName(), repositoryToUpdate.getId());
            Topology       topology             = topologyDAO.readTopologyForSystem(systemName);

            RepositoryInfo localRepositoryInfo  = converter.toRepositoryInfo();
            RepositoryInfo remoteRepositoryInfo = topology.getRepositoryInfo(systemName, localRepositoryInfo.getId());

            /*
             * changedLocally is used to flag any changes to repository info. If any changes were detected by the end of the
             * method, then the repository is updated in database. It is initialized with true if the repository info
             * does not exist in the database.
             */
            boolean changedLocally = remoteRepositoryInfo == null;
            if (!changedLocally) {
                // localRepositoryInfo already exists in the topology, then gets the list of hosts that monitor it
                LoggerFactory.getLogger(TopologyUpdater.class).info(
                    "{}:{}({}) -> Repository already exists in topology.", systemName, repositoryToUpdate.getName(),
                    repositoryToUpdate.getId());
                localRepositoryInfo.setMonitoredBy(remoteRepositoryInfo.getMonitoredBy());
            } else {
                LoggerFactory.getLogger(TopologyUpdater.class).info(
                    "{}:{}({}) -> Repository does not exist in topology.", systemName, repositoryToUpdate.getName(),
                    repositoryToUpdate.getId());
                localRepositoryInfo.addMonitoredBy(SystemUtils.getLocalHostname());
            }

            if (!changedLocally) {
                // check whether this computer must be added or removed from the monitoredby list.
                Set<String> monitoredBy = localRepositoryInfo.getMonitoredBy();
                String      hostname    = SystemUtils.getLocalHostname();

                if (repositoryToUpdate.isMarkedForDeletion()) {
                    changedLocally |= monitoredBy.remove(hostname);

                    if (changedLocally) {
                        LoggerFactory.getLogger(TopologyUpdater.class).info(
                            "{}:{}({}) -> Repository is marked for deletion and will be removed from the monitoredBy list.",
                            systemName, repositoryToUpdate.getName(), repositoryToUpdate.getId());
                    }
                } else {
                    changedLocally |= monitoredBy.add(hostname);

                    if (changedLocally) {
                        LoggerFactory.getLogger(TopologyUpdater.class).info(
                            "{}:{}({}) -> Repository is marked for deletion and will be removed from the monitoredBy list.",
                            systemName, repositoryToUpdate.getName(), repositoryToUpdate.getId());
                    }
                }
            }

            if (!changedLocally) {
                // Checks if the pullsFrom list must be updated
                Collection<String> insertedPullsFrom = CollectionUtils.subtract(localRepositoryInfo.getPullsFrom(),
                                                           remoteRepositoryInfo.getPullsFrom());
                Collection<String> removedPullsFrom = CollectionUtils.subtract(remoteRepositoryInfo.getPullsFrom(),
                                                          localRepositoryInfo.getPullsFrom());
                changedLocally |= !insertedPullsFrom.isEmpty();

                if (changedLocally) {
                    LoggerFactory.getLogger(TopologyUpdater.class).info(
                        "{}:{}({}) -> pullsFrom list has new members and will be updated.", systemName,
                        repositoryToUpdate.getName(), repositoryToUpdate.getId());
                }

                changedLocally |= !removedPullsFrom.isEmpty();

                if (changedLocally) {
                    LoggerFactory.getLogger(TopologyUpdater.class).info(
                        "{}:{}({}) -> pullsFrom list has deleted members and will be updated.", systemName,
                        repositoryToUpdate.getName(), repositoryToUpdate.getId());
                }
            }

            if (!changedLocally) {
                // Checks if the pushesTo list must be updated
                Collection<String> insertedPushesTo = CollectionUtils.subtract(localRepositoryInfo.getPushesTo(),
                                                          remoteRepositoryInfo.getPushesTo());
                Collection<String> removedPushesTo = CollectionUtils.subtract(remoteRepositoryInfo.getPushesTo(),
                                                         localRepositoryInfo.getPushesTo());
                changedLocally |= !insertedPushesTo.isEmpty();

                if (changedLocally) {
                    LoggerFactory.getLogger(TopologyUpdater.class).info(
                        "{}:{}({}) -> puhesTo list has new members and will be updated.", systemName,
                        repositoryToUpdate.getName(), repositoryToUpdate.getId());
                }

                changedLocally |= !removedPushesTo.isEmpty();

                if (changedLocally) {
                    LoggerFactory.getLogger(TopologyUpdater.class).info(
                        "{}:{}({}) -> puhesTo list has deleted members and will be updated.", systemName,
                        repositoryToUpdate.getName(), repositoryToUpdate.getId());
                }
            }

            if (changedLocally) {
                Date lastChanged = topologyDAO.upsertRepository(converter.toRepositoryInfo());
                topologyDAO.upsertRepositories(converter.getRelatedNewList());
                repositoryToUpdate.setLastChanged(lastChanged);
            }

            LoggerFactory.getLogger(TopologyUpdater.class).info("{}:{}({}) -> Finished updating repository.",
                                    systemName, repositoryToUpdate.getName(), repositoryToUpdate.getId());
        } catch (DyeVCException dex) {
            MessageManager.getInstance().addMessage("Error updating repository<" + repositoryToUpdate.getName()
                    + "> with id<" + repositoryToUpdate.getId() + "> " + dex.getMessage());
        } catch (RuntimeException re) {
            MessageManager.getInstance().addMessage("Error updating repository<" + repositoryToUpdate.getName()
                    + "> with id<" + repositoryToUpdate.getId() + "> " + re.getMessage());
            LoggerFactory.getLogger(TopologyUpdater.class).error("Error during topology update.", re);
        }

        LoggerFactory.getLogger(TopologyUpdater.class).trace("updateRepositoryTopology -> Exit.");
    }

    /**
     * Updates the topology for the specified repository sending any new commits found both to the repository and to
     * referenced repositories.
     */
    private void updateCommitTopology() {
        LoggerFactory.getLogger(TopologyUpdater.class).trace("updateCommitTopology -> Entry.");

        try {
            // TODO implement lock mechanism
            LoggerFactory.getLogger(TopologyUpdater.class).info(
                "{}:{}({}) -> Started updating commits. Will now retrieve previous snapshot.",
                repositoryToUpdate.getSystemName(), repositoryToUpdate.getName(), repositoryToUpdate.getId());
            // Retrieves previous snapshot from disk
            ArrayList<CommitInfo> previousSnapshot = retrieveSnapshot();
            LoggerFactory.getLogger(TopologyUpdater.class).info(
                "{}:{}({}) -> previousSnapshots: {}. Will now retrieve current snapshot.",
                repositoryToUpdate.getSystemName(), repositoryToUpdate.getName(), repositoryToUpdate.getId(),
                (previousSnapshot == null) ? "not found" : previousSnapshot.size() + " commit(s)");

            // Retrieves current snapshot from repository
            tools = GitCommitTools.getInstance(repositoryToUpdate, true);
            ArrayList<CommitInfo> currentSnapshot = (ArrayList)tools.getCommitInfos();
            LoggerFactory.getLogger(TopologyUpdater.class).info(
                "{}:{}({}) -> currentSnapshot: {} commits. Will now identify new commits.",
                repositoryToUpdate.getSystemName(), repositoryToUpdate.getName(), repositoryToUpdate.getId(),
                currentSnapshot.size());

            // If user asked to discard cache, remove this repository from the foundIn list in all commits.
            if (discardCache) {
                LoggerFactory.getLogger(TopologyUpdater.class).info(
                    "{}:{}({}) -> The user asked to discard cache. This repository will be removed from all commits "
                    + "in the topology before going on.", repositoryToUpdate.getSystemName(),
                        repositoryToUpdate.getName(), repositoryToUpdate.getId());
                commitDAO.removeRepositoryFromAllCommits(repositoryToUpdate.getSystemName(),
                        repositoryToUpdate.getId());
                LoggerFactory.getLogger(TopologyUpdater.class).info(
                    "{}:{}({}) -> Removed this repository from all commits in the topology. Will now proceed.",
                    repositoryToUpdate.getSystemName(), repositoryToUpdate.getName(), repositoryToUpdate.getId());
            }

            // Identifies new local commit(s) since previous snapshot
            ArrayList<CommitInfo> newCommits;
            if (previousSnapshot == null) {
                newCommits = new ArrayList<CommitInfo>(currentSnapshot);
            } else {
                newCommits = (ArrayList)CollectionUtils.subtract(currentSnapshot, previousSnapshot);
            }

            LoggerFactory.getLogger(TopologyUpdater.class).info(
                "{}:{}({}) -> newCommits: {} commits. Will now identify commits not found in known repositories.",
                repositoryToUpdate.getSystemName(), repositoryToUpdate.getName(), repositoryToUpdate.getId(),
                newCommits.size());

            // Identifies commits that are potentially not synchronized in the database, before inserting new commits
            boolean         dbIsEmpty = checkIfDbIsEmpty();
            Set<CommitInfo> commitsNotFoundInSomeReps;
            if (dbIsEmpty) {
                commitsNotFoundInSomeReps = new HashSet<CommitInfo>();
            } else {
                commitsNotFoundInSomeReps = getCommitsNotFoundInSomeReps();
            }

            LoggerFactory.getLogger(TopologyUpdater.class).info(
                "{}:{}({}) -> commitsNotFoundInSomeReps: {} commits. Will now identify which of newCommits should be inserted.",
                repositoryToUpdate.getSystemName(), repositoryToUpdate.getName(), repositoryToUpdate.getId(),
                commitsNotFoundInSomeReps.size());

            // Insert commits into the database
            ArrayList<CommitInfo> commitsToInsert = getCommitsToInsert(newCommits, dbIsEmpty);
            LoggerFactory.getLogger(TopologyUpdater.class).info(
                "{}:{}({}) -> commitsToInsert: {} commits. Will now insert them into the database.",
                repositoryToUpdate.getSystemName(), repositoryToUpdate.getName(), repositoryToUpdate.getId(),
                commitsToInsert.size());

            if (!commitsToInsert.isEmpty()) {
                insertCommits(commitsToInsert);
            }

            // Identifies commits that were deleted since previous snapshot
            ArrayList<CommitInfo> commitsToDelete;
            if (previousSnapshot == null) {
                commitsToDelete = new ArrayList<CommitInfo>();
            } else {
                commitsToDelete = (ArrayList<CommitInfo>)CollectionUtils.subtract(previousSnapshot, currentSnapshot);
            }

            LoggerFactory.getLogger(TopologyUpdater.class).info(
                "{}:{}({}) -> commitsToDelete: {} commits. Will now delete them from the database.",
                repositoryToUpdate.getSystemName(), repositoryToUpdate.getName(), repositoryToUpdate.getId(),
                commitsToDelete.size());

            // delete commits from database
            if (!commitsToDelete.isEmpty()) {
                deleteCommits(commitsToDelete);
            }

            // save current snapshot to disk
            saveSnapshot(currentSnapshot);

            // update commits that had their tracked attribute changed from false to true
            ArrayList<CommitInfo> nowTrackedCommits;
            if (previousSnapshot != null) {
                nowTrackedCommits = getNowTrackedCommits(previousSnapshot);
            } else {
                nowTrackedCommits = new ArrayList<CommitInfo>();
            }

            LoggerFactory.getLogger(TopologyUpdater.class).info(
                "{}:{}({}) -> nowTrackedCommits: {} commits. Will now update them into the database.",
                repositoryToUpdate.getSystemName(), repositoryToUpdate.getName(), repositoryToUpdate.getId(),
                nowTrackedCommits.size());

            if (!nowTrackedCommits.isEmpty()) {
                updateNowTrackedCommits(nowTrackedCommits);
            }

            // update commits that were not deleted and exist locally
            ArrayList<CommitInfo> commitsNotFoundInSomeRepsWithoutDeletions =
                (ArrayList)CollectionUtils.subtract(commitsNotFoundInSomeReps, commitsToDelete);
            LoggerFactory.getLogger(TopologyUpdater.class).info(
                "{}:{}({}) -> commitsNotFoundInSomeRepsWithoutDeletions: {} commits. Will now remove deleted commits.",
                repositoryToUpdate.getSystemName(), repositoryToUpdate.getName(), repositoryToUpdate.getId(),
                commitsNotFoundInSomeRepsWithoutDeletions.size());

            ArrayList<CommitInfo> commitsToUpdate =
                (ArrayList)CollectionUtils.intersection(commitsNotFoundInSomeRepsWithoutDeletions, currentSnapshot);
            LoggerFactory.getLogger(TopologyUpdater.class).info(
                "{}:{}({}) -> commitsToUpdate: {} commits. Will now check which of them are new in related repositories.",
                repositoryToUpdate.getSystemName(), repositoryToUpdate.getName(), repositoryToUpdate.getId(),
                commitsToUpdate.size());

            if (!commitsToUpdate.isEmpty()) {
                updateCommits(commitsToUpdate, commitsNotFoundInSomeRepsWithoutDeletions);
            }

            // removes orphaned commits (those that remained with an empty foundIn list.
            LoggerFactory.getLogger(TopologyUpdater.class).info("{}:{}({}) -> Checking orphaned commits.",
                                    repositoryToUpdate.getSystemName(), repositoryToUpdate.getName(),
                                    repositoryToUpdate.getId());
            commitDAO.deleteOrphanedCommits(repositoryToUpdate.getSystemName());
            LoggerFactory.getLogger(TopologyUpdater.class).info("{}:{}({}) -> Orphaned commits checked.",
                                    repositoryToUpdate.getSystemName(), repositoryToUpdate.getName(),
                                    repositoryToUpdate.getId());
            LoggerFactory.getLogger(TopologyUpdater.class).info("{}:{}({} -> Finished updating commits.)",
                                    repositoryToUpdate.getSystemName(), repositoryToUpdate.getName(),
                                    repositoryToUpdate.getId());
        } catch (DyeVCException dex) {
            MessageManager.getInstance().addMessage("Error updating commits from repository <"
                    + repositoryToUpdate.getName() + "> with id<" + repositoryToUpdate.getId() + "> "
                    + dex.getMessage());
        } catch (RuntimeException re) {
            MessageManager.getInstance().addMessage("Error updating commits from repository <"
                    + repositoryToUpdate.getName() + "> with id<" + repositoryToUpdate.getId() + "> "
                    + re.getMessage());
            LoggerFactory.getLogger(TopologyUpdater.class).error("Error during topology update.", re);
        }

        LoggerFactory.getLogger(TopologyUpdater.class).trace("updateCommitTopology -> Exit.");
    }

    /**
     * Verifies if the local monitored repositories marked for deletion are referenced in the topology. If not, delete
     * them.
     *
     * @throws DyeVCException
     */
    void verifyDeletedRepositories() throws DyeVCException {
        LoggerFactory.getLogger(TopologyUpdater.class).trace("verifyDeletedRepositories -> Entry.");

        for (Iterator<MonitoredRepository> it =
                MonitoredRepositories.getMarkedForDeletion().iterator(); it.hasNext(); ) {
            MonitoredRepository monitoredRepository = it.next();
            if (!monitoredRepository.hasSystemName()) {
                MessageManager.getInstance().addMessage("Repository <" + monitoredRepository.getName() + "> with id <"
                        + monitoredRepository.getId()
                        + "> has no system name configured and will not be added to the topology.");

                continue;
            }

            try {
                monitoredRepositories.removeMarkedForDeletion(monitoredRepository);
            } catch (RepositoryReferencedException rre) {
                StringBuilder message = new StringBuilder();
                message.append("Repository <").append(monitoredRepository.getName()).append("> with id <").append(
                    monitoredRepository.getId()).append(
                    "> could not be deleted because it is still referenced by the following clone(s): ");

                for (RepositoryInfo info : rre.getRelatedRepositories()) {
                    message.append("\n<").append(info.getCloneName()).append(">, id: <").append(info.getId()).append(
                        ">, located at host <").append(info.getHostName()).append(">");
                }

                LoggerFactory.getLogger(TopologyUpdater.class).warn(message.toString());
            } catch (RuntimeException re) {
                StringBuilder message = new StringBuilder();
                message.append("Repository <").append(monitoredRepository.getName()).append("> with id <").append(
                    monitoredRepository.getId()).append("> could not be deleted due to the following error: ").append(
                    re.getMessage());
                LoggerFactory.getLogger(TopologyUpdater.class).warn(message.toString(), re);
            }
        }

        LoggerFactory.getLogger(TopologyUpdater.class).trace("verifyDeletedRepositories -> Exit.");
    }

    /**
     * Retrieves the previous snapshot of commit infos from disk. If there was no previous snapshot saved, then returns
     * null.
     *
     * @return the saved snapshot of commit infos (null if no previous snapshot found).
     * @throws DyeVCException
     */
    private ArrayList<CommitInfo> retrieveSnapshot() throws DyeVCException {
        LoggerFactory.getLogger(TopologyUpdater.class).trace("retrieveSnapshot -> Entry.");

        if (discardCache) {
            LoggerFactory.getLogger(TopologyUpdater.class).info("{}:{}({}) -> Snapshot was requested to be discarded.",
                                    repositoryToUpdate.getSystemName(), repositoryToUpdate.getName(),
                                    repositoryToUpdate.getId());

            return null;
        }

        ObjectInput           input = null;
        String                snapshotPath;
        ArrayList<CommitInfo> recoveredCommits = null;
        try {
            snapshotPath = repositoryToUpdate.getWorkingCloneConnection().getPath() + IConstants.DIR_SEPARATOR
                           + IConstants.SNAPSHOT_FILE_NAME;
            input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(snapshotPath)));

            // deserialize the List
            recoveredCommits = (ArrayList<CommitInfo>)input.readObject();
        } catch (FileNotFoundException ex) {
            // do nothing. There is no previous snapshot, so will return null.
        } catch (ClassNotFoundException ex) {
            throw new MonitorException(ex);
        } catch (IOException ex) {
            throw new MonitorException(ex);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                LoggerFactory.getLogger(TopologyUpdater.class).warn("Error closing snapshot stream.", ex);
            }
        }

        LoggerFactory.getLogger(TopologyUpdater.class).trace("retrieveSnapshot -> Exit.");

        return recoveredCommits;
    }

    /**
     * Saves a snapshot with the specified list of commit infos to disk
     *
     * @param commitInfos the list of commit infos to be saved
     * @throws DyeVCException
     */
    private void saveSnapshot(ArrayList<CommitInfo> commitInfos) throws DyeVCException {
        LoggerFactory.getLogger(TopologyUpdater.class).trace("saveSnapshot -> Entry.");
        ObjectOutput output = null;
        String       snapshotPath;
        try {
            snapshotPath = repositoryToUpdate.getWorkingCloneConnection().getPath() + IConstants.DIR_SEPARATOR
                           + IConstants.SNAPSHOT_FILE_NAME;
            output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(snapshotPath)));
            output.writeObject(commitInfos);
            LoggerFactory.getLogger(TopologyUpdater.class).info("{}:{}({}) -> Current snapshot saved.",
                                    repositoryToUpdate.getSystemName(), repositoryToUpdate.getName(),
                                    repositoryToUpdate.getId());
        } catch (IOException ex) {
            throw new MonitorException(ex);
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException ex) {
                LoggerFactory.getLogger(TopologyUpdater.class).warn("Error closing snapshot stream.", ex);
            }
        }

        LoggerFactory.getLogger(TopologyUpdater.class).trace("saveSnapshot -> Exit.");
    }

    /**
     * Retrieves from the database the number of existing commits for the system that the repository being updated
     * belongs to and returns true if this number is not 0.
     *
     * @return true if there is any commit in the database
     * @throws ServiceException
     */
    private boolean checkIfDbIsEmpty() throws ServiceException {
        LoggerFactory.getLogger(TopologyUpdater.class).trace("countCommitsInDatabase -> Entry.");

        CommitFilter filter = new CommitFilter();
        filter.setSystemName(repositoryToUpdate.getSystemName());
        int commitCount = commitDAO.countCommitsByQuery(filter);

        LoggerFactory.getLogger(TopologyUpdater.class).trace("countCommitsInDatabase -> Exit.");

        return commitCount == 0;
    }

    /**
     * Retrieves from the database the list of commits that were not found in repositories related to the repository
     * being updated. If at least one related repository was not listed in the commits foundIn list, then the commit is
     * retrieved
     *
     * @return The list of commits that were not found in some of the related repositories
     */
    private Set<CommitInfo> getCommitsNotFoundInSomeReps() throws DyeVCException {
        LoggerFactory.getLogger(TopologyUpdater.class).trace("getCommitsNotFoundInSomeReps -> Entry.");
        Set<String>    repositoryIds = new HashSet<String>();
        RepositoryInfo info          = converter.toRepositoryInfo();

        repositoryIds.add(repositoryToUpdate.getId());
        repositoryIds.addAll(info.getPullsFrom());
        repositoryIds.addAll(info.getPushesTo());

        // Retrieves all tracked commits not found in any repository from repositoryIds
        Set<CommitInfo> commitsNotFound = commitDAO.getCommitsNotFoundInRepositories(repositoryIds,
                                              info.getSystemName(), false);

        LoggerFactory.getLogger(TopologyUpdater.class).trace("getCommitsNotFoundInSomeReps -> Exit.");

        return commitsNotFound;
    }

    /**
     * Verifies which of the snapshot's new commits already exist in the database, and return only those that do not
     * exist, this is, those that must be inserted into the database
     *
     * @param newCommits New commits found in the current repository snapshot
     * @param dbIsEmpty Indicates if database is empty for the system this repository belongs to.
     * @return the list of commits that do not exist in the database yet
     * @throws DyeVCException
     */
    private ArrayList<CommitInfo> getCommitsToInsert(ArrayList<CommitInfo> newCommits, boolean dbIsEmpty)
            throws DyeVCException {
        LoggerFactory.getLogger(TopologyUpdater.class).trace("getCommitsToInsert -> Entry.");
        ArrayList<CommitInfo> commitsToInsert = new ArrayList(newCommits);

        if (newCommits != null) {
            if (!dbIsEmpty) {
                // Create filter to return only hash and commitDate
                CommitFilter returnFieldsFilter = new CommitFilter();
                returnFieldsFilter.setHash("1");
                returnFieldsFilter.setCommitDate(new Date(1));
                // Check db only if it is not empty, otherwise all commits in newCommits have to be inserted.
                Set<CommitInfo> newCommitsInDatabase = commitDAO.getCommitsByHashes(newCommits,
                                                           converter.toRepositoryInfo().getSystemName(),
                                                           returnFieldsFilter);
                commitsToInsert = (ArrayList<CommitInfo>)CollectionUtils.subtract(newCommits, newCommitsInDatabase);
            }
        } else {
            // if newCommits is null, return an empty list (no commits to insert)
            commitsToInsert = new ArrayList<CommitInfo>();
        }

        LoggerFactory.getLogger(TopologyUpdater.class).trace("getCommitsToInsert -> Exit.");

        return commitsToInsert;
    }

    /**
     * Converts a set of URIishes into a set of repository ids.
     *
     * @param aheadSet the set of uriishes to be converted.
     * @return the set of uriishes converted into a set of repository ids.
     * @throws DyeVCException
     */
    private Set<String> convertURIishesToRepIds(final Set<URIish> aheadSet) throws DyeVCException {
        LoggerFactory.getLogger(TopologyUpdater.class).trace("convertURIishesToRepIds -> Entry.");
        Set<String> aheadRepIds = new HashSet<String>(aheadSet.size());
        for (URIish uriish : aheadSet) {
            String repId = converter.mapUriToRepositoryId(uriish);
            if (repId != null) {
                aheadRepIds.add(repId);
            }
        }

        LoggerFactory.getLogger(TopologyUpdater.class).trace("convertURIishesToRepIds -> Exit.");

        return aheadRepIds;
    }

    /**
     * Insert new commits into the database. Prior to the insertion, update each commit with the list of repositories
     * where they are known to be found.
     *
     * @param commitsToInsert the list of commits to be inserted
     */
    private void insertCommits(ArrayList<CommitInfo> commitsToInsert) throws DyeVCException {
        LoggerFactory.getLogger(TopologyUpdater.class).trace("insertCommits -> Entry.");

        commitsToInsert = updateWhereExists(commitsToInsert);
        commitDAO.insertCommits(commitsToInsert);

        LoggerFactory.getLogger(TopologyUpdater.class).trace("insertCommits -> Exit.");
    }

    /**
     * Updates commits into the database that had their tracked attribute changed from false to true.
     *
     * @param nowTrackedCommits Commits to be updated.
     * @throws DyeVCException
     */
    private void updateNowTrackedCommits(ArrayList<CommitInfo> nowTrackedCommits) throws DyeVCException {
        LoggerFactory.getLogger(TopologyUpdater.class).trace("updateCommitsNowTracked -> Entry.");
        commitDAO.updateNowTrackedCommits(nowTrackedCommits);
        LoggerFactory.getLogger(TopologyUpdater.class).trace("updateCommitsNowTracked -> Exit.");
    }

    /**
     * Updates commits into the database. Prior to the update, update each commit with the list of repositories where
     * they are known to be found. If the list of repositories has not changed since last run, then do not update the
     * commit. updateableCommits commitsToUpdate the list of commits to be updated
     *
     * @param commitsToUpdate Commits to be updated.
     * @param commitsNotFoundInSomeRepsWithoutDeletions All commits that are not found in some reps.
     * @throws DyeVCException
     */
    private void updateCommits(ArrayList<CommitInfo> commitsToUpdate,
                               ArrayList<CommitInfo> commitsNotFoundInSomeRepsWithoutDeletions)
            throws DyeVCException {
        LoggerFactory.getLogger(TopologyUpdater.class).trace("updateCommits -> Entry.");

        commitsToUpdate = updateWhereExists(commitsToUpdate);

        // Populates a map with groups of commits that should be updated with new repositories where they can now be found
        Map<String, List<CommitInfo>> commitsToUpdateByRepository = new TreeMap<String, List<CommitInfo>>();
        for (CommitInfo ci : commitsToUpdate) {
            Collection<String> newRepIds = CollectionUtils.subtract(ci.getFoundIn(), ci.getPreviousFoundIn());
            if (!newRepIds.isEmpty()) {
                // if collection of found repositories for the commits has changed, then include the commit to be updated for
                // new repository id can now be found.
                for (String repId : newRepIds) {
                    List<CommitInfo> cisToUpdate = commitsToUpdateByRepository.get(repId);
                    if (cisToUpdate == null) {
                        cisToUpdate = new ArrayList<CommitInfo>();
                        commitsToUpdateByRepository.put(repId, cisToUpdate);
                    }

                    cisToUpdate.add(ci);
                }
            }
        }

        if (commitsToUpdateByRepository.isEmpty()) {
            LoggerFactory.getLogger(TopologyUpdater.class).info(
                "{}:{}({}) -> No changes detected in where commits exist. There will not be updates.",
                repositoryToUpdate.getSystemName(), repositoryToUpdate.getName(), repositoryToUpdate.getId());
        }

        for (String repId : commitsToUpdateByRepository.keySet()) {
            List<CommitInfo>      cis             = commitsToUpdateByRepository.get(repId);
            ArrayList<CommitInfo> notToUpdateList =
                (ArrayList)CollectionUtils.subtract(commitsNotFoundInSomeRepsWithoutDeletions, cis);
            LoggerFactory.getLogger(TopologyUpdater.class).info(
                "{}:{}({}) -> {} commits must be updated to include repository {} and {} should not.",
                repositoryToUpdate.getSystemName(), repositoryToUpdate.getName(), repositoryToUpdate.getId(),
                cis.size(), repId, notToUpdateList.size());

            if (notToUpdateList.size() < CommitDAO.BULK_READ_UPDATE_COMMITS_SIZE) {
                // The size of notToUpdateList is smaller than amount of hashes we can send in one call.
                // Thus, update all commits but those in notToUpdateList.
                commitDAO.updateCommitsWithNewRepository(repositoryToUpdate.getSystemName(), notToUpdateList, repId,
                        false);
            } else {
                // Update all commits in cis list
                commitDAO.updateCommitsWithNewRepository(repositoryToUpdate.getSystemName(), cis, repId, true);
            }
        }

        LoggerFactory.getLogger(TopologyUpdater.class).trace("updateCommits -> Exit.");
    }

    /**
     * Delete the specified list of commits from the database.
     *
     * @param commitsToDelete the list of commits to be deleted.
     */
    private void deleteCommits(ArrayList<CommitInfo> commitsToDelete) throws ServiceException {
        commitDAO.deleteCommits(commitsToDelete, repositoryToUpdate.getSystemName());
    }

    /**
     * Updates each commit in the specified list with the list of repositories where they are known to be found. This is
     * done checking the repository status for each non-synchronized branch.
     *
     * @param commitList
     * @return
     */
    private ArrayList<CommitInfo> updateWhereExists(ArrayList<CommitInfo> commitList) throws DyeVCException {
        LoggerFactory.getLogger(TopologyUpdater.class).trace("updateWhereExists -> Entry.");
        Map<String, CommitInfo> commitsTrackedMap = tools.getCommitInfoTrackedMap();

        for (CommitInfo ci : commitList) {
            ci.setPreviousFoundIn(new HashSet<String>(ci.getFoundIn()));
            Set<URIish> aheadSet  = repositoryToUpdate.getRepStatus().getAheadRepsForCommit(ci.getHash());
            Set<URIish> behindSet = repositoryToUpdate.getRepStatus().getBehindRepsForCommit(ci.getHash());

            if (aheadSet.isEmpty() && behindSet.isEmpty()) {
                if (tools.getCommitInfoMap().containsKey(ci.getHash())) {
                    // If the commit is neither ahead nor behind any related repository, and exists locally,
                    // check whether it belongs to a tracked branch or not.
                    if (commitsTrackedMap.containsKey(ci.getHash())) {
                        // If it belongs to a tracked branch, than it exists in all push / pull repositories
                        ci.addAllToFoundIn(converter.toRepositoryInfo().getPullsFrom());
                        ci.addAllToFoundIn(converter.toRepositoryInfo().getPushesTo());
                    }

                    // In either case, it exists locally
                    ci.addFoundIn(repositoryToUpdate.getId());
                }

                continue;
            }

            if (!behindSet.isEmpty()) {
                // Commit is behind, so it does not exist locally and exists in all repositories in behindSet.
                Set<String> behindRepIds = convertURIishesToRepIds(behindSet);
                ci.addAllToFoundIn(behindRepIds);
            }

            if (!aheadSet.isEmpty()) {
                // Commit is ahead, so it exists locally and in all repositories that DO NOT have an ahead list containing it.
                Set<String>        aheadRepIds    = convertURIishesToRepIds(aheadSet);
                Collection<String> notAheadRepIds =
                    CollectionUtils.subtract(converter.toRepositoryInfo().getPushesTo(), aheadRepIds);
                ci.addAllToFoundIn(notAheadRepIds);
                ci.addFoundIn(repositoryToUpdate.getId());
            }
        }

        LoggerFactory.getLogger(TopologyUpdater.class).trace("updateWhereExists -> Exit.");

        return commitList;
    }

    /**
     * Gets commits that had their tracked attribute changed since previous snapshot, from false to true. Only existing
     * commits are returned (those that were deleted are discarded).
     *
     * @param previousSnapshot Previous snapshot
     * @return the set of commits that had their tracked attribute changed.
     * @throws VCSException
     */
    private ArrayList<CommitInfo> getNowTrackedCommits(ArrayList<CommitInfo> previousSnapshot) throws VCSException {
        // Finds commits that had their "tracked" attribute changed from false to true
        Predicate<CommitInfo> changedTrackedAttribute = new Predicate<CommitInfo>() {
            @Override
            public boolean evaluate(CommitInfo previousCommit) {
                boolean                 result        = false;
                Map<String, CommitInfo> nonTrackedMap = null;
                Map<String, CommitInfo> trackedMap    = null;
                try {
                    nonTrackedMap = tools.getCommitInfoNonTrackedMap();
                    trackedMap    = tools.getCommitInfoTrackedMap();
                } catch (VCSException ex) {
                    Logger.getLogger(TopologyUpdater.class.getName()).log(Level.SEVERE, null, ex);
                }

                // previous commit was not tracked and now is, but was not deleted (exists in trackedMap)
                if (!previousCommit.isTracked() &&!nonTrackedMap.containsKey(previousCommit.getHash())
                        && trackedMap.containsKey(previousCommit.getHash())) {
                    result = true;
                }

                return result;
            }
        };
        Collection<CommitInfo>  changedCommits    = CollectionUtils.select(previousSnapshot, changedTrackedAttribute);
        ArrayList<CommitInfo>   nowTrackedCommits = new ArrayList<CommitInfo>();
        Map<String, CommitInfo> trackedMap        = tools.getCommitInfoTrackedMap();
        for (CommitInfo info : changedCommits) {
            nowTrackedCommits.add(trackedMap.get(info.getHash()));
        }

        return nowTrackedCommits;
    }

    /**
     * This method does some processing depending on the version of dyevc that is running, to clean data or any other
     * processing that a new version demands.
     * @param previousVersion Previous version of this application that was running.
     */
    private void doVersionSpecificProcessing(String previousVersion) {
        // CommitInfo class structure changed on this version, so all snapshots should be discarded because they will give
        // ClassCastException when being loaded.
        if (ApplicationVersionUtils.isLessThanOrEqual(previousVersion, "0.2.16")) {
            discardCache = true;
        }
    }
}
