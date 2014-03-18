package br.uff.ic.dyevc.graph.transform.topology;

import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import edu.uci.ics.jung.visualization.LayeredIcon;
import edu.uci.ics.jung.visualization.renderers.Checkmark;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.Icon;

/**
 * Adds a checkmark icon when a vertex is picked and removes it when it is 
 * unpicked.
 *
 * @author Cristiano
 *
 */
public class TopologyPickWithIconListener implements ItemListener {

    TopologyVertexIconTransformer imager;
    Icon checked;

    public TopologyPickWithIconListener(TopologyVertexIconTransformer imager) {
        this.imager = imager;
        checked = new Checkmark();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Icon icon = imager.transform((RepositoryInfo) e.getItem());
        if (icon != null && icon instanceof LayeredIcon) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                ((LayeredIcon) icon).add(checked);
            } else {
                ((LayeredIcon) icon).remove(checked);
            }
        }
    }
}
