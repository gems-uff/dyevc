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
public class BranchRevisions {
    private List<LineRevisions> lines;
    private String name;
    private Revision head;
    
    public BranchRevisions(String name, Revision head){
        lines = new LinkedList<LineRevisions>();
        this.name = name;
        this.head = head;
    }
    
    public List<LineRevisions> getLinesRevisions(){
        return lines;
    }
    
    public void addLineRevisions(LineRevisions r){
        lines.add(0,r);
    }
    
    
    public String getName(){
        return name;
    }

    /**
     * @return the head
     */
    public Revision getHead() {
        return head;
    }

    /**
     * @param head the head to set
     */
    public void setHead(Revision head) {
        this.head = head;
    }
    
    public boolean haveLineRevisionByHeadId(String headId){
        boolean have = false;
        for (LineRevisions line : lines) {
            if(line.getHead().getId().equals(headId)){
                have = true;
                break;
            }
        }
        
        return have;
    }
    
}
