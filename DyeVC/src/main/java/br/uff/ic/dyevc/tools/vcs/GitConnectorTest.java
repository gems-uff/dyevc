package br.uff.ic.dyevc.tools.vcs;

import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.model.RepositoryStatus;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefDatabase;
import org.eclipse.jgit.transport.URIish;

/**
 * Hello world!
 *
 */
public class GitConnectorTest {

    public static void main(String[] args) {
        try {
            GitConnector cmd = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/labgcclone", "labgcclone");
            //cmd.createRepository("/F:/mybackups/Educacao/Mestrado-UFF/Git/outro");
//                        cmd.cloneRepository("/F:/mybackups/Educacao/Mestrado-UFF/Git/labgcclone", "/F:/mybackups/Educacao/Mestrado-UFF/Git/labgcclone2");
            //            cmd.pull();
            //            cmd.push();
            GitConnector clone2 = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/labgcclone2", "labgcclone2");
            //            clone2.commit("teste push");
            clone2.push();
//            cmd.getRemoteNames();
//            cmd.getTrackedBranches();
            GitConnector clone = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/labgcclone", "labgccloneClone");
            List<RepositoryStatus> relClone = clone.testAhead();
            for (Iterator<RepositoryStatus> it = relClone.iterator(); it.hasNext();) {
                RepositoryStatus rel = it.next();
                System.out.println(rel);
            }
            GitConnector cloneteste = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/labgccloneteste", "labgccloneteste");
            cloneteste.fetch("https://github.com/leomurta/labgc-2012.2.git", "+refs/heads/*:refs/remotes/origin/*");
            relClone = cloneteste.testAhead();
            for (Iterator<RepositoryStatus> it = relClone.iterator(); it.hasNext();) {
                RepositoryStatus rel = it.next();
                System.out.println(rel);
            }

            GitConnector outroclone = clone.cloneThis("C:\\dyevctemp\\labgcclone");
            outroclone.fetch("https://github.com/leomurta/labgc-2012.2.git", "+refs/heads/*:refs/remotes/origin/*");
//            outroclone.fetch("F:\\mybackups\\Educacao\\Mestrado-UFF\\Git\\labgc-2012.2", "+refs/heads/*:refs/remotes/origin/*");
            relClone = outroclone.testAhead();
            for (Iterator<RepositoryStatus> it = relClone.iterator(); it.hasNext();) {
                RepositoryStatus rel = it.next();
                System.out.println(rel);
            }
            
            GitConnector dyevc2 = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/dyevc2", "dyevc2");
            relClone = dyevc2.testAhead();
            for (Iterator<RepositoryStatus> it = relClone.iterator(); it.hasNext();) {
                RepositoryStatus rel = it.next();
                System.out.println(rel);
            }
        } catch (GitAPIException ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (VCSException ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
