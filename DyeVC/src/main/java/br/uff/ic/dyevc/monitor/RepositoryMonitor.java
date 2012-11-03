package br.uff.ic.dyevc.monitor;

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.gui.MainWindow;
import br.uff.ic.dyevc.gui.MessageManager;
import br.uff.ic.dyevc.model.BranchStatus;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.RepositoryStatus;
import br.uff.ic.dyevc.tools.vcs.GitConnector;
import br.uff.ic.dyevc.utils.PreferencesUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
    private MonitoredRepositories repos;
    private List<RepositoryStatus> statusList;
    private MainWindow container;

    public RepositoryMonitor(MainWindow container, MonitoredRepositories mr) {
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("Constructor -> Entry.");
        settings = PreferencesUtils.loadPreferences();
        this.container = container;
        repos = mr;
        this.start();
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("Constructor -> Exit.");
    }

    @Override
    public void run() {
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("Repository monitor is running.");
        while (true) {
            try {
                MessageManager.getInstance().addMessage("Repository monitor is running.");
                statusList = new ArrayList <RepositoryStatus>();
                LoggerFactory.getLogger(RepositoryMonitor.class).debug("Found {} repositories to monitor.", repos.getSize());
                checkWorkingFolder();
                for (Iterator<MonitoredRepository> it = repos.getMonitoredProjects().iterator(); it.hasNext();) {
                    MonitoredRepository monitoredRepository = it.next();
                    checkRepository(monitoredRepository);
                }
                container.notifyMessages(statusList);
                int sleepTime = settings.getRefreshInterval() * 1000;
                LoggerFactory.getLogger(RepositoryMonitor.class).debug("Will now sleep for {} seconds.", sleepTime);
                MessageManager.getInstance().addMessage("Repository monitor is sleeping.");
                Thread.sleep(settings.getRefreshInterval() * 1000);
                LoggerFactory.getLogger(RepositoryMonitor.class).debug("Waking up after sleeping.", sleepTime);
            } catch (InterruptedException ex) {
                LoggerFactory.getLogger(RepositoryMonitor.class).info("Waking up due to interruption received.");
            } catch (DyeVCException dex) {
                MessageManager.getInstance().addMessage(dex.getMessage());
            }
        }
    }

    /**
     * Checks if a given repository is behind and/or ahead its remotes, pupulating
     * a status list according to the results.
     * @param monitoredRepository the repository to be checked
     */
    private void checkRepository(MonitoredRepository monitoredRepository) {
        try {
            LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkRepository -> Entry. Repository: {}, id:{}"
                    , monitoredRepository.getName(), monitoredRepository.getId());
            GitConnector git = new GitConnector(monitoredRepository.getCloneAddress(), monitoredRepository.getId());
            LoggerFactory.getLogger(RepositoryMonitor.class)
                    .debug("checkRepository -> created gitConnector for repository {}, id={}"
                    , monitoredRepository.getName(), monitoredRepository.getId());
            checkAuthentication(monitoredRepository, git);

            Set<String> remotes = git.getRemoteNames();
            LoggerFactory.getLogger(RepositoryMonitor.class)
                    .debug("Repository {} has {} remotes.",
                    monitoredRepository.getId(), remotes.size());

            String pathTemp = settings.getWorkingPath()
                    + IConstants.DIR_SEPARATOR + monitoredRepository.getId();

            GitConnector temp = null;
            if (!GitConnector.isValidRepository(pathTemp)) {
                LoggerFactory.getLogger(RepositoryMonitor.class)
                        .debug("There is no temp repository at {}. Will create a temp by cloning {}.",
                        pathTemp, monitoredRepository.getId());
                try {
                    if (new File(pathTemp).exists()) {
                        FileUtils.cleanDirectory(new File(pathTemp));
                        LoggerFactory.getLogger(RepositoryMonitor.class)
                                .debug("Removed existing content at {}. ",
                                pathTemp);
                    }
                    temp = git.cloneThis(pathTemp);
                    LoggerFactory.getLogger(RepositoryMonitor.class).debug("Created temp clone for {}.", monitoredRepository.getId());
                } catch (IOException ex) {
                    LoggerFactory.getLogger(RepositoryMonitor.class).error("Error cleaning existing temp folder at" + pathTemp + ".", ex);
                }
            } else {
                LoggerFactory.getLogger(RepositoryMonitor.class)
                        .debug("There is a valid repository at {}. Creating a git connector to it.",
                        pathTemp);
                temp = new GitConnector(pathTemp, monitoredRepository.getId());
            }
            checkAuthentication(monitoredRepository, temp);

            for (Iterator<String> it = remotes.iterator(); it.hasNext();) {
                String remoteName = it.next();
                String remoteUrl = git.getRemoteUrl(remoteName);
                LoggerFactory.getLogger(RepositoryMonitor.class).debug("Fetching for remote {} on url {}.", remoteName, remoteUrl);


                temp.fetch(remoteUrl, IConstants.FETCH_SPECS);
            }

            List<BranchStatus> result = temp.testAhead();
            
            temp.close();
            LoggerFactory.getLogger(RepositoryMonitor.class).debug("Closing temp clone repository connection.");

            RepositoryStatus repStatus = new RepositoryStatus(monitoredRepository.getId());
            repStatus.addStatus(result);
            monitoredRepository.setRepStatus(repStatus);
            statusList.add(repStatus);
        } catch (VCSException ex) {
            LoggerFactory.getLogger(RepositoryMonitor.class)
                    .error("It was not possible to finish monitoring of repository <{}>",
                    monitoredRepository.getId());
        }
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkRepository -> Exit. Repository: {}", monitoredRepository.getId());
    }

    /**
     * Verifies whether the working folder exists or not. If false, create it, 
     * otherwise, look for orphaned folders related to projects not monitored anymore.
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
     * Checks if a given repository needs authentication to connect to. If true,
     * sets the credentials according to the configuration parameters provided by
     * the user.
     * @param monitoredRepository the repository to be checked
     * @param git the connector to the given repository
     */
    private void checkAuthentication(MonitoredRepository monitoredRepository, GitConnector git) {
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkAuthentication -> Entry");
        if (monitoredRepository.needsAuthentication()) {
            LoggerFactory.getLogger(RepositoryMonitor.class).debug("checkAuthentication -> repository {} needs authentication.", monitoredRepository.getId());
            git.setCredentials(monitoredRepository.getUser(), monitoredRepository.getPassword());
        }
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkAuthentication -> Exit");
    }

    /**
     * Deletes folders with clones related to projects that are not
     * monitored anymore.
     * @param pointer to the working folder
     */
    private void checkOrphanedFolders(File workingFolder) {
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkOrphanedFolders -> Entry.");
        String[] tmpFolders = workingFolder.list();
        for (int i = 0; i < tmpFolders.length; i++) {
            String tmpFolder = tmpFolders[i];
            if (repos.getMonitoredProjectById(tmpFolder) == null) {
                LoggerFactory.getLogger(RepositoryMonitor.class)
                        .debug("Repository with id={} is not being monitored anymore. Temp folder will be deleted."
                        , tmpFolder);
                try {
                    FileUtils.deleteDirectory(new File(workingFolder, tmpFolder));
                LoggerFactory.getLogger(RepositoryMonitor.class)
                        .debug("Temp folder for repository with id={} was successfully deleted."
                        , tmpFolder);
                } catch (IOException ex) {
                LoggerFactory.getLogger(RepositoryMonitor.class)
                        .error("It was not possible to delete temp folder for repository id=" + tmpFolder
                        , ex);
                }
            }
        }
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkOrphanedFolders -> Exit.");
    }
}
