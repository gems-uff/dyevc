package br.uff.ic.dyevc.monitor;

import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.gui.MainWindow;
import br.uff.ic.dyevc.gui.MessageManager;
import br.uff.ic.dyevc.model.BranchStatus;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.RepositoryStatus;
import br.uff.ic.dyevc.tools.vcs.git.GitConnector;
import br.uff.ic.dyevc.tools.vcs.git.GitTools;
import br.uff.ic.dyevc.utils.PreferencesUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

/**
 * This class continuously monitors the specified repositories, according to the
 * specified refresh rate.
 *
 * @author Cristiano
 */
public class RepositoryMonitor extends Thread {

    private ApplicationSettingsBean settings;
    private List<RepositoryStatus> statusList;
    private MainWindow container;
    private MonitoredRepository repositoryToMonitor;

    /**
     * Associates the specified window container and continuously monitors the
     * specified list of repositories.
     *
     * @param container the container for this monitor. It will be used to send
     * messages during monitoring.
     * @param mr the model of monitored repositories to be checked.
     */
    public RepositoryMonitor(MainWindow container, MonitoredRepositories mr) {
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("Constructor -> Entry.");
        settings = PreferencesUtils.loadPreferences();
        this.container = container;
        this.start();
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("Constructor -> Exit.");
    }

    /**
     * Runs the monitor. After running, monitor sleeps for the time specified by
     * the refresh interval application property.
     */
    @Override
    public void run() {
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("Repository monitor is running.");
        int sleepTime = settings.getRefreshInterval() * 1000;
        while (true) {
            try {
                MessageManager.getInstance().addMessage("Repository monitor is running.");
                checkWorkingFolder();
                statusList = new ArrayList<RepositoryStatus>();
                if (repositoryToMonitor == null) { //monitor all repositories
                    LoggerFactory.getLogger(RepositoryMonitor.class).debug("Found {} repositories to monitor.", MonitoredRepositories.getMonitoredProjects().size());
                    for (MonitoredRepository monitoredRepository : MonitoredRepositories.getMonitoredProjects()) {
                        checkRepository(monitoredRepository);
                    }
                } else { //monitor specified repository
                    LoggerFactory.getLogger(RepositoryMonitor.class).debug("Manual monitoring requested for repository {}.", repositoryToMonitor.getName());
                    checkRepository(repositoryToMonitor);
                    setRepositoryToMonitor(null);
                }
                container.notifyMessages(statusList);
                LoggerFactory.getLogger(RepositoryMonitor.class).debug("Will now sleep for {} seconds.", sleepTime);
                MessageManager.getInstance().addMessage("Repository monitor is sleeping.");
                Thread.sleep(settings.getRefreshInterval() * 1000);
                LoggerFactory.getLogger(RepositoryMonitor.class).debug("Waking up after sleeping for {} seconds.", sleepTime);
            } catch (DyeVCException dex) {
                try {
                    MessageManager.getInstance().addMessage(dex.getMessage());
                    Thread.sleep(settings.getRefreshInterval() * 1000);
                    LoggerFactory.getLogger(RepositoryMonitor.class).debug("Waking up after sleeping for {} seconds.", sleepTime);
                } catch (InterruptedException ex) {
                    LoggerFactory.getLogger(RepositoryMonitor.class).info("Waking up due to interruption received.");
                }
            } catch (RuntimeException re) {
                try {
                    MessageManager.getInstance().addMessage(re.getMessage());
                    LoggerFactory.getLogger(RepositoryMonitor.class).error("Error during monitoring.", re);
                    Thread.sleep(settings.getRefreshInterval() * 1000);
                    LoggerFactory.getLogger(RepositoryMonitor.class).debug("Waking up after sleeping for {} seconds.", sleepTime);
                } catch (InterruptedException ex) {
                    LoggerFactory.getLogger(RepositoryMonitor.class).info("Waking up due to interruption received.");
                }
            } catch (InterruptedException ex) {
                LoggerFactory.getLogger(RepositoryMonitor.class).info("Waking up due to interruption received.");
            }
        }


    }

