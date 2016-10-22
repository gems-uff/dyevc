/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.model;

import java.util.HashSet;
import javafx.scene.chart.PieChart;

/**
 *
 * @author Ruben
 */
public class CollapsedCommitInfo extends CommitInfo
{
    private HashSet<CommitInfo> commits;
    
    private CommitInfo descendant;
    private CommitInfo ancestor;
    
    public CollapsedCommitInfo(CommitInfo ci)
    {
        commits = new HashSet<CommitInfo>();
        this.hash = ci.hash;
        this.type = ci.type;
        this.commitDate = ci.commitDate;
        commits.add(ci);
    }
    
    public void SetDescendant(CommitInfo ci)
    {
        this.descendant = ci;
        this.commitDate = ci.commitDate;
    }
    
    public void SetAncestor(CommitInfo ci)
    {
        this.ancestor = ci;
    }
    
    public CommitInfo GetAncestor()
    {
        return ancestor;
    }
    
    public CommitInfo GetDescendant()
    {
        return descendant;
    }
    
    public void AddCommitToCollapse(CommitInfo commit)        
    {
        commits.add(commit);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CollapsedCommitInfo other = (CollapsedCommitInfo)obj;

        return hash == other.hash;
    }
    
    @Override public String toString()
    {
        return "... " + commits.size() + " ...";
    }
    
    public int NumberOfCollapsedNodes()
    {
        return commits.size();
    }
}
