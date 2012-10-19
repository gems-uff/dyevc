package br.uff.ic.dyevc.tools.vcs;

import br.uff.ic.dyevc.model.RepositoryRelationship;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
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
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.BranchConfig;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;

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

    /**
     * Returns branches with remote tracking configuration
     *
     * @return
     */
    public Set<String> getBranchesFromRemote() {
        Config storedConfig = repository.getConfig();
        return storedConfig.getSubsections("branch");
    }

    /**
     * Returns list of local branches
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
        PullResult result = pull.call();
        Logger
                .getLogger(GitConnectorTest.class
                .getName()).log(Level.INFO, result.toString());

        return result.isSuccessful();
    }

    /**
     * Fetches modifications from the default origin to the repository this
     * connector is connected with.
     *
     * @throws GitAPIException
     */
    public void fetch() throws GitAPIException {
        git.fetch().call();
    }

    /**
     * Fetches modifications from the specified remoteAddress origin to the repository this
     * connector is connected with.
     * @param remoteAddress Address to fetch from
     * @param refSpec the ref specs to use in fetch command
     *
     * @throws GitAPIException
     */
    public void fetch(String remoteAddress, String refSpec) throws GitAPIException {
        FetchCommand fetch = git.fetch();
        fetch.setRemote(remoteAddress);
        fetch.setRefSpecs(new RefSpec(refSpec));
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

    public List<RepositoryRelationship> testAhead() throws IOException {
        //TODO não está funcionando direito
        List<RepositoryRelationship> result = Collections.EMPTY_LIST;
        Set<String> branches = getBranchesFromRemote();
        for (Iterator<String> it = branches.iterator(); it.hasNext();) {
            String string = it.next();
            BranchTrackingStatus status = BranchTrackingStatus.of(repository, string);
            System.out.println(repository.getDirectory().getAbsolutePath());
            System.out.printf("Branch: %s \tRemote: %s\tAhead: %d\tBehind: %d\n\n", string, status.getRemoteTrackingBranch(), status.getAheadCount(), status.getBehindCount());
            RepositoryRelationship relationship = new RepositoryRelationship();
            relationship.setAhead(status.getAheadCount());
            relationship.setBehind(status.getBehindCount());
            br.uff.ic.dyevc.model.MonitoredRepository target = new br.uff.ic.dyevc.model.MonitoredRepository();
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
}