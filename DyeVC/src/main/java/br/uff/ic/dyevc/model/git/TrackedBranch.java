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
    
    
}
