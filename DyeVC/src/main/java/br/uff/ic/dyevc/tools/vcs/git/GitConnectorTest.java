package br.uff.ic.dyevc.tools.vcs.git;

import br.uff.ic.dyevc.model.BranchStatus;
import br.uff.ic.dyevc.model.git.TrackedBranch;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;

/**
 * Hello world!
 *
 */
public class GitConnectorTest {
    public static void main(String[] args) {
        GitConnectorTest test = new GitConnectorTest();
//        test.test();
        test.testAheadRemoteBranches();
//        test.testAdjustTargetConfiguration();
    }
    
    private void test() {
        GitConnector teste = null, clone = null, dyevc = null;
        try {
            teste = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/tmp", "tmp");
            clone = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/testeclone2", "testeclone2");
            
            Set<String> localBranches = clone.getLocalBranches();
            Ref originRefFromSource = clone.getBranchRemoteRef(GitConnector.REFS_DEFAULT_BRANCH, GitConnector.DEFAULT_ORIGIN);
            List<RemoteConfig> remoteConfigs = clone.getRemoteConfigs();
            Set<String> remoteNames = clone.getRemoteNames();
            List<TrackedBranch> trackedBranches = clone.getTrackedBranches();
            List<RemoteConfig> rcg = clone.getRemoteConfigs();
            Map<String, Ref> allRefs = clone.getRepository().getAllRefs();
            for(RemoteConfig cfg: rcg) {
                List<RefSpec> fetchRefSpecs = cfg.getFetchRefSpecs();
                for(RefSpec spec: fetchRefSpecs) {
                    RefSpec expandFromSource = spec.expandFromSource("master");
                    System.out.println("");
                }
            }
            
            List<BranchStatus> relClone = clone.testAhead();
            for (Iterator<BranchStatus> it = relClone.iterator(); it.hasNext();) {
                BranchStatus rel = it.next();
                System.out.println(rel);
            }
            clone.fetchAllRemotes(false);
            relClone = clone.testAhead();
            for (Iterator<BranchStatus> it = relClone.iterator(); it.hasNext();) {
                BranchStatus rel = it.next();
                System.out.println(rel);
            }
            
            
//            dyevc = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/dyevc", "dyevc");
//            dyevc.testLog();
//            GitCommitHistory ch = GitCommitHistory.getInstance(dyevc);
//            System.out.println(ch.toString());

//        } catch (GitAPIException ex) {
//            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            teste.close();
            clone.close();
            dyevc.close();
            
        }
    }
    private void testAdjustTargetConfiguration() {
                GitConnector target = null, source = null;
        try {
            target = new GitConnector("/C:/Users/Cristiano/.dyevc/rep1351967195798", "rep1351967195798");
            source = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/labgc-2012.2", "labgc-2012.2");
//            teste = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/tmpclone", "tmpclone");
            GitTools.adjustTargetConfiguration(source, target);
        } catch (Exception ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            target.close();
            source.close();
            
        }
    }
    
    private void testAheadRemoteBranches() {
        GitConnector teste = null;
        try {
            teste = new GitConnector("/C:/Users/Cristiano/.dyevc/rep1353176881646", "rep1353176881646");
//            teste = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/tmpclone", "tmpclone");
            
            teste.fetchAllRemotes(false);
            WorkingRepositoryBranchStatus of = WorkingRepositoryBranchStatus.of(teste, "branch1");
            System.out.printf("ahead: %d behind: %d\n", of.getAheadCount(), of.getBehindCount());
        } catch (Exception ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            teste.close();
            
        }
    }

}