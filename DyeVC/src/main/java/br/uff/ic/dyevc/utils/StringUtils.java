package br.uff.ic.dyevc.utils;

/**
 * String utility methods
 *
 * @author Cristiano
 */
public class StringUtils {

    public static String normalizePath(String text) {
        String result = text;
        while (result.charAt(0) == '/') {
            result = result.substring(1);
        }
        return result.replace("\\", "/");
    }
    
    public static String generateRepositoryId() {
        return "rep" + System.currentTimeMillis();
    }
}
