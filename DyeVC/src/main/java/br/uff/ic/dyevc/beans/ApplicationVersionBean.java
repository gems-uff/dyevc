package br.uff.ic.dyevc.beans;

import java.beans.*;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.slf4j.LoggerFactory;

/**
 * Reads application version from manifest file and publishes it as a property.
 *
 * @author Cristiano
 */
public class ApplicationVersionBean implements Serializable {

    public static final String PROP_APP_VERSION = "appVersion";
    private static final long serialVersionUID = -5459049343284022481L;
    private final String appVersion;
    private PropertyChangeSupport propertySupport;

    public ApplicationVersionBean() {
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

        Class clazz = ApplicationVersionBean.class;
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();
        if (classPath.startsWith("jar")) {
            String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1)
                    + "/META-INF/MANIFEST.MF";
            Manifest manifest;
            try {
                manifest = new Manifest(new URL(manifestPath).openStream());
                Attributes attr = manifest.getMainAttributes();
                out = "DyeVC Version: " + attr.getValue("Implementation-Version");
            } catch (IOException ex) {
                LoggerFactory.getLogger(ApplicationVersionBean.class).warn("Error reading manifest file.", ex);
            }
        }
        return out;

    }
}
