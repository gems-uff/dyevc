package br.uff.ic.dyevc.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Cristiano
 */
public class RepositoryStatus {

    private static final long serialVersionUID = 1735605826498282433L;
    private String repositoryId;
    private Date lastCheckedTime;
    private boolean invalid;
    
    private List<BranchStatus> syncedList;
    private List<BranchStatus> nonSyncedList;
    
    public RepositoryStatus(String repId) {
        this.repositoryId = repId;
        syncedList = new ArrayList<BranchStatus>();
        nonSyncedList = new ArrayList<BranchStatus>();
        if (!"".equals(repId)) {
            lastCheckedTime = new Date(System.currentTimeMillis());
        }
    }

    public void addStatus(List<BranchStatus> status) {
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
