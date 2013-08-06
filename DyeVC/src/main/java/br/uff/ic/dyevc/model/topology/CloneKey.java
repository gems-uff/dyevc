package br.uff.ic.dyevc.model.topology;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Identifies uniquely a clone, as each cloneName can be used once for each
 * hostName
 * @author Cristiano
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloneKey implements Comparable<CloneKey>{
    private String hostName;
    private String cloneName;

    public CloneKey() {
    }
    
    public CloneKey(String hostName, String cloneName) {
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
        final CloneKey other = (CloneKey) obj;
        if (!(other.getHostName().equalsIgnoreCase(getHostName()) || other.getCloneName().equalsIgnoreCase(getCloneName()))) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(CloneKey o) {
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
