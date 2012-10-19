package br.uff.ic.dyevc.monitor;

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.tools.vcs.GitConnector;
import br.uff.ic.dyevc.utils.PreferencesUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

/**
 * This class continuously monitors the specified repositories, according to the 
 * specified refresh rate.
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
        while (true) {
            try {
                Thread.sleep(settings.getRefreshInterval() * 1000);
            } catch (InterruptedException ex) {
                checkWorkingFolder();
                //TODO incluir código que verifica situação do repositório
                hasMessages = false;
                for (Iterator<MonitoredRepository> it = repos.iterator(); it.hasNext();) {
                    MonitoredRepository monitoredRepository = it.next();
                    checkRepository(monitoredRepository);
                }
            }
        }
    }

    private void checkRepository(MonitoredRepository monitoredRepository) {
        try {
            GitConnector git = new GitConnector(monitoredRepository.getCloneAddress());
            HashMap<String, String> remotes = git.getRemotes();
            GitConnector temp = git.cloneThis(settings.getWorkingPath() +
                    IConstants.DIR_SEPARATOR + monitoredRepository.getName());
            for (Map.Entry<String, String> entry : remotes.entrySet()) {
                String remoteName = entry.getKey();
                String remoteUrl = entry.getValue();
                temp.fetch(remoteUrl, IConstants.FETCH_SPECS);
                temp.testAhead();
            }
            //TODO recuperar as informações do testAhead;
            //TODO apagar diretorio temporário
            //TODO notificar na tray icon
        } catch (GitAPIException ex) {
            Logger.getLogger(RepositoryMonitor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (VCSException ex) {
            Logger.getLogger(RepositoryMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void checkWorkingFolder() {
        File workingFolder = new File(settings.getWorkingPath());
        if (!workingFolder.exists()) {
            workingFolder.mkdir();
        }
    }
}
