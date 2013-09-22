package br.uff.ic.dyevc.tools.vcs.git;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.model.CommitChange;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.CommitRelationship;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
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

import org.gitective.core.Assert;
import org.gitective.core.CommitUtils;
import org.gitective.core.GitException;

import org.slf4j.LoggerFactory;

import static org.eclipse.jgit.revwalk.filter.RevFilter.MERGE_BASE;

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
     * Map of commits with its properties. Each commit is identified by its id.
     */
    private Map<String, CommitInfo> commitInfoMap;

    /**
     * Collection of commit relationships, relating a parent commit and its children.
     */
    private List<CommitRelationship> commitRelationshipList;

    /**
     * Connector used by this class to connect to a Git repository.
     */
    private GitConnector git;

    /**
     * The monitored repository used to instantiate this class, or null if not informed.
     */
    private MonitoredRepository rep;
    private boolean             includeTopologyData;
    private boolean             initialized = false;

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
    public static final GitCommitTools getInstance(MonitoredRepository rep) throws VCSException {
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
    public static final GitCommitTools getInstance(MonitoredRepository rep, boolean includeTopologyData)
            throws VCSException {
        return new GitCommitTools(rep, includeTopologyData);
    }

    /**
     * Gets a new GitCommitTools instance.
     *
     * @param git the connector to be used to connect to a Git repository.
     * @return An instance of GitCommitTools.
     */
    public static final GitCommitTools getInstance(GitConnector git) {
        return new GitCommitTools(git, false);
    }

    /**
     * Gets a new GitCommitTools instance.
     *
     * @param git the connector to be used to connect to a Git repository.
     * @param includeTopologyData If true, CommitInfo objects are filled with topology data
     * @return An instance of GitCommitTools.
     */
    public static final GitCommitTools getInstance(GitConnector git, boolean includeTopologyData) {
        return new GitCommitTools(git, includeTopologyData);
    }

    /**
     * The list of commitInfos is created in this method.
     *
     * @throws VCSException
     */
    private void initialize() throws VCSException {
        this.commitInfoMap          = new TreeMap<String, CommitInfo>();
        this.commitRelationshipList = new ArrayList<CommitRelationship>();
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
     * Populates the commit history. This method traverses all commits in the Git repository. If commit does not yet
     * exist in the commitInfoMap, than includes it.
     */
    private final void populateHistory() throws VCSException {
        LoggerFactory.getLogger(GitCommitTools.class).trace("populateHistory -> Entry.");
        RevWalk walk = null;
        try {
            Iterator<RevCommit> commitsIterator = git.getAllCommitsIterator();
            walk = new RevWalk(git.getRepository());

            for (Iterator<RevCommit> it = commitsIterator; it.hasNext(); ) {
                RevCommit commit = walk.parseCommit(it.next());
                if (!commitInfoMap.containsKey(commit.getName())) {
                    createCommitInfo(commit, walk);
                }
            }

            for (String commitId : commitInfoMap.keySet()) {
                RevCommit commit = CommitUtils.getCommit(git.getRepository(), commitId);
                createCommitRelations(commit, walk);
            }

            LoggerFactory.getLogger(GitCommitTools.class).debug("populateHistory -> created history with {} items.",
                                    commitInfoMap.size());
        } catch (Exception ex) {
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
     * Extracts the commit info from repository and creates a CommitInfo object containing the commit properties. After
     * that, calls createCommitRelations to check relations between this commit and others.
     *
     * @param commit the repository commit object to extract properties from.
     * @param walk the walk to be used to check for relations with this commit.
     * @return a CommitInfo object
     * @throws IOException
     */
    private final CommitInfo createCommitInfo(RevCommit commit, RevWalk walk) throws IOException {
        LoggerFactory.getLogger(GitCommitTools.class).trace("createCommitInfo -> Entry.");
        CommitInfo ci = new CommitInfo(commit.getName(), git.getId());
        ci.setCommitDate(new Date(commit.getCommitTime() * 1000L));
        ci.setAuthor(commit.getAuthorIdent().getName());
        ci.setCommitter(commit.getCommitterIdent().getName());
        ci.setShortMessage(commit.getShortMessage());
        ci.setRepositoryId(rep.getId());

        if (includeTopologyData) {
            ci.getFoundIn().add(rep.getId());
            ci.setSystemName(rep.getSystemName());
        }

        commitInfoMap.put(ci.getHash(), ci);

        LoggerFactory.getLogger(GitCommitTools.class).trace("createCommitInfo -> Exit.");

        return ci;
    }

    /**
     * Includes the existing relationships between the specified commit and others. Basically, the relationships consist
     * of finding the parents of the specified commit.
     *
     * @param commit the commit to be checked for relationships
     * @param walk the walk used to parse parent commits.
     * @throws MissingObjectException
     * @throws IncorrectObjectTypeException
     * @throws IOException
     */
    private final void createCommitRelations(RevCommit commit, RevWalk walk)
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
     * Gets the changeset of a given revision string in the specified repository. The search is made in the working
     * clone.
     *
     * @param commitId the commitId whose changeset will be returned
     * @param repositoryId the repository id to look into.
     * @return the set of changes found in the given commit.
     */
    public final static Set<CommitChange> getCommitChangeSet(String commitId, String repositoryId) {
        LoggerFactory.getLogger(GitCommitTools.class).trace("getCommitChangeSet -> Entry.");
        Set<CommitChange> changes = new HashSet<CommitChange>();
        RevWalk           rw      = null;
        DiffFormatter     df      = null;
        try {
            Repository repo =
                MonitoredRepositories.getMonitoredProjectById(repositoryId).getWorkingCloneConnection().getRepository();
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
     * Get the common base commit between the given revisions.
     *
     * @param revisions
     * @return base commit or null if none
     * @throws DyeVCException
     */
    public CommitInfo getBase(final String... revisions) throws DyeVCException {
        if (revisions == null) {
            throw new DyeVCException("Revisions cannot be null");
        }

        if (revisions.length == 0) {
            throw new IllegalArgumentException(Assert.formatNotEmpty("Revisions"));
        }

        if (!initialized) {
            initialize();
        }

        final int          length = revisions.length;
        final CommitInfo[] cis    = new CommitInfo[length];
        for (int i = 0; i < length; i++) {
            cis[i] = commitInfoMap.get(revisions[i]);
        }

        return walkToBase(cis);
    }

    private CommitInfo walkToBase(CommitInfo[] cis) throws VCSException {
        final MergeBaseGenerator mbg = new MergeBaseGenerator(commitInfoMap);
        for (int i = 0; i < cis.length; i++) {
            mbg.markStart(cis[i]);
        }

        final CommitInfo base = mbg.getBase();

        return base;
    }
}
