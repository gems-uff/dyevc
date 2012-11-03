/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.utils;

import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.ImageIcon;

/**
 *
 * @author Cristiano
 */
public class ImageUtils {
    private static ImageUtils instance;
    
    static {
        instance = new ImageUtils();
    }
    
    private ImageUtils() {
    }
    
    public static ImageUtils getInstance() {
        return instance;
    }
    
    /**
     * Gets an image.
     *
     * @return
     */
    public Image getImage(String imageName) {
        return Toolkit.getDefaultToolkit().getImage(getClass().getResource("/br/uff/ic/dyevc/images/" + imageName));
    }

    /**
     * Gets an image as an icon.
     *
     * @return
     */
    public ImageIcon getIcon(String imageName) {
        return new ImageIcon(getClass().getResource("/br/uff/ic/dyevc/images/" + imageName));
    }

    
}
