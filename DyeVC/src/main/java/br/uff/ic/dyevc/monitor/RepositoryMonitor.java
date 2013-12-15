package br.uff.ic.dyevc.monitor;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.gui.core.MessageManager;
import br.uff.ic.dyevc.gui.main.MainWindow;
import br.uff.ic.dyevc.model.BranchStatus;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.RepositoryStatus;
import br.uff.ic.dyevc.tools.vcs.git.GitConnector;
import br.uff.ic.dyevc.tools.vcs.git.GitTools;
import br.uff.ic.dyevc.utils.ApplicationVersionUtils;
import br.uff.ic.dyevc.utils.PreferencesManager;

import org.apache.commons.io.FileUtils;

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class continuously monitors the specified repositories, according to the specified refresh rate.
 *
 * @author Cristiano
 */
public class RepositoryMonitor extends Thread {
    private final ApplicationSettingsBean   settings;
    private List<RepositoryStatus>          statusList;
    private MainWindow                      container;
    private final List<MonitoredRepository> monitorQueue =
        Collections.synchronizedList(new ArrayList<MonitoredRepository>());
    private final List<MonitoredRepository> cleanAndMonitorQueue =
        Collections.synchronizedList(new ArrayList<MonitoredRepository>());
    private MonitoredRepository      repositoryToMonitor;
    private final String             currentApplicationVersion;
    private final TopologyUpdater    updater;
    private static RepositoryMonitor instance;

    /**
     * Associates the specified window container and continuously monitors the specified list of repositories.
     *
     */
    private RepositoryMonitor() {
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("Constructor -> Entry.");
        settings                  = PreferencesManager.getInstance().loadPreferences();
        currentApplicationVersion = ApplicationVersionUtils.getAppVersion();
        this.updater              = new TopologyUpdater();
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("Constructor -> Exit.");
    }

    /**
     * Provides the singleton instance
     * @return the singleton instance.
     */
    public synchronized static RepositoryMonitor getInstance() {
        if (instance == null) {
            instance = new RepositoryMonitor();
        }

        return instance;
    }

    public void setContainer(MainWindow container) {
        this.container = container;
    }

    /**
     * Runs the monitor. After running, monitor sleeps for the time specified by the refresh interval application
     * property.
     */
    @Override
    public void run() {
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("Repository monitor is running.");
        int sleepTime = settings.getRefreshInterval() * 1000;
        try {
            checkWorkingFolder();
        } catch (DyeVCException ex) {
            return;
        }

        while (true) {
            try {
                MessageManager.getInstance().addMessage("Repository monitor is running.");

                if (!monitorQueue.isEmpty() ||!cleanAndMonitorQueue.isEmpty()) {
                    // Process pending repositories recently added to configuration
                    while (true) {
                        boolean discardTopologyCache = false;
                        if (!monitorQueue.isEmpty()) {
                            repositoryToMonitor = monitorQueue.remove(0);
                        } else if (!cleanAndMonitorQueue.isEmpty()) {
                            repositoryToMonitor  = cleanAndMonitorQueue.remove(0);
                            discardTopologyCache = true;
                        } else {
                            break;
                        }

                        MessageManager.getInstance().addMessage("Monitoring new repository <"
                                + repositoryToMonitor.getId() + "> with id <" + repositoryToMonitor.getName()
                                + ">. Check console for details.");
                        checkRepository(repositoryToMonitor);

                        if (!repositoryToMonitor.getRepStatus().isInvalid()) {
                            updater.update(repositoryToMonitor, discardTopologyCache);
                        }

                        repositoryToMonitor = null;
                    }
                } else {
                    // Normal processing
                    LoggerFactory.getLogger(RepositoryMonitor.class).info("Found {} repositories to monitor.",
                                            MonitoredRepositories.getMonitoredProjects().size());
                    statusList = new ArrayList<RepositoryStatus>();
                    int count = 0;
                    for (MonitoredRepository monitoredRepository : MonitoredRepositories.getMonitoredProjects()) {
                        MessageManager.getInstance().addMessage("Checking repository " + ++count + " of "
                                + MonitoredRepositories.getMonitoredProjects().size() + ": <"
                                + monitoredRepository.getId() + "> with id <" + monitoredRepository.getName()
                                + ">. Check console for details.");
                        checkRepository(monitoredRepository);

                        if (!monitoredRepository.getRepStatus().isInvalid()) {
                            updater.update(monitoredRepository);
                        }
                    }

                    MessageManager.getInstance().addMessage(
                        "Finished checking monitored repositories. Now verifying deleted repositories.");
                    updater.verifyDeletedRepositories();

                    MessageManager.getInstance().addMessage(
                        "Finished checking deleted repositories. Now persisting new information.");
                    PreferencesManager.getInstance().persistRepositories();

                    if (!(currentApplicationVersion.equals(settings.getLastApplicationVersionUsed()))) {
                        settings.setLastApplicationVersionUsed(currentApplicationVersion);
                        PreferencesManager.getInstance().storePreferences(settings);
                    }
                }

                container.notifyMessages(statusList);
                LoggerFactory.getLogger(RepositoryMonitor.class).debug("Will now sleep for {} seconds.", sleepTime);
                MessageManager.getInstance().addMessage("Repository monitor is sleeping.");
                Thread.sleep(settings.getRefreshInterval() * 1000);
                LoggerFactory.getLogger(RepositoryMonitor.class).debug("Waking up after sleeping for {} seconds.",
                                        sleepTime);
            } catch (DyeVCException dex) {
                try {
                    MessageManager.getInstance().addMessage(dex.getMessage());
                    Thread.sleep(settings.getRefreshInterval() * 1000);
                    LoggerFactory.getLogger(RepositoryMonitor.class).debug("Waking up after sleeping for {} seconds.",
                                            sleepTime);
                } catch (InterruptedException ex) {
                    LoggerFactory.getLogger(RepositoryMonitor.class).info("Waking up due to interruption received.");
                }
            } catch (RuntimeException re) {
                try {
                    MessageManager.getInstance().addMessage(re.getMessage());
                    LoggerFactory.getLogger(RepositoryMonitor.class).error("Error during monitoring.", re);
                    Thread.sleep(settings.getRefreshInterval() * 1000);
                    LoggerFactory.getLogger(RepositoryMonitor.class).debug("Waking up after sleeping for {} seconds.",
                                            sleepTime);
                } catch (InterruptedException ex) {
                    LoggerFactory.getLogger(RepositoryMonitor.class).info("Waking up due to interruption received.");
                }
            } catch (InterruptedException ex) {
                LoggerFactory.getLogger(RepositoryMonitor.class).info("Waking up due to interruption received.");
            }
        }

    }

