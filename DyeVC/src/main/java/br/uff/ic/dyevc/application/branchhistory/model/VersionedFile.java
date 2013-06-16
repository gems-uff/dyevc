/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.model;

import br.uff.ic.dyevc.application.branchhistory.model.constant.Constant;

/**
 *
 * @author wallace
 */
public class VersionedFile extends VersionedItem{

    private int type;
    private String name;
    private String relativePath;
    
    public VersionedFile(String name, String relativePath){
        super();
        this.name = name;    
        this.relativePath = relativePath;
        type = Constant.FILE;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public int getType() {
        return type;
    }
    
}
