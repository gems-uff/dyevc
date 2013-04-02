package br.uff.ic.dyevc.tools.vcs.git;

import br.uff.ic.dyevc.model.CommitChange;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.CommitRelationship;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.slf4j.LoggerFactory;

/**
 * This class stores a history of commits in a Git repository.
 *
 * @author Cristiano
 */
public class GitCommitHistory {

    /**
     * Map of commits with its properties. Each commit is identified by its id.
     */
    private Map<String, CommitInfo> commitInfoMap;
    /**
     * Collection of commit relationships, relating a parent commit and its
     * children.
     */
    private Collection<CommitRelationship> commitRelationshipList;
    /**
     * Connector used by this class to connect to a Git repository.
     */
    private GitConnector git;

    /**
     * The list of commits if created in this constructor.
     *
     * @param git the connector to be used to connect to a Git repository.
     */
    private GitCommitHistory(GitConnector git) {
        this.commitInfoMap = new TreeMap<String, CommitInfo>();
        this.commitRelationshipList = new ArrayList<CommitRelationship>();
        this.git = git;
        populateHistory();
    }

    /**
     * This class is NOT a singleton. Each call to this method returns a new
     * instance.
     *
     * @param git the connector to be used to connect to a Git repository.
     * @return an instance of this class.
     */
    public static GitCommitHistory getInstance(GitConnector git) {
        return new GitCommitHistory(git);
    }

    /**
     * Returns the list of relationships between commits and its children.
     *
     * @return the list of commit relationships
     */
    public Collection<CommitRelationship> getCommitRelationships() {
        return commitRelationshipList;
    }

    /**
     * Returns the list of commits with its properties.
     *
     * @return the list of commits
     */
    public Collection<CommitInfo> getCommitInfos() {
        return commitInfoMap.values();
    }

    /**
     * Populates the commit history. This method traverses all commits in the
     * Git repository. If commit does not yet exist in the commitInfoMap, than
     * includes it.
     */
    private void populateHistory() {
        LoggerFactory.getLogger(GitCommitHistory.class).trace("populateHistory -> Entry.");
        RevWalk walk = null;
        try {
            Iterator<RevCommit> commitsIterator = git.getAllCommitsIterator();
            walk = new RevWalk(git.getRepository());

            for (Iterator<RevCommit> it = commitsIterator; it.hasNext();) {
                RevCommit commit = walk.parseCommit(it.next());
                if (!commitInfoMap.containsKey(commit.getName())) {
                    createCommitInfo(commit, walk);
                }
            }
        } catch (Exception ex) {
            LoggerFactory.getLogger(GitCommitHistory.class).error("Error in populateHistory.", ex);
        } finally {
            if (walk != null) {
                walk.release();
            }
        }
        LoggerFactory.getLogger(GitCommitHistory.class).trace("populateHistory -> Exit.");
    }

    /**
     * Extracts the commit info from repository and creates a CommitInfo object
     * containing the commit properties. After that, calls createCommitRelations
     * to check relations between this commit and others.
     *
     * @param commit the repository commit object to extract properties from.
     * @param walk the walk to be used to check for relations with this commit.
     * @return a CommitInfo object
     * @throws IOException
     */
    private CommitInfo createCommitInfo(RevCommit commit, RevWalk walk) throws IOException {
        LoggerFactory.getLogger(GitCommitHistory.class).trace("createCommitInfo -> Entry.");
        CommitInfo ci = new CommitInfo(commit.getName());
        ci.setCommitDate(new Date(commit.getCommitTime() * 1000L));
        ci.setAuthor(commit.getAuthorIdent().getName());
        ci.setCommitter(commit.getCommitterIdent().getName());
        ci.setShortMessage(commit.getShortMessage());

        fillCommitDiff(commit, ci);
        commitInfoMap.put(ci.getId(), ci);

        createCommitRelations(commit, walk);

        LoggerFactory.getLogger(GitCommitHistory.class).trace("createCommitInfo -> Exit.");
        return ci;
    }

    /**
     * Includes the existing relationships between the specified commit and
     * others. Basically, the relationships consist of finding the parents of
     * the specified commit.
     *
     * @param commit the commit to be checked for relationships
     * @param walk the walk used to parse parent commits.
     * @throws MissingObjectException
     * @throws IncorrectObjectTypeException
     * @throws IOException
     */
    private void createCommitRelations(RevCommit commit, RevWalk walk) throws MissingObjectException, IncorrectObjectTypeException, IOException {
        LoggerFactory.getLogger(GitCommitHistory.class).trace("createCommitRelations -> Entry.");
        //gets the list of parents for the commit
        RevCommit[] parents = commit.getParents();
        for (int j = 0; j < parents.length; j++) {
            //for each parent in the list, parses it, creating a RevCommit
            RevCommit parent = walk.parseCommit(parents[j]);
            //if parent does not exist in map, recursively calls createCommitInfo to include it.
            if (!commitInfoMap.containsKey(parent.getName())) {
                createCommitInfo(parent, walk);
            }
            //adds a new relation between the commit and its parent in commitRelationshipList
            CommitRelationship relation =
                    new CommitRelationship(commitInfoMap.get(commit.getName()),
                    commitInfoMap.get(parent.getName()));
            commitRelationshipList.add(relation);
        }
        LoggerFactory.getLogger(GitCommitHistory.class).trace("createCommitRelations -> Exit.");
    }

    /**
     * Overrides toString, showing all commits, along with its properties and
     * relationships.
     *
     * @return the string representation of this commit history.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("CommitHistory:\n\t{Infos:\n");
        for (Iterator<CommitInfo> it = getCommitInfos().iterator(); it.hasNext();) {
            builder.append("\t\t").append(it.next()).append("\n");
        }
        builder.append("\tRelations:\n");
        for (Iterator<CommitRelationship> it = commitRelationshipList.iterator(); it.hasNext();) {
            builder.append("\t\t").append(it.next()).append("\n");
        }
        return builder.toString();
    }

    private void fillCommitDiff(RevCommit commit, CommitInfo ci) {
        LoggerFactory.getLogger(GitCommitHistory.class).trace("fillCommitDiff -> Entry.");
        RevWalk rw = null;
        try {
            rw = new RevWalk(git.getRepository());
            RevCommit parent;
            
            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setRepository(git.getRepository());
            df.setDiffComparator(RawTextComparator.DEFAULT);
            df.setDetectRenames(true);

            List<DiffEntry> diffs;
            if (commit.getParentCount() > 0) {
                parent = rw.parseCommit(commit.getParent(0).getId());
                diffs = df.scan(parent.getTree(), commit.getTree());
            } else {
                diffs = df.scan(new EmptyTreeIterator(),
                        new CanonicalTreeParser(null, rw.getObjectReader(), commit.getTree()));
            }
            for (DiffEntry diff : diffs) {
                CommitChange cc = new CommitChange();
                cc.setChangeType(diff.getChangeType().name());
                cc.setOldPath(diff.getOldPath());
                cc.setNewPath(diff.getNewPath());
                
                ci.addChangePath(cc);
            }
            df.release();
        } catch (Exception ex) {
            Logger.getLogger(GitCommitHistory.class.getName()).log(Level.SEVERE, "Error parsing change set for commit " + commit.getName(), ex);
        } finally {
            if (rw != null) {
                rw.release();
            }
        }
        LoggerFactory.getLogger(GitCommitHistory.class).trace("fillCommitDiff -> Exit.");
    }
}