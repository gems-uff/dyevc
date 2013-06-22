/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.view;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author 01245189158
 */
public class LineColours {
    List<LineColor> colors;
    LineColours(){
        colors = new LinkedList<LineColor>();
    }
    
    public void addColor(LineColor lc){
        colors.add(lc);
    }
    
    public LineColor getColorByName(String name){
        LineColor lc = null;
        for (int i = 0; i < colors.size(); i++) {
            LineColor aux = colors.get(i);
            if(aux.getBranchName().equals(name)){
                lc = aux;
                break;
            }
            
        }
        return lc;
    }
}
