package br.uff.ic.dyevc.tools.vcs;

import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.CommitRelationship;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Cristiano
 */
public class GitCommitHistory {

    private Map<String, CommitInfo> commitInfoList;
    private Collection<CommitRelationship> commitRelationshipList;
    private GitConnector git;
    private GitCommitHistory instance;

    private GitCommitHistory(GitConnector git) {
        this.commitInfoList = new HashMap<String, CommitInfo>();
        this.commitRelationshipList = new ArrayList<CommitRelationship>();
        this.git = git;
        populateHistory();
    }

    public static GitCommitHistory getInstance(GitConnector git) {
        return new GitCommitHistory(git);
    }
    
    public Collection<CommitRelationship> getCommitRelationships() {
        return commitRelationshipList;
    }

    public Collection<CommitInfo> getCommitInfos() {
        return commitInfoList.values();
    }

    private void populateHistory() {
        LoggerFactory.getLogger(GitConnector.class).trace("populateHistory -> Entry.");
        try {
            Iterator<RevCommit> commitsIterator = git.getAllCommitsIterator();
            RevWalk walk = new RevWalk(git.getRepository());

            for (Iterator<RevCommit> it = commitsIterator; it.hasNext();) {
                RevCommit commit = walk.parseCommit(it.next());
                if (!commitInfoList.containsKey(commit.getName())) {
                    createCommitInfo(commit, walk);
                }
            }
        } catch (Exception ex) {
            LoggerFactory.getLogger(GitConnector.class).error("Error in testRevCommit.", ex);
        }
        LoggerFactory.getLogger(GitConnector.class).trace("populateHistory -> Exit.");
    }

    private CommitInfo createCommitInfo(RevCommit commit, RevWalk walk) throws IOException {
        CommitInfo ci = new CommitInfo(commit.getName());
        ci.setCommitDate(new Date(commit.getCommitTime() * 1000L));
        ci.setAuthor(commit.getAuthorIdent().getName());
        ci.setCommitter(commit.getCommitterIdent().getName());
        ci.setShortMessage(commit.getShortMessage());
        commitInfoList.put(ci.getId(), ci);

        createCommitRelations(commit, walk);
        
        return ci;
    }

    private void createCommitRelations(RevCommit commit, RevWalk walk) throws MissingObjectException, IncorrectObjectTypeException, IOException {
        RevCommit[] parents = commit.getParents();
        for (int j = 0; j < parents.length; j++) {
            RevCommit parent = walk.parseCommit(parents[j]);
            if (!commitInfoList.containsKey(parent.getName())) {
                createCommitInfo(parent, walk);
            }
            CommitRelationship relation = 
                    new CommitRelationship(commitInfoList.get(commit.getName()), 
                    commitInfoList.get(parent.getName()));
            commitRelationshipList.add(relation);
        }
    }

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
    
}