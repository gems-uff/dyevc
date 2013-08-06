package br.uff.ic.dyevc.model.topology;

import java.util.ArrayList;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 *
 * @author Cristiano
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryInfo implements Comparable<RepositoryInfo>{
    private String name;
    private ArrayList<CloneInfo> clones;
    
    public RepositoryInfo() {
        clones = new ArrayList<CloneInfo>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<CloneInfo> getClones() {
        return clones;
    }

    public void setClones(ArrayList<CloneInfo> clones) {
        this.clones = clones;
    }
    
    public void addClone(CloneInfo clone) {
        this.clones.add(clone);
    }
    
    public void removeClone(CloneKey cloneKey) {
        if (cloneKey == null) return;
        
        for(CloneInfo clone: clones) {
            if (clone.getHostName().equals(cloneKey.getHostName())
                    && clone.getCloneName().equals(cloneKey.getCloneName())) {
                clones.remove(clone);
                return;
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RepositoryInfo other = (RepositoryInfo) obj;
        if (!other.getName().equalsIgnoreCase(getName())) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(RepositoryInfo o) {
        int result = 0;
        if (o == null) {
            throw new NullPointerException("Cannot compare to a null commit object.");
        }
        if (this.getName().compareToIgnoreCase(o.getName()) < 0) {
            result = -1;
        }
        if (this.getName().compareToIgnoreCase(o.getName()) > 0) {
            result = 1;
        }
        return result;
    }
    
}
