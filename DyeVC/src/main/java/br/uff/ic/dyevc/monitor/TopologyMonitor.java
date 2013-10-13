package br.uff.ic.dyevc.monitor;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.RepositoryReferencedException;
import br.uff.ic.dyevc.gui.core.MessageManager;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.persistence.TopologyDAO;
import br.uff.ic.dyevc.utils.PreferencesUtils;
import br.uff.ic.dyevc.utils.RepositoryConverter;

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.Iterator;

/**
 * This class continuously monitors the topology, according to the specified
 * refresh rate.
 *
 * @author Cristiano
 */
public class TopologyMonitor extends Thread {
    private ApplicationSettingsBean settings;
    private TopologyDAO             topologyDAO;
    MonitoredRepositories           monitoredRepositories;
    private MonitoredRepository     repositoryToMonitor;

    /**
     * Associates the specified window container and continuously monitors the
     * topology.
     *
     */
    public TopologyMonitor(MonitoredRepositories monitoredRepositories) {
        LoggerFactory.getLogger(TopologyMonitor.class).trace("Constructor -> Entry.");
        settings                   = PreferencesUtils.loadPreferences();
        topologyDAO                = new TopologyDAO();
        this.monitoredRepositories = monitoredRepositories;
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

                if (repositoryToMonitor == null) {
                    for (MonitoredRepository monitoredRepository : MonitoredRepositories.getMonitoredProjects()) {
                        updateLocalTopology(monitoredRepository);
                    }

                    verifyDeletedRepositories();
                } else {
                    updateLocalTopology(repositoryToMonitor);
                    setRepositoryToMonitor(null);
                }

                MessageManager.getInstance().addMessage("Topology monitor is sleeping.");
                Thread.sleep(sleepTime);
                LoggerFactory.getLogger(TopologyMonitor.class).debug("Waking up after sleeping for {} seconds.",
                                        sleepTime);
            } catch (DyeVCException dex) {
                try {
                    MessageManager.getInstance().addMessage(dex.getMessage());
                    Thread.sleep(settings.getRefreshInterval() * 1000);
                    LoggerFactory.getLogger(TopologyMonitor.class).debug("Waking up after sleeping for {} seconds.",
                                            sleepTime);
                } catch (InterruptedException ex) {
                    LoggerFactory.getLogger(TopologyMonitor.class).info("Waking up due to interruption received.");
                }
            } catch (RuntimeException re) {
                try {
                    MessageManager.getInstance().addMessage(re.getMessage());
                    LoggerFactory.getLogger(TopologyMonitor.class).error("Error during monitoring.", re);
                    Thread.sleep(sleepTime);
                    LoggerFactory.getLogger(TopologyMonitor.class).debug("Waking up after sleeping for {} seconds.",
                                            sleepTime);
                } catch (InterruptedException ex) {
                    LoggerFactory.getLogger(TopologyMonitor.class).info("Waking up due to interruption received.");
                }
            } catch (InterruptedException ex) {
                LoggerFactory.getLogger(TopologyMonitor.class).info("Waking up due to interruption received.");
            }
        }
    }

    /**
     * Creates a list with a subset of the topology containing the specified
     * monitored repository and the remote repositories related to it. This list
     * is then sent to the database, updating the existing repositories and
     * inserting the ones that do not exist.
     *
     * @throws DyeVCException
     */
    private void updateLocalTopology(MonitoredRepository monitoredRepository) throws DyeVCException {
        LoggerFactory.getLogger(TopologyMonitor.class).trace("updateLocalTopology -> Entry.");

        if (!monitoredRepository.hasSystemName()) {
            MessageManager.getInstance().addMessage("Clone <" + monitoredRepository.getName()
                    + "> has no system name configured and will not be added to the topology.");

            return;
        }

        RepositoryConverter converter = new RepositoryConverter(monitoredRepository);
        topologyDAO.upsertRepository(converter.toRepositoryInfo());
        topologyDAO.upsertRepositories(converter.getRelatedNew());

        LoggerFactory.getLogger(TopologyMonitor.class).trace("updateLocalTopology -> Exit.");
    }

    /**
     * Verifies if the local monitored repositories marked for deletion are
     * referenced in the topology. If not, delete them.
     *
     * @throws DyeVCException
     */
    private void verifyDeletedRepositories() throws DyeVCException {
        LoggerFactory.getLogger(TopologyMonitor.class).trace("removeMarkedForDeletion -> Entry.");

        for (Iterator<MonitoredRepository> it =
                MonitoredRepositories.getMarkedForDeletion().iterator(); it.hasNext(); ) {
            MonitoredRepository monitoredRepository = it.next();
            if (!monitoredRepository.hasSystemName()) {
                MessageManager.getInstance().addMessage("Clone <" + monitoredRepository.getName()
                        + "> has no system name configured and will cannot be removed from the topology.");

                continue;
            }

            try {
                monitoredRepositories.removeMarkedForDeletion(monitoredRepository);
            } catch (RepositoryReferencedException rre) {
                StringBuilder message = new StringBuilder();
                message.append("Repository <").append(monitoredRepository.getName()).append("> with id <").append(
                    monitoredRepository.getId()).append(
                    "> could not be deleted because it is still referenced by the following clone(s): ");

                for (RepositoryInfo info : rre.getRelatedRepositories()) {
                    message.append("\n<").append(info.getCloneName()).append(">, id: <").append(info.getId()).append(
                        ">, located at host <").append(info.getHostName()).append(">");
                }

                LoggerFactory.getLogger(TopologyMonitor.class).warn(message.toString());
            }
        }

        LoggerFactory.getLogger(TopologyMonitor.class).trace("removeMarkedForDeletion -> Exit.");
    }

    /**
     * @param repositoryToMonitor the repositoryToMonitor to set
     */
    public synchronized void setRepositoryToMonitor(MonitoredRepository repos) {
        repositoryToMonitor = repos;
    }
}
