package br.uff.ic.dyevc.tools.vcs.git;

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.model.BranchStatus;
import br.uff.ic.dyevc.model.git.TrackedBranch;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BranchConfig;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ResolveMerger.MergeFailureReason;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.slf4j.LoggerFactory;

/**
 * Connects to a git repository, providing a way to invoke commands upon it
 *
 * @author cristiano
 */
public class GitConnector {

    private static final String GIT_DIR = ".git";
    public static final String DEFAULT_REMOTE = "remote";
    public static final String REFS_REMOTES = "refs/remotes/";
    public static final String FETCH_REFS_HEADS = "+refs/heads/*";
    public static final String DEFAULT_ORIGIN = "origin";
    public static final String CONFIG_FILE = "config";
    public static final String REFS_DEFAULT_BRANCH = "master";
    public static final String KEY_FETCH = "fetch";
    public static final String KEY_URL = "url";
    private Repository repository;
    private Git git;
    private String id;

    /**
     * Gets the reference to the repository connected to this connector
     *
     * @return the repository connected to this connector
     */
    public Repository getRepository() {
        return repository;
    }

    public String getId() {
        return id;
    }

    /**
     * Constructor that connects this class to a repository located at the
     * specified path.
     *
     * @param path location of the repository to connect with
     * @throws IOException
     */
    public GitConnector(String path, String name) throws VCSException {
        LoggerFactory.getLogger(GitConnector.class).trace("GitConnector constructor -> Entry.");
        try {
            repository = new FileRepositoryBuilder().setGitDir(new File(checkRepositoryPathName(path))).readEnvironment() // scan environment GIT_* variables
                    .findGitDir() // scan up the file system tree
                    .build();
            git = new Git(repository);
            this.id = name;
        } catch (IOException ex) {
            LoggerFactory.getLogger(GitConnector.class)
                    .error("Error initializing a GitConnector", ex);
            throw new VCSException("Error initializing a git repository.", ex);
        }
        LoggerFactory.getLogger(GitConnector.class).trace("GitConnector constructor -> Exit.");
    }

    /**
     * Checks if the specified path is a valid repository.
     *
     * @param path location to check if it is a valid repository
     * @return true if the specified path is a valid repository and false if
     * it's not.
     */
    public static boolean isValidRepository(String path) {
        LoggerFactory.getLogger(GitConnector.class).trace("isValidRepository -> Entry.");
        LoggerFactory.getLogger(GitConnector.class).debug("Checking if <{}> is a valid repository path.", path);
        boolean result;
        File file = new File(path);
        if (!file.exists()) {
            result = false;
            LoggerFactory.getLogger(GitConnector.class).debug("Path <{}> IS NOT a valid repository.", path);
        } else {
            try {
                result = new FileRepository(checkRepositoryPathName(path)).getObjectDatabase().exists();
                LoggerFactory.getLogger(GitConnector.class).debug("Path <{}> IS a valid repository.", path);
            } catch (Throwable t) {
                result = false;
            }
        }
        LoggerFactory.getLogger(GitConnector.class).trace("isValidRepository -> Exit.");
        return result;
    }

    /**
     * Constructor that connects this class to the specified repository.
     *
     * @param rep repository to connect to
     */
    public GitConnector(Repository rep, String name) {
        LoggerFactory.getLogger(GitConnector.class).trace("GitConnector constructor  -> Entry.");
        repository = rep;
        git = new Git(repository);
        this.id = name;
        LoggerFactory.getLogger(GitConnector.class).trace("GitConnector constructor  -> Exit.");
    }

    /**
     * Returns the names of the configured remote repositories for the connected
     * repository.
     *
     * @return the list of known remotes for the connected repository
     */
    public Set<String> getRemoteNames() {
        LoggerFactory.getLogger(GitConnector.class).trace("getRemoteNames -> Entry.");
        Config storedConfig = repository.getConfig();
        Set<String> configs = storedConfig.getSubsections("remote");
        LoggerFactory.getLogger(GitConnector.class).trace("getRemoteNames -> Exit.");
        return configs;
    }

