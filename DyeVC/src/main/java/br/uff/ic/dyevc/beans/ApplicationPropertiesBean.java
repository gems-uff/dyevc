/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.beans;

import java.beans.*;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Cristiano
 */
public class ApplicationPropertiesBean implements Serializable {

    public static final String PROP_APP_VERSION = "appVersion";
    private static final long serialVersionUID = -5459049343284022481L;
    private final String appVersion;
    private PropertyChangeSupport propertySupport;

    public ApplicationPropertiesBean() {
        propertySupport = new PropertyChangeSupport(this);
        appVersion = getVersionFromManifest();
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    private String getVersionFromManifest() {
        String out = "DyeVC: Version not Identified!";

        Class clazz = ApplicationPropertiesBean.class;
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();
        if (classPath.startsWith("jar")) {
            String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1)
                    + "/META-INF/MANIFEST.MF";
            Manifest manifest;
            try {
                manifest = new Manifest(new URL(manifestPath).openStream());
                Attributes attr = manifest.getMainAttributes();
                out = "DieVC Version: " + attr.getValue("Implementation-Version");
            } catch (IOException ex) {
                Logger.getLogger(ApplicationPropertiesBean.class.getName()).log(Level.WARNING, "Error reading manifest file.", ex);
            }
        }
        return out;

    }
}
