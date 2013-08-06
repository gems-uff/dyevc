package br.uff.ic.dyevc.model.topology;

import java.util.Set;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 *
 * @author Cristiano
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostInfo implements Comparable<HostInfo>{
    private String commonName;
    private Set<String> aliases;

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public void setAliases(Set<String> aliases) {
        this.aliases = aliases;
    }
    
    public void addAlias(String alias) {
        aliases.add(alias);
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (this.commonName != null ? this.commonName.hashCode() : 0);
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
        final HostInfo other = (HostInfo) obj;
        if (!other.getCommonName().equalsIgnoreCase(getCommonName())) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(HostInfo o) {
        int result = 0;
        if (o == null) {
            throw new NullPointerException("Cannot compare to a null commit object.");
        }
        if (this.getCommonName().compareToIgnoreCase(o.getCommonName()) < 0) {
            result = -1;
        }
        if (this.getCommonName().compareToIgnoreCase(o.getCommonName()) > 0) {
            result = 1;
        }
        return result;
    }
    
}
