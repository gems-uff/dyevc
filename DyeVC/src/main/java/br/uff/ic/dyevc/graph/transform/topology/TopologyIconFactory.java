package br.uff.ic.dyevc.graph.transform.topology;

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import edu.uci.ics.jung.visualization.LayeredIcon;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.apache.commons.logging.LogFactory;

/**
 * A factory that returns an icon based on a key
 *
 * @author Cristiano
 */
class TopologyIconFactory {
    private static final String REGULAR_NODE = "nodeRegular";
    private static final String CALLER_NODE = "nodeCaller";
    private static final String CENTRAL_NODE = "nodeCentral";
    private static Map<RepositoryInfo, Icon> iconMap = new HashMap<RepositoryInfo, Icon>();

    /**
     * Returns the icon to the specified <code>v</code> or null if the icon cannot be
     * found.
     *
     * @return the icon
     */
    static Icon getIcon(RepositoryInfo v, String callerId) {
        Icon icon = iconMap.get(v);
        if (icon == null) {
            String key = IConstants.IMAGES_FOLDER + "topology/red-laptop.png";
            if (v.getId().equals(callerId)) {
                key = IConstants.IMAGES_FOLDER + "topology/green-laptop.png";
            }
            if (v.getPullsFrom().isEmpty()
                    && v.getPushesTo().isEmpty()) {
                key = IConstants.IMAGES_FOLDER + "topology/server.png";
            }
            try {
                icon = new LayeredIcon(new ImageIcon(TopologyIconFactory.class.getResource(key)).getImage());
                iconMap.put(v, icon);
            } catch (Exception ex) {
                LogFactory.getLog(TopologyIconFactory.class).error("It was not possible to load icon map. "
                        + "Nodes will be present as a raw ellipsis", ex);
            }
        }
        return icon;
    }
}
