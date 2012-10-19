package br.uff.ic.dyevc.tools.vcs;

import java.io.IOException;
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

/**
 * Hello world!
 *
 */
public class GitConnectorTest {

    public static void main(String[] args) {
        try {
            GitConnector cmd = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/labgcclone");
            //cmd.createRepository("/F:/mybackups/Educacao/Mestrado-UFF/Git/outro");
//                        cmd.cloneRepository("/F:/mybackups/Educacao/Mestrado-UFF/Git/labgcclone", "/F:/mybackups/Educacao/Mestrado-UFF/Git/labgcclone2");
            //            cmd.pull();
            //            cmd.push();
            GitConnector clone2 = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/labgcclone2");
            //            clone2.commit("teste push");
            clone2.push();
//            cmd.getRemotes();
//            cmd.getBranchesFromRemote();
            clone2.testAhead();
            GitConnector clone = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/labgcclone");
            clone.testAhead();
            cmd.cloneRepository("/F:/mybackups/Educacao/Mestrado-UFF/Git/labgcclone", "/F:/mybackups/Educacao/Mestrado-UFF/Git/labgccloneteste");
            GitConnector cloneteste = new GitConnector("/F:/mybackups/Educacao/Mestrado-UFF/Git/labgccloneteste");
            cloneteste.fetch("https://github.com/leomurta/labgc-2012.2.git", "+refs/heads/*:refs/remotes/origin/*");
            cloneteste.testAhead();
        } catch (GitAPIException ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GitConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
