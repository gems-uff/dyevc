package br.uff.ic.dyevc.graph.transform.topology;

import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.visualization.FourPassImageShaper;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;

/**
 * An implementation that transforms a node into an icon, according to the
 * node's attributes.
 */
public class TopologyVertexIconShapeTransformer implements Transformer<RepositoryInfo, Shape> {

    protected Map<Image, Shape> shapeMap = new HashMap<Image, Shape>();
    protected Transformer<RepositoryInfo, Shape> delegate;
    private String callerId;

    /**
     *
     *
     */
    public TopologyVertexIconShapeTransformer(String callerId) {
        this.delegate = new EllipseVertexShapeTransformer<RepositoryInfo>();
        this.callerId = callerId;
    }

    /**
     * @return Returns the delegate.
     */
    public Transformer<RepositoryInfo, Shape> getDelegate() {
        return delegate;
    }

    /**
     * @param delegate The delegate to set.
     */
    public void setDelegate(Transformer<RepositoryInfo, Shape> delegate) {
        this.delegate = delegate;
    }

    /**
     * get the shape from the image. If not available, get the shape from the
     * delegate VertexShapeFunction
     */
    @Override
    public Shape transform(RepositoryInfo v) {
        Icon icon = TopologyIconFactory.getIcon(v, callerId);
        if (icon != null && icon instanceof ImageIcon) {
            Image image = ((ImageIcon) icon).getImage();
            Shape shape = (Shape) shapeMap.get(image);
            if (shape == null) {
                shape = FourPassImageShaper.getShape(image, 30);
                if (shape.getBounds().getWidth() > 0
                        && shape.getBounds().getHeight() > 0) {
                    // don't cache a zero-sized shape, wait for the image
                    // to be ready
                    int width = image.getWidth(null);
                    int height = image.getHeight(null);
                    AffineTransform transform = AffineTransform
                            .getTranslateInstance(-width / 2, -height / 2);
                    shape = transform.createTransformedShape(shape);
                    shapeMap.put(image, shape);
                }
            }
            return shape;
        } else {
            return delegate.transform(v);
        }
    }

    /**
     * @return the shapeMap
     */
    public Map<Image, Shape> getShapeMap() {
        return shapeMap;
    }

    /**
     * @param shapeMap the shapeMap to set
     */
    public void setShapeMap(Map<Image, Shape> shapeMap) {
        this.shapeMap = shapeMap;
    }
}
