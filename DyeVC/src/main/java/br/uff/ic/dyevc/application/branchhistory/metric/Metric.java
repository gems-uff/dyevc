/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.metric;

import br.uff.ic.dyevc.application.branchhistory.model.ProjectRevisions;
import br.uff.ic.dyevc.application.branchhistory.model.Revision;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedItem;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedProject;
import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.tools.vcs.git.GitConnector;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;


/**
 *
 * @author wallace
 */
public abstract class Metric {
    
    String TEMP_BRANCHES_HISTORY_PATH = System.getProperty("user.home") + "/.dyevc/TEMP_BRANCHES_HISTORY/";
    abstract int getNumberOfRevisions();
    
    public String getValue(Revision revision, VersionedItem versionedItem){
        String auxiliarPaths[] = createDirs(versionedItem);
        return calculate(revision, versionedItem, auxiliarPaths);
    }
    
    public abstract String getName();
    
    abstract String calculate(Revision revision, VersionedItem versionedItem, String auxiliarPaths[]);
    
    private String[] createDirs(VersionedItem versionedItem){
        int numberOfRevisions = getNumberOfRevisions();
        String[] auxiliarPaths = new String[numberOfRevisions];
        for(int i = 0; i < numberOfRevisions; i++){
            File file = new File(TEMP_BRANCHES_HISTORY_PATH + versionedItem.getVersionedProject().getName() + "_" + (i+1));
            auxiliarPaths[i] = TEMP_BRANCHES_HISTORY_PATH + versionedItem.getVersionedProject().getName()+ "_" + (i+1)+"/";
            if(!file.exists())
            {
                createDirectory(TEMP_BRANCHES_HISTORY_PATH + versionedItem.getVersionedProject().getName()+ "_" + (i+1)+"/"+versionedItem.getVersionedProject().getName());
                try {
                    
                    FileUtils.copyDirectory(new File(versionedItem.getVersionedProject().getAbsolutePath()), new File(TEMP_BRANCHES_HISTORY_PATH + versionedItem.getVersionedProject().getName()+ "_" + (i+1)+"/"+versionedItem.getVersionedProject().getName()));
                } catch (IOException ex) {
                    System.out.println("Erro de cópia Metric.createDirs: "+ex.getMessage());
                    //Logger.getLogger(Metric.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try{
                GitConnector gitConnector = new GitConnector(TEMP_BRANCHES_HISTORY_PATH + versionedItem.getVersionedProject().getName()+ "_" + (i+1)+"/"+versionedItem.getVersionedProject().getName(), versionedItem.getVersionedProject().getName());

                Git git = new Git(gitConnector.getRepository());
                ResetCommand resetCommand = git.reset();
                resetCommand.setMode(ResetCommand.ResetType.HARD);
                resetCommand.call();
            } catch (VCSException ex) {
                System.out.println("Erro de reset de git (VCSException) Metric.createDirs: "+ex.getMessage());
            } catch (GitAPIException ex) {
                System.out.println("Erro de reset de git (GitAPIException) Metric.createDirs: "+ex.getMessage());
            }
            
        }
        return auxiliarPaths;
    }
    
    private void createDirectory(String name) {
        File file = new File(name);
        if (!file.exists()) {
            file.mkdirs();
        }
        
    }
    
    public String getSignature(){
        return this.getClass().getCanonicalName();
    }
    
}
