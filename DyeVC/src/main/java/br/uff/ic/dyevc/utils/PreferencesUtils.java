/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.utils;

import br.uff.ic.dyevc.application.DyeVC;
import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
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
    private static final int DEFAULT_CHECK_INTERVAL = 300;
    private static final String DEFAULT_WORKING_PATH = System.getProperty("user.home") + IConstants.DIR_SEPARATOR + ".dyevc";
    private static Preferences pref;
    private static ApplicationSettingsBean settingsBean;

    static {
        pref = Preferences.userNodeForPackage(DyeVC.class);
    }

    public static void storePreferences(ApplicationSettingsBean bean) {
        Preferences nodeToStore = pref.node(NODE_GENERAL_SETTINGS);
        nodeToStore.put(ApplicationSettingsBean.PROP_WORKING_PATH, bean.getWorkingPath());
        nodeToStore.putInt(ApplicationSettingsBean.PROP_REFRESHINTERVAL, bean.getRefreshInterval());
        settingsBean = bean;
    }

    public static ApplicationSettingsBean loadPreferences() {
        if (settingsBean == null) {
            Preferences nodeToLoad = pref.node(NODE_GENERAL_SETTINGS);
            ApplicationSettingsBean bean = new ApplicationSettingsBean();
            bean.setWorkingPath(nodeToLoad.get(ApplicationSettingsBean.PROP_WORKING_PATH, DEFAULT_WORKING_PATH));
            bean.setRefreshInterval(nodeToLoad.getInt(ApplicationSettingsBean.PROP_REFRESHINTERVAL, DEFAULT_CHECK_INTERVAL));
            settingsBean = bean;
        }
        return settingsBean;
    }

    public static void persistRepositories(MonitoredRepositories listBeans) {
        List<MonitoredRepository> reps = listBeans.getMonitoredProjects();
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
            for (Iterator<MonitoredRepository> it = reps.iterator(); it.hasNext();) {
                MonitoredRepository repositoryBean = it.next();
                nodeToStore.node("rep." + i).put("name", repositoryBean.getName());
                nodeToStore.node("rep." + i).put("cloneaddress", repositoryBean.getCloneAddress());
                nodeToStore.node("rep." + i).put("needsauthentication", Boolean.toString(repositoryBean.needsAuthentication()));
                if (repositoryBean.needsAuthentication()) {
                    nodeToStore.node("rep." + i).put("user", repositoryBean.getUser());
                    nodeToStore.node("rep." + i).put("password", repositoryBean.getPassword());
                }
                i++;
            }
        }
    }

    public static MonitoredRepositories loadMonitoredRepositories() {
        MonitoredRepositories monBean = new MonitoredRepositories();
        try {
            if (pref.nodeExists(NODE_MONITORED_REPOSITORIES)) {
                Preferences nodeToStore = pref.node(NODE_MONITORED_REPOSITORIES);
                String[] reps = nodeToStore.childrenNames();
                for (int i = 0; i < reps.length; i++) {
                    String rep = reps[i];
                    MonitoredRepository bean = new MonitoredRepository();
                    bean.setName(nodeToStore.node(rep).get("name", "no name"));
                    bean.setCloneAddress(nodeToStore.node(rep).get("cloneaddress", "no cloneaddress"));
                    bean.setNeedsAuthentication(new Boolean(nodeToStore.node(rep).get("needsauthentication", "false")));
                    if (bean.needsAuthentication()) {
                        bean.setUser(nodeToStore.node(rep).get("user", "user not set"));
                        bean.setPassword(nodeToStore.node(rep).get("password", "password not set"));
                    }
                    monBean.addMonitoredRepository(bean);
                }
            }

        } catch (BackingStoreException ex) {
            Logger.getLogger(PreferencesUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return monBean;
    }
}
