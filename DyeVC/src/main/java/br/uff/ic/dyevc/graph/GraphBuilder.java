package br.uff.ic.dyevc.graph;

import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.gui.MessageManager;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.CommitRelationship;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.topology.CloneInfo;
import br.uff.ic.dyevc.model.topology.CloneRelationship;
import br.uff.ic.dyevc.model.topology.PullRelationship;
import br.uff.ic.dyevc.model.topology.PushRelationship;
import br.uff.ic.dyevc.model.topology.Topology;
import br.uff.ic.dyevc.persistence.TopologyDAO;
import br.uff.ic.dyevc.tools.vcs.git.GitCommitTools;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.ArrayList;
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

    private static void createSampleData() {
        CloneInfo dyevcssh = new CloneInfo();
        dyevcssh.setHostName("cmcdell");
        dyevcssh.setCloneName("dyevcssh");
        CloneInfo dyevc = new CloneInfo();
        dyevc.setHostName("cmcdell");
        dyevc.setCloneName("dyevc");
        CloneInfo dyevc3 = new CloneInfo();
        dyevc3.setHostName("cmcdell");
        dyevc3.setCloneName("dyevc3");
        CloneInfo dyevc2 = new CloneInfo();
        dyevc2.setHostName("cmcdell");
        dyevc2.setCloneName("dyevc2");
        CloneInfo gems = new CloneInfo();
        gems.setHostName("github");
        gems.setCloneName("gems/dyevc");
        
        CloneRelationship r1 = new PullRelationship(gems, dyevcssh);
        CloneRelationship r2 = new PushRelationship(dyevcssh, gems);
        CloneRelationship r3 = new PullRelationship(gems, dyevc);
        CloneRelationship r4 = new PushRelationship(dyevc, gems);
        CloneRelationship r5 = new PullRelationship(dyevc, dyevc3);
        CloneRelationship r6 = new PushRelationship(dyevc3, dyevc);
        CloneRelationship r7 = new PullRelationship(dyevc2, dyevc3);
        CloneRelationship r8 = new PushRelationship(dyevc3, dyevc2);
        CloneRelationship r9 = new PullRelationship(dyevc, dyevc2);
        CloneRelationship r10 = new PushRelationship(dyevc2, dyevc);

        clones.add(dyevcssh);
        clones.add(dyevc);
        clones.add(dyevc3);
        clones.add(dyevc2);
        clones.add(gems);
        
        relations.add(r1);
        relations.add(r2);
        relations.add(r3);
        relations.add(r4);
        relations.add(r5);
        relations.add(r6);
        relations.add(r7);
        relations.add(r8);
        relations.add(r9);
        relations.add(r10);
    }
    
    private static ArrayList<CloneInfo> clones = new ArrayList<CloneInfo>();
    private static ArrayList<CloneRelationship> relations = new ArrayList<CloneRelationship>();
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
            createSampleData();
//            for (CloneInfo cloneInfo : top.getClonesForSystem(systemName)) {
            for (CloneInfo cloneInfo : clones) {
                graph.addVertex(cloneInfo);
                LoggerFactory.getLogger(GraphBuilder.class).debug("Vertex added: " + cloneInfo);
                System.out.println(cloneInfo);
            }
//            for (CloneRelationship cloneRelationship : top.getRelationshipsForSystem(systemName)) {
            for (CloneRelationship cloneRelationship : relations) {
                graph.addEdge(cloneRelationship, cloneRelationship.getOrigin(), cloneRelationship.getDestination(), EdgeType.DIRECTED);
                LoggerFactory.getLogger(GraphBuilder.class).debug("Edge added: " + cloneRelationship);
                System.out.println(cloneRelationship);
            }
        } catch (DyeVCException ex) {
            MessageManager.getInstance().addMessage("Error during graph creation: " + ex);
            throw ex;
        }
        LoggerFactory.getLogger(GraphBuilder.class).trace("createTopologyGraph -> Exit");
        return graph;
    }
}