package br.uff.ic.dyevc.model.topology;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Identifies uniquely a repository clone, as each cloneName can be used once 
 * in a system for each hostName
 * @author Cristiano
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryKey implements Comparable<RepositoryKey>{
    private String hostName;
    private String cloneName;

    public RepositoryKey() {
    }
    
    public RepositoryKey(String hostName, String cloneName) {
        this.hostName = hostName;
        this.cloneName = cloneName;
    }
    
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getCloneName() {
        return cloneName;
    }

    public void setCloneName(String cloneName) {
        this.cloneName = cloneName;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (this.hostName != null ? this.hostName.hashCode() : 0);
        hash = 83 * hash + (this.cloneName != null ? this.cloneName.hashCode() : 0);
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
        final RepositoryKey other = (RepositoryKey) obj;
        if (!(other.getHostName().equals(getHostName()) 
                && other.getCloneName().equals(getCloneName()))) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(RepositoryKey o) {
        int result = 0;
        if (o == null) {
            throw new NullPointerException("Cannot compare to a null commit object.");
        }
        if (this.getHostName().compareToIgnoreCase(o.getHostName()) < 0) {
            result = -1;
        }
        if (this.getHostName().compareToIgnoreCase(o.getHostName()) > 0) {
            result = 1;
        }
        if (this.getCloneName().compareToIgnoreCase(o.getCloneName()) < 0) {
            result = -1;
        }
        if (this.getCloneName().compareToIgnoreCase(o.getCloneName()) > 0) {
            result = 1;
        }
        return result;
    }
}
