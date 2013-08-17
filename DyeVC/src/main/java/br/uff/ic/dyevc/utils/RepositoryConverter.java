package br.uff.ic.dyevc.utils;

import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.ServiceException;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.topology.RepositoryFilter;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.persistence.TopologyDAO;
import br.uff.ic.dyevc.tools.vcs.git.GitConnector;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

/**
 * Conversion between MonitoredRepository and RepositoryInfo
 *
 * @author Cristiano
 */
public class RepositoryConverter {

    private MonitoredRepository monitoredRepository;
    private TopologyDAO topologyDAO;
    private ArrayList<RepositoryInfo> relatedNew;
    private RepositoryInfo info;
    private boolean processed;

    public RepositoryConverter(MonitoredRepository monitoredRepository) {
        this.topologyDAO = new TopologyDAO();
        this.relatedNew = new ArrayList<RepositoryInfo>();
        this.monitoredRepository = monitoredRepository;
        this.processed = false;
        this.info = new RepositoryInfo();

        info.setId(monitoredRepository.getId());
        info.setSystemName(monitoredRepository.getSystemName());
        info.setCloneName(monitoredRepository.getName());
        info.setClonePath(monitoredRepository.getNormalizedCloneAddress());
        info.setHostName(SystemUtils.getLocalHostname());
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

    /**
     * Returns the repositories related to this one that do not previously exist
     * in the database
     *
     * @return The list of non previously existing related repositories
     * @throws DyeVCException
     */
    public ArrayList<RepositoryInfo> getRelatedNew() throws DyeVCException {
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
            List<URIish> pushUris = config.getPushURIs();
            boolean createOnlyPushUris = pushUris.size() > 0;

            for (URIish pushUri : config.getPushURIs()) {
                addRelationship(pushUri, createOnlyPushUris);
            }
            for (URIish uri : config.getURIs()) {
                addRelationship(uri, createOnlyPushUris);
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
    private void addRelationship(URIish uri, boolean createOnlyPushRelation) throws ServiceException {
        String id;
        String scheme = uri.getScheme();
        String hostName = uri.getHost();
        boolean isLocal = (scheme == null && hostName == null)
                || (hostName != null && (hostName.equalsIgnoreCase("localhost")
                || hostName.equals("127.0.0.1")));
        if (isLocal) {
            hostName = SystemUtils.getLocalHostname();
        }

        //Takes out leading slashes and changes double backslashes by slashes
        String strippedPath = StringUtils.normalizePath(uri.getPath());
        //Remove ".git" in the end of the path
        if (strippedPath.endsWith(GitConnector.GIT_DIR)) {
            strippedPath = strippedPath.substring(0, strippedPath.lastIndexOf(GitConnector.GIT_DIR));
        }

        //Checks if there is a monitored repository to get the clone name from
        MonitoredRepository rep = MonitoredRepositories.getMonitoredProjectByPath(uri.getPath());
        if (rep != null) {
            id = rep.getId();
        } else {
            //If not, checks if there is a repository in the database to get the clone name from
            RepositoryFilter filter = new RepositoryFilter();
            filter.setHostName(hostName);
            filter.setClonePath(strippedPath);
            List<RepositoryInfo> listRepo = topologyDAO.getRepositoriesByQuery(filter);

            if (!listRepo.isEmpty()) {
                id = listRepo.get(0).getId();
            } else {
                //if not, adds a new repository that is referenced but not monitored
                String cloneName = SystemUtils.getFilenameOrLastPath(strippedPath);
                id = addNewRelatedRepository(hostName, cloneName, strippedPath).getId();
            }
        }

        info.addPushesTo(id);
        if (!createOnlyPushRelation) {
            info.addPullsFrom(id);
        }
    }

    /**
     * Adds a remote repository (toProcess) to be included in the database.
     *
     * @param hostName The hostName of the remote repository
     * @param strippedPath The Path to the remote repository (also used as its
     * clone name)
     */
    private RepositoryInfo addNewRelatedRepository(String hostName, String cloneName, String strippedPath) {
        //Creates a new repository info to be sent to database. 
        RepositoryInfo toProcess = new RepositoryInfo();
        toProcess.setId(StringUtils.generateRepositoryId());
        toProcess.setSystemName(info.getSystemName());
        toProcess.setHostName(hostName);
        toProcess.setClonePath(strippedPath);
        toProcess.setCloneName(cloneName);
        relatedNew.add(toProcess);
        return toProcess;
    }
}
