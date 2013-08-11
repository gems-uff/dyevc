package br.uff.ic.dyevc.model.topology;

import br.uff.ic.dyevc.persistence.Oid;
import br.uff.ic.dyevc.utils.StringUtils;
import java.util.HashSet;
import java.util.Set;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author Cristiano
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryInfo implements Comparable<RepositoryInfo>{
    private String id;
    private String systemName;
    private String hostName;
    private String cloneName;
    private String clonePath;
    private Set<RepositoryKey> pushesTo;
    private Set<RepositoryKey> pullsFrom;

    public RepositoryInfo() {
        pushesTo = new HashSet<RepositoryKey>();
        pullsFrom = new HashSet<RepositoryKey>();
        id = StringUtils.generateRepositoryId();
    }

    @JsonProperty(value = "_id")
    public String getId() {
        return id;
    }
    
    @JsonProperty(value = "_id")
    public void setId(String id) {
        this.id = id;
    }

    
    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
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

    public String getClonePath() {
        return clonePath;
    }

    public void setClonePath(String path) {
        this.clonePath = StringUtils.normalizePath(path);
    }

    public Set<RepositoryKey> getPushesTo() {
        return pushesTo;
    }
    
    public void addPushesTo(RepositoryKey key) {
        this.pushesTo.add(key);
    }
    
    public void addPullsFrom(RepositoryKey key) {
        this.pullsFrom.add(key);
    }

    public void setPushesTo(Set<RepositoryKey> pushesTo) {
        this.pushesTo = pushesTo;
    }

    public Set<RepositoryKey> getPullsFrom() {
        return pullsFrom;
    }

    public void setPullsFrom(Set<RepositoryKey> pullsFrom) {
        this.pullsFrom = pullsFrom;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (this.systemName != null ? this.systemName.hashCode() : 0);
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
        final RepositoryInfo other = (RepositoryInfo) obj;
        if (!(other.getSystemName().equalsIgnoreCase(getSystemName()) 
                || other.getHostName().equalsIgnoreCase(getHostName()) 
                || other.getCloneName().equalsIgnoreCase(getCloneName()))) {
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
        if (this.getSystemName().compareToIgnoreCase(o.getSystemName()) < 0) {
            result = -1;
        }
        if (this.getSystemName().compareToIgnoreCase(o.getSystemName()) > 0) {
            result = 1;
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
        return "<html><b>RepositoryInfo{</b>" + 
                "<br>&nbsp;&nbsp;&nbsp;<b>id = </b>" + id +
                "<br>&nbsp;&nbsp;&nbsp;<b>systemName = </b>" + systemName + 
                "<br>&nbsp;&nbsp;&nbsp;<b>hostName = </b>" + hostName + 
                "<br>&nbsp;&nbsp;&nbsp;<b>cloneName = </b>" + cloneName + 
                "<br>&nbsp;&nbsp;&nbsp;<b>path = </b>" + clonePath + "<br><b>}</b></html>";
    }   
}
