package br.uff.ic.dyevc.monitor;

import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.ServiceException;
import br.uff.ic.dyevc.gui.MainWindow;
import br.uff.ic.dyevc.gui.MessageManager;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.topology.RepositoryFilter;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.model.topology.RepositoryKey;
import br.uff.ic.dyevc.model.topology.Topology;
import br.uff.ic.dyevc.persistence.TopologyDAO;
import br.uff.ic.dyevc.tools.vcs.git.GitConnector;
import br.uff.ic.dyevc.utils.PreferencesUtils;
import br.uff.ic.dyevc.utils.StringUtils;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.slf4j.LoggerFactory;

/**
 * This class continuously monitors the topology, according to the specified
 * refresh rate.
 *
 * @author Cristiano
 */
public class TopologyMonitor extends Thread {

    private ApplicationSettingsBean settings;
    private MainWindow container;
    private ArrayList<RepositoryInfo> repsToProcess;
    private TopologyDAO topologyDAO;

    /**
     * Associates the specified window container and continuously monitors the
     * topology.
     *
     * @param container the container for this monitor. It will be used to send
     * messages during monitoring.
     */
    public TopologyMonitor(MainWindow container) {
        LoggerFactory.getLogger(TopologyMonitor.class).trace("Constructor -> Entry.");
        settings = PreferencesUtils.loadPreferences();
        topologyDAO = new TopologyDAO();
        this.container = container;
        this.start();
        LoggerFactory.getLogger(TopologyMonitor.class).trace("Constructor -> Exit.");
    }

    /**
     * Runs the monitor. After running, monitor sleeps for the time specified by
     * the refresh interval application property.
     */
    @Override
    public void run() {
        LoggerFactory.getLogger(TopologyMonitor.class).trace("Topology monitor is running.");
        int sleepTime = settings.getRefreshInterval() * 1000;
        while (true) {
            try {
                MessageManager.getInstance().addMessage("Topology monitor is running.");
                updateLocalTopology();
                MessageManager.getInstance().addMessage("Topology monitor is sleeping.");
                Thread.sleep(sleepTime);
                LoggerFactory.getLogger(TopologyMonitor.class).debug("Waking up after sleeping for {} seconds.", sleepTime);
            } catch (DyeVCException dex) {
                try {
                    MessageManager.getInstance().addMessage(dex.getMessage());
                    Thread.sleep(settings.getRefreshInterval() * 1000);
                    LoggerFactory.getLogger(TopologyMonitor.class).debug("Waking up after sleeping for {} seconds.", sleepTime);
                } catch (InterruptedException ex) {
                    LoggerFactory.getLogger(TopologyMonitor.class).info("Waking up due to interruption received.");
                }
            } catch (RuntimeException re) {
                try {
                    MessageManager.getInstance().addMessage(re.getMessage());
                    LoggerFactory.getLogger(TopologyMonitor.class).error("Error during monitoring.", re);
                    Thread.sleep(sleepTime);
                    LoggerFactory.getLogger(TopologyMonitor.class).debug("Waking up after sleeping for {} seconds.", sleepTime);
                } catch (InterruptedException ex) {
                    LoggerFactory.getLogger(TopologyMonitor.class).info("Waking up due to interruption received.");
                }
            } catch (InterruptedException ex) {
                LoggerFactory.getLogger(TopologyMonitor.class).info("Waking up due to interruption received.");
            }
        }
    }
    
    /**
     * Creates a subset of the topology containing the local known monitored repositories
     * and the remote repositories related to the local repositories that were not
     * found in the database. This list is then sent to the database, updating the
     * existing repositories and inserting the ones that do not exist.
     * 
     * @throws DyeVCException 
     */
    private void updateLocalTopology() throws DyeVCException {
        LoggerFactory.getLogger(TopologyMonitor.class).trace("updateLocalTopology -> Entry.");
        Topology localTopology = new Topology();
        ArrayList<RepositoryInfo> repositories = new ArrayList<RepositoryInfo>();
        repsToProcess = new ArrayList<RepositoryInfo>();

        for (MonitoredRepository monitoredRepository : MonitoredRepositories.getMonitoredProjects()) {
            try {
                if (!monitoredRepository.hasSystemName()) {
                    MessageManager.getInstance().addMessage("Clone <" + monitoredRepository.getName()
                            + "> has no system name configured and will not be added to the topology.");
                    continue;
                }
                RepositoryInfo info = new RepositoryInfo();
                info.setId(monitoredRepository.getId());
                info.setSystemName(monitoredRepository.getSystemName());
                info.setCloneName(monitoredRepository.getName());
                info.setClonePath(monitoredRepository.getNormalizedCloneAddress());
                info.setHostName(getLocalHostname());
                verifyRelationships(monitoredRepository, info);
                repositories.add(info);
            } catch (UnknownHostException ex) {
                LoggerFactory.getLogger(TopologyMonitor.class).error("Exception trying to read configuration for clone <."
                        + monitoredRepository.getName() + ">", ex);
                throw new DyeVCException("Exception trying to read configuration for clone <."
                        + monitoredRepository.getName() + ">", ex);
            }
        }

        for (RepositoryInfo rep : repsToProcess) {
            repositories.add(rep);
        }
        localTopology.resetTopology(repositories);
        sendTopologyToDatabase(localTopology);
        LoggerFactory.getLogger(TopologyMonitor.class).trace("updateLocalTopology -> Exit.");
    }

