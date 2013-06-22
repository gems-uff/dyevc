package br.uff.ic.dyevc.application.branchhistory.chart;

import br.uff.ic.dyevc.application.branchhistory.view.ProjectValues;
import br.uff.ic.dyevc.graph.transform.*;
import br.uff.ic.dyevc.model.CommitChange;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.utils.DateUtil;
import edu.uci.ics.jung.graph.Graph;
import org.apache.commons.collections15.Transformer;

/**
 * Transforms a CommitInfo in a string to be used as a tooltip.
 *
 * @author Cristiano
 */
public class CHVertexTooltipTransformer implements Transformer<Object, String> {

    /**
     * Returns a string to be used as a tooltip in a graph
     *
     * @param ci Info to be transformed in tooltip
     * @return the info formatted as a tooltip
     */
    private ProjectValues projectValues;
    CHVertexTooltipTransformer(ProjectValues projectValues){
        this.projectValues = projectValues;
    }
    @Override
    public String transform(Object o) {
        StringBuilder details = new StringBuilder();
        if ((o instanceof Graph)) {
            details.append("<html>This is a collapsed node which groups a total of <B>");
            details.append(((Graph) o).getVertexCount());
            details.append("</B> nodes.</html>");
        }
        if (o instanceof CommitInfo) {
            CommitInfo ci = (CommitInfo) o;
            double value = projectValues.getValueByVersionId(ci.getId());
            details.append("<html>");
            if (ci.getParentsCount() == 0) {
                details.append("<b>This is the first commit!</b>").append("<br><br>");
            }
            if (ci.getChildrenCount() == 0) {
                details.append("<b>This is a branch's head!</b>").append("<br><br>");
            }
            details.append("<b>Commit id: </b>").append(ci.getId()).append("<br>");
            details.append("<b>time: </b>")
                    .append(DateUtil.format(ci.getCommitDate(), "yyyy-MM-dd HH:mm:ss.SSS"))
                    .append("<br>");
            details.append("<b>commiter: </b>")
                    .append(ci.getCommitter())
                    .append("<br>");
            details.append("<b>message: </b>")
                    .append(ci.getShortMessage());
            
            if (!ci.getChangeSet().isEmpty()) {
                details.append("<br><br>");
                details.append("<b>Affected paths:</b><br>");
            }
            for (CommitChange cc : ci.getChangeSet()) {
                details.append("&nbsp;&nbsp;&nbsp;&nbsp;")
                        .append(cc.toString())
                        .append("<br>");
            }
            details.append("<br/>Metric value: "+value+"<br/>");
            details.append("</html>");
        }
        return details.toString();
    }
}
