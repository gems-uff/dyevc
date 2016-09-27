/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.model;

import java.util.HashSet;

/**
 *
 * @author Ruben
 */
public class CollapsedCommitsSet extends CommitInfo
{
    private HashSet<CommitInfo> commits;
    
    private Integer hash_code;
    
    public CollapsedCommitsSet(CommitInfo ci)
    {
        this.hash_code = ci.hashCode();
        commits.add(ci);
    }
    
    public void AddCommitToCollapse(CommitInfo commit)        
    {
        commits.add(commit);
    }
    
    @Override
    public int hashCode()
    {
        return hash_code;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CollapsedCommitsSet other = (CollapsedCommitsSet)obj;

        return hash_code == other.hashCode();
    }
}
