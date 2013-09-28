package br.uff.ic.dyevc.model;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.tools.vcs.git.GitCommitTools;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

//~--- JDK imports ------------------------------------------------------------

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Stores information about a singular commit made to VCS.
 *
 * @author Cristiano
 */
@JsonIgnoreProperties(
    value         = {
        "repositoryId", "parentsCount", "childrenCount", "changeSet", "visited", "author"
    },
    ignoreUnknown = true
)
@JsonPropertyOrder(value = {
    "_id", "systemName", "commitDate", "committer", "shortMessage", "parents", "foundIn"
})
public class CommitInfo implements Comparable<CommitInfo> {
    /**
     * Number of parents this commit has. If greater than one, this was a merge.
     * If zero, this was the first commit.
     */
    private int           parentsCount     = 0;
    private final Integer parentsCountLock = new Integer(0);

    /**
     * Number of children this commit has. If zero, this is a head. If greater
     * than one, there are branches after this commit.
     */
    private int           childrenCount     = 0;
    private final Integer childrenCountLock = new Integer(0);

    /**
     * Set of the paths that were affected by this commit
     */
    private Set<CommitChange> changeSet = null;

    /**
     * Indicates if this commit was already visited in the process of plotting the graph
     */
    private boolean visited = false;

    /**
     * Commit's identification.
     */
    @JsonProperty(value = "_id")
    private String hash;

    /**
     * System name where this commit is found
     */
    private String systemName;

    /**
     * Date the commit was done.
     */
    private Date commitDate;

    /**
     * Commiter (one's that effectively executed the commit action.
     */
    private String committer;

    /**
     * Short message written together with the commit.
     */
    private String      shortMessage;
    private Set<String> parents;
    private Set<String> foundIn;

    /**
     * The id of any repository where this commit was found
     */
    private String repositoryId;

    /**
     * Author of commit.
     */
    private String author;

    /**
     * Constructs ...
     */
    public CommitInfo() {}

    /**
     * Constructs ...
     *
     * @param id
     * @param repositoryId
     */
    public CommitInfo(String id, String repositoryId) {
        this.hash         = id;
        this.repositoryId = repositoryId;
        this.parents      = new HashSet<String>();
        this.foundIn      = new HashSet<String>();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getHash() {
        return hash;
    }

    /**
     * Method description
     *
     *
     * @param hash
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getSystemName() {
        return systemName;
    }

    /**
     * Method description
     *
     *
     * @param systemName
     */
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getRepositoryId() {
        return repositoryId;
    }

    /**
     * Method description
     *
     *
     * @param repositoryId
     */
    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Date getCommitDate() {
        return commitDate;
    }

    /**
     * Method description
     *
     *
     * @param commitDate
     */
    public void setCommitDate(Date commitDate) {
        this.commitDate = commitDate;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Method description
     *
     *
     * @param author
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getCommitter() {
        return committer;
    }

    /**
     * Method description
     *
     *
     * @param committer
     */
    public void setCommitter(String committer) {
        this.committer = committer;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getShortMessage() {
        return shortMessage;
    }

    /**
     * Method description
     *
     *
     * @param shortMessage
     */
    public void setShortMessage(String shortMessage) {
        this.shortMessage = shortMessage;
    }

    /**
     * Return ids of the parents for this CommitInfo
     *
     *
     * @return Set of parent ids for this CommitInfo
     */
    public Set<String> getParents() {

        // if inWalk, flags must be at least 1 to return parents.
        if (inWalk && (flags == 0)) {
            return null;
        }

        return parents;
    }

    /**
     * Method description
     *
     *
     * @param parents
     */
    public void setParents(Set<String> parents) {
        this.parents      = parents;
        this.parentsCount = parents.size();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Set<String> getFoundIn() {
        return foundIn;
    }

    /**
     * Method description
     *
     *
     * @param foundIn
     */
    public void setFoundIn(Set<String> foundIn) {
        this.foundIn = foundIn;
    }

    /**
     * Method description
     *
     */
    public void incrementChildren() {
        synchronized (childrenCountLock) {
            childrenCount++;
        }
    }

    /**
     * Method description
     *
     */
    public void incrementParents() {
        synchronized (parentsCountLock) {
            parentsCount++;
        }
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String toString() {
        return hash.substring(0, 5);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public int getParentsCount() {
        synchronized (parentsCountLock) {
            return parentsCount;
        }
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public int getChildrenCount() {
        synchronized (childrenCountLock) {
            return childrenCount;
        }
    }

    /**
     * Method description
     *
     *
     * @param o
     *
     * @return
     */
    @Override
    public int compareTo(CommitInfo o) {
        int result = 0;
        if (o == null) {
            throw new NullPointerException("Cannot compare to a null commit object.");
        }

        if ((this.getCommitDate()) != null && (this.getCommitDate().before(o.getCommitDate()))) {
            result = -1;
        }

        if ((this.getCommitDate()) != null && (this.getCommitDate().after(o.getCommitDate()))) {
            result = 1;
        }

        if ((this.getCommitDate()) != null && (this.getCommitDate().equals(o.getCommitDate()))) {
            result = this.getHash().compareTo(o.getHash());
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
        if (changeSet == null) {
            changeSet = GitCommitTools.getCommitChangeSet(hash, repositoryId);
        }

        return changeSet;
    }

    /**
     * @param changeSet the changeSet to set
     */
    public void setChangeSet(Set<CommitChange> changeSet) {
        this.changeSet = changeSet;
    }

    /**
     * Method description
     *
     *
     * @param cc
     */
    public void addChangeSet(CommitChange cc) {
        if (changeSet == null) {
            changeSet = new TreeSet<CommitChange>();
        }

        this.changeSet.add(cc);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + ((this.hash != null) ? this.hash.hashCode() : 0);

        return hash;
    }

    /**
     * Method description
     *
     *
     * @param obj
     *
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final CommitInfo other = (CommitInfo)obj;
        if ((this.hash == null) ? (other.hash != null) : !this.hash.equals(other.hash)) {
            return false;
        }

        return true;
    }

    /**
     * Indicates that this CommitInfo is being walked and though should use flags to decide
     * whether parents will be returned or not. If true, parents will be returned only if marked
     * as PARSED.
     * @see br.uff.ic.dyevc.tools.vcs.git.MergeBaseGenerator.PARSED
     */
    private boolean inWalk;

    /** Field description */
    private int flags;

    /**
     * Method description
     *
     *
     * @return
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Method description
     *
     *
     * @param flags
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }

    /**
     * Marks this CommitInfo as being in a walk.
     */
    public void markInWalk() {
        inWalk = true;
    }

    /**
     * Resets this CommitInfo and releases it to be walked another time.
     */
    public void resetWalk() {
        inWalk = false;
        this.setFlags(0);
    }
}
