/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.view;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author wallace
 */
public class ProjectValues {
    private String name;
    List<BranchValues> branchesValues;
    private HashMap hashValues;
    private double maxValue;
    ProjectValues(String name){
        this.name = name;
        branchesValues = new LinkedList<BranchValues>();
        maxValue = 0;
    }
    
    public void addBranchValues(BranchValues branchValues){
        branchesValues.add(branchValues);
    }
    
    public void setOnTop(String name){
        BranchValues br = getBranchValuesByName(name);
        branchesValues.remove(br);
        branchesValues.add(0,br);
    }
    
    private BranchValues getBranchValuesByName(String name){
        BranchValues br = null;
        for (int i = 0; i < branchesValues.size(); i++) {
            BranchValues aux = branchesValues.get(i);
            if(aux.getName().equals(name)){
                br = aux;
                break;
            }
            
        }
        return br;
    }
    
    public List<BranchValues> getBranchesValues(){
        return branchesValues;
    }
    
    public String getName(){
        return name;
    }

    /**
     * @return the maxValue
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * @param maxValue the maxValue to set
     */
    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    /**
     * @return the hashValues
     */
    public HashMap getHashValues() {
        return hashValues;
    }

    /**
     * @param hashValues the hashValues to set
     */
    public void setHashValues(HashMap hashValues) {
        this.hashValues = hashValues;
    }
    
    public double getValueByVersionId(String id){
        Double v = (Double) hashValues.get(id);
        return v;
    }
    
    
}
