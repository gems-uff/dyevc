package br.uff.ic.dyevc.monitor;

import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.utils.PreferencesUtils;

/**
 * This class continuously monitors the specified repositories, according to the 
 * specified refresh rate.
 * @author Cristiano
 */
public class RepositoryMonitor extends Thread {
    ApplicationSettingsBean settings;
    public RepositoryMonitor() {
        settings = PreferencesUtils.loadPreferences();
        this.start();
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                //TODO incluir código que verifica situação do repositório
                Thread.sleep(settings.getRefreshInterval() * 1000);
            } catch (InterruptedException ex) {
            }
        }
    }
}
