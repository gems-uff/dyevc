package br.uff.ic.dyevc.tools.vcs.git;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.exception.VCSException;

import org.eclipse.jgit.lib.BranchConfig;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

import org.gitective.core.CommitFinder;
import org.gitective.core.CommitUtils;
import org.gitective.core.filter.commit.CommitListFilter;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.List;

/**
 * Status between two remote branches in a temp repository
 */
public class WorkingRepositoryBranchStatus {
    /**
     * Compute the tracking status for the
     * <code>branchName</code> in
     * <code>repository</code>, regarding to its remote. Both refs for the tracked branch and for the remote are remote
     * refs, because this class works on working repositories that were cloned from a real repository.
     *
     * @param connector the git connector to repository's status that will be computed (this is the working repository)
     * @param branchName the branch to compute status. This is a local branch in the real repository, but in the working
     * repository it is also a remote branch.
     * @throws IOException
     */
    public static WorkingRepositoryBranchStatus of(GitConnector connector, String branchName)
            throws IOException, VCSException, URISyntaxException {

        String remoteName = connector.getRemoteForBranch(branchName);
        if (remoteName == null) {
            return null;
        }

        BranchConfig branchConfig            = new BranchConfig(connector.getRepository().getConfig(), branchName);

        String       trackingBranchRefString = branchConfig.getTrackingBranch();
        if (trackingBranchRefString == null) {
            return null;
        }

        Ref tracking = connector.getRepository().getRef(trackingBranchRefString);
        if (tracking == null) {
            return null;
        }

        String trackingBranchName   = trackingBranchRefString.substring(trackingBranchRefString.lastIndexOf("/") + 1);

        String localBranchRefString = trackingBranchRefString.replace(trackingBranchName, branchName);
        localBranchRefString = localBranchRefString.replace(GitConnector.REFS_REMOTES + remoteName,
                GitConnector.REFS_REMOTES + connector.getId());

        Ref local = connector.getRepository().getRef(localBranchRefString);
        if (local == null) {
            return null;
        }

        RevWalk   walk           = new RevWalk(connector.getRepository());

        RevCommit localCommit    = walk.parseCommit(local.getObjectId());
        RevCommit trackingCommit = walk.parseCommit(tracking.getObjectId());

        walk.setRevFilter(RevFilter.MERGE_BASE);
        walk.markStart(localCommit);
        walk.markStart(trackingCommit);
        RevCommit mergeBase = walk.next();

        walk.reset();
        walk.setRevFilter(RevFilter.ALL);
        final int aheadCount  = RevWalkUtils.count(walk, localCommit, mergeBase);
        final int behindCount = RevWalkUtils.count(walk, trackingCommit, mergeBase);

        /* Finding list of commits ahead */
        CommitFinder     finder  = new CommitFinder(connector.getRepository());
        CommitListFilter commits = new CommitListFilter();
        finder.setFilter(RevFilter.ALL);
        finder.setFilter(commits).findBetween(localCommit.getId(), mergeBase.getId());
        List<String> listAheadCommitIds = new ArrayList<String>();
        for (RevCommit commit : commits.getCommits()) {
            listAheadCommitIds.add(commit.getName());
        }

        /* Finding list of commits behind */
        commits.reset();
        finder.setFilter(commits).findBetween(trackingCommit.getId(), mergeBase.getId());
        List<String> listBehindCommitIds = new ArrayList<String>();
        for (RevCommit commit : commits.getCommits()) {
            listBehindCommitIds.add(commit.getName());
        }

        RemoteConfig cfg      = new RemoteConfig(connector.getRepository().getConfig(), remoteName);

        List<URIish> pushURIs = cfg.getPushURIs();
        List<URIish> pullURIs = cfg.getURIs();

        return new WorkingRepositoryBranchStatus(trackingBranchRefString, aheadCount, behindCount, listAheadCommitIds,
                listBehindCommitIds, pushURIs, pullURIs);
    }

    private final String       trackingBranch;
    private final int          aheadCount;
    private final int          behindCount;
    private final List<String> listAheadCommitIds;
    private final List<String> listBehindCommitIds;
    private final List<URIish> pushURIs;
    private final List<URIish> pullURIs;

    /**
     * Constructs a new WorkingRepositoryBranchStatus object
     *
     * @param trackingBranch
     * @param aheadCount
     * @param behindCount
     */
    private WorkingRepositoryBranchStatus(String trackingBranch, int aheadCount, int behindCount,
            List<String> aheadCommits, List<String> behindCommits, List<URIish> pushURIs, List<URIish> pullURIs) {
        this.trackingBranch      = trackingBranch;
        this.aheadCount          = aheadCount;
        this.behindCount         = behindCount;
        this.listAheadCommitIds  = aheadCommits;
        this.listBehindCommitIds = behindCommits;
        this.pushURIs            = pushURIs;
        this.pullURIs            = pullURIs;
    }

    /**
     * @return full tracking branch name
     */
    public String getTrackingBranch() {
        return trackingBranch;
    }

    /**
     * @return number of commits that the local branch is ahead of the tracking branch
     */
    public int getAheadCount() {
        return aheadCount;
    }

    /**
     * @return number of commits that the local branch is behind of the tracking branch
     */
    public int getBehindCount() {
        return behindCount;
    }

    /**
     * @return the list of commits that the local branch is ahead of the tracking branch
     */
    public List<String> getListAheadCommitIds() {
        return listAheadCommitIds;
    }

    /**
     * @return the list of commits that the local branch is behind of the tracking branch
     */
    public List<String> getListBehindCommitIds() {
        return listBehindCommitIds;
    }

    public List<URIish> getPushURIs() {
        return pushURIs;
    }

    public List<URIish> getPullURIs() {
        return pullURIs;
    }
}
