package br.uff.ic.dyevc.model;

public class RepositoryStatus {

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
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("RepositoryRelationship");
        result.append("\n\tURL: ").append(getRepositoryUrl());
        result.append("\n\tBranch: ").append(getRepositoryBranch());
        result.append("\tRemote URL: ").append(getReferencedRepositoryUrl());
        result.append("\tRemote Branch: ").append(getReferencedRepositoryBranch());
        result.append("\tAhead: ").append(getAhead()).append("\tBehind: ").append(getBehind());
        return result.toString();
    }
}