    /**
     * Checks if a given repository is behind and/or ahead its tracking remotes, populating a status list according to
     * the results.
     *
     * @param monitoredRepository the repository to be checked
     */
    private void checkRepository(MonitoredRepository monitoredRepository) {
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkRepository -> Entry. Repository: {}, id:{}",
                                monitoredRepository.getName(), monitoredRepository.getId());

        RepositoryStatus repStatus    = new RepositoryStatus(monitoredRepository.getId());
        String           cloneAddress = monitoredRepository.getCloneAddress();

        if (!GitConnector.isValidRepository(cloneAddress)) {
            repStatus.setInvalid("<" + cloneAddress + "> is not a valid repository path.");
        } else {
            try {
                List<BranchStatus> status = processRepository(monitoredRepository);
                repStatus.addBranchStatusList(status);
            } catch (VCSException ex) {
                MessageManager.getInstance().addMessage("It was not possible to finish monitoring of repository <"
                        + monitoredRepository.getName() + "> with id <" + monitoredRepository.getId() + ">.");
                LoggerFactory.getLogger(RepositoryMonitor.class).error(
                    "It was not possible to finish monitoring of repository <{}> with id <{}>",
                    monitoredRepository.getName(), monitoredRepository.getId());
                repStatus.setInvalid(ex.getCause().toString());
            }
        }

        monitoredRepository.setRepStatus(repStatus);

