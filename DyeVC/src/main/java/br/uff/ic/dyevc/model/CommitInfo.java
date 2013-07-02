package br.uff.ic.dyevc.model;

import br.uff.ic.dyevc.tools.vcs.git.GitCommitTools;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

/**
 * Stores information about a singular commit made to VCS.
 * 
 * @author Cristiano
 */
public class CommitInfo implements Comparable<CommitInfo> {

    /**
     * Commit's identification.
     */
    private String id;
    
    /**
     * The id of the repository where this commit is found.
     */
    private String repositoryId;
    
    /**
     * Date the commit was done.
     */
    private Date commitDate;
    
    /**
     * Author of commit.
     */
    private String author;
    
    /**
     * Commiter (one's that effectively executed the commit action.
     */
    private String committer;
    
    /**
     * Short message written together with the commit.
     */
    private String shortMessage;
    
    /**
     * Number of parents this commit has. If greater than one, this was a merge. 
     * If zero, this was the first commit.
     */
    private int parentsCount = 0;
    private final Integer parentsCountLock = new Integer(0);
    
    /**
     * Number of children this commit has. If zero, this is a head. If greater 
     * than one, there are branches after this commit.
     */
    private int childrenCount = 0;
    private final Integer childrenCountLock = new Integer(0);

    /**
     * Set of the paths that were affected by this commit
     */
    private Set<CommitChange> changeSet = null;
    
    /**
     * Indicates if this commit was already visited in the process of plotting the graph
     */
    private boolean visited = false;
    
    public CommitInfo(String id, String repositoryId) {
        this.id = id;
        this.repositoryId = repositoryId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public Date getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(Date commitDate) {
        this.commitDate = commitDate;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCommitter() {
        return committer;
    }

    public void setCommitter(String committer) {
        this.committer = committer;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public void setShortMessage(String shortMessage) {
        this.shortMessage = shortMessage;
    }

    public void incrementChildren() {
        synchronized (childrenCountLock) {
            childrenCount++;
        }
    }
    
    public void incrementParents() {
        synchronized (parentsCountLock) {
            parentsCount++;
        }
    }

    @Override
    public String toString() {
        return id.substring(0, 5);
    }

    public int getParentsCount() {
        synchronized (parentsCountLock) {
            return parentsCount;
        }
    }

    public int getChildrenCount() {
        synchronized (childrenCountLock) {
            return childrenCount;
        }
    }

    @Override
    public int compareTo(CommitInfo o) {
        int result = 0;
        if (o == null) {
            throw new NullPointerException("Cannot compare to a null commit object.");
        }
        if (this.getCommitDate().before(o.getCommitDate())) {
            result = -1;
        }
        if (this.getCommitDate().after(o.getCommitDate())) {
            result = 1;
        }
        if (this.getCommitDate().equals(o.getCommitDate())) {
            result = this.getId().compareTo(o.getId());
        }
        return result;
    }

    /**
     * @return the visited
     */
    public boolean isVisited() {
        return visited;
    }

    /**
     * @param visited the visited to set
     */
    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    /**
     * @return the changeSet
     */
    public synchronized Set<CommitChange> getChangeSet() {
        if (changeSet == null) changeSet = GitCommitTools.getCommitChangeSet(id, repositoryId);
        return changeSet;
    }

    /**
     * @param changeSet the changeSet to set
     */
    public void setChangeSet(Set<CommitChange> changeSet) {
        this.changeSet = changeSet;
    }
    
    public void addChangePath(CommitChange cc) {
        if (changeSet == null) changeSet = new TreeSet<CommitChange>();
        this.changeSet.add(cc);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CommitInfo other = (CommitInfo) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }
}
