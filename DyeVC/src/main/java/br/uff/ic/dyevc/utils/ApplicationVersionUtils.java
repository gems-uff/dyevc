package br.uff.ic.dyevc.utils;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.net.URL;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Reads application version from manifest file.
 *
 * @author Cristiano
 */
public class ApplicationVersionUtils {
    private static String appVersion;

    public static String getAppVersion() {
        if (appVersion == null) {
            appVersion = getVersionFromManifest();
        }

        return appVersion;
    }

    private static String getVersionFromManifest() {
        String out       = "not available";

        Class  clazz     = ApplicationVersionUtils.class;
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();
        if (classPath.startsWith("jar")) {
            String   manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
            Manifest manifest;
            try {
                manifest = new Manifest(new URL(manifestPath).openStream());
                Attributes attr = manifest.getMainAttributes();
                out = attr.getValue("Implementation-Version");
            } catch (IOException ex) {
                LoggerFactory.getLogger(ApplicationVersionUtils.class).warn("Error reading manifest file.", ex);
            }
        }

        return out;

    }
}
