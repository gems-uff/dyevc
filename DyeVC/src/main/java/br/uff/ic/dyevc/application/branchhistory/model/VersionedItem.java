/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.model;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author wallace
 */
public abstract class VersionedItem {
    
    private String BRANCHES_HISTORY_PATH = System.getProperty("user.home") + "/.dyevc/BRANCHES_HISTORY/";
    
    private List<Revision> revisions;
    private VersionedProject versionedProject;
    String relativePath;
    
    VersionedItem(VersionedProject versionedProject){
        revisions = new LinkedList<Revision>();
        versionedProject = versionedProject;
    }
    
    VersionedItem(){
        revisions = new LinkedList<Revision>();
        versionedProject = (VersionedProject) this;
    }
    
    public List<Revision> getRevisions(){
        return revisions;
    }
    
    public void addRevison(Revision revision){
        revisions.add(revision);
    }
    
    public boolean belongsToRevision(Revision revision){
        boolean belongs = false;
        
        for (Revision aux : revisions) {
            if(revision.getId().equals(aux.getId())){
                belongs = true;
                break;
            }
            
        }
        
        return belongs;
    }
    
    abstract public String getName();
    abstract public int getType();

    /**
     * @return the versionedProject
     */
    public VersionedProject getVersionedProject() {
        return versionedProject;
    }
    
    public String getRelativePath() {
        return relativePath;
    }
    
    public String getAbsolutePath(){
        return BRANCHES_HISTORY_PATH+relativePath;
    }
}
