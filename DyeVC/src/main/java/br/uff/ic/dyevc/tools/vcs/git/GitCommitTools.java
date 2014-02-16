package br.uff.ic.dyevc.tools.vcs.git;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.model.CommitChange;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.CommitRelationship;
import br.uff.ic.dyevc.model.git.TrackedBranch;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.persistence.CommitDAO;
import br.uff.ic.dyevc.utils.CommitInfoDateComparator;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import org.gitective.core.CommitUtils;

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
 * This class stores a history of commits in a Git repository.
 *
 * @author Cristiano
 */
public class GitCommitTools {
    /**
     * Map of commits associated that are pointed by any branch heads with the pointing branch names.
     */
    private Map<String, List<String>> commitsToBranchNamesMap;

    /**
     * Map of commits with its properties. Each commit is identified by its id.
     */
    private Map<String, CommitInfo> commitInfoMap;

    /**
     * Map of commits found in tracked branches.
     */
    private Map<String, CommitInfo> commitInfoTrackedMap;

    /**
     * Map of commits found in non-tracked branches.
     */
    private Map<String, CommitInfo> commitInfoNonTrackedMap;

    /**
     * Map of commits not found in any of the repositories that {@link #rep} pushes from.
     */
    Set<CommitInfo> notInPushListSet;

    /**
     * Map of commits not found in any of the repositories that {@link #rep} pulls to.
     */
    Set<CommitInfo> notInPullListSet;

    /**
     * Map of commits not found locally in {@link #rep}.
     */
    Set<CommitInfo> notInLocalRepositoryListSet;

    /**
     * Collection of commit relationships, relating a parent commit and its children.
     */
    private List<CommitRelationship> commitRelationshipList;

    /**
     * Connector used by this class to connect to a Git repository.
     */
    private GitConnector git;

    private RevWalk      walk;

    /**
     * The monitored repository used to instantiate this class, or null if not informed.
     */
    private MonitoredRepository rep;
    private boolean             includeTopologyData;
    private boolean             initialized = false;
    private static final int    PARSED      = 1 >> 0;

    /**
     * Creates a new instance of this class.
     *
     * @param rep The monitored repository to connect to.
     * @param includeTopologyData If true, CommitInfo objects are filled with topology data
     * @throws VCSException
     */
    private GitCommitTools(MonitoredRepository rep, boolean includeTopologyData) throws VCSException {
        this(rep.getConnection(), includeTopologyData);
        this.rep = rep;
    }

    /**
     * Creates a new instance of this class.
     *
     * @param git The connector to be used to connect to a Git repository.
     * @param includeTopologyData If true, CommitInfo objects are filled with topology data
     * @deprecated
     */
    private GitCommitTools(GitConnector git, boolean includeTopologyData) {
        this.git                 = git;
        this.includeTopologyData = includeTopologyData;
    }

    /**
     * Gets a new GitCommitTools instance.
     *
     * @param rep The monitored repository to connect to.
     * @return An instance of GitCommitTools.
     * @throws VCSException
     */
    public static GitCommitTools getInstance(MonitoredRepository rep) throws VCSException {
        return new GitCommitTools(rep, false);
    }

    /**
     * Gets a new GitCommitTools instance.
     *
     * @param rep The monitored repository to connect to.
     * @param includeTopologyData If true, CommitInfo objects are filled with topology data
     * @return An instance of GitCommitTools.
     * @throws VCSException
     */
    public static GitCommitTools getInstance(MonitoredRepository rep, boolean includeTopologyData) throws VCSException {
        return new GitCommitTools(rep, includeTopologyData);
    }

    /**
     * Gets a new GitCommitTools instance.
     *
     * @param git the connector to be used to connect to a Git repository.
     * @return An instance of GitCommitTools.
     * @deprecated
     */
    public static GitCommitTools getInstance(GitConnector git) {
        return new GitCommitTools(git, false);
    }

