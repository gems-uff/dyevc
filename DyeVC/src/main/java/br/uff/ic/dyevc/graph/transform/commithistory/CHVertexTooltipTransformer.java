package br.uff.ic.dyevc.graph.transform.commithistory;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.CommitChange;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.utils.DateUtil;

import edu.uci.ics.jung.graph.Graph;

import org.apache.commons.collections15.Transformer;

/**
 * Transforms a CommitInfo in a string to be used as a tooltip.
 *
 * @author Cristiano
 */
public class CHVertexTooltipTransformer implements Transformer<Object, String> {
    private final RepositoryInfo info;

    /**
     * Constructs ...
     *
     * @param info
     */
    public CHVertexTooltipTransformer(RepositoryInfo info) {
        super();
        this.info = info;
    }

    /**
     * Returns a string to be used as a tooltip in a graph
     *
     * @param o the object to be transformed in a tooltip. It can be either a Graph with collapsed vertices of a CommitInfo.
     * @return the object formatted as a tooltip
     */
    @Override
    public String transform(Object o) {
        StringBuilder details = new StringBuilder();
        if ((o instanceof Graph)) {
            details.append("<html>This is a collapsed node which groups a total of <B>");
            details.append(((Graph)o).getVertexCount());
            details.append("</B> nodes.</html>");
        }

        if (o instanceof CommitInfo) {
            StringBuilder header   = new StringBuilder();
            CommitInfo    ci       = (CommitInfo)o;
            int           children = ci.getChildrenCount();
            int           parents  = ci.getParentsCount();
            details.append("<html><body width=\"450px\">");

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

            details.append("<br><br>");

            if (!ci.isTracked()) {
                details.append("<span style=\"color:red\">This commit does not belong to a tracked branch and thus"
                               + " cannot be retrieved by any other repository</span><br><br>");
            }

            if (ci.getFoundIn().contains(info.getId())) {
                details.append("<b>Affected paths:</b><br>");
            } else {
                details.append("Affected paths could not be retrieved for this commit.<br>");
                details.append("This commit is known to exist in the repositories with the following ids:<ul>");

                for (String repId : ci.getFoundIn()) {
                    details.append("<li>").append(repId).append("</li>");
                }

                details.append("</ul>");
            }

            for (CommitChange cc : ci.getChangeSet()) {
                details.append("&nbsp;&nbsp;&nbsp;&nbsp;").append(cc.toString()).append("<br>");
            }

            details.append("</body></html>");
        }

        return details.toString();
    }
}
