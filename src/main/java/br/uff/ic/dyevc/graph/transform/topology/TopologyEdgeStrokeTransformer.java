package br.uff.ic.dyevc.graph.transform.topology;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.topology.PullRelationship;

import org.apache.commons.collections15.Transformer;

import static edu.uci.ics.jung.visualization.RenderContext.dotting;

//~--- JDK imports ------------------------------------------------------------

import java.awt.BasicStroke;
import java.awt.Stroke;

/**
 * Defines a pushStroke for topology edges, based on the type of edge (Push or Pull)
 * @author Cristiano
 * @param <E> The edge to transformed
 */
public class TopologyEdgeStrokeTransformer<E> implements Transformer<E, Stroke> {
    /** Dotting used in this stroke */
    private float[] dotting = { 2.0f, 6.0f };

    /** Stroke used for push edges */
    protected Stroke pushStroke = new BasicStroke(2);

    /** Stroke used for pull edges */
    protected Stroke pullStroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 2.0f, dotting,
                                      0f);

    /**
     * Builds a transformer with a default pushStroke of 2
     */
    public TopologyEdgeStrokeTransformer() {}

    /**
     * Transforms an edge, returning a stroke corresponding to the edge type.<BR>
     * Pull relationships maps to a dotted stroke and push relationships maps to a basic stroke
     *
     * @param e
     *
     * @return
     */
    @Override
    public Stroke transform(E e) {
        Stroke result = pushStroke;
        if (e instanceof PullRelationship) {
            result = pullStroke;
        }

        return result;
    }
}
