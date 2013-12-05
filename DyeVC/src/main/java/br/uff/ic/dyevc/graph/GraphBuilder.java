package br.uff.ic.dyevc.graph;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.gui.core.MessageManager;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.CommitRelationship;
import br.uff.ic.dyevc.model.topology.CloneRelationship;
import br.uff.ic.dyevc.model.topology.CommitFilter;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.model.topology.Topology;
import br.uff.ic.dyevc.persistence.CommitDAO;
import br.uff.ic.dyevc.persistence.TopologyDAO;
import br.uff.ic.dyevc.tools.vcs.git.GitCommitTools;
import br.uff.ic.dyevc.utils.DiffBetweenReps;

import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

/**
 * A basic repository history graph implemented as a DAG that relates all the commits with its parents and children
 *
 * @author Cristiano
 */
public class GraphBuilder {
    /**
     * Creates a dag representing the commit history for the specified repository
     *
     * @param tools An instance of GitCommitTools to get commits history.
     * @return a graph representing the repository
     * @throws VCSException
     */
    public static DirectedOrderedSparseMultigraph<CommitInfo,
            CommitRelationship> createBasicRepositoryHistoryGraph(GitCommitTools tools)
            throws VCSException {
        LoggerFactory.getLogger(GraphBuilder.class).trace("createBasicRepositoryHistoryGraph -> Entry");
        final DirectedOrderedSparseMultigraph<CommitInfo, CommitRelationship> graph =
            new DirectedOrderedSparseMultigraph<CommitInfo, CommitRelationship>();
        try {
            for (CommitInfo commitInfo : tools.getCommitInfos()) {
                graph.addVertex(commitInfo);
            }

            for (CommitRelationship commitRelationship : tools.getCommitRelationships()) {
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
     *
     * @param systemName the name of the system for which the graph will be created
     * @return a graph representing the topology
     * @throws DyeVCException
     */
    public static DirectedSparseMultigraph<RepositoryInfo, CloneRelationship> createTopologyGraph(String systemName)
            throws DyeVCException {
        LoggerFactory.getLogger(GraphBuilder.class).trace("createTopologyGraph -> Entry");
        final DirectedSparseMultigraph<RepositoryInfo, CloneRelationship> graph =
            new DirectedSparseMultigraph<RepositoryInfo, CloneRelationship>();
        try {
            TopologyDAO  dao    = new TopologyDAO();
            Topology     top    = dao.readTopologyForSystem(systemName);

            CommitFilter filter = new CommitFilter();
            filter.setSystemName("dyevc");
            Set<CommitInfo> allSystemCommits = new CommitDAO().getCommitsByQuery(filter);

            for (RepositoryInfo cloneInfo : top.getClonesForSystem(systemName)) {
                graph.addVertex(cloneInfo);
                LoggerFactory.getLogger(GraphBuilder.class).debug("Vertex added: " + cloneInfo);
            }

            DiffBetweenReps diff = new DiffBetweenReps();

            for (CloneRelationship cloneRelationship : top.getRelationshipsForSystem(systemName)) {
                diff.setOriginId(cloneRelationship.getOrigin().getId());
                diff.setDestinationId(cloneRelationship.getDestination().getId());
                int result = CollectionUtils.select(allSystemCommits, diff).size();
                cloneRelationship.setNonSyncCommitsCount(result);
                graph.addEdge(cloneRelationship, cloneRelationship.getOrigin(), cloneRelationship.getDestination(),
                              EdgeType.DIRECTED);
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
