package br.uff.ic.dyevc.graph;

import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.gui.MessageManager;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.CommitRelationship;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.topology.CloneInfo;
import br.uff.ic.dyevc.model.topology.CloneRelationship;
import br.uff.ic.dyevc.model.topology.Topology;
import br.uff.ic.dyevc.persistence.TopologyDAO;
import br.uff.ic.dyevc.tools.vcs.git.GitCommitTools;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.util.EdgeType;
import org.slf4j.LoggerFactory;

/**
 * A basic repository history graph implemented as a DAG that relates all the
 * commits with its parents and children
 *
 * @author Cristiano
 */
public class GraphBuilder {
    /**
     * Creates a dag representing the commit history for the specified repository
     * @param rep the repository for which the graph will be created
     * @return a graph representing the repository
     */
    public static DirectedOrderedSparseMultigraph<CommitInfo, CommitRelationship> createBasicRepositoryHistoryGraph(MonitoredRepository rep) throws VCSException {
        LoggerFactory.getLogger(GraphBuilder.class).trace("createBasicRepositoryHistoryGraph -> Entry");
        final DirectedOrderedSparseMultigraph<CommitInfo, CommitRelationship> graph = new DirectedOrderedSparseMultigraph<CommitInfo, CommitRelationship>();
        try {
            GitCommitTools ch = new GitCommitTools(rep);
            for (CommitInfo commitInfo : ch.getCommitInfos()) {
                graph.addVertex(commitInfo);
            }
            for (CommitRelationship commitRelationship : ch.getCommitRelationships()) {
                graph.addEdge(commitRelationship, commitRelationship.getChild(), commitRelationship.getParent());
            }
        } catch (VCSException ex) {
            MessageManager.getInstance().addMessage("Error during graph creation: " + ex);
            throw ex;
        }
        LoggerFactory.getLogger(GraphBuilder.class).trace("createBasicRepositoryHistoryGraph -> Exit");
        return graph;
    }
    
    /**
     * Creates a dag representing the topology of a given system
     * @param systemName the name of the system for which the graph will be created
     * @return a graph representing the topology
     */
    public static DirectedSparseMultigraph<CloneInfo, CloneRelationship> createTopologyGraph(String systemName) throws DyeVCException {
        LoggerFactory.getLogger(GraphBuilder.class).trace("createTopologyGraph -> Entry");
        final DirectedSparseMultigraph<CloneInfo, CloneRelationship> graph = new DirectedSparseMultigraph<CloneInfo, CloneRelationship>();
        try {
            TopologyDAO dao = new TopologyDAO();
            Topology top = dao.readTopology();
            for (CloneInfo cloneInfo : top.getClonesForSystem(systemName)) {
                graph.addVertex(cloneInfo);
                LoggerFactory.getLogger(GraphBuilder.class).debug("Vertex added: " + cloneInfo);

            }
            for (CloneRelationship cloneRelationship : top.getRelationshipsForSystem(systemName)) {
                graph.addEdge(cloneRelationship, cloneRelationship.getOrigin(), cloneRelationship.getDestination(), EdgeType.DIRECTED);
                LoggerFactory.getLogger(GraphBuilder.class).debug("Edge added: " + cloneRelationship);
            }
        } catch (DyeVCException ex) {
            MessageManager.getInstance().addMessage("Error during graph creation: " + ex);
            throw ex;
        }
        LoggerFactory.getLogger(GraphBuilder.class).trace("createTopologyGraph -> Exit");
        return graph;
    }
}
