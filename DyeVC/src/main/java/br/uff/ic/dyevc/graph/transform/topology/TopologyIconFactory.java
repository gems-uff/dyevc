package br.uff.ic.dyevc.graph.transform.topology;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;

import edu.uci.ics.jung.visualization.LayeredIcon;

import org.apache.commons.logging.LogFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * A factory that returns an icon based on a key
 *
 * @author Cristiano
 */
class TopologyIconFactory {
    private static final Map<RepositoryInfo, Icon>   iconMap = new HashMap<RepositoryInfo, Icon>();
    private static final Map<RepositoryInfo, String> keyMap  = new HashMap<RepositoryInfo, String>();

    /**
     * Returns the icon to the specified <code>v</code> or null if the icon cannot be
     * found.
     *
     * @return the icon
     */
    static Icon getIcon(RepositoryInfo v, String callerId) {
        Icon   icon = iconMap.get(v);
        String key  = IConstants.IMAGES_FOLDER + "topology/black-laptop.png";
        if (v.getId().equals(callerId)) {
            key = IConstants.IMAGES_FOLDER + "topology/blue-laptop.png";
        }

        if (v.getPullsFrom().isEmpty() && v.getPushesTo().isEmpty()) {
            key = IConstants.IMAGES_FOLDER + "topology/server.png";
        }

        String previousKey = keyMap.get(v);
        if ((icon == null) ||!key.equals(previousKey)) {
            try {
                icon = new LayeredIcon(new ImageIcon(TopologyIconFactory.class.getResource(key)).getImage());
                iconMap.put(v, icon);
                keyMap.put(v, key);
            } catch (Exception ex) {
                LogFactory.getLog(TopologyIconFactory.class).error("It was not possible to load icon map. "
                                  + "Nodes will be present as a raw ellipsis", ex);
            }
        }

        return icon;
    }
}
