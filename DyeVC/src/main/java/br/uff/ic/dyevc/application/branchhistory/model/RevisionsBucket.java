/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.model;

import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author wallace
 */
public class RevisionsBucket {
    private HashMap<String, Revision> hash; 
    
    public RevisionsBucket(){
        hash = new HashMap<String, Revision>(); 
    }
    
    public void addRevision(Revision revision){
        hash.put(revision.getId(), revision);
    }
    
    public Revision getRevisionById(String id){
        return hash.get(id);
    }
    
    public Collection<Revision> getRevisionCollection(){
        return hash.values();
    }
    
}
