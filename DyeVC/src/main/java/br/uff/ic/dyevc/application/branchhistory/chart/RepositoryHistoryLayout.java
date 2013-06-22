/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.chart;

import br.uff.ic.dyevc.model.CommitInfo;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import java.awt.geom.Point2D;

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
        //Starting Y position
        double yPos = 0.0;
        //Y position of node's branch's father
        double yFather = 0.0;
        //Starting X position
        double xPos = 0.0;

        V initialVertex = getInitialVertex();
        calcPositions(initialVertex, xPos, yPos, yFather);
    }

    /**
     * Calculates the position for each vertex of the graph
     *
     * @param v Vertex whose position will be set
     * @param xPos X position of previous vertex
     * @param yPos Y position of previous vertex
     * @param yFather Y position of the father of v's branch
     */
    protected synchronized void calcPositions(V v, double xPos, double yPos, double yFather) {
        if (v instanceof CommitInfo) {
            CommitInfo ci = (CommitInfo) v;
            if (!ci.isVisited()) {
                ci.setVisited(true);
                processNode(v, xPos, yPos, yFather);
            }
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

    private V getInitialVertex() {
        V result = null;
        for (V v : graph.getVertices()) {
            if (graph.getSuccessorCount(v) == 0) {
                result = v;
                break;
            }
        }
        return result;
    }

    private void processNode(V v, double xPos, double yPos, double yFather) {
        Point2D xyd = transform(v);

        int childrenCount = graph.getPredecessorCount(v);
        int parentsCount = graph.getSuccessorCount(v);

        if (parentsCount == 0) {
            //no out edges -> first commit (use provided coords)
        }

        if (parentsCount == 1) {
            // one father only -> same yPos, increment xPos
            xPos += XDISTANCE;
        }

        if (parentsCount > 1) {
            // more parents -> this is a merge, return to father's yPos and increment xPos
            yPos = yFather;
            xPos += XDISTANCE;
        }
        xyd.setLocation(xPos, yPos);

        boolean evenNode = false;
        double yChild = yPos;
        for (V vx : graph.getPredecessors(v)) {
            if (childrenCount == 1) {
                //only one child, does not change yPos
            } else {
                //more then one child, changes yPos
                if (evenNode) {
                    //even nodes go to a negative yPos
                    yChild *= -1;
                } else {
                    //odd nodes go to a positive yPos
                    yChild = Math.abs(yChild) + YDISTANCE;
                }
            }
            calcPositions(vx, xPos, yChild, yFather);
            evenNode = !evenNode;
        }
    }
}