    /**
     * Verifies the relations between the specified monitored repository and other
     * clones, pushing to or pulling from them. The relations are discovered by 
     * looking at the git configuration file
     * @param monitoredRepository The monitored repository for which the relations
     * will be verified
     * @param info The repository info for the topology, where the relations will
     * be inserted
     * @throws DyeVCException
     * @throws UnknownHostException 
     */
    private void verifyRelationships(MonitoredRepository monitoredRepository, RepositoryInfo info) throws DyeVCException, UnknownHostException {
        if (!GitConnector.isValidRepository(monitoredRepository.getCloneAddress())) {
            throw new DyeVCException("<" + monitoredRepository.getCloneAddress() + "> is not a valid repository path.");
        }

        List<RemoteConfig> configs = monitoredRepository.getConnection().getRemoteConfigs();
        for (RemoteConfig config : configs) {
            List<URIish> pushUris = config.getPushURIs();
            boolean createOnlyPushUris = pushUris.size() > 0;

            for (URIish pushUri : config.getPushURIs()) {
                addRelationship(info, pushUri, createOnlyPushUris);
            }
            for (URIish uri : config.getURIs()) {
                addRelationship(info, uri, createOnlyPushUris);
            }
        }
    }

    /**
     * Finds out the clone name of a referenced repository. If repository is not
     * local, tries to find this information in the database
     *
     * @param info The Repository that references this repository
     * @param uri The URIish that points to this repository
     * @param createOnlyPushRelation If true, create only a PushesTo
     * relationship. Otherwise, create both PushesTo and PullsFrom relationships
     * @throws ServiceException
     * @throws UnknownHostException
     */
    private void addRelationship(RepositoryInfo info, URIish uri, boolean createOnlyPushRelation) throws ServiceException, UnknownHostException {
        String cloneName = null;
        String scheme = uri.getScheme();
        String hostName = uri.getHost();
        boolean isLocal = (scheme == null && hostName == null)
                || (hostName != null && (hostName.equalsIgnoreCase("localhost")
                || hostName.equals("127.0.0.1")));
        if (isLocal) {
            hostName = getLocalHostname();
        }
        
        //Takes out leading slashes and changes double backslashes by slashes
        String strippedPath = StringUtils.normalizePath(uri.getPath());
        //Remove ".git" in the end of the path
        if (strippedPath.endsWith(GitConnector.GIT_DIR)) {
            strippedPath = strippedPath.substring(0, strippedPath.lastIndexOf(GitConnector.GIT_DIR));
        }

        if (isLocal) {
            //Checks if there is a monitored repository to get the clone name from
            MonitoredRepository rep = MonitoredRepositories.getMonitoredProjectByPath(uri.getPath());
            if (rep != null) {
                cloneName = rep.getName();
            }
        } else {
            //Checks if there is a repository in the database to get the clone name from
            RepositoryFilter filter = new RepositoryFilter();
            filter.setHostName(hostName);
            filter.setClonePath(strippedPath);
            List<RepositoryInfo> rep = topologyDAO.getRepositoriesByQuery(filter);

            if (!rep.isEmpty()) {
                cloneName = rep.get(0).getCloneName();
            } else {
                cloneName = strippedPath;
                addRepositoryToProcess(info, hostName, strippedPath);
            }
        }
        //if clone name is still null, add a new repository to be sent to database
        if (cloneName == null) {
            cloneName = strippedPath;
            addRepositoryToProcess(info, hostName, strippedPath);
        }

        RepositoryKey key = new RepositoryKey(hostName, cloneName);
        info.addPushesTo(key);
        if (!createOnlyPushRelation) {
            info.addPullsFrom(key);
        }
    }

    /**
     * Gets the hostname of the local computer, in canonical form (hostname + connection suffix)
     * @return The hostname of the local computer
     * @throws UnknownHostException 
     */
    private String getLocalHostname() throws UnknownHostException {
        return InetAddress.getLocalHost().getCanonicalHostName().toLowerCase();
    }

    /**
     * Adds a remote repository (toProcess) to be included in the database.
     * @param info The repository with which toProcess relates to
     * @param hostName The hostname of the remote repository
     * @param strippedPath The Path to the remote repository (also used as its clone name)
     */
    private void addRepositoryToProcess(RepositoryInfo info, String hostName, String strippedPath) {
        //Creates a new repository info to be sent to database. 
        RepositoryInfo toProcess = new RepositoryInfo();
        toProcess.setId(StringUtils.generateRepositoryId());
        toProcess.setSystemName(info.getSystemName());
        toProcess.setHostName(hostName);
        toProcess.setClonePath(strippedPath);
        toProcess.setCloneName(strippedPath);
        repsToProcess.add(toProcess);
    }

    /**
     * Updates the local topology information in the database
     * @param localTopology The topology to be updated
     */
    private void sendTopologyToDatabase(Topology localTopology) throws DyeVCException {
        topologyDAO.updateTopology(localTopology);
    }
}