    /**
     * Returns the remote configurations for the connected repository.
     *
     * @return the list of remote configurations for the connected repository
     * @throws URISyntaxException
     */
    public List<RemoteConfig> getRemoteConfigs() throws URISyntaxException {
        LoggerFactory.getLogger(GitConnector.class).trace("getRemoteConfigs -> Entry.");
        List<RemoteConfig> remoteConfigs = RemoteConfig.getAllRemoteConfigs(repository.getConfig());
        LoggerFactory.getLogger(GitConnector.class).trace("getRemoteConfigs -> Exit.");
        return remoteConfigs;
    }

    /**
     * Gets the remote name for the specified branch
     *
     * @param branchName branch to get the remote name
     * @return
     */
    public String getRemoteForBranch(String branchName) {
        LoggerFactory.getLogger(GitConnector.class).trace("getRemoteForBranch -> Entry.");
        String remoteName = repository.getConfig().getString(
                "branch", branchName, "remote");
        if (remoteName == null) {
            remoteName = "origin";
        }

        LoggerFactory.getLogger(GitConnector.class).trace("getRemoteForBranch -> Exit.");
        return remoteName;
    }

    /**
     * Returns the url of a existing remote repository
     *
     * @return the url of the repository specified by the giving remoteName
     */
    public String getRemoteUrl(String remoteName) {
        LoggerFactory.getLogger(GitConnector.class).trace("getRemoteUrl -> Entry.");
        Config storedConfig = repository.getConfig();
        String remoteUrl = storedConfig.getString("remote", remoteName, "url");
        LoggerFactory.getLogger(GitConnector.class).trace("getRemoteUrl -> Exit.");
        return remoteUrl;
    }

    /**
     * Returns branches with remote tracking configuration
     *
     * @return
     */
    public List<TrackedBranch> getTrackedBranches() {
        LoggerFactory.getLogger(GitConnector.class).trace("getTrackedBranches -> Entry.");
        List<TrackedBranch> result = new ArrayList<TrackedBranch>();
        Config storedConfig = repository.getConfig();
        Set<String> trackedBranches = storedConfig.getSubsections("branch");
        for (Iterator<String> it = trackedBranches.iterator(); it.hasNext();) {
            String branchName = it.next();
            BranchConfig branchCfg = new BranchConfig(storedConfig, branchName);
            TrackedBranch trackedBranch = new TrackedBranch();
            trackedBranch.setName(branchName);
            trackedBranch.setRemoteName(getRemoteForBranch(branchName));
            trackedBranch.setMergeSpec(branchCfg.getRemoteTrackingBranch());

            result.add(trackedBranch);

        }
        LoggerFactory.getLogger(GitConnector.class).trace("getTrackedBranches -> Exit.");
        return result;
    }

    /**
     * Returns list of local branches
     *
     * @return
     */
    public Set<String> getLocalBranches() {
        LoggerFactory.getLogger(GitConnector.class).trace("getLocalBranches -> Entry.");
        //TODO ver se tem a opção --no-merged via jgit
        Set<String> result = new TreeSet<String>();
        try {
            List<Ref> branchList = git.branchList().call();
            for (Iterator<Ref> it = branchList.iterator(); it.hasNext();) {
                Ref ref = it.next();
                result.add(ref.getName());


            }
        } catch (GitAPIException ex) {
            LoggerFactory.getLogger(GitConnector.class).error("Error retrieving local branches.", ex);
        }
        LoggerFactory.getLogger(GitConnector.class).trace("getLocalBranches -> Exit.");
        return result;
    }

