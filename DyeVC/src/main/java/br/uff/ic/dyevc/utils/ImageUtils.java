package br.uff.ic.dyevc.utils;

import br.uff.ic.dyevc.application.IConstants;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.ImageIcon;

/**
 * Singleton that loads application icons and images.
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
     * @param imageName the name of image to be loaded.
     *
     * @return the image specified by imageName.
     */
    public Image getImage(String imageName) {
        return Toolkit.getDefaultToolkit().getImage(getClass().getResource(IConstants.IMAGES_FOLDER + imageName));
    }

    /**
     * Gets an image as an icon.
     * 
     * @param imageName the name of image to be loaded as an icon.
     *
     * @return the image specified by imageName, converted to an icon.
     */
    public ImageIcon getIcon(String imageName) {
        return new ImageIcon(getClass().getResource(IConstants.IMAGES_FOLDER + imageName));
    }

    
}
