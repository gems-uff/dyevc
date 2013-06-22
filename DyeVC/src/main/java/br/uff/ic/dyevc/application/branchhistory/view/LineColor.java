/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.view;

import java.awt.Paint;
import java.awt.Shape;

/**
 *
 * @author 01245189158
 */
public class LineColor {
    private Paint p;
    private Shape s;
    private String branchName;
    
    private boolean show;
    
    LineColor(String branchName, Paint p, Shape s){
        this.branchName = branchName;
        this.p = p;
        this.s = s;
        show = true;
    }

    /**
     * @return the p
     */
    public Paint getP() {
        return p;
    }

    /**
     * @param p the p to set
     */
    public void setP(Paint p) {
        this.p = p;
    }

    /**
     * @return the s
     */
    public Shape getS() {
        return s;
    }

    /**
     * @param s the s to set
     */
    public void setS(Shape s) {
        this.s = s;
    }

    /**
     * @return the branchName
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * @param branchName the branchName to set
     */
    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    /**
     * @return the show
     */
    public boolean isShow() {
        return show;
    }

    /**
     * @param show the show to set
     */
    public void setShow(boolean show) {
        this.show = show;
    }
    
}
