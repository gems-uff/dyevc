package br.uff.ic.dyevc.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
     * @throws UnknownHostException 
     */
    public static String getLocalHostname() {
        String address = null;
        try {
            address = InetAddress.getLocalHost().getCanonicalHostName().toLowerCase();
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
    
}
