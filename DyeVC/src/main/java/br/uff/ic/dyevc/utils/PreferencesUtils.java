package br.uff.ic.dyevc.utils;

import br.uff.ic.dyevc.application.DyeVC;
import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.gui.MessageManager;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Cristiano
 */
public final class PreferencesUtils {

    private static final String NODE_GENERAL_SETTINGS = "generalsettings";
    private static final String NODE_MONITORED_REPOSITORIES = "monitoredrepositories";
    private static final int DEFAULT_CHECK_INTERVAL = 300;
    private static Preferences pref;
    private static ApplicationSettingsBean settingsBean;

    static {
        pref = Preferences.userNodeForPackage(DyeVC.class);
    }

    public static void storePreferences(ApplicationSettingsBean bean) {
        Preferences nodeToStore = pref.node(NODE_GENERAL_SETTINGS);
        nodeToStore.putInt(ApplicationSettingsBean.PROP_REFRESHINTERVAL, bean.getRefreshInterval());
        nodeToStore.put(ApplicationSettingsBean.PROP_LAST_USED_PATH, bean.getLastUsedPath());
        settingsBean = bean;
    }

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

    public static void persistRepositories(MonitoredRepositories listBeans) {
        List<MonitoredRepository> reps = listBeans.getMonitoredProjects();
        if (!reps.isEmpty()) {
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
            for (Iterator<MonitoredRepository> it = reps.iterator(); it.hasNext();) {
                MonitoredRepository repositoryBean = it.next();
                nodeToStore.node(repositoryBean.getId()).put("name", repositoryBean.getName());
                nodeToStore.node(repositoryBean.getId()).put("cloneaddress", repositoryBean.getCloneAddress());
                nodeToStore.node(repositoryBean.getId()).put("needsauthentication", Boolean.toString(repositoryBean.needsAuthentication()));
                if (repositoryBean.needsAuthentication()) {
                    nodeToStore.node(repositoryBean.getId()).put("user", repositoryBean.getUser());
                    nodeToStore.node(repositoryBean.getId()).put("password", repositoryBean.getPassword());
                }
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
                    Preferences repNode = nodeToStore.node(reps[i]);
                    MonitoredRepository bean = new MonitoredRepository();
                    bean.setId(repNode.name());
                    bean.setName(repNode.get("name", "no name"));
                    bean.setCloneAddress(repNode.get("cloneaddress", "no cloneaddress"));
                    bean.setNeedsAuthentication(Boolean.valueOf(repNode.get("needsauthentication", "false")));
                    if (bean.needsAuthentication()) {
                        bean.setUser(repNode.get("user", "user not set"));
                        bean.setPassword(repNode.get("password", "password not set"));
                    }
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
