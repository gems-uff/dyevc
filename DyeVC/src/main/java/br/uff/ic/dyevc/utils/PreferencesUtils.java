/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.utils;

import br.uff.ic.dyevc.application.DyeVC;
import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import java.util.prefs.Preferences;

/**
 *
 * @author Cristiano
 */
public final class PreferencesUtils {
    private static Preferences pref;
    static {
        pref = Preferences.userNodeForPackage(DyeVC.class).node("generalsettings");
    }

    public static void storePreferences(ApplicationSettingsBean bean) {
        pref.put("workingpath", bean.getWorkingPath());
        pref.putInt("refreshinterval", bean.getRefreshInterval());
    }

    public static ApplicationSettingsBean loadPreferences() {
        ApplicationSettingsBean bean = new ApplicationSettingsBean();
        bean.setWorkingPath(pref.get("workingpath",""));
        bean.setRefreshInterval(pref.getInt("refreshinterval", 0));
        
        return bean;
    }
}
