package br.uff.ic.dyevc.utils;

/**
 * System utility methods
 *
 * @author Cristiano
 */
public class SystemUtils {
    /**
     * Gets memory usage, in MB
     * @return 
     */
    public static long getMemoryUsage() {
        long memory = Runtime.getRuntime().totalMemory();
        return Math.round(memory / Math.pow(2, 20));
    }
    
}
