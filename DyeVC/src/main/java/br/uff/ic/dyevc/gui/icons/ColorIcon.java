package br.uff.ic.dyevc.gui.icons;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/**
 * This class implements a plain color icon.
 *
 * @author Cristiano
 */
public class ColorIcon implements Icon {

    private int height = 16;
    private int width = 16;
    private Color color;

    public ColorIcon(Color color) {
        this.color = color;
    }
    
    public ColorIcon(Color color, int width, int height) {
        this.color = color;
        this.width = width;
        this.height = height;
    }
    
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(color);  
        g.fillRect(x, y, width, height);  
  
        g.setColor(Color.black);  
        g.drawRect(x, y, width, height); 
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }
}
