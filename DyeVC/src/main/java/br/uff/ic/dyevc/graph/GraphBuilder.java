package br.uff.ic.dyevc.graph;

import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.gui.MessageManager;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.CommitRelationship;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.model.topology.CloneRelationship;
import br.uff.ic.dyevc.model.topology.PullRelationship;
import br.uff.ic.dyevc.model.topology.PushRelationship;
import br.uff.ic.dyevc.model.topology.Topology;
import br.uff.ic.dyevc.persistence.TopologyDAO;
import br.uff.ic.dyevc.tools.vcs.git.GitCommitTools;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.SparseMultigraph;
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

    /**
     * Creates a dag representing the topology of a given system
     * @param systemName the name of the system for which the graph will be created
     * @return a graph representing the topology
     */
    public static SparseMultigraph<RepositoryInfo, CloneRelationship> createTopologyGraph(String systemName) throws DyeVCException {
        LoggerFactory.getLogger(GraphBuilder.class).trace("createTopologyGraph -> Entry");
        final SparseMultigraph<RepositoryInfo, CloneRelationship> graph = new SparseMultigraph<RepositoryInfo, CloneRelationship>();
        try {
            TopologyDAO dao = new TopologyDAO();
            Topology top = dao.readTopology();
//            createSampleData();
            for (RepositoryInfo cloneInfo : top.getClonesForSystem(systemName)) {
                graph.addVertex(cloneInfo);
                LoggerFactory.getLogger(GraphBuilder.class).debug("Vertex added: " + cloneInfo);
                System.out.println(cloneInfo);
            }
            for (CloneRelationship cloneRelationship : top.getRelationshipsForSystem(systemName)) {
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
    
    private static ArrayList<RepositoryInfo> clones = new ArrayList<RepositoryInfo>();
    private static ArrayList<CloneRelationship> relations = new ArrayList<CloneRelationship>();
    
    private static void createSampleData() {
        RepositoryInfo dyevcssh = new RepositoryInfo();
        dyevcssh.setSystemName("dyevc");
        dyevcssh.setHostName("cmcdell");
        dyevcssh.setCloneName("dyevcssh");
        RepositoryInfo dyevc = new RepositoryInfo();
        dyevc.setSystemName("dyevc");
        dyevc.setHostName("cmcdell");
        dyevc.setCloneName("dyevc");
        RepositoryInfo dyevc3 = new RepositoryInfo();
        dyevc3.setSystemName("dyevc");
        dyevc3.setHostName("cmcdell");
        dyevc3.setCloneName("dyevc3");
        RepositoryInfo dyevc2 = new RepositoryInfo();
        dyevc2.setSystemName("dyevc");
        dyevc2.setHostName("cmcdell");
        dyevc2.setCloneName("dyevc2");
        RepositoryInfo gems = new RepositoryInfo();
        gems.setSystemName("dyevc");
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
}
