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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;

/**
 *
 * @author wallace
 */
public class DeleteMetric extends Metric {

    @Override
    int getNumberOfRevisions() {
        return 2;
    }
    
    @Override
    public String getName() {
        return "Delete Metric";
    }

    @Override
    String calculate(Revision revision, VersionedItem versionedItem, String[] auxiliarPaths) {
        double deletion = 0;

        try {
            if (!revision.getPrev().isEmpty()) {

                Revision prevRevision = revision.getPrev().get(0);




                GitConnector gitConnector = new GitConnector(auxiliarPaths[0] + versionedItem.getVersionedProject().getRelativePath(), versionedItem.getVersionedProject().getName());

                //FileUtils.deleteDirectory(new File(TEMP_BRANCHES_HISTORY_PATH+versionedProject.getName()+"_"+revision.getId()));

                //gitConnector = gitConnector.cloneRepository(versionedProject.getRelativePath(), TEMP_BRANCHES_HISTORY_PATH+versionedProject.getName()+"_"+revision.getId(),versionedProject.getName());

                Git git = new Git(gitConnector.getRepository());
                CheckoutCommand checkoutCommand = null;//git.checkout();

                checkoutCommand = git.checkout();
                checkoutCommand.setName(revision.getId());

                checkoutCommand.call();


                gitConnector = new GitConnector(auxiliarPaths[1] + versionedItem.getVersionedProject().getRelativePath(), versionedItem.getVersionedProject().getName());

                git = new Git(gitConnector.getRepository());
                checkoutCommand = null;//git.checkout();

                checkoutCommand = git.checkout();
                checkoutCommand.setName(prevRevision.getId());

                checkoutCommand.call();



                File dirAtual = new File(auxiliarPaths[0]);

                List<String> l = getNumberOfFiles(dirAtual, auxiliarPaths[0]);
                double totalDeletion = 0;
                for (String f : l) {
                    String pathAntigo = auxiliarPaths[1] + f;
                    File fileAntigo = new File(pathAntigo);
                    double partialDeletion = 0;
                    if (fileAntigo.exists()) {
                        String a = readFile(auxiliarPaths[0] + f);
                        String b = readFile(pathAntigo);
                        int lcs = llcs(a, b);
                        partialDeletion = b.length() - lcs;

                    }
                    totalDeletion = totalDeletion + partialDeletion;
                    System.gc();
                }

                deletion = totalDeletion;
            }

        } catch (Exception e) {
            System.out.println("ERRO CALCULAR: " + e.getMessage());
        }

        return String.valueOf(deletion);
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

    public List<String> getNumberOfFiles(File file, String absolutePath) {
        if (file.getName().startsWith(".")) {
            return null;
        }
        if (file.isFile()) {
            List l = new LinkedList<String>();
            l.add(file.getAbsolutePath().substring(absolutePath.length()));
            return l;
        } else {
            File files[] = file.listFiles();
            List<String> l = new LinkedList<String>();

            for (int i = 0; i < files.length; i++) {
                File file1 = files[i];
                List<String> l2 = getNumberOfFiles(file1, absolutePath);
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
        for (int x = a.length(), y = b.length(); x != 0 && y != 0;) {
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

        for (int i = 0; i < sb.reverse().toString().length(); i++) {
            System.out.print(sb.reverse().toString().charAt(i));
        }
        return sb.reverse().toString().length();

    }

    private int llcs(String a, String b) {
        int max = 0;
        int n = a.length();
        int blength = b.length();
        if (blength > n) {
            String aux = a;
            a = b;
            b = aux;
            int x = n;
            n = blength;
            blength = x;
        }

        //step 1
        List<Integer> matchLists[] = new List[n];
        HashMap<Character, List<Integer>> hashValues = new HashMap<Character, List<Integer>>();
        for (int i = 0; i < n; i++) {

            Character c = a.charAt(i);
            List<Integer> matchList = hashValues.get(c);
            if (matchList == null) {
                matchList = new LinkedList<Integer>();
                for (int j = 0; j < blength; j++) {
                    if (c == b.charAt(j)) {
                        matchList.add(j);
                    }
                }
                hashValues.put(c, matchList);
            }
            matchLists[i] = matchList;

        }
        //step 2
        int thresh[] = new int[n + 1];
        thresh[0] = 0;
        for (int i = 1; i <= n; i++) {
            thresh[i] = n + 1;
        }
        //System.out.println("thresh[1]: "+thresh[1]+"    -    thresh[2]"+thresh[2]+"    -    thresh[3]"+thresh[3]);

        int temp;
        int k;
        //step 3
        for (int i = 1; i <= n; i++) {
            temp = 0;
            k = 0;
            List<Integer> matchList = matchLists[i - 1];

            for (Integer j : matchList) {
                j++;
                if (j > temp) {
                    do {
                        k = k + 1;
                    } while (j > thresh[k]);
                    temp = thresh[k];
                    thresh[k] = j;
                    //System.out.println("K: "+k);
                }
            }

        }
        max = 0;
        for (int i = 0; i <= n; i++) {
            if (thresh[i] != (n + 1)) {
                max = i;
                //System.out.println("Pos: "+i);
            }
        }


        return max;
    }

    public static String lcsR(String a, String b) {
        int aLen = a.length();
        int bLen = b.length();
        if (aLen == 0 || bLen == 0) {
            return "";
        } else if (a.charAt(aLen - 1) == b.charAt(bLen - 1)) {
            return lcsR(a.substring(0, aLen - 1), b.substring(0, bLen - 1))
                    + a.charAt(aLen - 1);
        } else {
            String x = lcsR(a, b.substring(0, bLen - 1));
            String y = lcsR(a.substring(0, aLen - 1), b);
            return (x.length() > y.length()) ? x : y;
        }
    }
}
