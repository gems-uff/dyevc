/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.model;

import br.uff.ic.dyevc.application.branchhistory.model.constant.Constant;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author wallace
 */
public class VersionedItemsBucket {
    HashMap<String, VersionedItem> hash;
    HashMap<String, VersionedDirectory> directoryHash; 
    HashMap<String, VersionedFile> fileHash;
    
    public VersionedItemsBucket(){
        hash = new HashMap<String, VersionedItem>(); 
        directoryHash = new HashMap<String, VersionedDirectory>();
        fileHash = new HashMap<String, VersionedFile>();
    }
    
    public void addVersionedItem(VersionedItem versionedItem){
        hash.put(versionedItem.getRelativePath(), versionedItem);
        
        if(versionedItem.getType() == Constant.DIRECTORY){
            directoryHash.put(versionedItem.getRelativePath(), (VersionedDirectory) versionedItem);
        }else if(versionedItem.getType() == Constant.FILE){
            fileHash.put(versionedItem.getRelativePath(), (VersionedFile) versionedItem);
        }
    }
    
    public VersionedItem getVersionedItemByRelativePath(String path){
        return hash.get(path);
    }
    
    public Collection<VersionedItem> getRevisionCollection(){
        return hash.values();
    }
    
    public Collection<VersionedDirectory> getVersionedDirectories(){
        return directoryHash.values();
    }
    
    public Collection<VersionedFile> getVersionedFiles(){
        return fileHash.values();
    }
}
