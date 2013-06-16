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
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;


/**
 *
 * @author wallace
 */
public class NumberOfBytes implements Metric{
    
    private String TEMP_BRANCHES_HISTORY_PATH = System.getProperty("user.home")+"/.dyevc/TEMP_BRANCHES_HISTORY/";

    @Override
    public double getValue(Revision revision, VersionedItem versionedItem, VersionedProject versionedProject, ProjectRevisions projectRevisions) {
        
        long numberOfcharacters = 0;
        
        try{
            File file = new File(TEMP_BRANCHES_HISTORY_PATH+versionedProject.getName()+"_"+revision.getId());
            FileUtils.deleteDirectory(file);
        
        
        
            createDirectory(TEMP_BRANCHES_HISTORY_PATH+versionedProject.getName()+"_"+revision.getId());
        
            FileUtils.copyDirectory(new File(versionedProject.getRelativePath()), new File(TEMP_BRANCHES_HISTORY_PATH+versionedProject.getName()+"_"+revision.getId()));
        
            GitConnector gitConnector = new GitConnector(TEMP_BRANCHES_HISTORY_PATH+versionedProject.getName()+"_"+revision.getId(), versionedProject.getName());
            
            //FileUtils.deleteDirectory(new File(TEMP_BRANCHES_HISTORY_PATH+versionedProject.getName()+"_"+revision.getId()));
        
            //gitConnector = gitConnector.cloneRepository(versionedProject.getRelativePath(), TEMP_BRANCHES_HISTORY_PATH+versionedProject.getName()+"_"+revision.getId(),versionedProject.getName());
            
            Git git = new Git(gitConnector.getRepository());
            CheckoutCommand checkoutCommand = null;//git.checkout();
            
            checkoutCommand = git.checkout();
            checkoutCommand.setName(revision.getId());
                            
            checkoutCommand.call();
            
            File dir = new File(TEMP_BRANCHES_HISTORY_PATH+versionedProject.getName()+"_"+revision.getId());
            numberOfcharacters = getNumberOfCharacters(dir);
            
            FileUtils.deleteDirectory(dir);
            
        }catch(Exception e){
            System.out.println("ERRO CALCULAR: "+e.getMessage());
        }
        
        return numberOfcharacters;
    }
    
    private void createDirectory(String name){
        File file = new File(name);
        if(!file.exists()){
            file.mkdirs();
        }
    }
    
    public long getNumberOfCharacters(File file){
        if(file.getName().startsWith(".")){
            return 0;
        }
        if(file.isFile()){
            return  file.length();
        }else{
            File files[] = file.listFiles();
            long size = 0;
            for (int i = 0; i < files.length; i++) {
                File file1 = files[i];
                size = size + getNumberOfCharacters(file1);
            }
            return size;
        }
    }
    
    
    
}
