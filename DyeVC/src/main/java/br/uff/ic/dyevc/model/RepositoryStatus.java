package br.uff.ic.dyevc.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
     * List of branches that are synchronized.
     */
    private List<BranchStatus> syncedList;
    
    /**
     * List of branches that are not synchronized.
     */
    private List<BranchStatus> nonSyncedList;
    
    public RepositoryStatus(String repId) {
        this.repositoryId = repId;
        syncedList = new ArrayList<BranchStatus>();
        nonSyncedList = new ArrayList<BranchStatus>();
        if (!"".equals(repId)) {
            lastCheckedTime = new Date(System.currentTimeMillis());
        }
    }

    /**
     * Adds a list of BranchStatus to the list of known branches in this repository.
     * @param status List to be added
     */
    public void addBranchStatusList(List<BranchStatus> status) {
        for (Iterator<BranchStatus> it = status.iterator(); it.hasNext();) {
            BranchStatus branchStatus = it.next();
            if (branchStatus.getStatus() == BranchStatus.STATUS_OK) {
                syncedList.add(branchStatus);
            } else if (branchStatus.getStatus() == BranchStatus.STATUS_INVALID) {
                this.invalid = true;
            } else {
                nonSyncedList.add(branchStatus);
            }
        }
    }
    
    public List<BranchStatus> getSyncedRepositoryBranches() {
        return syncedList;
    }
    
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
    
    public int getSyncedBranchesCount() {
        return syncedList.size();
    }
    
    public int getTotalBranchesCount() {
        return nonSyncedList.size() + syncedList.size();
    }
    
    public boolean isInvalid() {
        return invalid;
    }
}