    /**
     * Pulls modifications from the default origin to the repository this
     * connector is connected with.
     *
     * @return whether the pull was successful or not.
     * @throws GitAPIException
     */
    public PullResult pull() throws VCSException {
        LoggerFactory.getLogger(GitConnector.class).trace("pull -> Entry.");
        PullResult result = null;
        try {
            PullCommand pull = git.pull();
            result = pull.call();
            LoggerFactory.getLogger(GitConnector.class).debug("Pull finished. Result: \n {}", result.isSuccessful());
        } catch (GitAPIException ex) {
            LoggerFactory.getLogger(GitConnector.class).error("Error during fetch.", ex);
            throw new VCSException("Error during pull.", ex);
        }
        LoggerFactory.getLogger(GitConnector.class).trace("pull -> Exit.");
        return result;
    }

    /**
     * Fetches modifications from the default origin to the repository this
     * connector is connected with.
     *
     * @param pruneBranch If true, prune branches that do not exist anymore in
     * the source repository which remote name is the same as this repository.
     *
     * @throws GitAPIException
     */
    public FetchResult fetchAllRemotes(boolean pruneBranch) throws VCSException {
        LoggerFactory.getLogger(GitConnector.class).trace("fetchAllRemotes -> Entry. Repository: {}", getId());
        FetchResult result = null;
        try {
            List<RemoteConfig> remotes = getRemoteConfigs();
            LoggerFactory.getLogger(GitConnector.class).info("Found {} remotes for repository {}", remotes.size(), getId());
            for (Iterator<RemoteConfig> it = remotes.iterator(); it.hasNext();) {
                RemoteConfig remoteConfig = it.next();
                RefSpec refSpec = remoteConfig.getFetchRefSpecs().get(0);
                URIish urish = remoteConfig.getURIs().get(0);
                boolean prune = pruneBranch && (remoteConfig.getName().equals(getId()));
                result = fetch(urish, refSpec, pruneBranch);
            }
        } catch (URISyntaxException ex) {
            LoggerFactory.getLogger(GitConnector.class).error("Error when fetching all remotes.", ex);
            throw new VCSException("Error fetching all remotes.", ex);
        }
        LoggerFactory.getLogger(GitConnector.class).trace("fetchAllRemotes -> Exit. Repository: {}", getId());
        return result;
    }

    /**
     * Fetches modifications from the specified urish to the repository this
     * connector is connected with.
     *
     * @param urish urish representing the remote address
     * @param refSpec the ref spec to use in fetch command
     * @param pruneBranch If true, prunes branches that does not exist in remote
     * repository
     *
     * @throws GitAPIException
     */
    private FetchResult fetch(URIish urish, RefSpec refSpec, boolean pruneBranch) throws VCSException {
        LoggerFactory.getLogger(GitConnector.class).trace("fetch -> Entry. Uri: {}", urish.toString());
        FetchResult result = null;
        FetchCommand fetch = git.fetch();
        fetch.setRemote(checkRepositoryPathName(urish.toString()));
        LoggerFactory.getLogger(GitConnector.class).debug("Fetch url: {}", fetch.getRemote());
        fetch.setRefSpecs(refSpec);
        fetch.setRemoveDeletedRefs(pruneBranch);
        try {
            result = fetch.call();
            StringBuilder msg = new StringBuilder();
            for (TrackingRefUpdate update : result.getTrackingRefUpdates()) {
                msg.append("\n").append(update.getRemoteName())
                        .append(" : ").append(update.getResult());
            }
            LoggerFactory.getLogger(GitConnector.class).debug("Fetch from {} finished. Result:{}", urish.toString(), msg.toString());
        } catch (GitAPIException ex) {
            LoggerFactory.getLogger(GitConnector.class).error("Error during fetch from ." + urish.toString(), ex);
            throw new VCSException("Error during fetch from ." + urish.toString(), ex);
        }
        LoggerFactory.getLogger(GitConnector.class).trace("fetch -> Exit. Uri: {}", urish.toString());
        return result;
    }

