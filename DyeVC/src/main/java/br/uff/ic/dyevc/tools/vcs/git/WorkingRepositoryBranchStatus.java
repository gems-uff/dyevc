package br.uff.ic.dyevc.tools.vcs.git;

import br.uff.ic.dyevc.exception.VCSException;
import java.io.IOException;
import org.eclipse.jgit.lib.BranchConfig;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.transport.RefSpec;

/**
 * Status between two remote branches in a temp repository
 */
public class WorkingRepositoryBranchStatus {

    /**
     * Compute the tracking status for the
     * <code>branchName</code> in
     * <code>repository</code>, regarding to its remote. Both refs for the
     * tracked branch and for the remote are remote refs, because this class
     * works on working repositories that were cloned from a real repository.
     *
     * @param connector the git connector to repository's status that will be
     * computed (this is the working repository)
     * @param branchName the branch to compute status. This is a local branch in
     * the real repository, but in the working repository it is also a remote
     * branch.
     * @throws IOException
     */
    public static WorkingRepositoryBranchStatus of(GitConnector connector, String branchName)
            throws IOException, VCSException {

        String remoteName = connector.getRemoteForBranch(branchName);
        if (remoteName == null) {
            return null;
        }
        
        BranchConfig branchConfig = new BranchConfig(connector.getRepository().getConfig(),
                branchName);

        String trackingBranchRefString = branchConfig.getTrackingBranch();
        if (trackingBranchRefString == null) {
            return null;
        }
        
        Ref tracking = connector.getRepository().getRef(trackingBranchRefString);
        if (tracking == null) {
            return null;
        }
        
        String trackingBranchName = trackingBranchRefString.substring(trackingBranchRefString.lastIndexOf("/") + 1);
        
        String localBranchRefString = trackingBranchRefString.replace(trackingBranchName, branchName);
        localBranchRefString = localBranchRefString.replace(GitConnector.REFS_REMOTES + remoteName, 
                GitConnector.REFS_REMOTES + connector.getId());
 
        Ref local = connector.getRepository().getRef(localBranchRefString);
        if (local == null) {
            return null;
        }

        RevWalk walk = new RevWalk(connector.getRepository());

        RevCommit localCommit = walk.parseCommit(local.getObjectId());
        RevCommit trackingCommit = walk.parseCommit(tracking.getObjectId());

        walk.setRevFilter(RevFilter.MERGE_BASE);
        walk.markStart(localCommit);
        walk.markStart(trackingCommit);
        RevCommit mergeBase = walk.next();

        walk.reset();
        walk.setRevFilter(RevFilter.ALL);
        int aheadCount = RevWalkUtils.count(walk, localCommit, mergeBase);
        int behindCount = RevWalkUtils.count(walk, trackingCommit, mergeBase);

        return new WorkingRepositoryBranchStatus(trackingBranchRefString, aheadCount, behindCount);
    }
    private final String trackingBranch;
    private final int aheadCount;
    private final int behindCount;

    private WorkingRepositoryBranchStatus(String trackingBranch, int aheadCount,
            int behindCount) {
        this.trackingBranch = trackingBranch;
        this.aheadCount = aheadCount;
        this.behindCount = behindCount;
    }

    /**
     * @return full tracking branch name
     */
    public String getTrackingBranch() {
        return trackingBranch;
    }

    /**
     * @return number of commits that the local branch is ahead of the tracking
     * branch
     */
    public int getAheadCount() {
        return aheadCount;
    }

    /**
     * @return number of commits that the local branch is behind of the tracking
     * branch
     */
    public int getBehindCount() {
        return behindCount;
    }
}