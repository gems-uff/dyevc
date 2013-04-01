package br.uff.ic.dyevc.graph.transform;

import br.uff.ic.dyevc.application.IConstants;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

/**
 * Creates a vertex shape that is either a polygon or a star. 
 * The number of sides corresponds to the number of vertices that were
 * collapsed into the vertex represented by this shape.
 *
 * @author Cristiano
 *
 * @param <V>
 */
public class ClusterVertexShapeTransformer<V> extends EllipseVertexShapeTransformer<V> {

    public ClusterVertexShapeTransformer() {
        setSizeTransformer(new ClusterVertexSizeTransformer(IConstants.GRAPH_VERTEX_SINGLE_SIZE));
    }

    @Override
    public Shape transform(V v) {
        if (v instanceof Graph) {
            int size = ((Graph) v).getVertexCount();
            if (size < 8) {
                int sides = Math.max(size, 3);
                return factory.getRegularPolygon(v, sides);
            } else {
                return factory.getRegularStar(v, size);
            }
        }
        
        return factory.getEllipse(v);
    }
}