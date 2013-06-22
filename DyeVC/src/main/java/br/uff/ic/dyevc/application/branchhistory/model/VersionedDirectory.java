/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.model;

import br.uff.ic.dyevc.application.branchhistory.model.constant.Constant;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author wallace
 */
public class VersionedDirectory extends VersionedItem{
    private int type;
    private String name;
    private String relativePath;
    
    private List<VersionedItem> versionedItems;
    
    public VersionedDirectory(String name, String relativePath){
        super();
        this.name = name;    
        this.relativePath = relativePath;
        type = Constant.DIRECTORY;
        versionedItems = new LinkedList<VersionedItem>();
    }
    
    public void addVersionedItem(VersionedItem versionedItem){
        versionedItems.add(versionedItem);
    }
    
    public List<VersionedItem> getVersionedItems(){
        return versionedItems;
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
