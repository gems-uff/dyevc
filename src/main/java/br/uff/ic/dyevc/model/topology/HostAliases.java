package br.uff.ic.dyevc.model.topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Map of host aliases. Maintains lists and reverse lists of existing aliases for
 * all known hosts in the topology
 *
 * @author Cristiano
 */
@SuppressWarnings("serial")
public class HostAliases {

    /**
     * Maps a common name to a host info
     */
    private HashMap<String, HostInfo> hostMap;
    
    /**
     * Maps an alias to its common name
     */
    private HashMap<String, String> commonNames;
    
    public HostAliases(ArrayList<HostInfo> hosts) {
        hostMap = new HashMap<String, HostInfo>();
        setHosts(hosts);
    }

    /**
     * Sets the list of known hosts
     * @param hosts  List of known hosts
     */
    public final void setHosts(ArrayList<HostInfo> hosts) {
        for (HostInfo hostInfo: hosts) {
            hostMap.put(hostInfo.getCommonName(), hostInfo);
            //fills the reverse list of aliases to common names
            for (String alias: hostInfo.getAliases()) {
                commonNames.put(alias, hostInfo.getCommonName());
            }
        }
    }
    
    /**
     * Gets all known hostnames
     * @return 
     */
    public Set<String> getCommonNames() {
        return hostMap.keySet();
    }

    /**
     * Includes host information for a given common name
     * @param commonName Common name of the host
     * @param value Host information to be added
     * @return The host information added
     */
    public HostInfo addHostInfo(String commonName, HostInfo value) {
        HostInfo result = hostMap.put(commonName, value);
        for (String alias: result.getAliases()) {
            commonNames.put(alias, commonName);
        }
        return result;
    }

    /**
     * Includes an alias for a host
     * @param commonName Common name of the host
     * @param alias Alias to be added
     */
    public void addAlias(String commonName, String alias) {
        hostMap.get(commonName).addAlias(alias);
        commonNames.put(alias, commonName);
    }
    
    /**
     * Gets the common name for an alias
     * @param alias The alias from which to retrieve common name
     * @return The common name for the given alias
     */
    public String getCommonName(String alias) {
        String result = alias;
        //If exists a mapping for the alias, gets it. Otherwise, common name
        //is the alias itself.
        if (commonNames.containsKey(alias)) {
            result = commonNames.get(alias);
        }
        return result;
    }

    /**
     * Gets all known aliases for the given common name
     * @param commonName Common Name of the host to get aliases
     * @return Set of known aliases for the given common name
     */
    public Set<String> getAliases(String commonName) {
        return hostMap.get(commonName).getAliases();
    }
    
}