    /**
     * Checks if a given repository is behind and/or ahead its tracking remotes,
     * populating a status list according to the results.
     *
     * @param monitoredRepository the repository to be checked
     */
    private void checkRepository(MonitoredRepository monitoredRepository) {
        LoggerFactory.getLogger(RepositoryMonitor.class)
                .trace("checkRepository -> Entry. Repository: {}, id:{}", monitoredRepository.getName(), monitoredRepository.getId());

        RepositoryStatus repStatus = new RepositoryStatus(monitoredRepository.getId());
        String cloneAddress = monitoredRepository.getCloneAddress();

        if (!GitConnector.isValidRepository(cloneAddress)) {
            repStatus.setInvalid("<" + cloneAddress + "> is not a valid repository path.");
        } else {
            try {
                List<BranchStatus> status = processRepository(monitoredRepository);
                repStatus.addBranchStatusList(status);
            } catch (VCSException ex) {
                MessageManager.getInstance().addMessage("It was not possible to finish monitoring of repository <"
                        + monitoredRepository.getName() + "> with id <" + monitoredRepository.getId() + ">.");
                LoggerFactory.getLogger(RepositoryMonitor.class)
                        .error("It was not possible to finish monitoring of repository <{}> with id <{}>",
                        monitoredRepository.getName(), monitoredRepository.getId());
                repStatus.setInvalid(ex.getCause().toString());
            }
        }

        monitoredRepository.setRepStatus(repStatus);

        statusList.add(repStatus);

        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkRepository -> Exit. Repository: {}", monitoredRepository.getName());
    }

    /**
     * Process a valid repository to check its behind and ahead status
     *
     * @param monitoredRepository the repository to be processed
     * @return a list of status for the given repository
     */
    private List<BranchStatus> processRepository(MonitoredRepository monitoredRepository) throws VCSException {
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("processRepository -> Entry. Repository: {}", monitoredRepository.getName());
        GitConnector sourceConnector = null;
        GitConnector tempConnector = null;
        List<BranchStatus> result = null;
        try {

            sourceConnector = monitoredRepository.getConnection();
            LoggerFactory.getLogger(RepositoryMonitor.class)
                    .debug("processRepository -> created gitConnector for repository {}, id={}", monitoredRepository.getName(), monitoredRepository.getId());

            String pathTemp = monitoredRepository.getWorkingCloneAddress();

            if (!GitConnector.isValidRepository(pathTemp)) {
                LoggerFactory.getLogger(RepositoryMonitor.class)
                        .debug("There is no temp repository at {}. Will create a temp by cloning {}.",
                        pathTemp, monitoredRepository.getId());
                monitoredRepository.setWorkingCloneConnection(createWorkingClone(pathTemp, sourceConnector));
            }
            tempConnector = monitoredRepository.getWorkingCloneConnection();
            GitTools.adjustTargetConfiguration(sourceConnector, tempConnector);

            tempConnector.fetchAllRemotes(true);
            result = tempConnector.testAhead();
        } finally {
            if (sourceConnector != null) {
                LoggerFactory.getLogger(RepositoryMonitor.class)
                        .debug("About to close connection with repository <{}> with id <{}>",
                        monitoredRepository.getName(), monitoredRepository.getId());
                sourceConnector.close();
            }
            if (tempConnector != null) {
                LoggerFactory.getLogger(RepositoryMonitor.class)
                        .debug("About to close connection with temp repository for <{}> with id <{}>",
                        monitoredRepository.getName(), monitoredRepository.getId());
                tempConnector.close();
            }
        }
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("processRepository -> Exit. Repository: {}", monitoredRepository.getName());
        return result;
    }

