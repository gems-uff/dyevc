package br.uff.ic.dyevc.model;

//~--- non-JDK imports --------------------------------------------------------

import org.eclipse.jgit.transport.URIish;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Indicates the status of a monitored repository, along with all known branches.
 *
 * @author Cristiano
 */
public class RepositoryStatus {
    private static final long serialVersionUID = 1735605826498282433L;

    /**
     * Repository identification.
     */
    private String repositoryId;

    /**
     * Last time this repository was checked for status changes.
     */
    private Date lastCheckedTime;

    /**
     * If true, indicates that this is an invalid repository. This status can
     * be due to access problems.
     */
    private boolean invalid;

    /**
     * If repository is invalid, stores the exception message received trying to
     * access it.
     */
    private String invalidMessage;

    /**
     * List of branches that are synchronized.
     */
    private List<BranchStatus> syncedList;

    /**
     * List of branches that are not synchronized.
     */
    private List<BranchStatus> nonSyncedList;

    /**
     * List of invalid branches
     */
    private List<BranchStatus> invalidList;

    /**
     * Number of branches that are ahead
     */
    private int aheadCount = 0;

    /**
     * Number of branches that are behind
     */
    private int behindCount = 0;

    /**
     * Map ahead commits with the list of repositories where they exist. Gets this list from the pushesTo attribute in
     * the BranchStatus.
     */
    private Map<String, Set<URIish>> aheadCommitsToRepsMap;

    /**
     * Map behind commits with the list of repositories where they do not exist. Gets this list from the pullsFrom
     * attribute in the BranchStatus.
     */
    private Map<String, Set<URIish>> behindCommitsToRepsMap;

    /**
     * Constructs an object of this type
     *
     * @param repId
     */
    public RepositoryStatus(String repId) {
        this.repositoryId      = repId;
        syncedList             = new ArrayList<BranchStatus>();
        nonSyncedList          = new ArrayList<BranchStatus>();
        invalidList            = new ArrayList<BranchStatus>();
        invalid                = false;
        aheadCommitsToRepsMap  = new TreeMap<String, Set<URIish>>();
        behindCommitsToRepsMap = new TreeMap<String, Set<URIish>>();

        if (!"".equals(repId)) {
            lastCheckedTime = new Date(System.currentTimeMillis());
        }
    }

    /**
     * Adds a list of BranchStatus to the list of known branches in this repository.
     * @param status List to be added
     */
    public void addBranchStatusList(List<BranchStatus> status) {
        for (Iterator<BranchStatus> it = status.iterator(); it.hasNext(); ) {
            BranchStatus branchStatus = it.next();
            if (branchStatus.getStatus() == BranchStatus.STATUS_OK) {
                syncedList.add(branchStatus);
            } else if (branchStatus.getStatus() == BranchStatus.STATUS_INVALID) {
                invalidList.add(branchStatus);
            } else {
                nonSyncedList.add(branchStatus);

                if (branchStatus.getAhead() > 0) {
                    aheadCount++;
                    updateAheadMap(branchStatus);
                }

                if (branchStatus.getBehind() > 0) {
                    behindCount++;
                    updateBehindMap(branchStatus);
                }
            }
        }
    }

    /**
     * Updates the map of behind commits with the behind commits in the specified branch, adding to them the pull list
     * associated with the branch.
     * @param branchStatus  The branchStatus to get the list of commits and the pull list to be associated to them.
     */
    private void updateBehindMap(BranchStatus branchStatus) {

        // for behind commits get pull uris, because these commits were pulled from them
        Set<URIish> behindBranchReps = new HashSet<URIish>(branchStatus.getPullURIs());
        for (String hash : branchStatus.getListBehindCommitIds()) {
            Set<URIish> behindCommitReps = new HashSet<URIish>(behindBranchReps);
            if (behindCommitsToRepsMap.containsKey(hash)) {
                behindCommitReps.addAll(behindCommitsToRepsMap.get(hash));
            }

            behindCommitsToRepsMap.put(hash, behindCommitReps);
        }
    }

