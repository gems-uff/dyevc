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
     * Stores the list of known clones of each system, mapped by its system
     * name.
     */
    private HashMap<String, CloneMap> repositoryMap;

    /**
     * Creates an empty topology map
     */
    public Topology() {
        repositoryMap = new HashMap<String, CloneMap>();
    }

    // <editor-fold defaultstate="collapsed" desc="RepositoryMap">
    /**
     * Resets the topology, replacing all existing repository information by the
     * list informed as parameter
     *
     * @param repos the new list of repositories to be added to the topology
     * @throws DyeVCException
     */
    public Topology resetTopology(ArrayList<RepositoryInfo> repos) {
        for (CloneMap map : repositoryMap.values()) {
            map.clear();
        }

        repositoryMap.clear();

        for (RepositoryInfo ri : repos) {
            addRepositoryInfo(ri);
        }
        return this;
    }

    /**
     * Includes info for a new repository in the topology
     *
     * @param repos Information to be added to topology
     */
    public void addRepositoryInfo(RepositoryInfo repos) {
        if (!repositoryMap.containsKey(repos.getSystemName())) {
            repositoryMap.put(repos.getSystemName(), new CloneMap());
        }
        String key = repos.getId();
        repositoryMap.get(repos.getSystemName()).put(key, repos);
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="CloneMap">
    /**
     * Gets clone information for the given key
     *
     * @param cloneKey Clone key to look for
     * @return The clone info requested
     */
    public RepositoryInfo getRepositoryInfo(String systemName, String cloneKey) {
        return repositoryMap.get(systemName).get(cloneKey);
    }

    /**
     * Gets all known clones for the given system name
     *
     * @param systemName System name from which the known clones will be
     * returned
     * @return List of known clones for the given system name
     */
    public Collection<RepositoryInfo> getClonesForSystem(String systemName) throws DyeVCException {
        if (!repositoryMap.containsKey(systemName)) {
            throw new DyeVCException("System " + systemName + " is not a known system name.");
        }
        return repositoryMap.get(systemName).values();
    }
    // </editor-fold>

    /**
     * Return all known systems in the topology
     *
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
        if (!repositoryMap.containsKey(systemName)) {
            throw new DyeVCException("System " + systemName + " is not a known system name.");
        }

        ArrayList<CloneRelationship> cis = new ArrayList<CloneRelationship>();
        CloneMap map = repositoryMap.get(systemName);
        for (RepositoryInfo repositoryInfo : map.values()) {
            //Clonekey of "pullsFrom" is the origin and this cloneInfo is the destination
            for (String cloneKey : repositoryInfo.getPullsFrom()) {
                PullRelationship cloneRelationship = new PullRelationship(map.get(cloneKey), repositoryInfo);
                cis.add(cloneRelationship);
            }
            // RepositoryKey of "pushesTo" is the destination and this cloneInfo is the origin
            for (String cloneKey : repositoryInfo.getPushesTo()) {
                PushRelationship cloneRelationship = new PushRelationship(repositoryInfo, map.get(cloneKey));
                cis.add(cloneRelationship);
            }
        }
        return cis;
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
    public void removeCloneInfo(String systemName, String cloneKey) {
        if (repositoryMap.containsKey(systemName)) {
            repositoryMap.get(systemName).remove(cloneKey);
        }
    }

    /**
     * A map of clones of a repository, where each key is a pair of hostname and
     * clone name, and each value contains information regarding a clone.
     *
     * @author Cristiano
     */
    @SuppressWarnings("serial")
    private class CloneMap extends HashMap<String, RepositoryInfo> {
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
