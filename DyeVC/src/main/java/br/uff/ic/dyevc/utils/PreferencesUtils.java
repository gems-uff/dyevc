/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.utils;

import br.uff.ic.dyevc.application.DyeVC;
import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.beans.MonitoredRepositoriesBean;
import br.uff.ic.dyevc.beans.RepositoryBean;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author Cristiano
 */
public final class PreferencesUtils {
    private static final String NODE_GENERAL_SETTINGS = "generalsettings";
    private static final String NODE_MONITORED_REPOSITORIES = "monitoredrepositories";
    private static Preferences pref;
    static {
        pref = Preferences.userNodeForPackage(DyeVC.class);
    }

    public static void storePreferences(ApplicationSettingsBean bean) {
        Preferences nodeToStore = pref.node(NODE_GENERAL_SETTINGS);
        nodeToStore.put(ApplicationSettingsBean.PROP_WORKING_PATH, bean.getWorkingPath());
        nodeToStore.putInt(ApplicationSettingsBean.PROP_REFRESHINTERVAL, bean.getRefreshInterval());
    }

    public static ApplicationSettingsBean loadPreferences() {
        Preferences nodeToLoad = pref.node(NODE_GENERAL_SETTINGS);
        ApplicationSettingsBean bean = new ApplicationSettingsBean();
        bean.setWorkingPath(nodeToLoad.get(ApplicationSettingsBean.PROP_WORKING_PATH,"not set"));
        bean.setRefreshInterval(nodeToLoad.getInt(ApplicationSettingsBean.PROP_REFRESHINTERVAL, 0));
        
        return bean;
    }
    
    public static void persistRepositories(MonitoredRepositoriesBean listBeans) {
        List<RepositoryBean> reps = listBeans.getMonitoredProjects();
        if (!reps.isEmpty()) {
            int i = 0;
            try {
                if (pref.nodeExists(NODE_MONITORED_REPOSITORIES)) {
                    pref.node(NODE_MONITORED_REPOSITORIES).removeNode();
                    pref.flush();
                }
            } catch (BackingStoreException ex) {
                Logger.getLogger(PreferencesUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
            Preferences nodeToStore = pref.node(NODE_MONITORED_REPOSITORIES);
            for (Iterator<RepositoryBean> it = reps.iterator(); it.hasNext();) {
                RepositoryBean repositoryBean = it.next();
                nodeToStore.node("rep." + i).put("name", repositoryBean.getName());
                nodeToStore.node("rep." + i).put("originurl", repositoryBean.getOriginUrl());
                nodeToStore.node("rep." + i).put("cloneaddress", repositoryBean.getCloneAddress());
                i++;
            }
        }
    }

    public static MonitoredRepositoriesBean loadMonitoredRepositories() {
        MonitoredRepositoriesBean monBean = new MonitoredRepositoriesBean();
        try {
            if (pref.nodeExists(NODE_MONITORED_REPOSITORIES)) {
                Preferences nodeToStore = pref.node(NODE_MONITORED_REPOSITORIES);
                String[] reps = nodeToStore.childrenNames();
                for (int i = 0; i < reps.length; i++) {
                    String rep = reps[i];
                    RepositoryBean bean = new RepositoryBean();
                    bean.setName(nodeToStore.node(rep).get("name", "no name"));
                    bean.setCloneAddress(nodeToStore.node(rep).get("cloneaddress", "no cloneaddress"));
                    bean.setOriginUrl(nodeToStore.node(rep).get("originurl", "no originurl"));
                    monBean.addMonitoredRepository(bean);
                }
            }

        } catch (BackingStoreException ex) {
            Logger.getLogger(PreferencesUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return monBean;
    }
}
