package br.uff.ic.dyevc.graph.layout;

import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.gui.core.SplashScreen;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.MonitoredRepository;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import javax.swing.JOptionPane;
import org.eclipse.jgit.revwalk.RevCommit;
import org.gitective.core.CommitUtils;
import org.slf4j.LoggerFactory;

/**
 * Layout for drawing a repository history
 *
 * @author Cristiano
 */
public class RepositoryHistoryLayout<V, E> extends AbstractLayout<V, E> implements IterativeContext {

    private V firstCommit = null;
    private static final double XDISTANCE = 70.0;
    private static final double YDISTANCE = 70.0;
    private static final double EPSILON = 0.000001D;
    
    /**
     * List of heads found in the repository (commits with no children)
     */
    private ArrayList<V> heads = new ArrayList<V>();
    
    /**
     * List of heads that were already processed
     */
    private ArrayList<V> processedHeads = new ArrayList<V>();
    
    /**
     * List of heights for each node. To find the height of a node, all that is
     * needed is to divide the X position by XDISTANCE and the integer result is
     * the position in the list where the node's height is stored.
     */
    private List<Integer> heights;
    
    /**
     * Stores the nodes in the order corresponding to its X position
     */
    private ArrayList<V> nodes = new ArrayList<V>();
    
    /**
     * Maps each node's hash info with its X position
     */
    private HashMap<String, Double> nodePositions = new HashMap<String, Double>();
    
    
    /**
     * DijkstraDistance is used to find out if there is a path between two nodes.
     */
    private DijkstraDistance<V, E> distances;
    
    /**
     * Repository from where this log is being drawn
     */
    MonitoredRepository rep;

