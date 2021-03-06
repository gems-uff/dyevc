package br.uff.ic.dyevc.utils;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.application.DyeVC;
import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.gui.core.MessageManager;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.Date;
import java.util.Iterator;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Utilities to load and store application preferences using java Preferences
 * API.
 *
 * @author Cristiano
 */
public final class PreferencesManager {
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
     * Singleton persistence manager
     */
    private static PreferencesManager instance;

    /**
     * Creates a persistence manager loading the persisted elements
     */
    private PreferencesManager() {
        pref = Preferences.userNodeForPackage(DyeVC.class);
    }

    /**
     * Provides the singleton instance
     * @return the singleton instance.
     */
    public synchronized static PreferencesManager getInstance() {
        if (instance == null) {
            instance = new PreferencesManager();
        }

        return instance;
    }

    /**
     * Object the points to a preferences instance.
     */
    private final Preferences pref;

    /**
     * Bean containing application general preferences.
     */
    private ApplicationSettingsBean settingsBean;

    private MonitoredRepository saveRepository(MonitoredRepository repositoryBean, Preferences nodeToStore) {
        nodeToStore.node(repositoryBean.getId()).put("systemName", repositoryBean.getSystemName());
        nodeToStore.node(repositoryBean.getId()).put("name", repositoryBean.getName());
        nodeToStore.node(repositoryBean.getId()).put("cloneaddress", repositoryBean.getCloneAddress());
        nodeToStore.node(repositoryBean.getId()).putBoolean("markedForDeletion", repositoryBean.isMarkedForDeletion());

        if (repositoryBean.getLastChanged() != null) {
            nodeToStore.node(repositoryBean.getId()).putLong("lastChanged", repositoryBean.getLastChanged().getTime());
        }

        return repositoryBean;
    }

    /**
     * Stores general preferences using java Preferences API.
     *
     * @param bean the bean to be stored.
     */
    public void storePreferences(ApplicationSettingsBean bean) {
        Preferences nodeToStore = pref.node(NODE_GENERAL_SETTINGS);
        nodeToStore.putInt(ApplicationSettingsBean.PROP_REFRESHINTERVAL, bean.getRefreshInterval());
        nodeToStore.putBoolean(ApplicationSettingsBean.PROP_PERFORMANCE_MODE, bean.isPerformanceMode());
        nodeToStore.put(ApplicationSettingsBean.PROP_LAST_USED_PATH, bean.getLastUsedPath());
        nodeToStore.put(ApplicationSettingsBean.PROP_DATABASE_PATH, bean.getDatabasePath());
        nodeToStore.put(ApplicationSettingsBean.PROP_APP_KEY, bean.getAppKey());

        if (bean.getLastApplicationVersionUsed() != null) {
            nodeToStore.put(ApplicationSettingsBean.PROP_LAST_APP_VERSION_USED, bean.getLastApplicationVersionUsed());
        }

        settingsBean = bean;
    }

    /**
     * Loads general preferences using java Preferences API.
     *
     * @return the bean containing loaded general preferences.
     */
    public ApplicationSettingsBean loadPreferences() {
        if (settingsBean == null) {
            Preferences             nodeToLoad = pref.node(NODE_GENERAL_SETTINGS);
            ApplicationSettingsBean bean       = new ApplicationSettingsBean();
            bean.setRefreshInterval(nodeToLoad.getInt(ApplicationSettingsBean.PROP_REFRESHINTERVAL,
                    DEFAULT_CHECK_INTERVAL));
            bean.setPerformanceMode(nodeToLoad.getBoolean(ApplicationSettingsBean.PROP_PERFORMANCE_MODE, false));
            bean.setLastUsedPath(nodeToLoad.get(ApplicationSettingsBean.PROP_LAST_USED_PATH, ""));
            bean.setLastApplicationVersionUsed(nodeToLoad.get(ApplicationSettingsBean.PROP_LAST_APP_VERSION_USED,
                    "0.0"));
            bean.setDatabasePath(nodeToLoad.get(ApplicationSettingsBean.PROP_DATABASE_PATH, ""));
            bean.setAppKey(nodeToLoad.get(ApplicationSettingsBean.PROP_APP_KEY, ""));
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
    public void persistRepositories() {
        try {
            if (pref.nodeExists(NODE_MONITORED_REPOSITORIES)) {
                pref.node(NODE_MONITORED_REPOSITORIES).removeNode();
                pref.flush();
            }
        } catch (BackingStoreException ex) {
            LoggerFactory.getLogger(PreferencesManager.class).error("Error saving repository to preferences store.",
                                    ex);
            MessageManager.getInstance().addMessage("Error saving repository to preferences store.");
        }

        Preferences nodeToStore = pref.node(NODE_MONITORED_REPOSITORIES);
        for (Iterator<MonitoredRepository> it =
                MonitoredRepositories.getMonitoredProjects().iterator(); it.hasNext(); ) {
            MonitoredRepository repositoryBean = it.next();
            saveRepository(repositoryBean, nodeToStore);
        }

        for (Iterator<MonitoredRepository> it =
                MonitoredRepositories.getMarkedForDeletion().iterator(); it.hasNext(); ) {
            MonitoredRepository repositoryBean = it.next();
            saveRepository(repositoryBean, nodeToStore);
        }
    }

    /**
     * Loads a list of monitored repositories from user preferences.
     *
     * @return a list of monitored repositories configuration
     */
    public MonitoredRepositories loadMonitoredRepositories() {
        MonitoredRepositories monBean = MonitoredRepositories.getInstance();
        try {
            if (pref.nodeExists(NODE_MONITORED_REPOSITORIES)) {
                Preferences nodeToStore = pref.node(NODE_MONITORED_REPOSITORIES);
                String[]    reps        = nodeToStore.childrenNames();
                for (String rep : reps) {
                    Preferences         repNode = nodeToStore.node(rep);
                    MonitoredRepository bean    = new MonitoredRepository(repNode.name());
                    bean.setSystemName(repNode.get("systemName", "no name"));
                    bean.setName(repNode.get("name", "no name"));
                    bean.setCloneAddress(repNode.get("cloneaddress", "no cloneaddress"));
                    bean.setMarkedForDeletion(repNode.getBoolean("markedForDeletion", false));
                    long lastChanged = repNode.getLong("lastChanged", 0);
                    if (lastChanged != 0) {
                        bean.setLastChanged(new Date(lastChanged));
                    }

                    monBean.addMonitoredRepository(bean);
                }
            }
        } catch (BackingStoreException ex) {
            LoggerFactory.getLogger(PreferencesManager.class).error(
                "Error reading repository info from preferences store.", ex);
            MessageManager.getInstance().addMessage("Error loading repository info from preferences store.");
        }

        return monBean;
    }
}