    /**
     * Sets the connection this instance must work with. Must be called before the class is initialized. Use it if you
     * want to work with a connection to a different clone of the repository (e.g. the temporary working clone).
     *
     * @param connection A connection to a repository
     */
    public void setConnection(GitConnector connection) throws VCSException {
        if (initialized) {
            throw new VCSException(
                "Class was already instantiated and connection cannot be changed. Create another instance instead.");
        }

        this.git = connection;
    }

    /**
     * The list of commitInfos is created in this method.
     *
     * @throws VCSException
     */
    private void initialize() throws VCSException {
        notInPushListSet             = Collections.EMPTY_SET;
        notInPullListSet             = Collections.EMPTY_SET;
        notInLocalRepositoryListSet  = Collections.EMPTY_SET;
        this.commitInfoMap           = new TreeMap<String, CommitInfo>();
        this.commitInfoTrackedMap    = new TreeMap<String, CommitInfo>();
        this.commitInfoNonTrackedMap = new TreeMap<String, CommitInfo>();
        this.commitRelationshipList  = new ArrayList<CommitRelationship>();
        populateHistory();
        initialized = true;
    }

    /**
     * Returns the list of relationships between commits and its children.
     *
     * @return the list of commit relationships
     * @throws VCSException
     */
    public Collection<CommitRelationship> getCommitRelationships() throws VCSException {
        if (!initialized) {
            initialize();
        }

        return commitRelationshipList;
    }

    /**
     * Returns the list of commits with its properties.
     *
     * @return the list of commits
     * @throws VCSException
     */
    public List<CommitInfo> getCommitInfos() throws VCSException {
        if (!initialized) {
            initialize();
        }

        List<CommitInfo>       cis        = new ArrayList<CommitInfo>(commitInfoMap.values());
        Comparator<CommitInfo> comparator = new CommitInfoDateComparator();
        Collections.sort(cis, comparator);

        return (List)cis;
    }

    /**
     * Returns a map with all commits, keyed by hash.
     *
     * @return a map with all commits, keyed by hash.
     * @throws VCSException
     */
    public Map<String, CommitInfo> getCommitInfoMap() throws VCSException {
        if (!initialized) {
            initialize();
        }

        return commitInfoMap;
    }

    /**
     * Returns a map with all commits in tracked branches, keyed by hash.
     *
     * @return a map with all commits in tracked branches, keyed by hash.
     * @throws VCSException
     */
    public Map<String, CommitInfo> getCommitInfoTrackedMap() throws VCSException {
        if (!initialized) {
            initialize();
        }

        return commitInfoTrackedMap;
    }

    /**
     * Returns a map with all commits in non-tracked branches, keyed by hash.
     *
     * @return a map with all commits in non-tracked branches, keyed by hash.
     * @throws VCSException
     */
    public Map<String, CommitInfo> getCommitInfoNonTrackedMap() throws VCSException {
        if (!initialized) {
            initialize();
        }

        return commitInfoNonTrackedMap;
    }

    /**
     * Returns a set with all commits not found locally.
     *
     * @return a set with all commits not found locally.
     * @throws VCSException
     */
    public Set<CommitInfo> getCommitsNotFoundLocally() throws VCSException {
        if (!initialized) {
            initialize();
        }

        return notInLocalRepositoryListSet;
    }

    /**
     * Returns a set with all commits not found in any of repositories that {@link #rep} pushes from.
     *
     * @return a set with all commits not found in any of the repositories that {@link #rep} pushes from.
     * @throws VCSException
     */
    public Set<CommitInfo> getCommitsNotInPushList() throws VCSException {
        if (!initialized) {
            initialize();
        }

        return notInPushListSet;
    }

    /**
     * Returns a set with all commits not found in any of repositories that {@link #rep} pulls to.
     *
     * @return a set with all commits not found in any of the repositories that {@link #rep} pulls to.
     * @throws VCSException
     */
    public Set<CommitInfo> getCommitsNotInPullList() throws VCSException {
        if (!initialized) {
            initialize();
        }

        return notInPullListSet;
    }

