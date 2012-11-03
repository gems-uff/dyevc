package br.uff.ic.dyevc.model;

import br.uff.ic.dyevc.utils.EqualsUtil;

public class BranchStatus {
    public static int STATUS_BEHIND = 2;
    public static int STATUS_AHEAD = 1;
    public static int STATUS_OK = 0;
    public static int STATUS_AHEAD_BEHIND = 4;

    private int ahead;
    private int behind;
    
    private String referencedRepositoryUrl;
    private String repositoryUrl;
    private String repositoryBranch;
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

    public int getStatus() {
        int rc;
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
