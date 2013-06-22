/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.metric;

import br.uff.ic.dyevc.application.branchhistory.model.ProjectRevisions;
import br.uff.ic.dyevc.application.branchhistory.model.Revision;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedItem;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedProject;
import br.uff.ic.dyevc.tools.vcs.git.GitConnector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;

/**
 *
 * @author wallace
 */
public class LCSAbsoluteMetric implements Metric{
    private String TEMP_BRANCHES_HISTORY_PATH = System.getProperty("user.home") + "/.dyevc/TEMP_BRANCHES_HISTORY/";

    @Override
    public double getValue(Revision revision, VersionedItem versionedItem, VersionedProject versionedProject, ProjectRevisions projectRevisions) {
        double similaridade = 0;

        try {
            if (revision.getPrev().size() == 0) {
                similaridade = 1;
            } else {
                Revision prevRevision = projectRevisions.getRoot();


                File file = new File(TEMP_BRANCHES_HISTORY_PATH + versionedProject.getName() + "_" + revision.getId());
                FileUtils.deleteDirectory(file);
                createDirectory(TEMP_BRANCHES_HISTORY_PATH + versionedProject.getName() + "_" + revision.getId());
                FileUtils.copyDirectory(new File(versionedProject.getRelativePath()), new File(TEMP_BRANCHES_HISTORY_PATH + versionedProject.getName() + "_" + revision.getId()));

                file = new File(TEMP_BRANCHES_HISTORY_PATH + versionedProject.getName() + "_" + prevRevision.getId());
                FileUtils.deleteDirectory(file);
                createDirectory(TEMP_BRANCHES_HISTORY_PATH + versionedProject.getName() + "_" + prevRevision.getId());
                FileUtils.copyDirectory(new File(versionedProject.getRelativePath()), new File(TEMP_BRANCHES_HISTORY_PATH + versionedProject.getName() + "_" + prevRevision.getId()));






                GitConnector gitConnector = new GitConnector(TEMP_BRANCHES_HISTORY_PATH + versionedProject.getName() + "_" + revision.getId(), versionedProject.getName());

                //FileUtils.deleteDirectory(new File(TEMP_BRANCHES_HISTORY_PATH+versionedProject.getName()+"_"+revision.getId()));

                //gitConnector = gitConnector.cloneRepository(versionedProject.getRelativePath(), TEMP_BRANCHES_HISTORY_PATH+versionedProject.getName()+"_"+revision.getId(),versionedProject.getName());

                Git git = new Git(gitConnector.getRepository());
                CheckoutCommand checkoutCommand = null;//git.checkout();

                checkoutCommand = git.checkout();
                checkoutCommand.setName(revision.getId());

                checkoutCommand.call();


                gitConnector = new GitConnector(TEMP_BRANCHES_HISTORY_PATH + versionedProject.getName() + "_" + prevRevision.getId(), versionedProject.getName());

                git = new Git(gitConnector.getRepository());
                checkoutCommand = null;//git.checkout();

                checkoutCommand = git.checkout();
                checkoutCommand.setName(prevRevision.getId());

                checkoutCommand.call();



                File dirAtual = new File(TEMP_BRANCHES_HISTORY_PATH + versionedProject.getName() + "_" + revision.getId());
                File dirAnterior = new File(TEMP_BRANCHES_HISTORY_PATH + versionedProject.getName() + "_" + prevRevision.getId());

                List<String> l = getNumberOfFiles(dirAtual);
                double similaridadeTotal = 0;
                for (String f : l) {
                    String pathAntigo = TEMP_BRANCHES_HISTORY_PATH + versionedProject.getName() + "_" + prevRevision.getId() + f.substring((TEMP_BRANCHES_HISTORY_PATH + versionedProject.getName() + "_" + revision.getId()).length());
                    File fileAntigo = new File(pathAntigo);
                    double similaridadeParcial = 0;
                    if (fileAntigo.exists()) {
                        String a = readFile(f);
                        String b = readFile(pathAntigo);
                        int lcs = lcs(a,b);
                        similaridadeParcial = ((double) (lcs*2))/((double) (a.length() + b.length()));
                        
                    }
                    similaridadeTotal = similaridadeTotal + similaridadeParcial;
                }

                

                FileUtils.deleteDirectory(dirAtual);
                FileUtils.deleteDirectory(dirAnterior);
                
                similaridade = similaridadeTotal/l.size();
            }

        } catch (Exception e) {
            System.out.println("ERRO CALCULAR: " + e.getMessage());
        }

        return similaridade;
    }

    private String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }

        return stringBuilder.toString();
    }

    private void createDirectory(String name) {
        File file = new File(name);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public List<String> getNumberOfFiles(File file) {
        if (file.getName().startsWith(".")) {
            return null;
        }
        if (file.isFile()) {
            List l = new LinkedList<String>();
            l.add(file.getAbsolutePath());
            return l;
        } else {
            File files[] = file.listFiles();
            List<String> l = new LinkedList<String>();

            for (int i = 0; i < files.length; i++) {
                File file1 = files[i];
                List<String> l2 = getNumberOfFiles(file1);
                if (l2 != null) {
                    for (String f : l2) {
                        l.add(f);
                    }
                }

            }
            return l;
        }
    }

    private int lcs(String a, String b) {
        int[][] lengths = new int[a.length() + 1][b.length() + 1];

        // row 0 and column 0 are initialized to 0 already

        for (int i = 0; i < a.length(); i++) {
            for (int j = 0; j < b.length(); j++) {
                if (a.charAt(i) == b.charAt(j)) {
                    lengths[i + 1][j + 1] = lengths[i][j] + 1;
                } else {
                    lengths[i + 1][j + 1] =
                            Math.max(lengths[i + 1][j], lengths[i][j + 1]);
                }
            }
        }

        // read the substring out from the matrix
        StringBuffer sb = new StringBuffer();
        for (int x = a.length(), y = b.length();
                x != 0 && y != 0;) {
            if (lengths[x][y] == lengths[x - 1][y]) {
                x--;
            } else if (lengths[x][y] == lengths[x][y - 1]) {
                y--;
            } else {
                assert a.charAt(x - 1) == b.charAt(y - 1);
                sb.append(a.charAt(x - 1));
                x--;
                y--;
            }
        }

        return sb.reverse().toString().length();
    }
}
