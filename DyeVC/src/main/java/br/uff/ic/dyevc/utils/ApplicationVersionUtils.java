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
    private static String                  appVersion;
    private static String                  major         = "";
    private static String                  minor         = "";
    private static String                  patch         = "";
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

    /**
     * Checks if versionToBeChecked is less than or equal to versionThreeshold. Each component of the version is checked and,
     * if a greater component is less than or equal, the smaller components are not checked. For instance, if you compare
     * version 2.3.5 with version 3.2.4, the first version component (2) is already less than 3, so, the answer will be true.
     *
     * @param versionToBeChecked The version you want to be checked against some other version
     * @param versionThreeshold The version you want to set as threshold.
     * @return true, if versionToBeChecked is less than or equal to versionThreeshold
     */
    public static boolean isLessThanOrEqual(String versionToBeChecked, String versionThreeshold) {
        getInstance();

        // if running locally, always return false
        if (NOT_AVAILABLE.equals(appVersion)) {
            return false;
        }

        // if previous version was ran locally, return true
        if (NOT_AVAILABLE.equals(versionToBeChecked)) {
            return true;
        }

        int             versionMajor      = 0;
        int             versionMinor      = 0;
        int             versionPatch      = 0;
        int             checkVersionMajor = 0;
        int             checkVersionMinor = 0;
        int             checkVersionPatch = 0;

        StringTokenizer tokens            = new StringTokenizer(versionToBeChecked, ".");
        if (tokens.hasMoreTokens()) {
            versionMajor = Integer.parseInt(tokens.nextToken());
        }

        if (tokens.hasMoreTokens()) {
            versionMinor = Integer.parseInt(tokens.nextToken());
        }

        if (tokens.hasMoreTokens()) {
            versionPatch = Integer.parseInt(tokens.nextToken());
        }

        tokens = new StringTokenizer(versionThreeshold, ".");

        if (tokens.hasMoreTokens()) {
            checkVersionMajor = Integer.parseInt(tokens.nextToken());
        }

        if (tokens.hasMoreTokens()) {
            checkVersionMinor = Integer.parseInt(tokens.nextToken());
        }

        if (tokens.hasMoreTokens()) {
            checkVersionPatch = Integer.parseInt(tokens.nextToken());
        }

        if (versionMajor > checkVersionMajor) {
            return false;
        } else if (versionMajor < checkVersionMajor) {
            return true;
        }

        if (versionMinor > checkVersionMinor) {
            return false;
        } else if (versionMinor < checkVersionMinor) {
            return true;
        }

        if (versionPatch > checkVersionPatch) {
            return false;
        } else if (versionMinor <= checkVersionMinor) {
            return true;
        }

        return false;
    }
}
