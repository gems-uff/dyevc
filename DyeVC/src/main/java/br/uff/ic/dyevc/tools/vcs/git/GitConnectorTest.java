package br.uff.ic.dyevc.tools.vcs.git;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.gui.graph.CommitHistoryWindow;
import br.uff.ic.dyevc.model.BranchStatus;
import br.uff.ic.dyevc.model.CommitChange;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.git.TrackedBranch;
import br.uff.ic.dyevc.model.MonitoredRepository;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import org.gitective.core.CommitFinder;
import org.gitective.core.CommitUtils;
import org.gitective.core.filter.commit.CommitListFilter;

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
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

//      test.testFindNewCommits();
//        test.testGetBase();

        test.testGetBase2();

//      test.testGetURIs();
//      test.testGetBase();
//      test.testGetDiff();
//      test.testBare();
//      test.testGraph();
//      test.test();
//      test.testCommitHistory();
//      test.testAheadRemoteBranches();
//      test.testAdjustTargetConfiguration();
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
        } catch (Exception ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (egit != null) {
                egit.close();
            }
        }
    }

    private void testGetBase2() {
        GitConnector egit = null;
        try {
            egit = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/sapos", "sapos");
            GitCommitTools tools = GitCommitTools.getInstance(egit);

//          CommitInfo     ci    = tools.getBase("c7f13e2d8f8f4073d9d9c7bd60751c60427d4823",
//                                     "99e739fc78c3684bf290c2b682883fd988b20c50");

//            CommitInfo ci = tools.getBase("2a5d14a1c641104003f9671ff771161bdd5dbcbc",
//                                          "9cc61c7dac77f9a5c98d40617cb4129ff1b439fd");

            CommitInfo ci;

            ci = new CommonAncestorFinder(tools.getCommitInfoMap()).getCommonAncestor(
                "0802a2f64b8d08b8de6cc80330b64f105587fb7b", "dcc2737e5207986bc97c45cd3ac17d095a3d1842");

            System.out.println(ci.getHash());

            ci = new CommonAncestorFinder(tools.getCommitInfoMap()).getCommonAncestor(
                "0d629b74c155b5facfd54545014eff94ed82aaa8", "dcc2737e5207986bc97c45cd3ac17d095a3d1842");
            System.out.println(ci.getHash());
        } catch (Exception ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (egit != null) {
                egit.close();
            }
        }
    }

    private void testGetBase() {
        GitConnector egit = null;
        try {
            egit = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/sapos", "sapos");
            Repository repo = egit.getRepository();

//          RevCommit  commit = CommitUtils.getBase(repo, "2a5d14a1c641104003f9671ff771161bdd5dbcbc",
//                                  "9cc61c7dac77f9a5c98d40617cb4129ff1b439fd");
            RevCommit commit = CommitUtils.getBase(repo, "dcc2737e5207986bc97c45cd3ac17d095a3d1842",
                                   "0d629b74c155b5facfd54545014eff94ed82aaa8");
            System.out.println(commit.getId());
        } catch (Exception ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (egit != null) {
                egit.close();
            }
        }
    }

    private void testGetURIs() {
        GitConnector dyevc = null;
        try {
            dyevc = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/dyevcssh", "dyevcssh");
            List<RemoteConfig> configs = dyevc.getRemoteConfigs();
            for (RemoteConfig config : configs) {
                System.out.println(config.getURIs());
            }
        } catch (Exception ex) {
            LoggerFactory.getLogger(GitCommitTools.class).error(null, ex);
        } finally {
            if (dyevc != null) {
                dyevc.close();
            }
        }
    }

    private void testGetDiff() {
        GitConnector dyevc = null;
        try {
            dyevc = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/dyevc", "dyevc");
            Repository        repo    = dyevc.getRepository();
            ObjectId          objId   = repo.resolve("fd1388da9a8bbe0a7ced85817b2f9252b5e23424");
            RevWalk           rw      = null;
            DiffFormatter     df      = null;
            Set<CommitChange> changes = new HashSet<CommitChange>();
            RevCommit         commit  = CommitUtils.getCommit(repo, objId);
            try {
                rw = new RevWalk(repo);
                RevCommit parent;
                df = new DiffFormatter(DisabledOutputStream.INSTANCE);
                df.setRepository(repo);
                df.setDiffComparator(RawTextComparator.DEFAULT);
                df.setDetectRenames(true);
                List<DiffEntry> diffs;
                if (commit.getParentCount() > 0) {
                    parent = rw.parseCommit(commit.getParent(0).getId());
                    diffs  = df.scan(parent.getTree(), commit.getTree());
                } else {
                    diffs = df.scan(new EmptyTreeIterator(),
                                    new CanonicalTreeParser(null, rw.getObjectReader(), commit.getTree()));
                }

                for (DiffEntry diff : diffs) {
                    CommitChange cc = new CommitChange();
                    cc.setChangeType(diff.getChangeType().name());
                    cc.setOldPath(diff.getOldPath());
                    cc.setNewPath(diff.getNewPath());
                    changes.add(cc);
                }

                for (CommitChange cc : changes) {
                    System.out.println(cc);
                }
            } catch (Exception ex) {
                LoggerFactory.getLogger(GitCommitTools.class).error("Error parsing change set for commit "
                                        + commit.getName(), ex);
            } finally {
                if (df != null) {
                    df.release();
                }

                if (rw != null) {
                    rw.release();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (dyevc != null) {
                dyevc.close();
            }
        }
    }

    private void testGraph() {
        MonitoredRepository rep = new MonitoredRepository("rep1363653250218");
        new CommitHistoryWindow(rep).setVisible(true);
    }

    private void testCommitHistory() {
        GitConnector dyevc2 = null;
        try {
            dyevc2 = new GitConnector("/C:/Users/Cristiano/.dyevc/rep1361714490249", "tmp");
            Iterator<RevCommit> commitsIterator = dyevc2.getAllCommitsIterator();
        } catch (Exception ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (dyevc2 != null) {
                dyevc2.close();
            }
        }
    }

    private void testBare() {
        GitConnector bare = null;
        try {
            bare = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/t", "tmp");
            System.out.println(bare.getRepository().isBare());
        } catch (Exception ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (bare != null) {
                bare.close();
            }
        }
    }

    private void test() {
        GitConnector teste = null,
                     clone = null,
                     dyevc = null;
        try {
            teste = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/tmp", "tmp");
            clone = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/testeclone2", "testeclone2");
            Set<String> localBranches       = clone.getLocalBranches();
            Ref         originRefFromSource = clone.getBranchRemoteRef(GitConnector.REFS_DEFAULT_BRANCH,
                                                  GitConnector.DEFAULT_ORIGIN);
            List<RemoteConfig>  remoteConfigs   = clone.getRemoteConfigs();
            Set<String>         remoteNames     = clone.getRemoteNames();
            List<TrackedBranch> trackedBranches = clone.getTrackedBranches();
            List<RemoteConfig>  rcg             = clone.getRemoteConfigs();
            Map<String, Ref>    allRefs         = clone.getRepository().getAllRefs();
            for (RemoteConfig cfg : rcg) {
                List<RefSpec> fetchRefSpecs = cfg.getFetchRefSpecs();
                for (RefSpec spec : fetchRefSpecs) {
                    RefSpec expandFromSource = spec.expandFromSource("master");
                    System.out.println("");
                }
            }

            List<BranchStatus> relClone = clone.testAhead();
            for (Iterator<BranchStatus> it = relClone.iterator(); it.hasNext(); ) {
                BranchStatus rel = it.next();
                System.out.println(rel);
            }

            clone.fetchAllRemotes(false);
            relClone = clone.testAhead();

            for (Iterator<BranchStatus> it = relClone.iterator(); it.hasNext(); ) {
                BranchStatus rel = it.next();
                System.out.println(rel);
            }

//          dyevc = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/dyevc", "dyevc");
//          dyevc.testLog();
//          GitCommitHistory ch = GitCommitHistory.getInstance(dyevc);
//          System.out.println(ch.toString());
//        } catch (GitAPIException ex) {
//          Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (teste != null) {
                teste.close();
            }

            if (clone != null) {
                clone.close();
            }

            if (dyevc != null) {
                dyevc.close();
            }
        }
    }

    private void testAdjustTargetConfiguration() {
        GitConnector target = null,
                     source = null;
        try {
            target = new GitConnector("/C:/Users/Cristiano/.dyevc/rep1351967195798", "rep1351967195798");
            source = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/labgc-2012.2", "labgc-2012.2");

//          teste = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/tmpclone", "tmpclone");
            GitTools.adjustTargetConfiguration(source, target);
        } catch (Exception ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (target != null) {
                target.close();
            }

            if (source != null) {
                source.close();
            }
        }
    }

    private void testAheadRemoteBranches() {
        GitConnector teste = null;
        try {
            teste = new GitConnector("/C:/Users/Cristiano/.dyevc/rep1353176881646", "rep1353176881646");

//          teste = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/tmpclone", "tmpclone");
            teste.fetchAllRemotes(false);
            WorkingRepositoryBranchStatus of = WorkingRepositoryBranchStatus.of(teste, "branch1");
            System.out.printf("ahead: %d behind: %d\n", of.getAheadCount(), of.getBehindCount());
        } catch (Exception ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (teste != null) {
                teste.close();
            }
        }
    }
}
