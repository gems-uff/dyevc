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
import org.eclipse.jgit.api.CleanCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;


/**
 *
 * @author wallace
 */
public class NumberOfBytes extends Metric{
    
    @Override
    int getNumberOfRevisions() {
        return 1;
    }
    
    @Override
    public String getName() {
        return "Number Of Bytes";
    }

    @Override
    String calculate(Revision revision, VersionedItem versionedItem, String[] auxiliarPaths) {
        long numberOfcharacters = 0;
        
        try{
           
            
            GitConnector gitConnector = new GitConnector(auxiliarPaths[0] + versionedItem.getVersionedProject().getRelativePath(), versionedItem.getVersionedProject().getName());

           
            Git git = new Git(gitConnector.getRepository());
            CheckoutCommand checkoutCommand = null;//git.checkout();
          
            checkoutCommand = git.checkout();
            checkoutCommand.setName(revision.getId());
                            
            checkoutCommand.call();
            
            File dir = new File(auxiliarPaths[0] + versionedItem.getVersionedProject().getRelativePath());
            numberOfcharacters = getNumberOfCharacters(dir);
            
            
        }catch(Exception e){
            System.out.println("ERRO CALCULAR: "+e.getMessage());
        }
        
        return String.valueOf(numberOfcharacters);
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
