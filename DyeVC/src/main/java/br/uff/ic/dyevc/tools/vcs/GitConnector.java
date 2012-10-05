package br.uff.ic.dyevc.tools.vcs;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevWalkUtils;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;

/**
 * Connects to a git repository, providing a way to invoke commands upon it
 *
 * @author cristiano
 */
public class GitConnector {

    private Repository repository;
    private Git git;

    /**
     * Gets the reference to the repository connected to this connector
     *
     * @return the repository connected to this connector
     */
    public Repository getRepository() {
        return repository;
    }

    /**
     * Sets a new repository and connects this connector to it
     *
     * @param repository the new repository to be connected to
     */
    public void setRepository(Repository repository) {
        this.repository = repository;
        git = new Git(this.repository);
    }

    /**
     * Constructor that connects this class to a repository located at the
     * specified path.
     *
     * @param path location of the repository to connect with
     * @throws IOException
     */
    public GitConnector(String path) throws IOException {
        repository = new FileRepositoryBuilder().setGitDir(new File(path + "/.git")).readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build();
        git = new Git(repository);
    }

    public HashMap<String, String> getRemotes() {
        HashMap<String, String> result = new HashMap<String, String>();
        Config storedConfig = repository.getConfig();
        Set<String> remotes = storedConfig.getSubsections("remote");

        System.out.println("Known remotes:");
        for (String remoteName : remotes) {
            String url = storedConfig.getString("remote", remoteName, "url");
            System.out.println("\t" + remoteName + " " + url);
            result.put(remoteName, url);
        }
        return result;
    }

    public HashMap<String, String> getBranches() {
        HashMap<String, String> result = new HashMap<String, String>();
        Config storedConfig = repository.getConfig();
        Set<String> branches = storedConfig.getSubsections("branch");
        System.out.println("Known branches:");
        for (String branchName : branches) {
            String merge = storedConfig.getString("branch", branchName, "merge");
            System.out.println("\t" + branchName + " " + merge);
            result.put(branchName, merge);
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
        PullResult result = pull.call();
        Logger.getLogger(GitConnectorTest.class.getName()).log(Level.INFO, result.toString());

        return result.isSuccessful();
    }

    /**
     * Pushes modifications from the repository this connector is connected with
     * to its default origin
     *
     * @throws GitAPIException
     */
    public void push() throws GitAPIException {
        PushCommand push = git.push();
        Iterator<PushResult> it = push.call().iterator();
        if (it.hasNext()) {
            System.out.println(it.next().toString());
        }
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
        Repository fileRepo = new FileRepository(path + "/.git");
        fileRepo.create();
        return fileRepo;
    }

    /**
     * Clone the source repository to the target path
     *
     * @param source address of the repository to be cloned
     * @param target path to clone the repository to
     * @return the clone of the source repository
     * @throws GitAPIException
     */
    public Repository cloneRepository(String source, File target) throws GitAPIException {
        CloneCommand cloneCmd = Git.cloneRepository().setURI(source).setDirectory(target);
        Git result = cloneCmd.call();
        return result.getRepository();
    }

    /**
     * Method that allows to clone a repository with target represented by a
     * String
     *
     * @param source address of the repository to be cloned
     * @param target path to clone the repository to
     * @return the clone of the source repository
     * @see #cloneRepository(java.lang.String, java.io.File)
     * @throws GitAPIException
     */
    public Repository cloneRepository(String source, String target) throws GitAPIException {
        return cloneRepository(source, new File(target));
    }
}