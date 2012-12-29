package br.uff.ic.dyevc.model;

import java.util.Date;

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

    public CommitInfo(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
            result = 0;
        }
        return result;
    }
    
}
