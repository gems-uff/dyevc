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
    
    private List<Revision> revisions;
    
    VersionedItem(){
        revisions = new LinkedList<Revision>();
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
    abstract public String getRelativePath();
    abstract public int getType();
}
