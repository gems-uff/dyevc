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
public class ProjectRevisions {
    private String name;
    private List<BranchRevisions> branches;
    
    private RevisionsBucket revisionsBucket;
    
    private List<Revision> roots;
    
    public ProjectRevisions(String name){
        this.name = name;
        roots = new LinkedList<Revision>();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the absolutePath
     */


    /**
     * @return the branches
     */
    public List<BranchRevisions> getBranchesRevisions() {
        return branches;
    }

    /**
     * @param branches the branches to set
     */
    public void setBranchesRevisions(List<BranchRevisions> branches) {
        this.branches = branches;
    }

    /**
     * @return the revisionsBucket
     */
    public RevisionsBucket getRevisionsBucket() {
        return revisionsBucket;
    }

    /**
     * @param revisionsBucket the revisionsBucket to set
     */
    public void setRevisionsBucket(RevisionsBucket revisionsBucket) {
        this.revisionsBucket = revisionsBucket;
    }

    /**
     * @return the root
     */
    public List<Revision> getRoots() {
        return roots;
    }

    /**
     * @param root the root to set
     */
    public void addRoot(Revision root) {
        roots.add(root);
    }
}
