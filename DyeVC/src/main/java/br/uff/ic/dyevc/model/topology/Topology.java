package br.uff.ic.dyevc.model.topology;

import br.uff.ic.dyevc.exception.DyeVCException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * Represents a repository topology as a map where each key is the name of a
 * global known repository and each value contains the repository info.
 *
 * @author Cristiano
 */
@SuppressWarnings("serial")
public class Topology {

    /**
     * Stores the list of known repositories, mapped by its system name.
     */
    private HashMap<String, RepositoryInfo> repositoryMap;
    
    /**
     * A map of clones of a repository, where each key is the name of a system
     * and each value contains a map of clones for that system.
     */
    private HashMap<String, CloneMap> cloneMap;
    
    private static Topology instance;

    /**
     * Creates an empty topology map
     */
    private Topology() {
        repositoryMap = new HashMap<String, RepositoryInfo>();
        cloneMap = new HashMap<String, CloneMap>();
    }
    
    public static synchronized Topology getTopology() {
        if (instance == null) {
            instance = new Topology();
        }
        
        return instance;
    }

    // <editor-fold defaultstate="collapsed" desc="RepositoryMap">
    public void resetTopology(ArrayList<RepositoryInfo> repos) throws DyeVCException {
        repositoryMap.clear();
        for (RepositoryInfo ri : repos) {
            addRepositoryInfo(ri);
        }
    }

    public void addRepositoryInfo(RepositoryInfo repos) {
        repositoryMap.put(repos.getName(), repos);
        resetClonesForSystem(repos.getName());

        for (CloneInfo cloneInfo : repos.getClones()) {
            CloneMap map = cloneMap.get(repos.getName());
            CloneKey key = new CloneKey(cloneInfo.getHostName(), cloneInfo.getCloneName());
            map.put(key, cloneInfo);
        }
    }
    // </editor-fold>

    
    // <editor-fold defaultstate="collapsed" desc="CloneMap">
    /**
     * Gets clone information for the given key in the given system name
     *
     * @param systemName System name to look for the clone information
     * @param cloneKey Clone key to look for
     * @return The clone info requested
     */
    public CloneInfo getCloneInfo(String systemName, CloneKey cloneKey) {
        return cloneMap.get(systemName).get(cloneKey);
    }

    /**
     * Includes clone information for a given system name
     *
     * @param systemName Name of the system where clone info will be added
     * @param value Clone information to be added
     * @return The clone information added
     */
    public void addCloneInfo(String systemName, CloneInfo value) throws DyeVCException {
        if (!repositoryMap.containsKey(systemName)) {
            throw new DyeVCException("System " + systemName + " is not a known system name.");
        }

        RepositoryInfo ri = repositoryMap.get(systemName);
        ri.addClone(value);

        CloneMap map = cloneMap.get(systemName);
        CloneKey key = new CloneKey(value.getHostName(), value.getCloneName());
        map.put(key, value);
    }

    /**
     * Clears the clone list for the specified system
     *
     * @param systemName System name where the clones will be added to
     */
    private void resetClonesForSystem(String systemName) {
        if (!cloneMap.containsKey(systemName)) {
            cloneMap.put(systemName, new CloneMap());
        } else {
            cloneMap.get(systemName).clear();
        }
    }

    /**
     * Gets all known clones for the given system name
     *
     * @param systemName System name from which the known clones will be
     * returned
     * @return List of known clones for the given system name
     */
    public Collection<CloneInfo> getClonesForSystem(String systemName) throws DyeVCException {
        if (!cloneMap.containsKey(systemName)) {
            throw new DyeVCException("System " + systemName + " is not a known system name.");
        }
        return cloneMap.get(systemName).values();
    }
    // </editor-fold>
    

    /**
     * Return all known systems in the topology
     * @return The set of known systems in the topology
     */
    public Set<String> getSystems() {
        return repositoryMap.keySet();
    }
    
    /**
     * Gets all known relationships between clones for the given system.
     *
     * @param systemName System name from which the clone relationships will be
     * returned
     * @return List of known relationships for the given system name
     */
    public Collection<CloneRelationship> getRelationshipsForSystem(String systemName) throws DyeVCException {
        if (!cloneMap.containsKey(systemName)) {
            throw new DyeVCException("System " + systemName + " is not a known system name.");
        }

        ArrayList<CloneRelationship> cis = new ArrayList<CloneRelationship>();
        CloneMap map = cloneMap.get(systemName);
        for (CloneInfo cloneInfo : map.values()) {
            //Clonekey of "pullsFrom" is the origin and this cloneInfo is the destination
            for (CloneKey cloneKey : cloneInfo.getPullsFrom()) {
                PullRelationship cloneRelationship = new PullRelationship(map.get(cloneKey), cloneInfo);
                cis.add(cloneRelationship);
            }
            // CloneKey of "pushesTo" is the destination and this cloneInfo is the origin
            for (CloneKey cloneKey : cloneInfo.getPushesTo()) {
                PushRelationship cloneRelationship = new PushRelationship(cloneInfo, map.get(cloneKey));
                cis.add(cloneRelationship);
            }
        }
        return cis;
    }

    /**
     * Removes all topology data for the given system name
     *
     * @param systemName System name for which data will be removed
     */
    public void remove(String systemName) {
        repositoryMap.remove(systemName);
        if (cloneMap.containsKey(systemName)) {
            CloneMap map = cloneMap.get(systemName);
            map.clear();
            cloneMap.remove(systemName);
        }
    }

    /**
     * Removes the clone information which has the given key, in the given
     * system
     *
     * @param systemName Name of the system where the clone information which
     * will be erased resides
     * @param cloneKey Clone key of the clone to be erased
     * @return The clone information erased
     */
    public void removeCloneInfo(String systemName, CloneKey cloneKey) {
        if (repositoryMap.containsKey(systemName)) {
            repositoryMap.get(systemName).removeClone(cloneKey);
        }

        if (cloneMap.containsKey(systemName)) {
            cloneMap.get(systemName).remove(cloneKey);
        }
    }

    /**
     * A map of clones of a repository, where each key is a pair of hostname and
     * clone name, and each value contains information regarding a clone.
     *
     * @author Cristiano
     */
    @SuppressWarnings("serial")
    private class CloneMap extends HashMap<CloneKey, CloneInfo> {
    }

    @Override
    public String toString() {
        StringBuilder value = new StringBuilder("Topology known systems: ");
        for (String key : repositoryMap.keySet()) {
            value.append("<").append(key).append("> ");
        }
        return value.toString();
    }
}