    /**
     * Creates an instance for the specified graph.
     */
    public RepositoryHistoryLayout(DirectedOrderedSparseMultigraph<V, E> g, MonitoredRepository rep) {
        super(g);
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("Constructor -> Entry");
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).debug("Constructor -> Graph has {} nodes to be plotted.", g.getVertexCount());
        this.rep = rep;
        distances = new DijkstraDistance<V, E>(g);
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("Constructor -> Exit");
    }

    @Override
    public void reset() {
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("reset -> Entry");
        doInit();
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("reset -> Exit");
    }

    @Override
    public void initialize() {
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("initialize -> Entry");
        doInit();
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("initialize -> Exit");
    }
    
    public int getWidth() {
        return graph.getVertexCount() * (int)XDISTANCE;
    }

    private void doInit() {
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("doInit -> Entry");
        SplashScreen splash = SplashScreen.getInstance();
        try {
            //Starting X position
            double xPos = 0.0;

            heads.clear();
            processedHeads.clear();
            nodes.clear();

            splash.setStatus("Calculating X positions...");
            splash.setVisible(true);
            calcXPositionsAndFindHeads(xPos);
            LoggerFactory.getLogger(RepositoryHistoryLayout.class).debug("doInit -> Graph has {} nodes and {} heads", nodes.size(), heads.size());
            LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("doInit -> Initializing visited state for all graph nodes");

            splash.setStatus("Resetting visited status for " + graph.getVertexCount() + " vertices...");
            for (V v: graph.getVertices()) {
                //resets the attribute "visited" of each node to repaint graph uppon user demand
                if (v instanceof CommitInfo) ((CommitInfo)v).setVisited(false);
            }
            LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("doInit -> Finished initializing visited state for all graph nodes");

            int i = 1;
            while (!heads.isEmpty()) {
                splash.setStatus("Calculating Y positions starting in head " + i++ +"/"+ heads.size() + "...");            
                //Initial height of tree
                int height = 0;
                V v = heads.remove(heads.size() - 1);

                //Get max height 
                for (V head: processedHeads) {
                    height = Math.max(height, findMaxHeightBetweenSubtrees(v, head));
                }
                processedHeads.add(v);
                // There is no problem in starting with height = 0, because the
                // algorithm will change it if necessary
                calcYPositions(v, height);
            }
            splash.setVisible(false);
        } catch (VCSException vcse) {
            JOptionPane.showMessageDialog(null, "Application received the following exception trying to show repository commit history:\n" +
                    vcse + "\n\nOpen console window to see error details.", "Error found!", JOptionPane.ERROR_MESSAGE);
            
        } catch(RuntimeException ex) {
            ex.printStackTrace(System.err);
            JOptionPane.showMessageDialog(null, "Application received the following exception trying to show repository commit history:\n" +
                    ex + "\n\nOpen console window to see error details.", "Error found!", JOptionPane.ERROR_MESSAGE);
        } finally {
            splash.dispose();
        }
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("doInit -> Exit");
    }

    /**
     * Calculates the X position for each node. Positions are calculated starting 
     * from the first commit, which will be placed at X position equals zero. 
     * then, each subsequent node is placed to the right of the previous one. 
     * When a split is found, then the dates of each commit are used to determine 
     * which one will be plotted first.<br>
     * This is to give a cronological order of the commits. The dates are not used 
     * solely because the repositories are distributed, which can lead to different 
     * clocks being used, with no central time predefined. <br>
     * 
     * The usage of the dates to determine the order in splits is implicit, as 
     * the CommitInfo implements the Comparable interface using the commit date as 
     * the comparison attribute.
     * @param xPos
     */
    private void calcXPositionsAndFindHeads(double xPos) {
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("calcXPositionsAndFindHeads -> Entry.");
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).debug("calcXPositionsAndFindHeads -> Initial xPos is <{}>", xPos);
        //Initializes the heights list with -1 for each node, meaning that the
        //height was not calculated yet
        Integer heightsArray[] = new Integer[graph.getVertexCount()];
        Arrays.fill(heightsArray, new Integer(-1));
        heights = Arrays.asList(heightsArray);

        //Gets the first commit and adds it to the list of nodes to be processed.
        TreeSet<V> nodesToProcess = new TreeSet<V>();
        nodesToProcess.add(getFirstCommit());
        
        //Leave the loop if there is no node to process
        while (!nodesToProcess.isEmpty()) {
            //As the nodes natural order are based on the commit's date, first
            //node will be the most ancient commit
            V v = nodesToProcess.first();
            nodesToProcess.remove(v);
            
            //In the case of a merge, the v will be found more than once.
            //If this happens, v will be shifted to a new X position and all nodes
            //that were after it before will be left shifted.
            if (nodes.contains(v)) {
                double newXPos = xPos - (XDISTANCE * 2);
                for (int i = nodes.indexOf(v); i < nodes.size(); i++) {
                    //ajustar a posição x, recuando em xdistance
                    V node = nodes.get(i);
                    Point2D coords = transform(node);
                    coords.setLocation(newXPos, coords.getY());
                    nodePositions.put(getHashFromNode(node), new Double(newXPos));
                }
                xPos -= XDISTANCE;
                nodes.remove(v);
            }
            Point2D xyd = transform(v);
            xyd.setLocation(xPos, xyd.getY());
            nodePositions.put(getHashFromNode(v), new Double(xPos));
            nodes.add(v);
            xPos += XDISTANCE;

            int childrenCount = graph.getPredecessorCount(v);
            
            if (childrenCount == 0)  {
                //A new head was found. Include it in the list, because Y position
                //is calculated starting with the heads.
                heads.add(v);
            } else {
                //Include predecessors in the Set to be processed, taking care of
                //its type, because collapsed graphs will not be processed here.
                for (V child : graph.getPredecessors(v)) {
                    if (child instanceof CommitInfo) {
                        nodesToProcess.add(child);
                    }
                }
            }
        }
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).debug("calcXPositionsAndFindHeads Final xPos is <{}>", xPos);
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("calcXPositionsAndFindHeads -> Exit.");
    }

    /**
     * Calculates the Y position for each vertex of the graph
     *
     * @param v Initial vertex of subtree to calculate height
     * @param childHeight Initial height of subtree
     * @return true if height is different from initial childHeight
     */
    protected synchronized boolean calcYPositions(V v, int childHeight) {
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("calcYPositions -> Entry");
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).debug("calcYPositions -> Will now calculate yPos for node <{}>, which initially received childHeight <{}>", ((CommitInfo)v).getHash(), childHeight);
        boolean result = false;
        boolean visited = false;
        while (!visited) { //Visits each node only once
            if (v instanceof CommitInfo) {
                CommitInfo ci = (CommitInfo) v;
                visited = ci.isVisited();
                if (!visited) {
                    ci.setVisited(true);
                    Point2D xyd = transform(v);

                    /* Verifies if there is any node with height greater or equals
                     * to childHeight, between v and its sucessors and predecessors, 
                     * and gets the highest number to be used as v's height */
                    int height = Math.max(childHeight, findMaxHeightBetweenSuccessors(v, childHeight));
                    height = Math.max(height, findMaxHeightBetweenPredecessors(v, height));
                    xyd.setLocation(xyd.getX(), YDISTANCE * height);

                    if (childHeight != height) {
                        result = true;
                        V nodeToCheck = v;
                        while (isOnlyParentAndChild(nodeToCheck)) {
                            /*
                             * If node has only one child and is the only parent of this child, then
                             * children before it should be at the same height until a merge is found
                             */
                            V child = graph.getPredecessors(nodeToCheck).iterator().next();
                            Point2D xydChild = transform(child);
                            xydChild.setLocation(xydChild.getX(), xyd.getY());
                            nodeToCheck = child;
                        }
                    }

                    childHeight = height;

                    heights.set(calcIndexFromXPosition(v), childHeight);

                    int parentsCount = graph.getSuccessorCount(v);
                    if (parentsCount == 1) {
                        // only one parent, process it using the same childHeight
                        v = graph.getSuccessors(v).iterator().next();
                    } else {
                        // more parents -> process each one, increasing height for each one of them
                        //i is initially -1 because the first subtree will be at same height as its child
                        int i = -1;
                        for (V parent : graph.getSuccessors(v)) {
                            i++;
                            boolean heightChanged = calcYPositions(parent, childHeight + i);
                            if (heightChanged) {
                                //if height was changed during parent's processing,
                                //update it to make sure that further parents do not to collide.
                                childHeight = getHeightFromXPosition(parent);
                            }
                        }
                    }
                }
            }
        }
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("calcYPositions -> Exit");
        return result;
    }
    /**
     * Calculates the max height between v and its most ancient successor, not
     * including them.
     *
     * @param v Node for which max height will be calculated
     * @return max height found
     */
    private int findMaxHeightBetweenSuccessors(V v, int height) {
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("findMaxHeightBetweenSuccessors -> Entry");
        int vIndex = calcIndexFromXPosition(v);
        int minIndex = vIndex;
        int result = -1;

        for (V parent : graph.getSuccessors(v)) {
            minIndex = Math.min(minIndex, calcIndexFromXPosition(parent));
        }
        minIndex++;

        if (minIndex >= vIndex) { //There is no node situated between v and its most ancient successor
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("findMaxHeightBetweenSuccessors -> Exit");
            return height;
        }

        List<Integer> heightsSubList = heights.subList(minIndex, vIndex);
        List<V> nodesSubList = nodes.subList(minIndex, vIndex);
        if (heightsSubList.isEmpty()) {
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("findMaxHeightBetweenSuccessors -> Exit");
            return height;
        } else {
            for (V node : nodesSubList) {
                if (getHeightFromXPosition(node) > -1) {
                    //Node was already processed and its height was calculated
                    if ((distances.getDistance(v, node)) == null) {
                        //if there is no path from node to v, then v is in a different
                        // branch from node and thus must be assigned a higher height than node
                        int nonSuccessorHeight = getHeightFromXPosition(node);
                        result = nonSuccessorHeight > result ? nonSuccessorHeight : result;
                    }
                }
            }
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("findMaxHeightBetweenSuccessors -> Exit");
            return result + 1;
        }
    }

    /**
     * Calculates the max height between v and its most ancient predecessor, not
     * including them.
     *
     * @param v Node for which max height will be calculated
     * @return
     */
    private int findMaxHeightBetweenPredecessors(V v, int height) {
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("findMaxHeightBetweenPredecessors -> Entry");
        int vIndex = calcIndexFromXPosition(v);
        int maxIndex = vIndex;
        int result = -1;

        for (V child : graph.getPredecessors(v)) {
            maxIndex = Math.max(maxIndex, calcIndexFromXPosition(child));
        }
        maxIndex--;

        if (maxIndex <= vIndex) { //There is no node situated between v and its most ancient successor
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("findMaxHeightBetweenPredecessors -> Exit");
            return height;
        }

        List<Integer> heightsSubList = heights.subList(vIndex, maxIndex);
        List<V> nodesSubList = nodes.subList(vIndex, maxIndex);
        if (heightsSubList.isEmpty()) {
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("findMaxHeightBetweenPredecessors -> Exit");
            return height;
        } else {
            for (V node : nodesSubList) {
                if (getHeightFromXPosition(node) > -1) {
                    if ((distances.getDistance(node, v)) == null) {
                        //if there is no path from node to v, then v should be on a greater height than node
                        int nonPredecessorHeight = getHeightFromXPosition(node);
                        result = nonPredecessorHeight > result ? nonPredecessorHeight : result;
                    }
                }
            }
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("findMaxHeightBetweenPredecessors -> Exit");
            return result + 1;
        }
    }

    /**
     * Calculates the max height between subtrees headed by v1 and v2, until their
     * merge base
     *
     * @param head1 First head
     * @param head2 Second head
     * @return the max height of nodes between nodes v1 and v2
     */
    private int findMaxHeightBetweenSubtrees(V head1, V head2) throws VCSException {
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("findMaxHeightBetweenSubtrees -> Entry");
        int index1 = calcIndexFromXPosition(head1);
        
        RevCommit commit = CommitUtils.getBase(rep.getConnection().getRepository(), getHashFromNode(head1), getHashFromNode(head2));
        String hashBase = commit.getName();
        int index2 = calcIndexFromXPosition(nodePositions.get(hashBase));
        
        if (index1 > index2) {
            int temp = index1;
            index1 = index2;
            index2 = temp;
        }
        
        List<Integer> heightsSubList = heights.subList(index1, index2);
        
        int result = Collections.max(heightsSubList);
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("findMaxHeightBetweenSubtrees -> Exit");
        return result + 1;
    }

    /**
     * Calculates the position in the heights list, based on X position. The
     * calculation involves dividing the X position by XDISTANCE.
     *
     * @param v Node for which position will be calculated.
     * @return
     */
    private int calcIndexFromXPosition(V v) {
        return calcIndexFromXPosition(transform(v).getX());
    }

    /**
     * Calculates the position in the heights list, based on a node's hash
     *
     * @param hash Node's hash for which position will be calculated.
     * @return
     */
    private int calcIndexFromHash(String hash) {
        return calcIndexFromXPosition(nodePositions.get(hash));
    }

    /**
     * Calculates the position in the heights list, based on X position. The
     * calculation involves dividing the X position by XDISTANCE.
     *
     * @param xPosition Position to be calculated
     * @return
     */
    private int calcIndexFromXPosition(Double xPosition) {
        return (int) (xPosition / XDISTANCE);
    }

    /**
     * Verifies if node v has only one child node and is the only parent of this
     * child node
     *
     * @param v Node to be checked
     * @return true, if node v has only one child and is the only parent of this
     * child node
     */
    private boolean isOnlyParentAndChild(V v) {
        int childrenCount = graph.getPredecessorCount(v);
        boolean isOnlyParentAndChild = childrenCount == 1
                && graph.getSuccessorCount(graph.getPredecessors(v).iterator().next()) == 1;
        return isOnlyParentAndChild;
    }

    /**
     * Gets the height of a node, based on its X Position
     *
     * @param node Node to retrieve the height for
     * @return node's height
     */
    private int getHeightFromXPosition(V node) {
        return heights.get(calcIndexFromXPosition(node)).intValue();
    }

    /**
     * Returns the first commit in the graph (node with no successors)
     *
     * @return the first commit in the graph;
     */
    private V getFirstCommit() {
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("getFirstCommit -> Entry");
        if (firstCommit == null) {
            for (V v : graph.getVertices()) {
                if (graph.getSuccessorCount(v) == 0) {
                    firstCommit = v;
                    break;
                }
            }

        }
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).debug("getFirstCommit -> First commit for this repository has id <{}>", ((CommitInfo)firstCommit).getHash());
        LoggerFactory.getLogger(RepositoryHistoryLayout.class).trace("getFirstCommit -> Exit");
        return firstCommit;
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
     * Gets the hash of a node
     * @param node The node to get the hash
     * @return  the hash of the specified node
     */
    private String getHashFromNode(V node) {
        return ((CommitInfo)node).getHash();
    }
}