    /**
     * Loads external commits (not found locally) into the list of commits. After this, all known commits in the
     * topology that includes {@link #rep} will be loaded into {@link #commitInfoMap}. The method also loads different
     * sets, each one holding commits not found locally, commits not found in any of the push list repositories and
     * commits not found in any of the pull list repositories.
     *
     * @param info The repository info to get related repositories from
     *
     * @throws DyeVCException
     */
    public void loadExternalCommits(RepositoryInfo info) throws DyeVCException {
        if (rep == null) {
            throw new VCSException(
                "Cannot include external commits without a monitored repository. Get an instance of "
                + "this class using one of the constructors that receive a MonitoredRepository as parameter.");
        }

        if (!initialized) {
            initialize();
        }

        CommitDAO dao          = new CommitDAO();

        Set       pushesToSet  = new HashSet<String>(),
                  pullsFromSet = new HashSet<String>();
        pushesToSet.addAll(info.getPushesTo());
        pullsFromSet.addAll(info.getPullsFrom());

        notInPullListSet            = dao.getCommitsNotFoundInRepositories(pullsFromSet, info.getSystemName());
        notInPushListSet            = dao.getCommitsNotFoundInRepositories(pushesToSet, info.getSystemName());
        notInLocalRepositoryListSet = dao.getCommitsNotFoundInRepository(rep.getId(), info.getSystemName());

        includeExternalCommits();
    }

    /**
     * Include the external commits in the {@link #commitInfoMap}, along with its relationships.
     */
    private void includeExternalCommits() {
        for (CommitInfo ci : notInLocalRepositoryListSet) {
            commitInfoMap.put(ci.getHash(), ci);
        }

        for (CommitInfo ci : notInLocalRepositoryListSet) {
            for (String hash : ci.getParents()) {
                CommitRelationship cr = new CommitRelationship(ci, commitInfoMap.get(hash), false);
                commitRelationshipList.add(cr);
            }
        }
    }

    /**
     * Populates the commit history. This method traverses all commits in the Git repository. If commit does not yet
     * exist in the commitInfoMap, than includes it.
     */
    private void populateHistory() throws VCSException {
        LoggerFactory.getLogger(GitCommitTools.class).trace("populateHistory -> Entry.");

        try {
            // separate tracked branches from local branches
            Set<String>         nonTrackedBranchesRefs = git.getLocalBranches();
            Set<String>         trackedBranchesRefs    = new HashSet<String>();
            Set<String>         remoteBranchesRefs     = git.getRemoteBranches();
            List<TrackedBranch> trackedBranches        = git.getTrackedBranches();

            for (TrackedBranch tracked : trackedBranches) {
                String ref = IConstants.REFS_HEADS + tracked.getName();
                if (nonTrackedBranchesRefs.contains(ref)) {
                    nonTrackedBranchesRefs.remove(ref);
                    trackedBranchesRefs.add(ref);
                }
            }

            trackedBranchesRefs.addAll(remoteBranchesRefs);

            walk = new RevWalk(git.getRepository());

            Iterator<RevCommit> it = git.getLogForHeads(trackedBranchesRefs);
            while (it.hasNext()) {
                RevCommit commit = it.next();
                createCommitInfo(commit, true);
            }

            parseLocalCommits(nonTrackedBranchesRefs);

            for (String commitId : commitInfoMap.keySet()) {
                RevCommit commit = CommitUtils.getCommit(git.getRepository(), commitId);
                createCommitRelations(commit);
            }

            LoggerFactory.getLogger(GitCommitTools.class).debug("populateHistory -> created history with {} items.",
                                    commitInfoMap.size());
        } catch (IOException ex) {
            LoggerFactory.getLogger(GitCommitTools.class).error("Error in populateHistory.", ex);

            throw new VCSException("Error getting repository history.", ex);
        } finally {
            if (walk != null) {
                walk.dispose();
            }
        }

        LoggerFactory.getLogger(GitCommitTools.class).trace("populateHistory -> Exit.");
    }

