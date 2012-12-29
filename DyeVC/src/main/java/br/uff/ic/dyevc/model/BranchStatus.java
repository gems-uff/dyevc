package br.uff.ic.dyevc.model;

import br.uff.ic.dyevc.utils.EqualsUtil;

/**
 * This class represents the status of a Branch related to a referenced branch. 
 * The status can be one of:<BR>
 * <ul>
 *   <li>STATUS_OK, if both branches are synchronized</li>
 *   <li>STATUS_BEHIND, if referenced branch has commits that do not exist in this branch</li>
 *   <li>STATUS_AHEAD, if this branch has commits that do not exist in referenced branch</li>
 *   <li>STATUS_AHEAD_BEHIND, if branch is both behind and ahead the referenced branch</li>
 *   <li>STATUS_INVALID, if this branch is invalid, for any reason</li>
 * </ul>
 * <BR>
 * Attributes <b>ahead</b> and <b>behind</b> hold the number of commits this branch
 * is ahead or behind the referenced branch. Values in these attributes are valid
 * only if status is any of STATUS_BEHIND, STATUS_AHEAD or STATUS_AHEAD_BEHIND. 
 * Otherwise, their values are not to be used anyway.
 * @author Cristiano
 */
public class BranchStatus {
    /**
     * Indicates that this branch is behind the referenced branch.
     */
    public static int STATUS_BEHIND = 2;
    /**
     * Indicates that this branch is ahead the referenced branch.
     */
    public static int STATUS_AHEAD = 1;
    /**
     * Indicates that this branch is ahead and behind the referenced branch.
     */
    public static int STATUS_AHEAD_BEHIND = 4;
    /**
     * Indicates that this branch is in sync with the referenced branch.
     */
    public static int STATUS_OK = 0;
    /**
     * Indicates that this branch is invalid for any reason.
     */
    public static int STATUS_INVALID = -1;

    /**
     * The number of commits this branch has that are not found in referenced branch.
     */
    private int ahead;
    
    /**
     * The number of commits the referenced branch has that are not found in this branch.
     */
    private int behind;
    
    /**
     * URL of the repository where the referenced branch exists.
     */
    private String referencedRepositoryUrl;
    
    /**
     * URL of the repository where this branch exists.
     */
    private String repositoryUrl;
    
    /**
     * Identification of this branch
     */
    private String repositoryBranch;

    /**
     * Identification of the referenced branch
     */
    private String referencedRepositoryBranch;

    public int getAhead() {
        return ahead;
    }

    public void setAhead(int ahead) {
        this.ahead = ahead;
    }

    public int getBehind() {
        return behind;
    }

    public void setBehind(int behind) {
        this.behind = behind;
    }

    public String getReferencedRepositoryUrl() {
        return referencedRepositoryUrl;
    }

    public void setReferencedRepositoryUrl(String referencedRepositoryUrl) {
        this.referencedRepositoryUrl = referencedRepositoryUrl;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getRepositoryBranch() {
        return repositoryBranch;
    }

    public void setRepositoryBranch(String repositoryBranch) {
        this.repositoryBranch = repositoryBranch;
    }

    public String getReferencedRepositoryBranch() {
        return referencedRepositoryBranch;
    }

    public void setReferencedRepositoryBranch(String referencedRepositoryBranch) {
        this.referencedRepositoryBranch = referencedRepositoryBranch;
    }
    
    public void setInvalid() {
        ahead = -1;
        behind = -1;
    }

    public int getStatus() {
        int rc;
        if (getBehind() < 0 && getAhead() < 0) {
            return STATUS_INVALID;
        }
        if (getBehind() > 0) {
            if (getAhead() > 0 ) {
                rc = STATUS_AHEAD_BEHIND;
            } else {
                rc = STATUS_BEHIND;
            }
        } else {
            if (getAhead() > 0) {
                rc = STATUS_AHEAD;
            } else {
                rc = STATUS_OK;
            }
        }
        return rc;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("BranchStatus: ");
        result.append("\n\tURL: ").append(getRepositoryUrl());
        result.append("\n\tBranch: ").append(getRepositoryBranch());
        result.append("\tRemote URL: ").append(getReferencedRepositoryUrl());
        result.append("\tRemote Branch: ").append(getReferencedRepositoryBranch());
        result.append("\tAhead: ").append(getAhead()).append("\tBehind: ").append(getBehind());
        return result.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + this.ahead;
        hash = 13 * hash + this.behind;
        hash = 13 * hash + (this.referencedRepositoryUrl != null ? this.referencedRepositoryUrl.hashCode() : 0);
        hash = 13 * hash + (this.repositoryUrl != null ? this.repositoryUrl.hashCode() : 0);
        hash = 13 * hash + (this.repositoryBranch != null ? this.repositoryBranch.hashCode() : 0);
        hash = 13 * hash + (this.referencedRepositoryBranch != null ? this.referencedRepositoryBranch.hashCode() : 0);
        return hash;
    }
    
  @Override public boolean equals(Object aThat) {
    if ( this == aThat ) {
          return true;
      }

    if ( !(aThat instanceof BranchStatus) ) {
          return false;
      }

    BranchStatus that = (BranchStatus)aThat;

    //now a proper field-by-field evaluation can be made
    return
      EqualsUtil.areEqual(this.ahead, that.ahead) &&
      EqualsUtil.areEqual(this.behind, that.behind) &&
      EqualsUtil.areEqual(this.referencedRepositoryBranch, that.referencedRepositoryBranch) &&
      EqualsUtil.areEqual(this.referencedRepositoryUrl, that.referencedRepositoryUrl) &&
      EqualsUtil.areEqual(this.repositoryBranch, that.repositoryBranch) &&
      EqualsUtil.areEqual(this.repositoryUrl, that.repositoryUrl);
  }    
}
