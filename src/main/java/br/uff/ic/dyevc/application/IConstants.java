package br.uff.ic.dyevc.application;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;

/**
 * Constants used in the system
 * @author Cristiano
 */
public interface IConstants {
    /** Max number of lines to keep in log window buffer. */
    public static final int DEFAULT_MAX_LOG_LINES = 10000;

    /** Max number of lines to keep in message window buffer. */
    public static final int DEFAULT_MAX_MESSAGE_LINES = 100;

    /** Specs to fetch nodes */
    public static final String FETCH_SPECS = "+refs/heads/*:refs/remotes/origin/*";

    /** Separator used for folders, according to the platform where the system is running. */
    public static final String DIR_SEPARATOR = System.getProperty("file.separator");

    /** The application root package. */
    public static final String APPLICATION_PACKAGE = "br.uff.ic.dyevc";

    /** Folder where images are stored. */
    public static final String IMAGES_FOLDER = "/br/uff/ic/dyevc/images/";

    /** String to reference the heads folder. */
    public static final String REFS_HEADS = "refs/heads/";

    /** String to reference the remotes folder. */
    public static final String REFS_REMOTES = "refs/remotes/";

    /** Color used to paint background in graphs. */
    public static final Color BACKGROUND_COLOR = Color.WHITE;

    /** Color used in regular nodes in graphs. */
    public static final Color COLOR_REGULAR = Color.CYAN;

    /** Color used for nodes where both a split and a merge occurs. */
    public static final Color COLOR_MERGE_SPLIT = Color.YELLOW;

    /** Color used to identify a head commmit. */
    public static final Color COLOR_HEAD = Color.GRAY;

    /** Color used to identify first commit in history. */
    public static final Color COLOR_FIRST = Color.BLACK;

    /** Color used to identify a merge node. */
    public static final Color COLOR_MERGE = Color.GREEN;

    /** Color used to identify a split node (where a branch occurs). */
    public static final Color COLOR_SPLIT = Color.RED;

    /** Color used to identify a auto collapsed node. */
    public static final Color COLOR_AUTO_COLLAPSE = Color.WHITE;
    
    /** Color used to identify a node that holds collapsed nodes. */
    public static final Color COLOR_COLLAPSED = Color.MAGENTA;

    /** Mask used to identify a commit for which type has not yet been determined. */
    public static final byte COMMIT_MASK_NOT_SET = 0;

    /** Mask used to identify a commit that all related repositories have. */
    public static final byte COMMIT_MASK_ALL_HAVE = 1 << 1;

    /** Mask used to identify a commit that I have but no repositories I push to have. */
    public static final byte COMMIT_MASK_I_HAVE_PUSH_DONT = 1 << 2;

    /** Mask used to identify a commit that I don't have bot someone I pull from has. */
    public static final byte COMMIT_MASK_I_DONT_PULL_HAS = 1 << 3;

    /** Mask used to identify a commit located in a repository not related to the one being analyzed. */
    public static final byte COMMIT_MASK_NON_RELATED_HAS = 1 << 4;

    /** Mask used to identify a commit that does not belong to a tracked branch and thus cannot be sent to other repositories. */
    public static final byte COMMIT_MASK_NOT_TRACKED = 1 << 5;

    /** Mask used to identify a collapsed node. */
    public static final byte COMMIT_MASK_COLLAPSED = 1 << 6;

    /** Color used to identify a commit that all related repositories have. */
    public static final Color TOPOLOGY_COLOR_ALL_HAVE = Color.WHITE;

    /** Color used to identify a commit that I have but no repositories I push to have. */
    public static final Color TOPOLOGY_COLOR_I_HAVE_PUSH_DONT = Color.GREEN;

    /** Color used to identify a commit that I don't have bot someone I pull from has. */
    public static final Color TOPOLOGY_COLOR_I_DONT_PULL_HAS = Color.YELLOW;

    /** Color used to identify a commit located in a repository not related to the one being analyzed. */
    public static final Color TOPOLOGY_COLOR_NON_RELATED_HAS = Color.RED;

    /** Color used to identify a commit that does not belong to a tracked branch and thus cannot be sent to other repositories. */
    public static final Color TOPOLOGY_COLOR_NOT_TRACKED = Color.LIGHT_GRAY;

    /** Color used to identify the stroke of a commit that referenced by a branch. */
    public static final Color TOPOLOGY_STROKE_COLOR_REFERENCED_BY_BRANCH = Color.BLACK;

    /** Color used to identify the stroke of a commit that referenced by a tag (Orange Red). */
    public static final Color TOPOLOGY_STROKE_COLOR_REFERENCED_BY_TAG = new Color(255, 69, 0);

    /** Default size for single vertices in graphs. */
    public static final int GRAPH_VERTEX_SINGLE_SIZE = 40;

    /** Default size for cluster vertices in graphs. */
    public static final int GRAPH_VERTEX_CLUSTER_SIZE = 60;

    /** The line separator used, according to the platform where the system is running. */
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /** Name of snapshot file */
    public static final String SNAPSHOT_FILE_NAME = "snapshot.ser";
}
