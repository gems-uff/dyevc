package br.uff.ic.dyevc.graph;

//~--- non-JDK imports --------------------------------------------------------

import edu.uci.ics.jung.graph.Graph;

/**
 * Maps a graph to its domain. Used when domain information is needed when building graph layouts.
 *
 * @author Cristiano
 * @param <D> Specifies the domain associated with this graph.
 */
public class GraphDomainMapper<D> {
    private final Graph graph;
    private final D     domain;

    /**
     * Constructs a new mapper from {@link #graph} to {@link #domain}.
     *
     * @param graph The graph
     * @param domain The domain
     */
    public GraphDomainMapper(Graph graph, D domain) {
        this.graph  = graph;
        this.domain = domain;
    }

    /**
     * Gets the graph.
     * @return the graph.
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Gets the domain.
     * @return the domain.
     */
    public D getDomain() {
        return domain;
    }
}
