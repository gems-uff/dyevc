package br.uff.ic.dyevc.tools.vcs;

import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.model.BranchStatus;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Hello world!
 *
 */
public class GitConnectorTest {

    public static void main(String[] args) {
        GitConnector labgcclone2 = null, clone = null, cloneteste = null, dyevc = null;
        try {
            labgcclone2 = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/labgcclone2", "labgcclone2");
            //            clone2.commit("teste push");
            labgcclone2.push();

            clone = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/labgcclone", "labgccloneClone");
            List<BranchStatus> relClone = clone.testAhead();
            for (Iterator<BranchStatus> it = relClone.iterator(); it.hasNext();) {
                BranchStatus rel = it.next();
                System.out.println(rel);
            }
            cloneteste = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/labgccloneteste", "labgccloneteste");
            cloneteste.fetchAllRemotes();
            relClone = cloneteste.testAhead();
            for (Iterator<BranchStatus> it = relClone.iterator(); it.hasNext();) {
                BranchStatus rel = it.next();
                System.out.println(rel);
            }
            
            
            dyevc = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/dyevc", "dyevc");
//            dyevc.testLog();
            GitCommitHistory ch = GitCommitHistory.getInstance(dyevc);
            System.out.println(ch.toString());

        } catch (GitAPIException ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (VCSException ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            labgcclone2.close();
            clone.close();
            cloneteste.close();
            dyevc.close();
            
        }
    }

}
