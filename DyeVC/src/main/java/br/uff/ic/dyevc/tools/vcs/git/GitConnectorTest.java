package br.uff.ic.dyevc.tools.vcs.git;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.ServiceException;
import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.persistence.CommitDAO;
import br.uff.ic.dyevc.utils.DateUtil;
import br.uff.ic.dyevc.utils.PreferencesUtils;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import org.gitective.core.CommitFinder;
import org.gitective.core.filter.commit.CommitListFilter;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;

/**
 * Hello world!
 *
 */
public class GitConnectorTest {
    /**
     * Method description
     *
     *
     * @param args
     */
    public static void main(String[] args) {
        GitConnectorTest test = new GitConnectorTest();

        test.testFindNewCommits();
        test.testGetCommitHashes();
        test.testSerializeCommitsToDisk();
        test.testUpdateCommits();
    }

    private void testSerializeCommitsToDisk() {
        ObjectOutput output         = null;
        ObjectInput  input          = null;
        String       objectFilePath = null;
        try {
            MonitoredRepositories reps        = PreferencesUtils.loadMonitoredRepositories();
            MonitoredRepository   rep         = MonitoredRepositories.getMonitoredProjectById("rep1376735192420");
            GitCommitTools        tools       = GitCommitTools.getInstance(rep, false);
            List<CommitInfo>      commitInfos = tools.getCommitInfos();
            objectFilePath = rep.getWorkingCloneConnection().getPath() + IConstants.DIR_SEPARATOR + "lastRun.ser";

            OutputStream os  = new FileOutputStream(objectFilePath);
            OutputStream bos = new BufferedOutputStream(os);
            output = new ObjectOutputStream(bos);
            output.writeObject(commitInfos);
        } catch (VCSException ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            InputStream is  = new FileInputStream(objectFilePath);
            InputStream bis = new BufferedInputStream(is);
            input = new ObjectInputStream(bis);

            // deserialize the List
            List<CommitInfo> recoveredCommits = (List<CommitInfo>)input.readObject();

            // display its data
            for (CommitInfo ci : recoveredCommits) {
                System.out.println("Recovered Commit: " + ci.getHash());
            }
        } catch (IOException ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void testGetCommitHashes() {
        GitConnector sapos = null;
        try {
            sapos = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/dyevc", "dyevc");
            CommitDAO        dao           = new CommitDAO();
            Set<CommitInfo>  remoteCommits = dao.getCommitHashesByQuery(null);

            GitConnector     git           = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/dyevc", "dyevc");
            GitCommitTools   tools         = GitCommitTools.getInstance(git);
            List<CommitInfo> localCommits  = tools.getCommitInfos();

            // All elements that do not exist in both sets
            Collection<CommitInfo> testDisjunction = CollectionUtils.disjunction(localCommits, remoteCommits);

            // All elements that exist in both sets
            Collection<CommitInfo> testIntersection = CollectionUtils.intersection(localCommits, remoteCommits);

            // All elements that exists in left set, but does not exist in right set
            Collection<CommitInfo> onlyInLocal           = CollectionUtils.subtract(localCommits, remoteCommits);
            Collection<CommitInfo> onlyInRemote          = CollectionUtils.subtract(remoteCommits, localCommits);

            Predicate<CommitInfo>  commitDateGreaterThan = new Predicate<CommitInfo>() {
                Date compareDate = DateUtil.addDays(new Date(System.currentTimeMillis()), -10);

                @Override
                public boolean evaluate(CommitInfo object) {
                    return object.getCommitDate().getTime() <= compareDate.getTime();
                }
            };

            Collection<CommitInfo> greaterThanDate = CollectionUtils.select(localCommits, commitDateGreaterThan);
            System.out.println("finish");

        } catch (ServiceException ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (VCSException ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (sapos != null) {
                sapos.close();
            }
        }
    }

    private void testUpdateCommits() {
        GitConnector sapos = null;
        try {
            sapos = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/dyevc", "dyevc");
            CommitInfo commit = new CommitInfo("zzz", "rep1376735192420");
            commit.setSystemName("dyevc");
            List<CommitInfo> list = new ArrayList<CommitInfo>();
            list.add(commit);
            CommitDAO dao = new CommitDAO();
            dao.updateCommitsWithNewRepository(list, "xxx");
        } catch (DyeVCException ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (sapos != null) {
                sapos.close();
            }
        }
    }

    private void testFindNewCommits() {
        GitConnector egit = null;
        try {
            egit = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/sapos", "sapos");
            Repository       repo    = egit.getRepository();
            CommitListFilter commits = new CommitListFilter();
            CommitFinder     finder  = new CommitFinder(repo);
            finder.setFilter(commits).findBetween("0802a2f64b8d08b8de6cc80330b64f105587fb7b",
                             "0e4af846a0b31045ae6dc372fdf9061cd386a774");
            System.out.println(
                "Commits between <0802a2f64b8d08b8de6cc80330b64f105587fb7b> and <0e4af846a0b31045ae6dc372fdf9061cd386a774>");

            for (RevCommit commit : commits.getCommits()) {
                System.out.println(commit.getId());
            }

            commits.reset();
            finder.setFilter(commits).findBetween("dcc2737e5207986bc97c45cd3ac17d095a3d1842",
                             "9cc61c7dac77f9a5c98d40617cb4129ff1b439fd");
            System.out.println(
                "\n\n\nCommits between <dcc2737e5207986bc97c45cd3ac17d095a3d1842> and <9cc61c7dac77f9a5c98d40617cb4129ff1b439fd>");

            for (RevCommit commit : commits.getCommits()) {
                System.out.println(commit.getId());
            }

            commits.reset();
            ObjectId id = null;
            finder.setFilter(commits).findBetween("aa8141dd72a6391d56d9a327b5c75866aa87e081", id);
            System.out.println("\n\n\nCommits between <aa8141dd72a6391d56d9a327b5c75866aa87e081> and null");

            for (RevCommit commit : commits.getCommits()) {
                System.out.println(commit.getId());
            }

            commits.reset();
            finder.setFilter(commits).findUntil("0e4af846a0b31045ae6dc372fdf9061cd386a774");
            System.out.println("\n\n\nCommits until <0e4af846a0b31045ae6dc372fdf9061cd386a774>");

            for (RevCommit commit : commits.getCommits()) {
                System.out.println(commit.getId());
            }

            commits.reset();
            finder.setFilter(commits).findUntil("9cc61c7dac77f9a5c98d40617cb4129ff1b439fd");
            System.out.println("\n\n\nCommits until <9cc61c7dac77f9a5c98d40617cb4129ff1b439fd>");

            for (RevCommit commit : commits.getCommits()) {
                System.out.println(commit.getId());
            }
        } catch (VCSException ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (egit != null) {
                egit.close();
            }
        }
    }
}
