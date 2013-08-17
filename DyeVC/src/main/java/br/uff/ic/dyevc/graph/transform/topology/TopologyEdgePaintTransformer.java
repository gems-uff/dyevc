package br.uff.ic.dyevc.graph.transform.topology;

import br.uff.ic.dyevc.model.topology.CloneRelationship;
import br.uff.ic.dyevc.model.topology.PullRelationship;
import br.uff.ic.dyevc.model.topology.PushRelationship;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import java.awt.Color;
import java.awt.Paint;

/**
 * Transformer to paint edges in a topology graph
 *
 * @author Cristiano
 */
public class TopologyEdgePaintTransformer extends PickableEdgePaintTransformer<CloneRelationship> {

    /**
     * Builds a transformer to paint topology vertices
     *
     * @param pi The info about whether the edge is picked or not
     */
    public TopologyEdgePaintTransformer(PickedInfo<CloneRelationship> pi) {
        super(pi, Color.BLACK, Color.YELLOW);
    }

    /**
     * Paints edges. Default color is black. Otherwise: 
     * <ul>
     *      <li>Red, if edge corresponds to a Push relationship. </li>
     *      <li>Green, if edge corresponds to a Pull relationship. </li>
     *      <li>Yellow, if edge is picked. </li>
     * </ul>
     */
    @Override
    public Paint transform(CloneRelationship e) {
        Paint paint = super.transform(e);
        if (!pi.isPicked(e)) {
            if (e instanceof PushRelationship) {
                paint = Color.RED;
            }
            if (e instanceof PullRelationship) {
                paint = Color.GREEN;
            }
        }
        return paint;
    }
}
