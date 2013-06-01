package br.uff.ic.dyevc.graph.layout;

import br.uff.ic.dyevc.model.CommitInfo;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Layout for drawing a repository history
 *
 * @author Cristiano
 */
public class RepositoryHistoryLayout<V, E> extends AbstractLayout<V, E> implements IterativeContext {

    private static final double XDISTANCE = 50.0;
    private static final double YDISTANCE = 50.0;
    private static final double VARIATION = 30;
    private static final double EPSILON = 0.000001D;
    private ArrayList<V> heads = new ArrayList<V>();

    /**
     * Creates an instance for the specified graph.
     */
    public RepositoryHistoryLayout(Graph<V, E> g) {
        super(g);
    }

    @Override
    public void reset() {
        doInit();
    }

    @Override
    public void initialize() {
        doInit();
    }

    private void doInit() {
        //Initial height of tree
        int height = 0;
        //Y position of node's branch's father
        double yFather = 0.0;
        //Starting X position
        double xPos = 0.0;

        heads.clear();
        calcXPositionsAndSelectHeads(xPos);
        while (!heads.isEmpty()) {
            V v = heads.remove(heads.size() - 1);
            height = calcYPositions(v, height);
        }
    }

    protected boolean Equals(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    /**
     * This one is an incremental visualization.
     */
    public boolean isIncremental() {
        return true;
    }

    /**
     * Returns true once the current iteration has passed the maximum count,
     * <tt>MAX_ITERATIONS</tt>.
     */
    @Override
    public boolean done() {
        return true;
    }

    @Override
    public void step() {
    }

    /**
     * Calculates the position for each vertex of the graph
     *
     * @param v Initial vertex of subtree to calculate height
     * @param height Initial height of subtree
     */
    protected synchronized int calcYPositions(V v, int height) {
        //maxHeight of subtree is initially equals to height
        int maxHeight = height;
        boolean visited = false;
        while (!visited) {
            if (v instanceof CommitInfo) {
                CommitInfo ci = (CommitInfo) v;
                visited = ci.isVisited();
                if (!visited) {
                    ci.setVisited(true);
                    Point2D xyd = transform(v);
                    xyd.setLocation(xyd.getX(), YDISTANCE * height);
                    int parentsCount = graph.getSuccessorCount(v);
                    if (parentsCount == 1) {
                        // only one parent, process it
                        v = graph.getSuccessors(v).iterator().next();
                    } else {
                        // more parents -> process each one, increasing height after for each one of them
                        //i is initially -1 because the first subtree will be at same height as its child
                        int i = -1;
                        for (V parent : graph.getSuccessors(v)) {
                            i++;
                            maxHeight = maxHeight + i;
                            calcYPositions(parent, maxHeight);
                        }
                    }
                }
            }
        }
        return maxHeight;
    }

    /**
     *
     * @param xPos
     */
    private void calcXPositionsAndSelectHeads(double xPos) {
        for (V v : graph.getVertices()) {
            Point2D xyd = transform(v);
            xyd.setLocation(xPos, xyd.getY());
            xPos += XDISTANCE;
            if (graph.getPredecessorCount(v) == 0) {
                heads.add(v);
            }
        }
    }
}