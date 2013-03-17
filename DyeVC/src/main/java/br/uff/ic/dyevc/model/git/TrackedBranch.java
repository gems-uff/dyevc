package br.uff.ic.dyevc.model.git;

/**
 * Models a tracked branch in Git VCS.
 *
 * @author Cristiano
 */
public class TrackedBranch {
    /**
     * Name of branch.
     */
    private String name;
    
    /**
     * Remote name of branch.
     */
    private String remoteName;

    /**
     * MergeSpec of the branch (remote tracking branch).
     */
    private String mergeSpec;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemoteName() {
        return remoteName;
    }

    public void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }

    /**
     * @return the mergeSpec
     */
    public String getMergeSpec() {
        return mergeSpec;
    }

    /**
     * @param mergeSpec the mergeSpec to set
     */
    public void setMergeSpec(String mergeSpec) {
        this.mergeSpec = mergeSpec;
    }
    
    
}
