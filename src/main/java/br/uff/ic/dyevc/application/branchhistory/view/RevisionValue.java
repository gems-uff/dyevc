/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.view;

/**
 *
 * @author wallace
 */
public class RevisionValue {
    private double value;
    private String id;
    
    RevisionValue(double value, String id){
        this.value = value;
        this.id = id;
    }
    
    public double getValue(){
        return value;
    }
    
    public String getId(){
        return id;
    }
}
