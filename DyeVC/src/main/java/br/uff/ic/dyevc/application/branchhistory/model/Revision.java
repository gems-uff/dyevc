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
public class Revision {
    private String id;
    
    private List<Revision> next;
    private List<Revision> prev;
    
    public Revision(String id){
        this.id = id;
        next = new LinkedList<Revision>();
        prev = new LinkedList<Revision>();
    }
    
    public String getId(){
        return id;
    }

    /**
     * @return the next
     */
    public List<Revision> getNext() {
        return next;
    }

    /**
     * @param next the next to set
     */
    public void addNext(Revision revision) {
        this.next.add(revision);
    }

    /**
     * @return the prev
     */
    public List<Revision> getPrev() {
        return prev;
    }

    /**
     * @param prev the prev to set
     */
    public void addPrev(Revision revision) {
        this.prev.add(revision);
    }
}
