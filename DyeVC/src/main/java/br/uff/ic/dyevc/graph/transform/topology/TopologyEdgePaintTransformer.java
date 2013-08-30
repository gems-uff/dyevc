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
        super(pi, Color.BLACK, Color.YELLOW);
    }

    /**
     * Paints edges. Default color is black. Otherwise:
     * <ul>
     *      <li>Red, if edge corresponds to a Push relationship. </li>
     *      <li>Green, if edge corresponds to a Pull relationship. </li>
     *      <li>Yellow, if edge is picked. </li>
     * </ul>
     *
     * @param e The edge to be painted
     *
     * @return The Paint to be used
     */
    @Override
    public Paint transform(CloneRelationship e) {
        return super.transform(e);

//      Paint paint = super.transform(e);
//      if (!pi.isPicked(e)) {
//          if (e instanceof PushRelationship) {
//              paint = Color.RED;
//          }
//          if (e instanceof PullRelationship) {
//              paint = Color.GREEN;
//          }
//      }
//      return paint;
    }
}
