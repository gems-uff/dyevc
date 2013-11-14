package br.uff.ic.dyevc.monitor;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.MonitorException;
import br.uff.ic.dyevc.exception.RepositoryReferencedException;
import br.uff.ic.dyevc.gui.core.MessageManager;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.topology.CommitFilter;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.model.topology.Topology;
import br.uff.ic.dyevc.persistence.CommitDAO;
import br.uff.ic.dyevc.persistence.TopologyDAO;
import br.uff.ic.dyevc.tools.vcs.git.GitCommitTools;
import br.uff.ic.dyevc.utils.RepositoryConverter;

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * This class updates the topology.
 *
 * @author Cristiano
 */
public class TopologyUpdater {
    private final TopologyDAO   topologyDAO;
    private Topology            topology;
    MonitoredRepositories       monitoredRepositories;
    RepositoryConverter         converter;
    private MonitoredRepository repositoryToUpdate;
    private final Object        lock = new Object();
    private Boolean             running;

    /**
     * Associates the specified window container and continuously monitors the topology.
     *
     */
    public TopologyUpdater() {
        LoggerFactory.getLogger(TopologyUpdater.class).trace("Constructor -> Entry.");
        topologyDAO                = new TopologyDAO();
        this.monitoredRepositories = MonitoredRepositories.getInstance();
        running                    = false;
        LoggerFactory.getLogger(TopologyUpdater.class).trace("Constructor -> Exit.");
    }

    /**
     * Updates the topology.
     */
    public void update(MonitoredRepository repositoryToUpdate) throws MonitorException {
        synchronized (lock) {
            if (running) {
                throw new MonitorException("Topology updater is currently running. Try again later.");
            }

            running = true;
        }

        LoggerFactory.getLogger(TopologyUpdater.class).trace("Topology updater is running.");

        if (!repositoryToUpdate.hasSystemName()) {
            MessageManager.getInstance().addMessage("Repository <" + repositoryToUpdate.getName() + "> with id <"
                    + repositoryToUpdate.getId()
                    + "> has no system name configured and will not be added to the topology.");

            return;
        }

        this.repositoryToUpdate = repositoryToUpdate;
        this.converter          = new RepositoryConverter(repositoryToUpdate);

        MessageManager.getInstance().addMessage("Updating topology for repository <" + repositoryToUpdate.getId()
                + "> with id <" + repositoryToUpdate.getName() + ">");

        updateRepositoryTopology();
        updateCommitTopology();

        MessageManager.getInstance().addMessage("Finished update topology for repository <"
                + repositoryToUpdate.getId() + "> with id <" + repositoryToUpdate.getName() + ">");
        running = false;
        LoggerFactory.getLogger(TopologyUpdater.class).trace("Topology updater finished running.");
    }

    /**
     * Updates the topology for the specified repository in the database, including any new referenced repositories
     * that do not yet exist and refreshes local topology cache.
     *
     * @see RepositoryConverter
     *
     * @throws DyeVCException
     */
    private void updateRepositoryTopology() {
        LoggerFactory.getLogger(TopologyUpdater.class).trace("updateRepositoryTopology -> Entry.");

        try {
            Date lastChanged = topologyDAO.upsertRepository(converter.toRepositoryInfo());
            topologyDAO.upsertRepositories(converter.getRelatedNewList());
            repositoryToUpdate.setLastChanged(lastChanged);
            topology = topologyDAO.readTopologyForSystem(repositoryToUpdate.getSystemName());
        } catch (DyeVCException dex) {
            MessageManager.getInstance().addMessage("Error updating repository<" + repositoryToUpdate.getName()
                    + "> with id<" + repositoryToUpdate.getId() + ">\n\t" + dex.getMessage());
        } catch (RuntimeException re) {
            MessageManager.getInstance().addMessage("Error updating repository<" + repositoryToUpdate.getName()
                    + "> with id<" + repositoryToUpdate.getId() + ">\n\t" + re.getMessage());
            LoggerFactory.getLogger(TopologyUpdater.class).error("Error during topology update.", re);
        }

        LoggerFactory.getLogger(TopologyUpdater.class).trace("updateRepositoryTopology -> Exit.");
    }

    /**
     * Updates the topology for the specified repository sending any new commits found both to the repository and to
     * referenced repositories. The following logic is used:<br><br>
     * <ul>
     *      <li></li>
     * </ul>
     * @throws DyeVCException
     */
    private void updateCommitTopology() {
        LoggerFactory.getLogger(TopologyUpdater.class).trace("updateCommitTopology -> Entry.");

//      repositoryToUpdate.getRepStatus().getNonSyncedRepositoryBranches().get(0).getReferencedRemote()

        try {
            CommitDAO    dao    = new CommitDAO();
            CommitFilter filter = new CommitFilter();
            filter.setSystemName("dyevc");

            // Gets all known commit hashes for the system in the topology
            Set<CommitInfo>       remoteHashes = dao.getCommitHashesByQuery(filter);
            ArrayList<CommitInfo> toUpdate     = new ArrayList<CommitInfo>();

            GitCommitTools        commitTools  = GitCommitTools.getInstance(repositoryToUpdate, true);
            commitTools.setConnection(repositoryToUpdate.getWorkingCloneConnection());
            commitTools.loadExternalCommits(converter.toRepositoryInfo());
            ArrayList workingHashes = (ArrayList)commitTools.getCommitInfos();

            System.out.println("commits updated.");

        } catch (DyeVCException dex) {
            MessageManager.getInstance().addMessage("Error updating commits for repository<"
                    + repositoryToUpdate.getName() + "> with id<" + repositoryToUpdate.getId() + ">\n\t"
                    + dex.getMessage());
        } catch (RuntimeException re) {
            MessageManager.getInstance().addMessage("Error updating commits for repository<"
                    + repositoryToUpdate.getName() + "> with id<" + repositoryToUpdate.getId() + ">\n\t"
                    + re.getMessage());
            LoggerFactory.getLogger(TopologyUpdater.class).error("Error during commits update.", re);
        }

        LoggerFactory.getLogger(TopologyUpdater.class).trace("updateCommitTopology -> Exit.");
    }

    /**
     * Verifies if the local monitored repositories marked for deletion are referenced in the topology. If not, delete
     * them.
     *
     * @throws DyeVCException
     */
    void verifyDeletedRepositories() throws DyeVCException {
        LoggerFactory.getLogger(TopologyUpdater.class).trace("verifyDeletedRepositories -> Entry.");

        for (Iterator<MonitoredRepository> it =
                MonitoredRepositories.getMarkedForDeletion().iterator(); it.hasNext(); ) {
            MonitoredRepository monitoredRepository = it.next();
            if (!monitoredRepository.hasSystemName()) {
                MessageManager.getInstance().addMessage("Repository <" + monitoredRepository.getName() + "> with id <"
                        + monitoredRepository.getId()
                        + "> has no system name configured and will not be added to the topology.");

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

                LoggerFactory.getLogger(TopologyUpdater.class).warn(message.toString());
            } catch (RuntimeException re) {
                StringBuilder message = new StringBuilder();
                message.append("Repository <").append(monitoredRepository.getName()).append("> with id <").append(
                    monitoredRepository.getId()).append("> could not be deleted due to the following error: ").append(
                    re.getMessage());
                LoggerFactory.getLogger(TopologyUpdater.class).warn(message.toString(), re);
            }
        }

        LoggerFactory.getLogger(TopologyUpdater.class).trace("verifyDeletedRepositories -> Exit.");
    }
}
