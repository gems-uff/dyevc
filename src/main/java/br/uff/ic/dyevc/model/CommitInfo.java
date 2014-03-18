package br.uff.ic.dyevc.model;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.tools.vcs.git.GitCommitTools;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import java.util.Collection;
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
    value = {
        "repositoryId", "inWalk", "parentsCount", "parentsCountLock", "childrenCount", "childrenCountLock", "changeSet",
        "visited", "author", "flags", "previousFoundIn", "type"
    },
    ignoreUnknown = true
)
@JsonPropertyOrder(value = {
    "_id", "systemName", "commitDate", "committer", "shortMessage", "parents", "foundIn", "lastChanged", "tracked"
})
public class CommitInfo implements Comparable<CommitInfo>, Serializable {
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
    private Set<String> previousFoundIn;
    private boolean     tracked;
    private byte        type;

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public boolean isTracked() {
        return tracked;
    }

    public void setTracked(boolean tracked) {
        this.tracked = tracked;
    }

    public Set<String> getPreviousFoundIn() {
        return previousFoundIn;
    }

    public void setPreviousFoundIn(Set<String> previousFoundIn) {
        this.previousFoundIn = previousFoundIn;
    }

    /**
     * The id of any repository where this commit was found
     */
    private String repositoryId;

    /**
     * Author of commit.
     */
    private String author;

    /**
     * Date this commit was last changed in database;
     */
    private Date lastChanged;

    /**
     * Constructs a new CommitInfo object. This constructor should not be used by the application. It exists just
     * and only to be used by JSON framework, when (de)serializing objects to / from JSON notation.
     */
    public CommitInfo() {}

    /**
     * Constructs a CommitInfo object with the specified parameters.
     * @param id The id of this CommitInfo, represented by its hash.
     * @param repositoryId The id of the repository from where this commit was read.
     */
    public CommitInfo(String id, String repositoryId) {
        this.hash         = id;
        this.repositoryId = repositoryId;
        this.parents      = new HashSet<String>();
        this.foundIn      = new HashSet<String>();
    }

    /**
     * Gets the hash of this CommitInfo.
     * @return The hash of this CommitInfo.
     */
    public String getHash() {
        return hash;
    }

    /**
     * Sets the hash of this CommitInfo.
     * @param hash The new hash.
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * Gets the system name of this CommitInfo.
     * @return The system name of this CommitInfo.
     */
    public String getSystemName() {
        return systemName;
    }

    /**
     * Sets the systemName.
     * @param systemName The systemName to set.
     */
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    /**
     * Gets the repositoryId of this CommitInfo.
     * @return The repositoryId of this CommitInfo.
     */
    public String getRepositoryId() {
        return repositoryId;
    }

    /**
     * Sets the repositoryId
     * @param repositoryId The repositoryId to set.
     */
    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    /**
     * Gets the commitDate of this CommitInfo.
     * @return The commitDate of this CommitInfo.
     */
    public Date getCommitDate() {
        return commitDate;
    }

    /**
     * Sets the commitDate.
     * @param commitDate The commitDate to set.
     */
    public void setCommitDate(Date commitDate) {
        this.commitDate = commitDate;
    }

    /**
     * Gets the author of this CommitInfo.
     * @return The author of this CommitInfo.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author.
     * @param author The author to set.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Gets the committer of this CommitInfo.
     * @return The committer of this CommitInfo.
     */
    public String getCommitter() {
        return committer;
    }

    /**
     * Sets the committer.
     * @param committer The committer to set.
     */
    public void setCommitter(String committer) {
        this.committer = committer;
    }

    /**
     * Gets the shortMessage of this CommitInfo.
     * @return The shortMessage of this CommitInfo.
     */
    public String getShortMessage() {
        return shortMessage;
    }

    /**
     * Sets the shortMessage.
     * @param shortMessage The shortMessage to set.
     */
    public void setShortMessage(String shortMessage) {
        this.shortMessage = shortMessage;
    }

    /**
     * Returns a set with the id of each parent for this CommitInfo
     * @return Set of parent id for this CommitInfo.
     */
    public Set<String> getParents() {

        // if inWalk, flags must be at least 1 to return parents.
        if (inWalk && (flags == 0)) {
            return null;
        }

        return parents;
    }

    /**
     * Sets the parents.
     * @param parents  The parents to set.
     */
    public void setParents(Set<String> parents) {
        this.parents      = parents;
        this.parentsCount = parents.size();
    }

    /**
     * Gets the set of repository ids where this CommitInfo is known to exist.
     * @return The set of repository ids where this CommitInfo is known to exist.
     */
    public Set<String> getFoundIn() {
        return foundIn;
    }

    /**
     * Sets the foundIn.
     * @param foundIn The foundIn to set.
     */
    public void setFoundIn(Set<String> foundIn) {
        this.foundIn = foundIn;
    }

    /**
     * Adds a repository id to the set of foundIn repository ids.
     * @param repId The repository id to be added.
     */
    public void addFoundIn(String repId) {
        this.foundIn.add(repId);
    }

    /**
     * Adds a collection of repository ids to the set of foundIn repository ids.
     * @param repIds The collection of repository ids to be added.
     */
    public void addAllToFoundIn(Collection<String> repIds) {
        this.foundIn.addAll(repIds);
    }

    /**
     * Gets the date this object was last changed in database.
     * @return the last changed date.
     */
    public Date getLastChanged() {
        return lastChanged;
    }

    /**
     * Sets the lastChanged
     * @param lastChanged  The lastChanged to set.
     */
    public void setLastChanged(Date lastChanged) {
        this.lastChanged = lastChanged;
    }

    /**
     * Increment the number of children for this CommitInfo.
     */
    public void incrementChildren() {
        synchronized (childrenCountLock) {
            childrenCount++;
        }
    }

    /**
     * Increment the number of parents for this CommitInfo.
     */
    public void incrementParents() {
        synchronized (parentsCountLock) {
            parentsCount++;
        }
    }

    @Override
    public String toString() {
        return hash.substring(0, 5);
    }

    /**
     * Gets the number of parents of this CommitInfo.
     * @return The number of parents of this CommitInfo.
     */
    public int getParentsCount() {
        synchronized (parentsCountLock) {
            return parentsCount;
        }
    }

    /**
     * Gets the number of children of this CommitInfo.
     *
     * @return The number of children of this CommitInfo.
     */
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
     * Adds a commit change to this CommitInfo change set.
     * @param cc The change to added to the change set.
     */
    public void addChangeSet(CommitChange cc) {
        if (changeSet == null) {
            changeSet = new TreeSet<CommitChange>();
        }

        this.changeSet.add(cc);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + ((this.hash != null) ? this.hash.hashCode() : 0);

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

        final CommitInfo other = (CommitInfo)obj;

        return !((this.hash == null) ? (other.hash != null) : !this.hash.equals(other.hash));
    }

    /**
     * Indicates that this CommitInfo is being walked and though should use flags to decide
     * whether parents will be returned or not. If true, parents will be returned only if marked
     * as {@link br.uff.ic.dyevc.tools.vcs.git.CommonAncestralFinder#PARSED}.
     */
    private boolean inWalk;

    /** Flags is used to walk through commits. */
    private int flags;

    /**
     * Gets the flags.
     * @return The flags.
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Sets the flags
     * @param flags The flags to set.
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
