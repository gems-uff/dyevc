package br.uff.ic.dyevc.application.branchhistory.chart;

import br.uff.ic.dyevc.application.branchhistory.model.ProjectRevisions;
import br.uff.ic.dyevc.graph.*;
import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.gui.MessageManager;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.CommitRelationship;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.tools.vcs.git.GitCommitTools;
import br.uff.ic.dyevc.tools.vcs.git.GitConnector;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A basic repository history graph implemented as a DAG that relates all the
 * commits with its parents and children
 *
 * @author Cristiano
 */
public class BasicRepositoryHistoryGraph {
    /**
     * Creates a dag representing the commit history for the specified repository
     * @param rep the repository for which the graph will be created
     * @return a graph representing the repository
     */
    
    private static String BRANCHES_HISTORY_PATH = System.getProperty("user.home")+"/.dyevc/BRANCHES_HISTORY/";
    
    public static DirectedOrderedSparseMultigraph<CommitInfo, CommitRelationship> createBasicRepositoryHistoryGraph(ProjectRevisions rep) {
        final DirectedOrderedSparseMultigraph<CommitInfo, CommitRelationship> graph = new DirectedOrderedSparseMultigraph<CommitInfo, CommitRelationship>();
        GitConnector git = null;
        try {
            git = new GitConnector(BRANCHES_HISTORY_PATH+rep.getName(), rep.getName());
            GitCommitTools ch = new GitCommitTools(git);
            for (CommitInfo commitInfo : ch.getCommitInfos()) {
                graph.addVertex(commitInfo);
            }
            for (CommitRelationship commitRelationship : ch.getCommitRelationships()) {
                graph.addEdge(commitRelationship, commitRelationship.getChild(), commitRelationship.getParent());
            }
            return graph;
        } catch (VCSException ex) {
            Logger.getLogger(BasicRepositoryHistoryGraph.class.getName()).log(Level.SEVERE, null, ex);
            MessageManager.getInstance().addMessage("Error during graph creation: " + ex);
        } finally {
            if (git != null) {
                git.close();
            }
        }
        
        return graph;
    }
}
