package br.uff.ic.dyevc.tools.vcs;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Hello world!
 *
 */
public class GitConnectorTest {

    public static void main(String[] args) {
        try {
            GitConnector cmd = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/labgcclone");
            //cmd.createRepository("/F:/mybackups/Educacao/Mestrado-UFF/Git/outro");
                        cmd.cloneRepository("/F:/mybackups/Educacao/Mestrado-UFF/Git/labgcclone", "/F:/mybackups/Educacao/Mestrado-UFF/Git/labgcclone2");
            //            cmd.pull();
            //            cmd.push();
            //            GitConnector clone2 = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/labgcclone2");
            //            clone2.commit("teste push");
            //            clone2.push();
            cmd.getRemotes();
            cmd.getBranches();
        } catch (GitAPIException ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
