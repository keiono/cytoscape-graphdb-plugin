package org.cytoscape.blueprints.graphdb;

import java.util.Collection;

import org.cytoscape.model.CyNetwork;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public interface GraphConverter {

	CyNetwork createCyNetwork(final Collection<Vertex> vertices,
			final Collection<Edge> edges);

}
