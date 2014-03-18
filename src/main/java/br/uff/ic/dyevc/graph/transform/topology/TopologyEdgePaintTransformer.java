package br.uff.ic.dyevc.graph.transform.topology;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.topology.CloneRelationship;

import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.picking.PickedInfo;

//~--- JDK imports ------------------------------------------------------------

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
        super(pi, Color.GREEN, Color.YELLOW);
    }

    /**
     * Paints edges. Default color is green and indicates that the origin node of the relationship is in sync with the
     * destination node of the relationship. If it's not, than color is changed to red.
     *
     * @param e The edge to be painted
     *
     * @return The Paint to be used
     */
    @Override
    public Paint transform(CloneRelationship e) {
        Paint paint = super.transform(e);
        if (!pi.isPicked(e)) {
            if (e.getNonSyncCommitsCount() != 0) {
                paint = Color.RED;
            }
        }

        return paint;
    }
}