    /**
     * Updates the map of ahead commits with the ahead commits in the specified branch, adding to them the push list
     * associated with the branch.
     * @param branchStatus  The branchStatus to get the list of commits and the push list to be associated to them.
     */
    private void updateAheadMap(BranchStatus branchStatus) {

        // for ahead commits get push uris, because these commits were not sent to them
        Set<URIish> aheadBranchReps = new HashSet<URIish>(branchStatus.getPushURIs());
        if (aheadBranchReps.isEmpty()) {

            // if empty, then push uris are the same as pull uris.
            aheadBranchReps.addAll(branchStatus.getPullURIs());
        }

        for (String hash : branchStatus.getListAheadCommitIds()) {
            Set<URIish> aheadCommitReps = new HashSet<URIish>(aheadBranchReps);
            if (aheadCommitsToRepsMap.containsKey(hash)) {
                aheadCommitReps.addAll(aheadCommitsToRepsMap.get(hash));
            }

            aheadCommitsToRepsMap.put(hash, aheadCommitReps);
        }
    }

    /**
     * Gets the list of branches that are synchronized with the this repository.
     * @return the list of synchronized branches.
     */
    public List<BranchStatus> getSyncedRepositoryBranches() {
        return syncedList;
    }

    /**
     * Gets the list of branches that are invalid.
     * @return the list of invalid branches.
     */
    public List<BranchStatus> getInvalidRepositoryBranches() {
        return invalidList;
    }

    /**
     * Gets the list of branches that are not synchronized with this repository.
     * @return  the list of branches that are not synchronized.
     */
    public List<BranchStatus> getNonSyncedRepositoryBranches() {
        return nonSyncedList;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public Date getLastCheckedTime() {
        return lastCheckedTime;
    }

    public int getNonSyncedBranchesCount() {
        return nonSyncedList.size();
    }

    public int getInvalidBranchesCount() {
        return invalidList.size();
    }

    public int getSyncedBranchesCount() {
        return syncedList.size();
    }

    public int getTotalBranchesCount() {
        return nonSyncedList.size() + syncedList.size() + invalidList.size();
    }

    public boolean isInvalid() {
        return invalid;
    }

    /**
     * @return the aheadCount
     */
    public int getAheadCount() {
        return aheadCount;
    }

    /**
     * @return the behindCount
     */
    public int getBehindCount() {
        return behindCount;
    }

    /**
     * @return the invalidMessage
     */
    public String getInvalidMessage() {
        return invalidMessage;
    }

    /**
     * Sets this repository as invalid.
     * @param invalidMessage the message that explains why the repository was set as invalid.
     */
    public void setInvalid(String invalidMessage) {
        this.invalid        = true;
        this.invalidMessage = invalidMessage;
    }

    /**
     * Gets the list of URIs representing repositories that are ahead for the specified commit hash, returning an empty
     * Set if the hash is not found.
     * @param hash the hash of the commit to be queried
     * @return the list of URIs that do not contain the specified commit hash, or null if the hash is not found in the
     * <code>aheadCommitsToRepsMap</code>.
     */
    public Set<URIish> getAheadRepsForCommit(String hash) {
        Set<URIish> result = aheadCommitsToRepsMap.get(hash);

        return (result != null) ? result : new HashSet<URIish>();
    }

    /**
     * Gets the list of URIs representing repositories that are behind for the specified commit hash, returning an empty
     * Set if the hash is not found.
     * @param hash the hash of the commit to be queried
     * @return the list of URIs that contains the specified commit hash, or null if the hash is not found in the
     * <code>behindCommitsToRepsMap</code>.
     */
    public Set<URIish> getBehindRepsForCommit(String hash) {
        Set<URIish> result = behindCommitsToRepsMap.get(hash);

        return (result != null) ? result : new HashSet<URIish>();
    }
}
