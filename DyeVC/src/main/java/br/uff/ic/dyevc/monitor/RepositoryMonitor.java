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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;

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
        settings = PreferencesUtils.loadPreferences();
        repos = PreferencesUtils.loadMonitoredRepositories().getMonitoredProjects();
        statusList = new RepositoryStatusMessages();
        this.container = container;
        this.start();
    }

    @Override
    public void run() {
        MessageManager.getInstance().addMessage("Repository monitor is running.");
        while (true) {
            try {
                checkWorkingFolder();
                for (Iterator<MonitoredRepository> it = repos.iterator(); it.hasNext();) {
                    MonitoredRepository monitoredRepository = it.next();
                    checkRepository(monitoredRepository);
                }
                notifyMessages();
                Thread.sleep(settings.getRefreshInterval() * 1000);
            } catch (InterruptedException ex) {
            }
        }
    }

    private void checkRepository(MonitoredRepository monitoredRepository) {
        try {
            GitConnector git = new GitConnector(monitoredRepository.getCloneAddress(), monitoredRepository.getName());
            checkAuthentication(monitoredRepository, git);
            
            Set<String> remotes = git.getRemoteNames();
            GitConnector temp = git.cloneThis(settings.getWorkingPath()
                    + IConstants.DIR_SEPARATOR + monitoredRepository.getName());
            checkAuthentication(monitoredRepository, temp);

            for (Iterator<String> it = remotes.iterator(); it.hasNext();) {
                String remoteName = it.next();
                String remoteUrl = git.getRemoteUrl(remoteName);
                temp.fetch(remoteUrl, IConstants.FETCH_SPECS);
            }

            List<RepositoryStatus> result = temp.testAhead();
            temp.close();
            statusList.addMessages(monitoredRepository, result);
        } catch (GitAPIException ex) {
            Logger.getLogger(RepositoryMonitor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (VCSException ex) {
            Logger.getLogger(RepositoryMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void checkWorkingFolder() {
        File workingFolder = new File(settings.getWorkingPath());
        if (workingFolder.exists()) {
            try {
                FileUtils.cleanDirectory(workingFolder);
            } catch (IOException ex) {
                Logger.getLogger(RepositoryMonitor.class.getName()).log(Level.SEVERE, null, ex);
                MessageManager.getInstance().addMessage(ex.getMessage());
            }
        } else {
            workingFolder.mkdir();
        }
    }

    private void notifyMessages() {
        List<String> messages = statusList.getAllMessages();
        if (messages.size() > 0) {
            StringBuilder message = new StringBuilder();
            for (Iterator<String> it = messages.iterator(); it.hasNext();) {
                message.append(it.next()).append("\n");
            }
            MessageManager.getInstance().addMessage(message.toString());
            container.notifyMessage(message.toString());
        }
    }

    private void checkAuthentication(MonitoredRepository monitoredRepository, GitConnector git) {
        if (monitoredRepository.needsAuthentication()) {
            git.setCredentials(monitoredRepository.getUser(), monitoredRepository.getPassword());
        }
    }
}
