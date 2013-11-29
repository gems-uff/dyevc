package br.uff.ic.dyevc.utils;

//~--- JDK imports ------------------------------------------------------------

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System utility methods
 *
 * @author Cristiano
 */
public class SystemUtils {
    /**
     * Gets the hostname of the local computer, in canonical form (hostname + connection suffix)
     * @return The hostname of the local computer
     */
    public static String getLocalHostname() {
        String address = null;
        try {
            address = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            Logger.getLogger(SystemUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

        return address;
    }

    /**
     * Gets memory usage, in MB
     * @return
     */
    public static long getMemoryUsage() {
        long memory = Runtime.getRuntime().totalMemory();

        return Math.round(memory / Math.pow(2, 20));
    }

    /**
     * Parses a full path, returning the filename. If full path is a directory,
     * returns its last part
     * @param path The path to be parsed
     * @return The filename of last part of the specified full path
     */
    public static String getFilenameOrLastPath(String path) {
        Path p = Paths.get(path);

        return p.getFileName().toString();
    }
}
