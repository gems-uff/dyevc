package br.uff.ic.dyevc.tools.vcs;

import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.model.BranchStatus;
import br.uff.ic.dyevc.model.git.TrackedBranch;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ResolveMerger.MergeFailureReason;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
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
    public static final String DEFAULT_ORIGIN = "origin";
    public static final String CONFIG_FILE = "config";
    public static final String REFS_DEFAULT_BRANCH = "master";
    public static final String COMMAND_FETCH = "fetch";
    
    private Repository repository;
    private Git git;
    private String id;
    private UsernamePasswordCredentialsProvider credentialsProvider;
    private String user;
    private String password;

    /**
     * Gets the reference to the repository connected to this connector
     *
     * @return the repository connected to this connector
     */
    public Repository getRepository() {
        return repository;
    }

    /**
     * Constructor that connects this class to a repository located at the
     * specified path.
     *
     * @param path location of the repository to connect with
     * @throws IOException
     */
    public GitConnector(String path, String name) throws VCSException {
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
    }

    /**
     * Checks if the specified path is a valid repository.
     *
     * @param path location to check if it is a valid repository
     * @return true if the specified path is a valid repository and false if
     * it's not.
     */
    public static boolean isValidRepository(String path) {
        boolean result;
        File file = new File(path);
        if (!file.exists()) {
            result = false;
        } else {
            try {
                result = new FileRepository(checkRepositoryPathName(path)).getObjectDatabase().exists();
            } catch (IOException e) {
                result = false;
            }
        }
        return result;
    }

    /**
     * Constructor that connects this class to the specified repository.
     *
     * @param rep repository to connect to
     */
    public GitConnector(Repository rep, String name) {
        repository = rep;
        git = new Git(repository);
        this.id = name;
    }

    /**
     * Returns the names of the configured remote repositories for the connected
     * repository.
     *
     * @return the list of known remotes for the connected repository
     */
    public Set<String> getRemoteNames() {
        Config storedConfig = repository.getConfig();
        return storedConfig.getSubsections("remote");
    }

    /**
     * Returns the remote configurations for the connected repository.
     *
     * @return the list of remote configurations for the connected repository
     * @throws URISyntaxException
     */
    public List<RemoteConfig> getRemoteConfigs() throws URISyntaxException {
        return RemoteConfig.getAllRemoteConfigs(repository.getConfig());
    }

    /**
     * Gets the remote name for the specified branch
     *
     * @param branchName branch to get the remote name
     * @return
     */
    public String getRemoteForBranch(String branchName) {
        String remoteName = repository.getConfig().getString(
                "branch", branchName, "remote");
        if (remoteName == null) {
            return "origin";
        } else {
            return remoteName;
        }
    }

    /**
     * Returns the url of a existing remote repository
     *
     * @return the url of the repository specified by the giving remoteName
     */
    public String getRemoteUrl(String remoteName) {
        Config storedConfig = repository.getConfig();
        return storedConfig.getString("remote", remoteName, "url");
    }

    /**
     * Returns branches with remote tracking configuration
     *
     * @return
     */
    public List<TrackedBranch> getTrackedBranches() {
        List<TrackedBranch> result = new ArrayList<TrackedBranch>();
        Config storedConfig = repository.getConfig();
        Set<String> trackedBranches = storedConfig.getSubsections("branch");
        for (Iterator<String> it = trackedBranches.iterator(); it.hasNext();) {
            String branchName = it.next();
            TrackedBranch trackedBranch = new TrackedBranch();
            trackedBranch.setName(branchName);
            trackedBranch.setRemoteName(getRemoteForBranch(branchName));

            result.add(trackedBranch);

        }
        return result;
    }

    /**
     * Returns list of local branches
     *
     * @return
     */
    public Set<String> getLocalBranches() {
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
            if (credentialsProvider != null) {
                pull.setCredentialsProvider(credentialsProvider);
            }
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
     * @throws GitAPIException
     */
    public FetchResult fetchAllRemotes() throws VCSException {
        LoggerFactory.getLogger(GitConnector.class).trace("fetchAllRemotes -> Entry. Repository: {}", getId());
        FetchResult result = null;
        try {
            List<RemoteConfig> remotes = getRemoteConfigs();
            LoggerFactory.getLogger(GitConnector.class).info("Found {} remotes for repository {}", remotes.size(), getId());
            for (Iterator<RemoteConfig> it = remotes.iterator(); it.hasNext();) {
                RemoteConfig remoteConfig = it.next();
                RefSpec refSpec = remoteConfig.getFetchRefSpecs().get(0);
                URIish urish = remoteConfig.getURIs().get(0);
                result = fetch(urish, refSpec);
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
     *
     * @throws GitAPIException
     */
    public FetchResult fetch(URIish urish, RefSpec refSpec) throws VCSException {
        LoggerFactory.getLogger(GitConnector.class).trace("fetch -> Entry. Uri: {}", urish.toString());
        FetchResult result = null;
        FetchCommand fetch = git.fetch();
        if ((credentialsProvider != null) && (urish.getScheme() != null) 
                && (urish.getHost() != null)) {
            LoggerFactory.getLogger(GitConnector.class).trace("Repository needs authentication. Setting credentials.");
            fetch.setCredentialsProvider(credentialsProvider);
            urish = urish.setUser(user);
            urish = urish.setPass(password);
        }
        fetch.setRemote(checkRepositoryPathName(urish.toPrivateString()));
        fetch.setRefSpecs(refSpec);
        try {
            result = fetch.call();
            LoggerFactory.getLogger(GitConnector.class).debug("Fetch finished. Result: \n {}", result.getMessages());
        } catch (GitAPIException ex) {
            LoggerFactory.getLogger(GitConnector.class).error("Error during fetch.", ex);
            throw new VCSException("Error during fetch.", ex);
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
        PushCommand push = git.push();
        if (credentialsProvider != null) {
            push.setCredentialsProvider(credentialsProvider);
        }
        Iterator<PushResult> it = push.call().iterator();
    }

    /**
     * Commits all pending modifications in the repository connected to this
     * connector.
     *
     * @param message commit message
     * @throws GitAPIException
     */
    public void commit(String message) throws GitAPIException {
        CommitCommand commit = git.commit();
        commit.setAll(true);
        commit.setMessage(message);
        commit.call();
    }

    /**
     * Gets the reference string for the remote branch from where this
     * repository was cloned.
     *
     * @return the required reference object.
     */
    public Ref getOriginRefFromSource() throws VCSException {
        LoggerFactory.getLogger(GitConnector.class).trace("getOriginRefFromSource -> Entry.");
        Ref ref = null;
        try {
            String refToFind = REFS_REMOTES + getId() + "/" + REFS_DEFAULT_BRANCH;
            ref = repository.getRef(refToFind);
        } catch (IOException ex) {
            LoggerFactory.getLogger(GitConnector.class).error("Error getting remote ref for repository.", ex);
            throw new VCSException("Error getting remote ref for repository " + getId());
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
        Repository fileRepo = new FileRepository(checkRepositoryPathName(path));
        fileRepo.create();
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
        CloneCommand cloneCmd = Git.cloneRepository().setURI(source).setDirectory(target);
        if (credentialsProvider != null) {
            cloneCmd.setCredentialsProvider(credentialsProvider);
        }
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

        return new GitConnector(result.getRepository(), id);
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
        return cloneRepository(source, new File(target), id);
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
        return cloneRepository(getPath(), new File(target), this.getId());
    }

    public void close() {
        repository.close();
    }

    public List<BranchStatus> testAhead() throws VCSException {
        //TODO não está funcionando direito
        List<BranchStatus> result = new ArrayList<BranchStatus>();
        List<TrackedBranch> branches = getTrackedBranches();
        for (Iterator<TrackedBranch> it = branches.iterator(); it.hasNext();) {
            try {
                TrackedBranch trackedBranch = it.next();
                BranchTrackingStatus status = BranchTrackingStatus.of(repository, trackedBranch.getName());

                BranchStatus relationship = new BranchStatus();
                relationship.setAhead(status.getAheadCount());
                relationship.setBehind(status.getBehindCount());
                relationship.setRepositoryBranch(trackedBranch.getName());
                relationship.setRepositoryUrl(getPath());
                relationship.setReferencedRepositoryBranch(trackedBranch.getRemoteName());
                relationship.setReferencedRepositoryUrl(getRemoteUrl(trackedBranch.getRemoteName()));

                result.add(relationship);

            } catch (IOException ex) {
                LoggerFactory.getLogger(GitConnector.class)
                        .error("Error calculating ahead count.", ex);
                throw new VCSException(
                        "Error calculating ahead count.", ex);
            }
        }
        return result;
    }

    public void testRevCommit() {
        //TODO teste, pega mensagens de log de todos os commits
        try {
            RevWalk walk = new RevWalk(repository);

            RevCommit commit = null;

            // Add all files
            // AddCommand add = git.add();
            // add.addFilepattern(".").call();

            // Commit them
            // CommitCommand commit = git.commit();
            // commit.setMessage("Commiting from java").call();

            Iterable<RevCommit> logs = git.log().call();
            Iterator<RevCommit> i = logs.iterator();

            while (i.hasNext()) {
                commit = walk.parseCommit(i.next());
                System.out.print(commit.abbreviate(10).name());
                System.out.print(" " + new Date(commit.getCommitTime() * 1000L));
                System.out.println(" " + commit.getCommitterIdent().getName());
                System.out.println("\t" + commit.getShortMessage());
            }
        } catch (Exception ex) {
            LoggerFactory.getLogger(GitConnector.class).error("Error in testRevCommit.", ex);
        }

    }

    /**
     * Returns the path to this repository
     *
     * @return path to this repository
     */
    public String getPath() {

        return repository.getDirectory().getAbsolutePath();
    }

    /**
     * Checks if specified path ends with "/.git" and appends it if it doesn't
     *
     * @param path the path to be checked
     * @return a path to a git repository
     */
    public static String checkRepositoryPathName(String path) {

        return (path.endsWith(GIT_DIR))
                ? path
                : (path.startsWith("http") ? path + GIT_DIR : path + "/" + GIT_DIR);
    }

    public String getRepositoryPath() {
        return checkRepositoryPathName(getPath());
    }

    public String getId() {
        return id;
    }

    public void setCredentials(String user, String password) {
        credentialsProvider = new UsernamePasswordCredentialsProvider(user, password);
        this.user = user;
        this.password = password;
    }
}