    /**
     * Verifies whether the working folder exists or not. If false, creates it,
     * otherwise, looks for orphaned folders related to projects not monitored
     * anymore.
     *
     * @throws DyeVCException
     */
    private void checkWorkingFolder() throws DyeVCException {
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkWorkingFolder -> Entry.");
        File workingFolder = new File(settings.getWorkingPath());
        LoggerFactory.getLogger(RepositoryMonitor.class).info("checkWorkingFolder -> Working folder is at {}.", workingFolder.getAbsoluteFile());
        if (workingFolder.exists()) {
            LoggerFactory.getLogger(RepositoryMonitor.class).debug("Working folder already exists.");
            checkOrphanedFolders(workingFolder);
            if (!workingFolder.canWrite()) {
                LoggerFactory.getLogger(RepositoryMonitor.class).error("Working folder is not writable.");
                MessageManager.getInstance()
                        .addMessage("Working folder is not writable. Please check folder permissions at "
                        + workingFolder.getAbsolutePath());
                throw new DyeVCException("Temp folder located at " + workingFolder.getAbsolutePath() + " is not writable.");
            }
        } else {
            workingFolder.mkdir();
            LoggerFactory.getLogger(RepositoryMonitor.class).debug("Working folder does not exist. A brand new one was created.");
        }
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkWorkingFolder -> Exit.");
    }

    /**
     * Deletes folders with clones related to projects that are not monitored
     * anymore.
     *
     * @param pointer to the working folder
     */
    private void checkOrphanedFolders(File workingFolder) {
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkOrphanedFolders -> Entry.");
        String[] tmpFolders = workingFolder.list();
        for (int i = 0; i < tmpFolders.length; i++) {
            String tmpFolder = tmpFolders[i];
            if (MonitoredRepositories.getMonitoredProjectById(tmpFolder) == null) {
                LoggerFactory.getLogger(RepositoryMonitor.class)
                        .debug("Repository with id={} is not being monitored anymore. Temp folder will be deleted.", tmpFolder);
                deleteDirectory(workingFolder, tmpFolder);
            }
        }
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkOrphanedFolders -> Exit.");
    }

    /**
     * Creates a working clone for the repository and copies the source
     * configuration to the clone
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
                LoggerFactory.getLogger(RepositoryMonitor.class)
                        .debug("Removed existing content at {}. ", pathTemp);
            }
            target = source.cloneThis(pathTemp);
            GitTools.adjustTargetConfiguration(source, target);
            LoggerFactory.getLogger(RepositoryMonitor.class).debug("Created temp clone.");
        } catch (IOException ex) {
            LoggerFactory.getLogger(RepositoryMonitor.class).error("Error cleaning existing temp folder at" + pathTemp + ".", ex);
            throw new VCSException("Error cleaning existing temp folder at" + pathTemp + ".", ex);
        }
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("createWorkingClone -> Entry.");
        return target;
    }

    /**
     * Removes pathToDelete from the specified parent folder.
     *
     * @param parentFolder Folder within path to delete resides.
     * @param pathToDelete path to be deleted. Can be the name of a file or
     * folder
     */
    private void deleteDirectory(File parentFolder, String pathToDelete) {
        try {
            FileUtils.deleteDirectory(new File(parentFolder, pathToDelete));
            LoggerFactory.getLogger(RepositoryMonitor.class)
                    .debug("Folder {} was successfully deleted.", pathToDelete);
        } catch (IOException ex) {
            LoggerFactory.getLogger(RepositoryMonitor.class)
                    .error("It was not possible to delete folder " + pathToDelete, ex);
        }
    }

    /**
     * @param repositoryToMonitor the repositoryToMonitor to set
     */
    public synchronized void setRepositoryToMonitor(MonitoredRepository repos) {
        repositoryToMonitor = repos;
    }

    /**
     * @return the repositoryToMonitor
     */
    public synchronized MonitoredRepository getRepositoryToMonitor() {
        return repositoryToMonitor;
    }
}
