package br.uff.ic.dyevc.tools.vcs;

import br.uff.ic.dyevc.model.git.TrackedBranch;
import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.model.RepositoryStatus;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * Connects to a git repository, providing a way to invoke commands upon it
 *
 * @author cristiano
 */
public class GitConnector {

    private static final String GIT_DIR = ".git";
    private Repository repository;
    private Git git;
    private String name;
    private UsernamePasswordCredentialsProvider credentialsProvider;

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
            repository = new FileRepositoryBuilder().setGitDir(new File(getGitPath(path))).readEnvironment() // scan environment GIT_* variables
                    .findGitDir() // scan up the file system tree
                    .build();
            git = new Git(repository);
            this.name = name;
        } catch (IOException ex) {
            Logger.getLogger(GitConnector.class.getName()).log(Level.SEVERE, null, ex);
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
        try {
            result = new FileRepository(getGitPath(path)).getObjectDatabase().exists();
        } catch (IOException e) {
            result = false;
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
        this.name = name;
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
     * Gets the remote name for the specified branch
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
            //            System.out.println("Known branches:");
            //            for (String branchName : branches) {
            //                String merge = storedConfig.getString("branch", branchName, "merge");
            //                System.out.println("\t" + branchName + " " + merge);
            //                result.put(branchName, merge);
            //            }
        } catch (GitAPIException ex) {
            Logger.getLogger(GitConnector.class.getName()).log(Level.SEVERE, null, ex);
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
    public boolean pull() throws GitAPIException {
        PullCommand pull = git.pull();
        if (credentialsProvider != null) {
            pull.setCredentialsProvider(credentialsProvider);
        }
        PullResult result = pull.call();
        return result.isSuccessful();
    }

    /**
     * Fetches modifications from the default origin to the repository this
     * connector is connected with.
     *
     * @throws GitAPIException
     */
    public void fetch() throws GitAPIException {
        FetchCommand fetch = git.fetch();
        if (credentialsProvider != null) {
            fetch.setCredentialsProvider(credentialsProvider);
        }
        fetch.call();
    }

    /**
     * Fetches modifications from the specified remoteAddress origin to the
     * repository this connector is connected with.
     *
     * @param remoteAddress Address to fetch from
     * @param refSpec the ref specs to use in fetch command
     *
     * @throws GitAPIException
     */
    public void fetch(String remoteAddress, String refSpec) throws GitAPIException {
        remoteAddress = getGitPath(remoteAddress);
        FetchCommand fetch = git.fetch();
        fetch.setRemote(remoteAddress);
        fetch.setRefSpecs(new RefSpec(refSpec));
        if (credentialsProvider != null) {
            fetch.setCredentialsProvider(credentialsProvider);
        }
        fetch.call();
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
     * Creates a new repository at the specified path
     *
     * @param path where the new repository will be created
     * @throws IOException
     */
    public Repository createRepository(String path) throws IOException {
        Repository fileRepo = new FileRepository(getGitPath(path));
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
    public GitConnector cloneRepository(String source, File target, String id) throws GitAPIException {
        CloneCommand cloneCmd = Git.cloneRepository().setURI(source).setDirectory(target);
        if (credentialsProvider != null) {
            cloneCmd.setCredentialsProvider(credentialsProvider);
        }
        cloneCmd.setCloneAllBranches(true);
        cloneCmd.setCloneSubmodules(true);
        Git result = cloneCmd.call();

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
    public GitConnector cloneRepository(String source, String target, String id) throws GitAPIException {
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
    public GitConnector cloneThis(String target) throws GitAPIException {
        return cloneRepository(repository.getDirectory().getAbsolutePath(), new File(target), this.getName());
    }

    public void close() {
        repository.close();
    }

    public List<RepositoryStatus> testAhead() throws VCSException {
        //TODO não está funcionando direito
        List<RepositoryStatus> result = new ArrayList<RepositoryStatus>();
        List<TrackedBranch> branches = getTrackedBranches();
        for (Iterator<TrackedBranch> it = branches.iterator(); it.hasNext();) {
            try {
                TrackedBranch trackedBranch = it.next();
                BranchTrackingStatus status = BranchTrackingStatus.of(repository, trackedBranch.getName());

                RepositoryStatus relationship = new RepositoryStatus();
                relationship.setAhead(status.getAheadCount());
                relationship.setBehind(status.getBehindCount());
                relationship.setRepositoryBranch(trackedBranch.getName());
                relationship.setRepositoryUrl(repository.getDirectory().getAbsolutePath());
                relationship.setReferencedRepositoryBranch(trackedBranch.getRemoteName());
                relationship.setReferencedRepositoryUrl(getRemoteUrl(trackedBranch.getRemoteName()));

                result.add(relationship);

            } catch (IOException ex) {
                Logger.getLogger(GitConnector.class
                        .getName()).log(Level.SEVERE, null, ex);
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

                System.out.println(commit.getFullMessage());





            }
        } catch (MissingObjectException ex) {
            Logger.getLogger(GitConnector.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IncorrectObjectTypeException ex) {
            Logger.getLogger(GitConnector.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GitConnector.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (GitAPIException ex) {
            Logger.getLogger(GitConnector.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Checks if specified path ends with "/.git" and appends it if it doesn't
     *
     * @param path the path to be checked
     * @return a path to a git repository
     */
    private static final String getGitPath(String path) {
        
        return (path.endsWith(GIT_DIR))
                ? path
                : (path.startsWith("http") ? path + GIT_DIR : path + "/" + GIT_DIR);
    }

    public String getName() {
        return name;
    }
    
    public void setCredentials(String user, String password) {
        credentialsProvider = new UsernamePasswordCredentialsProvider(user, password);
    }
}