    /**
     * Pushes modifications from the repository this connector is connected with
     * to its default origin
     *
     * @throws GitAPIException
     */
    public void push() throws GitAPIException {
        LoggerFactory.getLogger(GitConnector.class).trace("push -> Entry.");
        PushCommand push = git.push();
        Iterator<PushResult> it = push.call().iterator();
        LoggerFactory.getLogger(GitConnector.class).trace("push -> Exit.");
    }

    /**
     * Commits all pending modifications in the repository connected to this
     * connector.
     *
     * @param message commit message
     * @throws GitAPIException
     */
    public void commit(String message) throws GitAPIException {
        LoggerFactory.getLogger(GitConnector.class).trace("commit -> Entry.");
        CommitCommand commit = git.commit();
        commit.setAll(true);
        commit.setMessage(message);
        commit.call();
        LoggerFactory.getLogger(GitConnector.class).trace("commit -> Exit.");
    }

    /**
     * Gets the reference string for a specified branch in the specified remote
     * name.
     *
     * @param branchName branch to retrieve ref for
     * @param remoteName remote name of the repository to look branch ref
     * @return the required reference object.
     */
    public Ref getBranchRemoteRef(String branchName, String remoteName) throws VCSException {
        LoggerFactory.getLogger(GitConnector.class).trace("getBranchRemoteRef -> Entry.");
        Ref ref = null;
        try {
            String refToFind = REFS_REMOTES + remoteName + "/" + branchName;
            ref = repository.getRef(refToFind);
        } catch (IOException ex) {
            LoggerFactory.getLogger(GitConnector.class).error("Error getting remote ref for branch "
                    + branchName + " in repository " + remoteName, ex);
            throw new VCSException("Error getting remote ref for branch "
                    + branchName + " in repository " + remoteName);
        }
        LoggerFactory.getLogger(GitConnector.class).trace("getOriginRefFromSource -> Exit.");
        return ref;
    }

    public MergeResult merge(Ref anyRef) throws VCSException {
        LoggerFactory.getLogger(GitConnector.class).trace("merge -> Entry.");
        MergeResult result = null;
        if (anyRef == null) {
            LoggerFactory.getLogger(GitConnector.class).debug("Reference is null. Merge will not be executed");
        } else {
            try {
                MergeCommand merge = git.merge();
                merge.setStrategy(MergeStrategy.RESOLVE);
                merge.include(anyRef);
                result = merge.call();
                if (!result.getMergeStatus().isSuccessful()) {
                    StringBuilder msg = new StringBuilder();
                    msg.append("Error merging repository ").append(getId())
                            .append(". MergeStatus: ").append(result.getMergeStatus())
                            .append("\nFailing paths:");

                    Map<String, MergeFailureReason> failingPaths = result.getFailingPaths();
                    for (Map.Entry<String, MergeFailureReason> entry : failingPaths.entrySet()) {
                        String path = entry.getKey();
                        MergeFailureReason mergeFailureReason = entry.getValue();
                        msg.append("\n\t").append(path).append(". Reason: ")
                                .append(mergeFailureReason);
                    }
                    LoggerFactory.getLogger(GitConnector.class).warn(msg.toString());
                }
            } catch (GitAPIException ex) {
                LoggerFactory.getLogger(GitConnector.class).error("Error during merge.", ex);
                throw new VCSException("Error during merge.", ex);
            }
        }
        LoggerFactory.getLogger(GitConnector.class).trace("merge -> Exit.");
        return result;
    }

    /**
     * Creates a new repository at the specified path
     *
     * @param path where the new repository will be created
     * @throws IOException
     */
    public Repository createRepository(String path) throws IOException {
        LoggerFactory.getLogger(GitConnector.class).trace("createRepository -> Entry.");
        Repository fileRepo = new FileRepository(checkRepositoryPathName(path));
        fileRepo.create();
        LoggerFactory.getLogger(GitConnector.class).trace("createRepository -> Exit.");
        return fileRepo;
    }