    /**
     * Parse commits from the repository, starting with references specified in the branchHeads and taking all their
     * parents, until all the repository commits are processed. The method stops when queue is empty.
     * @param branchHeads List of heads for each branch to start traverse from. Taken from the refs/heads of the repository.
     * @throws IOException
     */
    private void parseLocalCommits(Set<String> branchHeads) throws IOException {
        ArrayList<RevCommit> queue = new ArrayList<RevCommit>();

        for (String ref : branchHeads) {
            RevCommit commit = CommitUtils.getCommit(git.getRepository(), ref);
            if (!commitInfoMap.containsKey(commit.getName())) {
                queue.add(commit);
            }
        }

        while (!queue.isEmpty()) {
            RevCommit commit = walk.parseCommit(queue.remove(0));
            createCommitInfo(commit, false);

            for (RevCommit parent : commit.getParents()) {
                if (!commitInfoMap.containsKey(parent.getName())) {
                    queue.add(parent);
                }
            }
        }
    }

    /**
     * Extracts the commit info from repository and creates an object containing the commit properties. After that,
     * calls {@link #createCommitRelations(org.eclipse.jgit.revwalk.RevCommit, org.eclipse.jgit.revwalk.RevWalk)} to
     * check relations between this commit and others.
     *
     * @param commit the repository commit object to extract properties from.
     * @param tracked Indicates whether or not these set of references are heads of tracked branches
     * @return a CommitInfo object
     * @throws IOException
     */
    private CommitInfo createCommitInfo(RevCommit commit, boolean tracked) throws IOException {
        LoggerFactory.getLogger(GitCommitTools.class).trace("createCommitInfo -> Entry.");
        CommitInfo ci = new CommitInfo(commit.getName(), git.getId());
        ci.setCommitDate(new Date(commit.getCommitTime() * 1000L));
        ci.setAuthor(commit.getAuthorIdent().getName());
        ci.setCommitter(commit.getCommitterIdent().getName());
        ci.setShortMessage(commit.getShortMessage());
        ci.setRepositoryId(git.getId());
        ci.setTracked(tracked);

        if (includeTopologyData) {
            ci.getFoundIn().add(rep.getId());
            ci.setSystemName(rep.getSystemName());
        }

        commitInfoMap.put(ci.getHash(), ci);

        if (tracked) {
            commitInfoTrackedMap.put(ci.getHash(), ci);
        } else {
            commitInfoNonTrackedMap.put(ci.getHash(), ci);
        }

        LoggerFactory.getLogger(GitCommitTools.class).trace("createCommitInfo -> Exit.");

        return ci;
    }

    /**
     * Includes the existing relationships between the specified commit and others. Basically, the relationships consist
     * of finding the parents of the specified commit.
     *
     * @param commit the commit to be checked for relationships
     * @throws MissingObjectException
     * @throws IncorrectObjectTypeException
     * @throws IOException
     */
    private void createCommitRelations(RevCommit commit)
            throws MissingObjectException, IncorrectObjectTypeException, IOException {
        LoggerFactory.getLogger(GitCommitTools.class).trace("createCommitRelations -> Entry.");

        // gets the list of parents for the commit
        RevCommit[] parents = commit.getParents();
        for (int j = 0; j < parents.length; j++) {

            // for each parent in the list, parses it, creating a RevCommit
            RevCommit  parent = walk.parseCommit(parents[j]);
            CommitInfo child  = commitInfoMap.get(commit.getName());
            child.getParents().add(parent.getName());
            CommitRelationship relation = new CommitRelationship(child, commitInfoMap.get(parent.getName()));
            commitRelationshipList.add(relation);
        }

        LoggerFactory.getLogger(GitCommitTools.class).trace("createCommitRelations -> Exit.");
    }

