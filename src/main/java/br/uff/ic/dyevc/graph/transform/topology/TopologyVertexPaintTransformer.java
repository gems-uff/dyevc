package br.uff.ic.dyevc.graph.transform.topology;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.topology.RepositoryInfo;

import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.picking.PickedInfo;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;
import java.awt.Paint;

/**
 * Transformer to paint vertices in a topology graph
 *
 * @author Cristiano
 */
public class TopologyVertexPaintTransformer extends PickableVertexPaintTransformer<RepositoryInfo> {
    private final String callerId;

    /**
     * Builds a transformer to paint topology vertices
     *
     * @param pi The info about whether the vertex is picked or not
     * @param callerId The id of the repository to be shown as the focus
     */
    public TopologyVertexPaintTransformer(PickedInfo<RepositoryInfo> pi, String callerId) {
        super(pi, Color.BLACK, Color.YELLOW);
        this.callerId = (callerId != null) ? callerId : "";
    }

    /**
     * Paints vertex. Default is red. Otherwise:
     * <ul>
     *      <li>Blue, if vertex corresponds to the clone where user chose to view topology. </li>
     *      <li>Yellow, if vertex is picked. </li>
     *      <li>Black, otherwise. </li>
     * </ul>
     */
    @Override
    public Paint transform(RepositoryInfo info) {
        Paint paint = super.transform(info);
        if (!pi.isPicked(info)) {
            if (info.getId().equals(callerId)) {
                paint = Color.BLUE;
            }

            if (info.getPullsFrom().isEmpty() && info.getPushesTo().isEmpty()) {
                paint = Color.BLACK;
            }
        }

        return paint;
    }
}
