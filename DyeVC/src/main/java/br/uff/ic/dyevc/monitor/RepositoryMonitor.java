package br.uff.ic.dyevc.monitor;

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.gui.MainWindow;
import br.uff.ic.dyevc.gui.MessageManager;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.RepositoryStatus;
import br.uff.ic.dyevc.model.RepositoryStatusMessages;
import br.uff.ic.dyevc.tools.vcs.GitConnector;
import br.uff.ic.dyevc.utils.PreferencesUtils;
import java.io.File;
import java.io.IOException;
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
    private List<MonitoredRepository> repos;
    private RepositoryStatusMessages statusList;
    private MainWindow container;

    public RepositoryMonitor(MainWindow container) {
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("Constructor -> Entry.");
        settings = PreferencesUtils.loadPreferences();
        repos = PreferencesUtils.loadMonitoredRepositories().getMonitoredProjects();
        statusList = new RepositoryStatusMessages();
        this.container = container;
        this.start();
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("Constructor -> Exit.");
    }

    @Override
    public void run() {
        MessageManager.getInstance().addMessage("Repository monitor is running.");
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("Repository monitor is running.");
        while (true) {
            try {
                checkWorkingFolder();
                LoggerFactory.getLogger(RepositoryMonitor.class).debug("Found {} repositories to monitor.", repos.size());
                for (Iterator<MonitoredRepository> it = repos.iterator(); it.hasNext();) {
                    MonitoredRepository monitoredRepository = it.next();
                    checkRepository(monitoredRepository);
                }
                notifyMessages();
                int sleepTime = settings.getRefreshInterval() * 1000;
                LoggerFactory.getLogger(RepositoryMonitor.class).debug("Will now sleep for {} seconds.", sleepTime);
                Thread.sleep(settings.getRefreshInterval() * 1000);
                LoggerFactory.getLogger(RepositoryMonitor.class).debug("Waking up after sleeping.", sleepTime);
            } catch (InterruptedException ex) {
                LoggerFactory.getLogger(RepositoryMonitor.class).debug("Waking up due to interruption received.");
            }
        }
    }

    private void checkRepository(MonitoredRepository monitoredRepository) {
        try {
            LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkRepository -> Entry. Repository: {}", monitoredRepository.getName());
            GitConnector git = new GitConnector(monitoredRepository.getCloneAddress(), monitoredRepository.getName());
            LoggerFactory.getLogger(RepositoryMonitor.class).debug("checkRepository -> created gitConnector");
            checkAuthentication(monitoredRepository, git);

            Set<String> remotes = git.getRemoteNames();
            LoggerFactory.getLogger(RepositoryMonitor.class)
                    .debug("Repository {} has {} remotes.",
                    monitoredRepository.getName(), remotes.size());

            GitConnector temp = git.cloneThis(settings.getWorkingPath()
                    + IConstants.DIR_SEPARATOR + monitoredRepository.getName());
            LoggerFactory.getLogger(RepositoryMonitor.class).debug("Created temp clone for {}.", monitoredRepository.getName());

            checkAuthentication(monitoredRepository, temp);

            for (Iterator<String> it = remotes.iterator(); it.hasNext();) {
                String remoteName = it.next();
                String remoteUrl = git.getRemoteUrl(remoteName);
                LoggerFactory.getLogger(RepositoryMonitor.class).debug("Fetching for remote {} on url {}.", remoteName, remoteUrl);


                temp.fetch(remoteUrl, IConstants.FETCH_SPECS);
            }

            List<RepositoryStatus> result = temp.testAhead();
            temp.close();
            LoggerFactory.getLogger(RepositoryMonitor.class).debug("Closing temp clone repository connection.", monitoredRepository.getName(), remotes.size());

            statusList.addMessages(monitoredRepository, result);
        } catch (VCSException ex) {
            LoggerFactory.getLogger(RepositoryMonitor.class)
                    .error("It was not possible to finish monitoring of repository <{}>",
                    monitoredRepository.getName());
        }
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkRepository -> Exit. Repository: {}", monitoredRepository.getName());
    }

    private void checkWorkingFolder() {
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkWorkingFolder -> Entry.");
        File workingFolder = new File(settings.getWorkingPath());
        LoggerFactory.getLogger(RepositoryMonitor.class).info("checkWorkingFolder -> Working folder is at {}.", workingFolder.getAbsoluteFile());
        if (workingFolder.exists()) {
            LoggerFactory.getLogger(RepositoryMonitor.class).debug("Working folder already exists.");
            if (!workingFolder.canWrite()) {
                LoggerFactory.getLogger(RepositoryMonitor.class).error("Working folder is not writable.");
                MessageManager.getInstance()
                        .addMessage("Working folder is not writable. Please check folder permissions at "
                        + workingFolder.getAbsolutePath());
            }
            try {
                LoggerFactory.getLogger(RepositoryMonitor.class).debug("Beginning working folder clean up process.");
                FileUtils.cleanDirectory(workingFolder);
                LoggerFactory.getLogger(RepositoryMonitor.class).debug("Working folder clean up complete.");
            } catch (IOException ex) {
                LoggerFactory.getLogger(RepositoryMonitor.class).error("Error during working folder clean up.", ex);
                MessageManager.getInstance().addMessage(ex.getMessage());
            }
        } else {
            workingFolder.mkdir();
            LoggerFactory.getLogger(RepositoryMonitor.class).debug("Working folder does not exist. A brand new one was created.");
        }
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkWorkingFolder -> Exit.");
    }

    private void notifyMessages() {
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("notifyMessages -> Entry");
        List<String> messages = statusList.getAllMessages();
        if (messages.size() > 0) {
            StringBuilder message = new StringBuilder();
            for (Iterator<String> it = messages.iterator(); it.hasNext();) {
                message.append(it.next()).append("\n");
            }
            MessageManager.getInstance().addMessage(message.toString());
            container.notifyMessage(message.toString());
        }
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("notifyMessages -> Exit");
    }

    private void checkAuthentication(MonitoredRepository monitoredRepository, GitConnector git) {
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkAuthentication -> Entry");
        if (monitoredRepository.needsAuthentication()) {
            LoggerFactory.getLogger(RepositoryMonitor.class).debug("checkAuthentication -> repository {} needs authentication.", monitoredRepository.getName());
            git.setCredentials(monitoredRepository.getUser(), monitoredRepository.getPassword());
        }
        LoggerFactory.getLogger(RepositoryMonitor.class).trace("checkAuthentication -> Exit");
    }
}
