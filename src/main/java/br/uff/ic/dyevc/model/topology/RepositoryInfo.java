package br.uff.ic.dyevc.model.topology;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.utils.StringUtils;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Cristiano
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryInfo implements Comparable<RepositoryInfo> {
    @JsonProperty(value = "_id")
    private String      id;
    private String      systemName;
    private String      hostName;
    private String      cloneName;
    private String      clonePath;
    private Set<String> pushesTo;
    private Set<String> pullsFrom;
    private Set<String> monitoredBy;
    private Date        lastChanged;

    /**
     * Constructs a new RepositoryInfo.
     */
    public RepositoryInfo() {
        pushesTo    = new HashSet<String>();
        pullsFrom   = new HashSet<String>();
        monitoredBy = new HashSet<String>();
        id          = StringUtils.generateRepositoryId();
    }

    public String getId() {
        return id;
    }

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

    public Set<String> getPushesTo() {
        return pushesTo;
    }

    public boolean addPushesTo(String key) {
        return this.pushesTo.add(key);
    }

    public boolean addAllPushesTo(Collection<String> keysToAdd) {
        return this.pushesTo.addAll(keysToAdd);
    }

    public boolean removeAllPushesTo(Collection<String> keysToRemove) {
        return this.pushesTo.removeAll(keysToRemove);
    }

    public boolean addPullsFrom(String key) {
        return this.pullsFrom.add(key);
    }

    public boolean addAllPullsFrom(Collection<String> keysToAdd) {
        ;
        return this.pullsFrom.addAll(keysToAdd);
    }

    public boolean removeAllPullsFrom(Collection<String> keysToRemove) {
        return this.pullsFrom.removeAll(keysToRemove);
    }

    public void setPushesTo(Set<String> pushesTo) {
        this.pushesTo = pushesTo;
    }

    public Set<String> getPullsFrom() {
        return pullsFrom;
    }

    public void setPullsFrom(Set<String> pullsFrom) {
        this.pullsFrom = pullsFrom;
    }

    public Set<String> getMonitoredBy() {
        return monitoredBy;
    }

    public void setMonitoredBy(Set<String> monitoredBy) {
        this.monitoredBy = monitoredBy;
    }

    /**
     * Adds a hostname to the list of hostnames that monitor this repository
     * @param hostname The hostname to be added.
     * @return true if the hostname was added. False if it alread existed.
     */
    public boolean addMonitoredBy(String hostname) {
        return this.monitoredBy.add(hostname);
    }

    /**
     * Removes the specified hostname from the list of hostnames that monitor this repository
     * @param hostname The hostname to be removed.
     * @return true if the hostname was removed. False if it did not exist.
     */
    public boolean removeMonitoredBy(String hostname) {
        return this.monitoredBy.remove(hostname);
    }

    public Date getLastChanged() {
        return lastChanged;
    }

    public void setLastChanged(Date lastChanged) {
        this.lastChanged = lastChanged;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + ((this.id != null) ? this.id.hashCode() : 0);
        hash = 83 * hash + ((this.systemName != null) ? this.systemName.hashCode() : 0);
        hash = 83 * hash + ((this.hostName != null) ? this.hostName.hashCode() : 0);
        hash = 83 * hash + ((this.cloneName != null) ? this.cloneName.hashCode() : 0);

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

        final RepositoryInfo other = (RepositoryInfo)obj;
        if (!(other.getId().equals(getId())
                || (other.getSystemName().equals(getSystemName()) && other.getHostName().equals(getHostName())
                    && other.getCloneName().equals(getCloneName())))) {
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

        if (this.getSystemName().compareTo(o.getSystemName()) < 0) {
            result = -1;
        }

        if (this.getSystemName().compareTo(o.getSystemName()) > 0) {
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

        if (this.getId().compareTo(o.getId()) < 0) {
            result = -1;
        }

        if (this.getId().compareTo(o.getId()) > 0) {
            result = 1;
        }

        return result;
    }

    @Override
    public String toString() {
        super.toString();

        return "RepositoryInfo{" + "\n    id = " + id + "\n    systemName = " + systemName + "\n    hostName = "
               + hostName + "\n    cloneName = " + cloneName + "\n    path = " + clonePath + "" + "\n}";
    }
}
