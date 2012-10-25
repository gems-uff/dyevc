package br.uff.ic.dyevc.monitor;

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.gui.MessageManager;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.RepositoryRelationship;
import br.uff.ic.dyevc.tools.vcs.GitConnector;
import br.uff.ic.dyevc.utils.PreferencesUtils;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
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

    ApplicationSettingsBean settings;
    List<MonitoredRepository> repos;
    boolean hasMessages = false;

    public RepositoryMonitor() {
        settings = PreferencesUtils.loadPreferences();
        repos = PreferencesUtils.loadMonitoredRepositories().getMonitoredProjects();
        this.start();
    }

    @Override
    public void run() {
        MessageManager.getInstance().addMessage("Repository monitor is running.");
        while (true) {
            try {
                checkWorkingFolder();
                hasMessages = false;
                for (Iterator<MonitoredRepository> it = repos.iterator(); it.hasNext();) {
                    MonitoredRepository monitoredRepository = it.next();
                    checkRepository(monitoredRepository);
                }
                Thread.sleep(settings.getRefreshInterval() * 1000);
            } catch (InterruptedException ex) {
            }
        }
    }

    private void checkRepository(MonitoredRepository monitoredRepository) {
        try {
            GitConnector git = new GitConnector(monitoredRepository.getCloneAddress(), monitoredRepository.getName());
            Set<String> remotes = git.getRemoteNames();
            GitConnector temp = git.cloneThis(settings.getWorkingPath()
                    + IConstants.DIR_SEPARATOR + monitoredRepository.getName());

//            for (Iterator<String> it = remotes.iterator(); it.hasNext();) {
//                String remoteName = it.next();
//                String remoteUrl = git.getRemoteUrl(remoteName);
//                temp.fetch(remoteUrl, IConstants.FETCH_SPECS);
//            }

            List<RepositoryRelationship> relationships = temp.testAhead();
            for (Iterator<RepositoryRelationship> it = relationships.iterator(); it.hasNext();) {
                RepositoryRelationship rel = it.next();
                MessageManager.getInstance().addMessage(rel.toString());
            }
            temp.close();
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
                MessageManager.getInstance().addMessage(ex.getMessage());
            }
        } else {
            workingFolder.mkdir();
        }
      
    }
}