    /**
     * Clone the source repository to the target path
     *
     * @param source address of the repository to be cloned
     * @param target path to clone the repository to
     * @return a connector to the clone of this repository
     * @throws GitAPIException
     */
    public GitConnector cloneRepository(String source, File target, String id) throws VCSException {
        LoggerFactory.getLogger(GitConnector.class).trace("cloneRepository -> Entry.");
        CloneCommand cloneCmd = Git.cloneRepository();
        cloneCmd.setURI(source);
        cloneCmd.setDirectory(target);
        cloneCmd.setCloneAllBranches(true);
        cloneCmd.setCloneSubmodules(true);
        Git result = null;
        try {
            result = cloneCmd.call();
        } catch (GitAPIException ex) {
            LoggerFactory.getLogger(GitConnector.class).error("Error cloning {} to {}.", source, target.getAbsolutePath());
            LoggerFactory.getLogger(GitConnector.class).error("", ex);
            throw new VCSException("Error cloning repository.", ex);
        }

        GitConnector cloneConnector = new GitConnector(result.getRepository(), id);
        LoggerFactory.getLogger(GitConnector.class).trace("cloneRepository -> Exit.");
        return cloneConnector;
    }

    /**
     * Method that allows to clone a repository with target represented by a
     * String
     *
     * @param source address of the repository to be cloned
     * @param target path to clone the repository to
     * @return a connector to the clone of this repository
     * @see #cloneRepository(java.lang.String, java.io.File)
     * @throws GitAPIException
     */
    public GitConnector cloneRepository(String source, String target, String id) throws VCSException {
        LoggerFactory.getLogger(GitConnector.class).trace("cloneRepository -> Entry.");
        GitConnector result = cloneRepository(source, new File(target), id);
        LoggerFactory.getLogger(GitConnector.class).trace("cloneRepository -> Exit.");
        return result;
    }

    /**
     * Method that allows to clone this repository to target represented by a
     * String
     *
     * @param target path to clone the repository to
     * @return a connector to the clone of this repository
     * @see #cloneRepository(java.lang.String, java.io.File)
     * @throws GitAPIException
     */
    public GitConnector cloneThis(String target) throws VCSException {
        LoggerFactory.getLogger(GitConnector.class).trace("cloneThis -> Entry.");
        GitConnector result = cloneRepository(getPath(), new File(target), this.getId());
        LoggerFactory.getLogger(GitConnector.class).trace("cloneThis -> Exit.");
        return result;
    }

    public void close() {
        LoggerFactory.getLogger(GitConnector.class).trace("close -> Entry.");
        LoggerFactory.getLogger(GitConnector.class).debug("Closing connection with repository <{}>.", getId());
        repository.close();
        LoggerFactory.getLogger(GitConnector.class).trace("close -> Exit.");
    }

    public List<BranchStatus> testAhead() throws VCSException {
        LoggerFactory.getLogger(GitConnector.class).trace("testAhead -> Entry.");
        List<BranchStatus> result = new ArrayList<BranchStatus>();
        List<TrackedBranch> branches = getTrackedBranches();
        if (branches == null) {
            LoggerFactory.getLogger(GitConnector.class).error("There are no tracked branches for repository <{}>, which is located at <{}>.", getId(), getPath());
            throw new VCSException("There are no tracked branches for repository <" + getId()
                    + ">, which is located at <" + getPath() + ">.");
        }
        for (Iterator<TrackedBranch> it = branches.iterator(); it.hasNext();) {
            TrackedBranch trackedBranch = it.next();
            BranchStatus relationship = new BranchStatus();
            relationship.setRepositoryBranch(trackedBranch.getName());
            relationship.setRepositoryUrl(getPath());
            relationship.setReferencedRemote(trackedBranch.getRemoteName());
            relationship.setMergeSpec(trackedBranch.getMergeSpec());
            relationship.setReferencedRepositoryUrl(getRemoteUrl(trackedBranch.getRemoteName()));
            try {
                WorkingRepositoryBranchStatus status = WorkingRepositoryBranchStatus.of(this, trackedBranch.getName());

                if (status != null) {
                    relationship.setAhead(status.getAheadCount());
                    relationship.setBehind(status.getBehindCount());
                } else {
                    LoggerFactory.getLogger(GitConnector.class).warn("cannot testAhead branch <{}> declared in repository <{}>.",
                            trackedBranch.getName(), getId());
                }

            } catch (IOException ex) {
                LoggerFactory.getLogger(GitConnector.class)
                        .error("Error calculating ahead count.", ex);
                throw new VCSException(
                        "Error calculating ahead count.", ex);
            }

            result.add(relationship);
        }
        LoggerFactory.getLogger(GitConnector.class).trace("testAhead -> Exit.");
        return result;
    }

