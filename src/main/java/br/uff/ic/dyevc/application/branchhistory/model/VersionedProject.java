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
public class VersionedProject extends VersionedItem{
    private int type;
    private String name;
    
    private List<VersionedItem> versionedItems;
    
    private VersionedItemsBucket versionedItemsBucket;
    
    public VersionedProject(String name, String relativePath){
        super();
        this.name = name;    
        this.relativePath = relativePath;
        type = Constant.PROJECT;
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
    public int getType() {
        return type;
    }

    /**
     * @return the versionedItemsBucket
     */
    public VersionedItemsBucket getVersionedItemsBucket() {
        return versionedItemsBucket;
    }

    /**
     * @param versionedItemsBucket the versionedItemsBucket to set
     */
    public void setVersionedItemsBucket(VersionedItemsBucket versionedItemsBucket) {
        this.versionedItemsBucket = versionedItemsBucket;
    }
}