    /**
     * Overrides toString, showing all commits, along with its properties and relationships.
     *
     * @return the string representation of this commit history.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("CommitHistory:\n\t{Infos:\n");
        for (Iterator<CommitInfo> it = commitInfoMap.values().iterator(); it.hasNext(); ) {
            builder.append("\t\t").append(it.next()).append("\n");
        }

        builder.append("\tRelations:\n");

        for (Iterator<CommitRelationship> it = commitRelationshipList.iterator(); it.hasNext(); ) {
            builder.append("\t\t").append(it.next()).append("\n");
        }

        return builder.toString();
    }

    /**
     * Gets the change set of a given revision string in the specified repository.
     *
     * @param commitId the commitId whose change set will be returned
     * @param repositoryId the repository id to look into.
     * @return the set of changes found in the given commit.
     */
    public static Set<CommitChange> getCommitChangeSet(String commitId, String repositoryId) {
        LoggerFactory.getLogger(GitCommitTools.class).trace("getCommitChangeSet -> Entry.");
        Set<CommitChange> changes = new HashSet<CommitChange>();
        RevWalk           rw      = null;
        DiffFormatter     df      = null;
        try {
            Repository repo =
                MonitoredRepositories.getMonitoredProjectById(repositoryId).getConnection().getRepository();
            ObjectId  objId  = repo.resolve(commitId);
            RevCommit commit = CommitUtils.getCommit(repo, objId);
            rw = new RevWalk(repo);
            RevCommit parent;

            df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setRepository(repo);
            df.setDiffComparator(RawTextComparator.DEFAULT);
            df.setDetectRenames(true);

            List<DiffEntry> diffs;
            if (commit.getParentCount() > 0) {
                parent = rw.parseCommit(commit.getParent(0).getId());
                diffs  = df.scan(parent.getTree(), commit.getTree());
            } else {
                diffs = df.scan(new EmptyTreeIterator(),
                                new CanonicalTreeParser(null, rw.getObjectReader(), commit.getTree()));
            }

            for (DiffEntry diff : diffs) {
                CommitChange cc = new CommitChange();
                cc.setChangeType(diff.getChangeType().name());
                cc.setOldPath(diff.getOldPath());
                cc.setNewPath(diff.getNewPath());

                changes.add(cc);
            }
        } catch (Exception ex) {
            LoggerFactory.getLogger(GitCommitTools.class).error("Error parsing change set for commit " + commitId, ex);
        } finally {
            if (df != null) {
                df.release();
            }

            if (rw != null) {
                rw.dispose();
            }
        }

        LoggerFactory.getLogger(GitCommitTools.class).trace("getCommitChangeSet -> Exit.");

        return changes;
    }

    /**
     * Get the common ancestral between the given revisions.
     *
     * @param revisions The revisions to start traversing the commit tree from.
     * @return base commit or null if none
     * @throws DyeVCException
     */
    public CommitInfo getBase(final String... revisions) throws DyeVCException {
        return new CommonAncestorFinder(commitInfoMap).getCommonAncestor(revisions);
    }

    /**
     * Retrieves a map with every commit pointed by a branch head. The key is the commit hash and the value is the list
     * of branch names that point to it.
     *
     * @return base commit or null if none
     * @throws DyeVCException
     */
    public Map<String, List<String>> getHeadsCommitsMap() throws DyeVCException {
        if (commitsToBranchNamesMap == null) {
            commitsToBranchNamesMap = new TreeMap<String, List<String>>();
            Set<String> branches = git.getAllBranches();
            for (String branch : branches) {
                try {
                    String       ref        = git.getRepository().getRef(branch).getObjectId().name();
                    List<String> branchList = commitsToBranchNamesMap.get(ref);
                    if (branchList == null) {
                        branchList = new ArrayList<String>();
                        commitsToBranchNamesMap.put(ref, branchList);
                    }

                    branchList.add(branch);
                } catch (IOException ex) {
                    LoggerFactory.getLogger(GitCommitTools.class).error("Error resolving a reference to " + branch, ex);
                }
            }
        }

        return commitsToBranchNamesMap;
    }
}
