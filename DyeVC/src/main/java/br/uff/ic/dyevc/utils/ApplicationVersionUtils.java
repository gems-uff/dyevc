package br.uff.ic.dyevc.utils;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.net.URL;

import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.StringTokenizer;

/**
 * Reads application version from manifest file.
 *
 * @author Cristiano
 */
public class ApplicationVersionUtils {
    private static ApplicationVersionUtils instance;
    private String                         appVersion;
    private String                         major         = "";
    private String                         minor         = "";
    private String                         patch         = "";
    private static final String            NOT_AVAILABLE = "not available";

    /**
     * Constructs an instance of ApplicationVersionUtils
     */
    private ApplicationVersionUtils() {
        appVersion = getVersionFromManifest();

        if (!NOT_AVAILABLE.equals(appVersion)) {
            StringTokenizer tokens = new StringTokenizer(appVersion, ".");
            if (tokens.hasMoreTokens()) {
                major = tokens.nextToken();
            }

            if (tokens.hasMoreTokens()) {
                minor = tokens.nextToken();
            }

            if (tokens.hasMoreTokens()) {
                patch = tokens.nextToken();
            }
        }
    }

    public synchronized static ApplicationVersionUtils getInstance() {
        if (instance == null) {
            instance = new ApplicationVersionUtils();
        }

        return instance;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getMajor() {
        return major;
    }

    public String getMinor() {
        return minor;
    }

    public String getPatch() {
        return patch;
    }

    private String getVersionFromManifest() {
        String out       = NOT_AVAILABLE;

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
