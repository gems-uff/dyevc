package br.uff.ic.dyevc.model.topology;

import java.util.ArrayList;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 *
 * @author Cristiano
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloneInfo implements Comparable<CloneInfo>{
    private String hostName;
    private String cloneName;
    private String path;
    private ArrayList<CloneKey> pushesTo;
    private ArrayList<CloneKey> pullsFrom;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ArrayList<CloneKey> getPushesTo() {
        return pushesTo;
    }

    public void setPushesTo(ArrayList<CloneKey> pushesTo) {
        this.pushesTo = pushesTo;
    }

    public ArrayList<CloneKey> getPullsFrom() {
        return pullsFrom;
    }

    public void setPullsFrom(ArrayList<CloneKey> pullsFrom) {
        this.pullsFrom = pullsFrom;
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
        final CloneInfo other = (CloneInfo) obj;
        if (!(other.getHostName().equalsIgnoreCase(getHostName()) || other.getCloneName().equalsIgnoreCase(getCloneName()))) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(CloneInfo o) {
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

    @Override
    public String toString() {
        super.toString();
        return "CloneInfo{" + "hostName=" + hostName + ", cloneName=" + cloneName + ", path=" + path + '}';
    }
    
}
