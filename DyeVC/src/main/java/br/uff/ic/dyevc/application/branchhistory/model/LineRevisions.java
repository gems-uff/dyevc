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
public class LineRevisions {
    private List<Revision> revisions;
    private Revision head;
    
    public LineRevisions(Revision revision){
        this.head = revision;
        revisions = new LinkedList<Revision>();
        revisions.add(0, revision);
    }
    
    public List<Revision> getRevisions(){
        return revisions;
    }
    
    public void addRevision(Revision r){
        revisions.add(0,r);
    }
    
    public Revision getHead(){
        return head;
    }
    
}
