/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.model;

import java.util.Date;

/**
 *
 * @author Cristiano
 */
public class CommitInfo implements Comparable<CommitInfo> {

    private String id;
    private Date commitDate;
    private String author;
    private String committer;
    private String shortMessage;
    private int parentsCount = 0;
    private int childrenCount = 0;

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
        childrenCount++;
    }
    
    public void incrementParents() {
        parentsCount++;
    }

    @Override
    public String toString() {
        return id.substring(0, 5);
    }

    public int getParentsCount() {
        return parentsCount;
    }

    public int getChildrenCount() {
        return childrenCount;
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
