package br.uff.ic.dyevc.graph.transform.commithistory;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.CollapsedCommitInfo;
import br.uff.ic.dyevc.model.CommitChange;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.utils.DateUtil;

import edu.uci.ics.jung.graph.Graph;

import org.apache.commons.collections15.Transformer;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.Map;

/**
 * Transforms a CommitInfo in a string to be used as a tooltip.
 *
 * @author Cristiano
 */
public class CHVertexTooltipTransformer implements Transformer<Object, String> {
    private final RepositoryInfo            info;
    private final Map<String, List<String>> commitToBranchNamesMap;
    private final Map<String, List<String>> commitToTagNamesMap;

    /**
     * Constructs an instance of this transformer
     *
     * @param info the repository info of the repository that contains this vertex.
     * @param commitMap Map of commits to branch names that point to them.
     * @param tagMap Map of commits to tag names that point to them.
     */
    public CHVertexTooltipTransformer(RepositoryInfo info, Map<String, List<String>> commitMap,
                                      Map<String, List<String>> tagMap) {
        super();
        this.info                   = info;
        this.commitToBranchNamesMap = commitMap;
        this.commitToTagNamesMap    = tagMap;
    }

    /**
     * Returns a string to be used as a tooltip in a graph
     *
     * @param o the object to be transformed in a tooltip. It can be either a Graph with collapsed vertices of a
     * CommitInfo.
     * @return the object formatted as a tooltip
     */
    @Override
    public String transform(Object o) {
        StringBuilder details = new StringBuilder();
        if ((o instanceof Graph)) {
            details.append("<html>This is a collapsed node which groups a total of <B>");
            details.append(Integer.toString(getNodecount((Graph)o)));
            details.append("</B> nodes.</html>");
        }
        
        if ((o instanceof CollapsedCommitInfo)) {
            CollapsedCommitInfo ccs = (CollapsedCommitInfo)o;
            details.append("<html>This is a node representing a linear chain of commits which groups a total of <b>");
            details.append(ccs.toString());
            details.append("</b> nodes.</html>");
        }

        if (o instanceof CommitInfo) {
            StringBuilder header   = new StringBuilder();
            CommitInfo    ci       = (CommitInfo)o;

            int           children = ci.getChildrenCount();
            int           parents  = ci.getParentsCount();
            details.append("<html><body width=\"400px\">");

            if (parents == 0) {
                header.append("<b>This is the first commit!</b><br>");
            }

            if (children == 0) {
                header.append("<b>This is a branch's head!</b><br>");
            }

            if (children > 1) {
                header.append("<b> This node splits into ").append(children).append(" branches.</b><br>");
            }

            if (ci.getParentsCount() > 1) {
                header.append("<b> This node merges ").append(parents).append(" branches.</b><br>");
            }

            if (header.length() > 0) {
                header.append("<br>");
            }

            details.append(header);
            details.append("<b>Commit id: </b>").append(ci.getHash()).append("<br>");
            details.append("<b>time: </b>").append(DateUtil.format(ci.getCommitDate(),
                    "yyyy-MM-dd HH:mm:ss.SSS")).append("<br>");
            details.append("<b>commiter: </b>").append(ci.getCommitter()).append("<br>");
            details.append("<b>message: </b>").append(ci.getShortMessage());

            if (commitToBranchNamesMap.containsKey(ci.getHash())) {
                details.append("<br><br>");
                details.append("<b>Branches that point to this commit:</b>");

                for (String branchName : commitToBranchNamesMap.get(ci.getHash())) {
                    details.append("<br>").append("&nbsp;&nbsp;&nbsp;&nbsp;").append(branchName);
                }
            }

            if (commitToTagNamesMap.containsKey(ci.getHash())) {
                details.append("<br><br>");
                details.append("<span style=\"color:orange\"><b>Tags that point to this commit:</b></span>");

                for (String tagName : commitToTagNamesMap.get(ci.getHash())) {
                    details.append("<br>").append("&nbsp;&nbsp;&nbsp;&nbsp;").append(tagName);
                }
            }

            details.append("<br><br>");

            if (!ci.isTracked()) {
                details.append("<span style=\"color:red\">This commit does not belong to a tracked branch and thus"
                               + " cannot be retrieved by any other repository</span><br><br>");
            }

            if (ci.getFoundIn().contains(info.getId())) {
                details.append("<b>Affected paths:</b><br>");

                for (CommitChange cc : ci.getChangeSet()) {
                    details.append("&nbsp;&nbsp;&nbsp;&nbsp;").append(cc.toString()).append("<br>");
                }

            } else {
                details.append("Affected paths could not be retrieved for this commit.<br>");
                details.append("This commit is known to exist in the repositories with the following ids:<ul>");

                for (String repId : ci.getFoundIn()) {
                    details.append("<li>").append(repId).append("</li>");
                }

                details.append("</ul>");
            }

            details.append("</body></html>");
        }

        return details.toString();
    }

    private int getNodecount(Graph graph) {
        int total = 0;
        for (Object o : graph.getVertices()) {
            if (o instanceof Graph) {
                total += getNodecount((Graph)o);
            } else {
                total++;
            }
        }

        return total;
    }
}
