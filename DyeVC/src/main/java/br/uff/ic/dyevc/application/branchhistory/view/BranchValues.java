/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.view;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author wallace
 */
public class BranchValues {
    private List<LineValues> lineValues;
    private String name;
    
    BranchValues(String name){
        this.name = name;
        lineValues = new LinkedList<LineValues>();
    }
    
    public void addLineValues(LineValues line){
        lineValues.add(line);
    }
    
    
    public List<LineValues> getLineValues(){
        return lineValues;
    }
    
    public String getName(){
        return name;
    }
}
