package br.uff.ic.dyevc.graph.layout;

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

        //Check if there are nodes at the same place, if so apply repulsion
        for (V v3 : graph.getVertices()) {
            calcRepulsion(v3);
        }
    }

    /**
     * Calculates the position for each vertex of the graph
     * @param v Vertex whose position will be set
     * @param xPos X position of previous vertex
     * @param yPos Y position of previous vertex
     * @param yFather  Y position of the father of v's branch
     */
    protected synchronized void calcPositions(V v, double xPos, double yPos, double yFather) {
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

    protected synchronized void calcPositions(V v) {
//        Point2D xyd = transform(v);
//
//        double newXPos = xyd.getX();
//        double newYPos = xyd.getY();
//
//        if (v instanceof Node) {
//            //Node's X position is defined by the day it was created
//            newXPos = ((Node) v).getDate() * XDISTANCE;
//            //If node is a ProjectNode-type
//            if (v instanceof ProjectNode) {
//                //I want the Project-type node to always be on Y = 0
//                newYPos = 0;
//                xyd.setLocation(newXPos + XDISTANCE * 0.2, newYPos);
//            } //If node is a ClientNode-type
//            else if (v instanceof ClientNode) {
//                newYPos = -YDISTANCE * 6;
//                xyd.setLocation(newXPos - XDISTANCE, newYPos);
//            } //If node is a ArtifactNode-type
//            else if (v instanceof ArtifactNode) {
//                newYPos = -YDISTANCE * 6;
//                xyd.setLocation(newXPos, newYPos);
//            } //If node is a ProcessNode-type
//            else if (v instanceof ProcessNode) {
//                //The XY position for this type of node is dependable of the
//                //agent who executed the process
//
//                //Get edges from node v
//                Collection<E> edges = graph.getOutEdges(v);
//                for (E edge : edges) {
//                    //if the edge link to an Agent-node
//                    if (graph.getDest(edge) instanceof AgentNode) {
//                        //Compute position according to the agent position
//                        Point2D agentPos = transform(graph.getDest(edge));
//                        //Adding an offset to not be in the same line
//                        newYPos = agentPos.getY() + 50;
//                        //Compute X from the Agent position, removing the -XDISTANCE
//                        //to start at x=0, instead of x= -XDISTANCE position
//                        newXPos = agentPos.getX() + XDISTANCE + newXPos;
//                        xyd.setLocation(newXPos, newYPos);
//                    }
//                }
//            }
//        }
    }

    //Check if 2 nodes are at the same position, if so add an offset
    protected synchronized void calcRepulsion(V v1) {
        //Only Process and Artifact types can have the same position, so lets check
//        if ((v1 instanceof ProcessNode) || (v1 instanceof ArtifactNode)) {
//            try {
//                for (V v2 : graph.getVertices()) {
//                    if ((v2 instanceof ProcessNode) || (v2 instanceof ArtifactNode)) {
//                        //A check to see if we are not comparing him with himself
//                        if (v1 != v2) {
//                            Point2D p1 = transform(v1);
//                            Point2D p2 = transform(v2);
//                            if (p1 == null || p2 == null) {
//                                continue;
//                            }
//                            //Need to check both X and Y positions, so it is from the same employee
//                            if (Equals(p1.getX(), p2.getX()) && Equals(p1.getY(), p2.getY())) {
//                                p1.setLocation(p1.getX(), p1.getY() - variation * 0.5);
//                                p2.setLocation(p2.getX(), p2.getY() + variation * 0.5);
//                                //Need to check again in case another node is at the same new position
//                                calcRepulsion(v1);
//                            }
//                        }
//                    }
//                }
//            } catch (ConcurrentModificationException cme) {
////                calcRepulsion(v1);
//            }
//        }
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
//        throw new UnsupportedOperationException("Not supported yet.");
    }
//
//    protected static class FRVertexData extends Point2D.Double
//    {
//        protected void offset(double x, double y)
//        {
//            this.x += x;
//            this.y += y;
//        }
//
//        protected double norm()
//        {
//            return Math.sqrt(x*x + y*y);
//        }
//     }

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
}