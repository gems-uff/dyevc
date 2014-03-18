package br.uff.ic.dyevc.graph.transform.common;

import br.uff.ic.dyevc.application.IConstants;
import edu.uci.ics.jung.graph.Graph;
import org.apache.commons.collections15.Transformer;

public class ClusterVertexSizeTransformer<V>
  implements Transformer<V, Integer>
{
  int size;

  public ClusterVertexSizeTransformer(Integer size)
  {
    this.size = size.intValue();
  }

  @Override
  public Integer transform(V v) {
    if ((v instanceof Graph)) {
      return IConstants.GRAPH_VERTEX_CLUSTER_SIZE;
    }
    return Integer.valueOf(this.size);
  }
}