    /**
     * Gets an iterator with all commits that happened to this repository, in 
     * all branches, whether they are local or remote.
     *
     * @return the commit history
     */
    public Iterator<RevCommit> getAllCommitsIterator() {
        LoggerFactory.getLogger(GitConnector.class).trace("getAllCommitsIterator -> Entry.");
        Iterator<RevCommit> result = new TreeSet<RevCommit>().iterator();
        try {
            LogCommand logcmd = git.log();
            Map<String, Ref> mapRefsHeads = repository.getRefDatabase().getRefs(IConstants.REFS_HEADS);
            Map<String, Ref> mapRefsRemotes = repository.getRefDatabase().getRefs(IConstants.REFS_REMOTES);
            
            for (Map.Entry<String, Ref> entry : mapRefsHeads.entrySet()) {
                Ref ref = entry.getValue();
                logcmd.add(ref.getObjectId());
            }
            
            for (Map.Entry<String, Ref> entry : mapRefsRemotes.entrySet()) {
                Ref ref = entry.getValue();
                logcmd.add(ref.getObjectId());
            }
            
            result = logcmd.call().iterator();

        } catch (Exception ex) {
            LoggerFactory.getLogger(GitConnector.class).error("Error in getAllCommitsIterator.", ex);
        }
        LoggerFactory.getLogger(GitConnector.class).trace("getAllCommitsIterator -> Exit.");
        return result;
    }

    /**
     * Returns the path to this repository
     *
     * @return path to this repository
     */
    public String getPath() {
        LoggerFactory.getLogger(GitConnector.class).trace("getPath -> Entry.");

        String path = repository.getDirectory().getAbsolutePath();
        LoggerFactory.getLogger(GitConnector.class).trace("getPath -> Exit.");
        return path;
    }

    /**
     * Checks if specified path ends with "/.git" and appends it if it doesn't
     *
     * @param path the path to be checked
     * @return a path to a git repository
     */
    public static String checkRepositoryPathName(String path) {
        LoggerFactory.getLogger(GitConnector.class).trace("checkRepositoryPathName -> Entry.");

        String gitPath = path;
        
        if (!path.endsWith(GIT_DIR)) {
            if (path.startsWith("http")) {
                gitPath = path + GIT_DIR;
            } else if (new File(path + "/" + GIT_DIR).exists()) gitPath = path + "/" + GIT_DIR;
        }
        LoggerFactory.getLogger(GitConnector.class).trace("checkRepositoryPathName -> Exit.");
        return gitPath;
    }

    public String getRepositoryPath() {
        LoggerFactory.getLogger(GitConnector.class).trace("getRepositoryPath -> Entry.");
        String gitPath = checkRepositoryPathName(getPath());
        LoggerFactory.getLogger(GitConnector.class).trace("getRepositoryPath -> Exit.");
        return gitPath;
    }
    
    /**
     * Returns an object id for a given revision string.
     *
     */
    public ObjectId resolve(String revisionString) {
        ObjectId result = null;
        try {
            result = getRepository().resolve(revisionString);
        } catch (Exception ex) {
            LoggerFactory.getLogger(GitConnector.class).error("Error resolving a reference to " + revisionString, ex);
        }
        return result;
    }
}