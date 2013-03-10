package br.uff.ic.dyevc.application;

import java.awt.Color;


/**
 *
 * @author Cristiano
 */
public interface IConstants {
    public static final String FETCH_SPECS = "+refs/heads/*:refs/remotes/origin/*";
    public static final String DIR_SEPARATOR = System.getProperty("file.separator");
    public static final String APPLICATION_PACKAGE = "br.uff.ic.dyevc";
    public static final String IMAGES_FOLDER = "/br/uff/ic/dyevc/images/";
    public static final Color BACKGROUND_COLOR = Color.WHITE;
    public static final Color COLOR_REGULAR = Color.CYAN;
    public static final Color COLOR_MERGE_SPLIT = Color.YELLOW;
    public static final Color COLOR_HEAD = Color.GRAY;
    public static final Color COLOR_FIRST = Color.BLACK;
    public static final Color COLOR_MERGE = Color.GREEN;
    public static final Color COLOR_SPLIT = Color.RED;
}
