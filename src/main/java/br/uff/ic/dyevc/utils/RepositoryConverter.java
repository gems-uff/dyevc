package br.uff.ic.dyevc.utils;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.ServiceException;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.topology.RepositoryFilter;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.persistence.TopologyDAO;
import br.uff.ic.dyevc.tools.vcs.git.GitConnector;

import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

//~--- JDK imports ------------------------------------------------------------

import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Conversion between MonitoredRepository and RepositoryInfo
 *
 * @author Cristiano
 */
public class RepositoryConverter {
    private final MonitoredRepository       monitoredRepository;
    private final TopologyDAO               topologyDAO;
    private final ArrayList<RepositoryInfo> relatedNew;
    private String                          relatedSystem;
    private final RepositoryInfo            info;
    private boolean                         processed;
    private final HashMap<URIish, String>   uriishToIdMap;

    /**
     * Constructs ...
     *
     * @param monitoredRepository
     */
    public RepositoryConverter(MonitoredRepository monitoredRepository) {
        this.topologyDAO         = new TopologyDAO();
        this.relatedNew          = new ArrayList<RepositoryInfo>();

        this.relatedSystem       = monitoredRepository.getSystemName();
        this.monitoredRepository = monitoredRepository;
        this.processed           = false;
        this.info                = new RepositoryInfo();
        this.uriishToIdMap       = new HashMap<URIish, String>();

        info.setId(monitoredRepository.getId());
        info.setSystemName(monitoredRepository.getSystemName());
        info.setCloneName(monitoredRepository.getName());
        info.setClonePath(monitoredRepository.getNormalizedCloneAddress());
        info.setHostName(SystemUtils.getLocalHostname());
        info.setLastChanged(monitoredRepository.getLastChanged());
    }

    public RepositoryInfo toRepositoryInfo() throws DyeVCException {
        if (!processed) {
            initialize();
        }

        return info;
    }

    private void initialize() throws DyeVCException {
        verifyRelationships();
        processed = true;
    }

    public String getRelatedSystem() throws DyeVCException {
        if (!processed) {
            initialize();
        }

        return relatedSystem;
    }

    /**
     * Returns the repositories related to this one that do not previously exist
     * in the database
     *
     * @return The list of non previously existing related repositories
     * @throws DyeVCException
     */
    public ArrayList<RepositoryInfo> getRelatedNewList() throws DyeVCException {
        if (!processed) {
            initialize();
        }

        return relatedNew;
    }

    /**
     * Verifies the relations between the converted repository info and other
     * clones, pushing to or pulling from them. The relations are discovered by
     * looking at the git configuration file
     *
     * @throws DyeVCException
     * @throws UnknownHostException
     */
    private void verifyRelationships() throws DyeVCException {
        if (!GitConnector.isValidRepository(monitoredRepository.getCloneAddress())) {
            throw new DyeVCException("<" + monitoredRepository.getCloneAddress() + "> is not a valid repository path.");
        }

        List<RemoteConfig> configs = monitoredRepository.getConnection().getRemoteConfigs();
        for (RemoteConfig config : configs) {
            List<URIish> pushUris       = config.getPushURIs();
            int          pushUrisSize   = pushUris.size();
            boolean      createPushUris = pushUrisSize == 0;

            for (URIish pushUri : config.getPushURIs()) {
                addRelationship(pushUri, true, false);
            }

            for (URIish uri : config.getURIs()) {
                addRelationship(uri, createPushUris, true);
            }
        }
    }

    /**
     * Finds out the clone name of a referenced repository. If repository is not
     * local, tries to find this information in the database.
     *
     * @param uri The URIish that points to this repository
     * @param createOnlyPushRelation If true, create only a PushesTo
     * relationship. Otherwise, create both PushesTo and PullsFrom relationships
     * @throws ServiceException
     */
    private void addRelationship(URIish uri, boolean createPushUri, boolean createPullUri) throws ServiceException {
        String id;
        if (uriishToIdMap.containsKey(uri)) {
            id = uriishToIdMap.get(uri);
        } else {
            id = getIdFromUri(uri);
            uriishToIdMap.put(uri, id);
        }

        if (createPushUri) {
            info.addPushesTo(id);
        }

        if (createPullUri) {
            info.addPullsFrom(id);
        }
    }

    /**
     * Gets the id of a repository from an URIIsh object. If id cannot be identified, than creates a new repository.
     * @param uri The URIIsh to be used to find out the repository id
     * @return The id corresponding to the specified URIIsh
     * @throws ServiceException
     */
    private String getIdFromUri(URIish uri) throws ServiceException {
        String  id;
        String  scheme   = uri.getScheme();
        String  hostName = uri.getHost();
        boolean isLocal  = ((scheme == null) && (hostName == null))
                           || ((hostName != null)
                               && (hostName.equalsIgnoreCase("localhost") || hostName.equals("127.0.0.1")));
        if (isLocal) {
            hostName = SystemUtils.getLocalHostname();
        }

        // Takes out leading slashes and changes double backslashes by slashes
        String strippedPath = StringUtils.normalizePath(uri.getPath());

        // Remove ".git" in the end of the path
        if (strippedPath.endsWith(GitConnector.GIT_DIR)) {
            strippedPath = strippedPath.substring(0, strippedPath.lastIndexOf(GitConnector.GIT_DIR));
        }

        // Checks if there is a monitored repository to get the clone name from
        MonitoredRepository rep = MonitoredRepositories.getMonitoredProjectByPath(uri.getPath());
        if (rep != null) {
            id = rep.getId();

            if (!rep.getSystemName().equals(info.getSystemName())) {
                relatedSystem = rep.getSystemName();
            }
        } else {

            // If not, checks if there is a repository in the database to get the clone name from
            RepositoryFilter filter = new RepositoryFilter();
            filter.setHostName(hostName);
            filter.setClonePath(strippedPath);
            List<RepositoryInfo> listRepo = topologyDAO.getRepositoriesByQuery(filter);

            if (!listRepo.isEmpty()) {
                id = listRepo.get(0).getId();
                String system = listRepo.get(0).getSystemName();
                if (!system.equals(info.getSystemName())) {
                    relatedSystem = system;
                }
            } else {

                // if not, adds a new repository that is referenced but not monitored
                String cloneName = SystemUtils.getFilenameOrLastPath(strippedPath);
                id = addNewRelatedRepository(hostName, cloneName, strippedPath).getId();
            }
        }

        return id;
    }

    /**
     * Adds a remote repository (toProcess) to be included in the database.
     *
     * @param hostName The hostName of the remote repository
     * @param strippedPath The Path to the remote repository (also used as its
     * clone name)
     */
    private RepositoryInfo addNewRelatedRepository(String hostName, String cloneName, String strippedPath) {

        // Creates a new repository info to be sent to database.
        RepositoryInfo toProcess = new RepositoryInfo();
        toProcess.setId(StringUtils.generateRepositoryId());
        toProcess.setSystemName(info.getSystemName());
        toProcess.setHostName(hostName);
        toProcess.setClonePath(strippedPath);
        toProcess.setCloneName(cloneName);
        relatedNew.add(toProcess);

        return toProcess;
    }

    /**
     * Returns the repository unique id related to the specified uri
     * @param uri The uri of the repository to return the id. The URIish is the one set in git's config file.
     * @return The repository id that maps to the specified uri.
     * @throws DyeVCException
     */
    public String mapUriToRepositoryId(URIish uri) throws DyeVCException {
        if (!processed) {
            initialize();
        }

        return uriishToIdMap.get(uri);
    }
}
