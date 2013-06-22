package br.uff.ic.dyevc.application.branchhistory.chart;

import br.uff.ic.dyevc.graph.transform.*;
import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.application.branchhistory.view.ProjectValues;
import br.uff.ic.dyevc.model.CommitInfo;
import edu.uci.ics.jung.graph.Graph;
import java.awt.Color;
import java.awt.Paint;
import org.apache.commons.collections15.Transformer;

/**
 * Transformer to paint vertices in a commit history graph
 *
 * @author Cristiano
 */
public class CHVertexPaintTransformer implements Transformer<Object, Paint> {

    /**
     * Paints vertex. Default is cyan. If vertex splits in various children,
     * paints it in red. If vertex is a merge, paints it in green. If vertex is
     * both a merge and another split, paints it in yellow.
     */
    ProjectValues projectValues;
    CHVertexPaintTransformer(ProjectValues projectValues){
        this.projectValues = projectValues;
    }
    @Override
    public Paint transform(Object o) {
        Paint paint = IConstants.COLOR_COLLAPSED;
        if (o instanceof Graph) {
          String text = ((Graph)o).toString();
//          if (text.contains("Project")) {
//            return new Color(255, 222, 173);
//          }
//          if (text.contains("Agent")) {
//            return new Color(119, 136, 153);
//          }
//          if (text.contains("Process")) {
//            return new Color(190, 190, 190);
//          }

          return paint;
        }
        if (o instanceof CommitInfo) {
            double max = projectValues.getMaxValue();
            
            CommitInfo ci = (CommitInfo) o;
            double value = projectValues.getValueByVersionId(ci.getId());
            
            double comparacao = value/max;
            int total = ((int) (255*comparacao));
            total = 255 - total;
            
            paint = new Color(total,total,total);
            
        }
        return paint;
    }
}