        statusList.add(repStatus);

        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkRepository -> Exit. Repository: {}",
                                monitoredRepository.getName());
    }

    /**
     * Process a valid repository to check its behind and ahead status
     *
     * @param monitoredRepository the repository to be processed
     * @return a list of status for the given repository
     */
    private List<BranchStatus> processRepository(MonitoredRepository monitoredRepository) throws VCSException {
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("processRepository -> Entry. Repository: {}",
                                monitoredRepository.getName());
        GitConnector       sourceConnector = null;
        GitConnector       tempConnector   = null;
        List<BranchStatus> result          = null;
        try {

            sourceConnector = monitoredRepository.getConnection();
            LoggerFactory.getLogger(RepositoryMonitor.class).debug(
                "processRepository -> created gitConnector for repository {}, id={}", monitoredRepository.getName(),
                monitoredRepository.getId());

            String pathTemp = monitoredRepository.getWorkingCloneAddress();

            if (!GitConnector.isValidRepository(pathTemp)) {
                LoggerFactory.getLogger(RepositoryMonitor.class).debug(
                    "There is no valid temp repository at {}. Will create a valid temp by cloning {}.", pathTemp,
                    monitoredRepository.getId());
                monitoredRepository.setWorkingCloneConnection(createWorkingClone(pathTemp, sourceConnector));
            }

            tempConnector = monitoredRepository.getWorkingCloneConnection();
            GitTools.adjustTargetConfiguration(sourceConnector, tempConnector);

            tempConnector.fetchAllRemotes(true);
            result = tempConnector.testAhead();
        } finally {
            if (sourceConnector != null) {
                LoggerFactory.getLogger(RepositoryMonitor.class).debug(
                    "About to close connection with repository <{}> with id <{}>", monitoredRepository.getName(),
                    monitoredRepository.getId());
                sourceConnector.close();
            }

            if (tempConnector != null) {
                LoggerFactory.getLogger(RepositoryMonitor.class).debug(
                    "About to close connection with temp repository for <{}> with id <{}>",
                    monitoredRepository.getName(), monitoredRepository.getId());
                tempConnector.close();
            }
        }

        LoggerFactory.getLogger(RepositoryMonitor.class).trace("processRepository -> Exit. Repository: {}",
                                monitoredRepository.getName());

        return result;
    }

    /**
     * Verifies whether the working folder exists or not. If false, creates it, otherwise, looks for orphaned folders
     * related to projects not monitored anymore.
     *
     * @throws DyeVCException
     */
    private void checkWorkingFolder() throws DyeVCException {
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkWorkingFolder -> Entry.");
        File workingFolder = new File(settings.getWorkingPath());
        LoggerFactory.getLogger(RepositoryMonitor.class).info("checkWorkingFolder -> Working folder is at {}.",
                                workingFolder.getAbsoluteFile());

        if (workingFolder.exists()) {
            LoggerFactory.getLogger(RepositoryMonitor.class).debug("Working folder already exists.");
            checkOrphanedFolders(workingFolder);

            if (!workingFolder.canWrite()) {
                LoggerFactory.getLogger(RepositoryMonitor.class).error("Working folder is not writable.");
                MessageManager.getInstance().addMessage(
                    "Working folder is not writable. Please check folder permissions at "
                    + workingFolder.getAbsolutePath());

                throw new DyeVCException("Temp folder located at " + workingFolder.getAbsolutePath()
                                         + " is not writable. Cannot run monitor.");
            }
        } else {
            workingFolder.mkdir();
            LoggerFactory.getLogger(RepositoryMonitor.class).debug(
                "Working folder does not exist. A brand new one was created.");
        }

        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkWorkingFolder -> Exit.");
    }

    /**
     * Deletes folders with clones related to projects that are not monitored anymore.
     *
     * @param pointer to the working folder
     */
    private void checkOrphanedFolders(File workingFolder) {
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkOrphanedFolders -> Entry.");
        String[] tmpFolders = workingFolder.list();
        for (String tmpFolder : tmpFolders) {
            if (MonitoredRepositories.getMonitoredProjectById(tmpFolder) == null) {
                LoggerFactory.getLogger(RepositoryMonitor.class).debug(
                    "Repository with id={} is not being monitored anymore. Temp folder will be deleted.", tmpFolder);
                deleteDirectory(workingFolder, tmpFolder);
            }
        }

        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkOrphanedFolders -> Exit.");
    }

    /**
     * Creates a working clone for the repository and copies the source configuration to the clone
     *
     * @param pathTemp the path where the clone will be created
     * @param source the source to be cloned
     * @return a GitConnector pointing to temp clone
     * @throws VCSException
     */
    private GitConnector createWorkingClone(String pathTemp, GitConnector source) throws VCSException {
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("createWorkingClone -> Entry.");
        GitConnector target = null;
        try {
            if (new File(pathTemp).exists()) {
                FileUtils.cleanDirectory(new File(pathTemp));
                LoggerFactory.getLogger(RepositoryMonitor.class).debug("Removed existing content at {}. ", pathTemp);
            }

            target = source.cloneThis(pathTemp);
            GitTools.adjustTargetConfiguration(source, target);
            LoggerFactory.getLogger(RepositoryMonitor.class).debug("Created temp clone.");
        } catch (IOException ex) {
            LoggerFactory.getLogger(RepositoryMonitor.class).error("Error cleaning existing temp folder at" + pathTemp
                                    + ".", ex);

            throw new VCSException("Error cleaning existing temp folder at" + pathTemp + ".", ex);
        }

        LoggerFactory.getLogger(RepositoryMonitor.class).trace("createWorkingClone -> Entry.");

        return target;
    }

    /**
     * Removes pathToDelete from the specified parent folder.
     *
     * @param parentFolder Folder within path to delete resides.
     * @param pathToDelete path to be deleted. Can be the name of a file or folder
     */
    private void deleteDirectory(File parentFolder, String pathToDelete) {
        try {
            FileUtils.deleteDirectory(new File(parentFolder, pathToDelete));
            LoggerFactory.getLogger(RepositoryMonitor.class).debug("Folder {} was successfully deleted.", pathToDelete);
        } catch (IOException ex) {
            LoggerFactory.getLogger(RepositoryMonitor.class).error("It was not possible to delete folder "
                                    + pathToDelete, ex);
        }
    }

    /**
     * Adds a repository to the queue to be monitored as soon as the current run finishes
     *
     * @param repos the repositoryToMonitor to add
     */
    public synchronized void addRepositoryToMonitor(MonitoredRepository repos) {
        monitorQueue.add(repos);
    }

    /**
     * Adds a repository to the forcedQueue to be monitored as soon as the current run finishes, discarding its cache
     *
     * @param repos the repositoryToMonitor to add
     */
    public synchronized void addRepositoryToCleanAndMonitor(MonitoredRepository repos) {
        cleanAndMonitorQueue.add(repos);
    }
}
