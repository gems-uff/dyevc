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
public class LineValues {
    private List<RevisionValue> values;
    
    LineValues(){
        values = new LinkedList<RevisionValue>();
    }
    
    public void addRevisionValue(RevisionValue rev){
        values.add(rev);
    }
    
    
    public List<RevisionValue> getRevisionsValues(){
        return values;
    }
    
}
