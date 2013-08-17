package br.uff.ic.dyevc.utils;

import br.uff.ic.dyevc.application.DyeVC;
import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.gui.MessageManager;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
import java.util.Iterator;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.slf4j.LoggerFactory;

/**
 * Utilities to load and store application preferences using java Preferences
 * API.
 *
 * @author Cristiano
 */
public final class PreferencesUtils {

    /**
     * Name of node to store general settings.
     */
    private static final String NODE_GENERAL_SETTINGS = "generalsettings";
    /**
     * Name of node to store monitored repositories configuration.
     */
    private static final String NODE_MONITORED_REPOSITORIES = "monitoredrepositories";
    /**
     * Default interval between checks, in seconds. Defaults to 10 minutes.
     */
    private static final int DEFAULT_CHECK_INTERVAL = 600;
    /**
     * Object the points to a preferences instance.
     */
    private static Preferences pref;
    /**
     * Bean containing application general preferences.
     */
    private static ApplicationSettingsBean settingsBean;

    static {
        pref = Preferences.userNodeForPackage(DyeVC.class);
    }

    private static MonitoredRepository saveRepository(MonitoredRepository repositoryBean, Preferences nodeToStore) {
        nodeToStore.node(repositoryBean.getId()).put("systemName", repositoryBean.getSystemName());
        nodeToStore.node(repositoryBean.getId()).put("name", repositoryBean.getName());
        nodeToStore.node(repositoryBean.getId()).put("cloneaddress", repositoryBean.getCloneAddress());
        nodeToStore.node(repositoryBean.getId()).putBoolean("markedForDeletion", repositoryBean.isMarkedForDeletion());
        return repositoryBean;
    }

    private PreferencesUtils() {
    }

    /**
     * Stores general preferences using java Preferences API.
     *
     * @param bean the bean to be stored.
     */
    public static void storePreferences(ApplicationSettingsBean bean) {
        Preferences nodeToStore = pref.node(NODE_GENERAL_SETTINGS);
        nodeToStore.putInt(ApplicationSettingsBean.PROP_REFRESHINTERVAL, bean.getRefreshInterval());
        nodeToStore.put(ApplicationSettingsBean.PROP_LAST_USED_PATH, bean.getLastUsedPath());
        settingsBean = bean;
    }

    /**
     * Loads general preferences using java Preferences API.
     *
     * @return the bean containing loaded general preferences.
     */
    public static ApplicationSettingsBean loadPreferences() {
        if (settingsBean == null) {
            Preferences nodeToLoad = pref.node(NODE_GENERAL_SETTINGS);
            ApplicationSettingsBean bean = new ApplicationSettingsBean();
            bean.setRefreshInterval(nodeToLoad.getInt(ApplicationSettingsBean.PROP_REFRESHINTERVAL, DEFAULT_CHECK_INTERVAL));
            bean.setLastUsedPath(nodeToLoad.get(ApplicationSettingsBean.PROP_LAST_USED_PATH, ""));
            settingsBean = bean;
        }
        return settingsBean;
    }

    /**
     * Stores the configuration for all monitored repositories into user
     * preferences.
     *
     * @see MonitoredRepositories
     */
    public static void persistRepositories() {
        try {
            if (pref.nodeExists(NODE_MONITORED_REPOSITORIES)) {
                pref.node(NODE_MONITORED_REPOSITORIES).removeNode();
                pref.flush();
            }
        } catch (BackingStoreException ex) {
            LoggerFactory.getLogger(PreferencesUtils.class).error("Error saving repository to preferences store.", ex);
            MessageManager.getInstance().addMessage("Error saving repository to preferences store.");
        }
        Preferences nodeToStore = pref.node(NODE_MONITORED_REPOSITORIES);
        for (Iterator<MonitoredRepository> it = MonitoredRepositories.getMonitoredProjects().iterator(); it.hasNext();) {
            MonitoredRepository repositoryBean = it.next();
            saveRepository(repositoryBean, nodeToStore);
        }
        for (Iterator<MonitoredRepository> it = MonitoredRepositories.getMarkedForDeletion().iterator(); it.hasNext();) {
            MonitoredRepository repositoryBean = it.next();
            saveRepository(repositoryBean, nodeToStore);
        }
    }

    /**
     * Loads a list of monitored repositories from user preferences.
     *
     * @return a list of monitored repositories configuration
     */
    public static MonitoredRepositories loadMonitoredRepositories() {
        MonitoredRepositories monBean = new MonitoredRepositories();
        try {
            if (pref.nodeExists(NODE_MONITORED_REPOSITORIES)) {
                Preferences nodeToStore = pref.node(NODE_MONITORED_REPOSITORIES);
                String[] reps = nodeToStore.childrenNames();
                for (int i = 0; i < reps.length; i++) {
                    Preferences repNode = nodeToStore.node(reps[i]);
                    MonitoredRepository bean = new MonitoredRepository(repNode.name());
                    bean.setSystemName(repNode.get("systemName", "no name"));
                    bean.setName(repNode.get("name", "no name"));
                    bean.setCloneAddress(repNode.get("cloneaddress", "no cloneaddress"));
                    bean.setMarkedForDeletion(repNode.getBoolean("markedForDeletion", false));
                    monBean.addMonitoredRepository(bean);
                }
            }
        } catch (BackingStoreException ex) {
            LoggerFactory.getLogger(PreferencesUtils.class).error("Error reading repository info from preferences store.", ex);
            MessageManager.getInstance().addMessage("Error loading repository info from preferences store.");
        }
        return monBean;
    }
}
